/*
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 */
package dev.barebones.commander.commons.file.archive.zip;

import dev.barebones.commander.commons.file.archive.ArchiveEntry;

import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * End-to-end test for the zip-slip defence: build a real malicious
 * ZIP via {@code java.util.zip.ZipOutputStream}, iterate it through
 * the production {@link JavaUtilZipEntryIterator}, and verify that
 * the unsafe entries are silently skipped while the safe ones still
 * come through.
 *
 * Catches regressions in:
 *  - the iterator forgetting to call {@code SafePath.validate}
 *  - the iterator failing the WHOLE archive on first unsafe entry
 *    instead of skipping
 *  - {@code SafePath.validate} accepting a path it shouldn't
 */
public class ZipUnsafeEntrySkipTest {

    private static final Function<dev.barebones.commander.commons.file.archive.zip.provider.ZipEntry, ArchiveEntry>
        TRIVIAL_FACTORY = z ->
            new ArchiveEntry(z.getName(), z.isDirectory(), z.getTime(), z.getSize(), true);

    private static byte[] buildArchive(String... entryPaths) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (String name : entryPaths) {
                ZipEntry entry = new ZipEntry(name);
                zos.putNextEntry(entry);
                zos.write(("body of " + name).getBytes());
                zos.closeEntry();
            }
        }
        return baos.toByteArray();
    }

    private static List<String> iterateNames(byte[] archive) throws Exception {
        List<String> names = new ArrayList<>();
        try (ZipInputStream zin = new ZipInputStream(
                new java.io.ByteArrayInputStream(archive))) {
            JavaUtilZipEntryIterator it = new JavaUtilZipEntryIterator(zin, TRIVIAL_FACTORY);
            ArchiveEntry e;
            while ((e = it.nextEntry()) != null) {
                names.add(e.getPath());
            }
        }
        return names;
    }

    @Test
    public void zipSlipEntryIsSkippedSafeEntriesPassThrough() throws Exception {
        byte[] archive = buildArchive(
            "good/safe.txt",
            "../../etc/passwd",        // classic zip-slip
            "good/another.txt"
        );
        List<String> names = iterateNames(archive);

        Set<String> got = new HashSet<>(names);
        assertTrue(got.contains("good/safe.txt"),
            "safe entry must pass through, got " + got);
        assertTrue(got.contains("good/another.txt"),
            "safe entry must pass through, got " + got);
        assertFalse(got.contains("../../etc/passwd"),
            "unsafe entry must be skipped, got " + got);
        assertEquals(names.size(), 2, "exactly the 2 safe entries");
    }

    @Test
    public void multipleUnsafeEntriesAllSkipped() throws Exception {
        byte[] archive = buildArchive(
            "../escape1",
            "good.txt",
            "/absolute/escape",
            "..\\windows\\evil",
            "still/good.txt"
        );
        List<String> names = iterateNames(archive);

        Set<String> got = new HashSet<>(names);
        assertEquals(got.size(), 2);
        assertTrue(got.contains("good.txt"));
        assertTrue(got.contains("still/good.txt"));
    }

    @Test
    public void allUnsafeEntriesYieldsEmptyListing() throws Exception {
        byte[] archive = buildArchive(
            "../a",
            "../../b",
            "/c"
        );
        assertEquals(iterateNames(archive).size(), 0,
            "all entries unsafe → no entries returned");
    }

    /** A regression guard: iterator must continue past the first
     *  unsafe entry, not stop. */
    @Test
    public void iterationContinuesAfterUnsafeEntry() throws Exception {
        // Unsafe entry FIRST, safe entries AFTER.
        byte[] archive = buildArchive(
            "../bad",
            "good1.txt",
            "good2.txt"
        );
        List<String> names = iterateNames(archive);
        assertEquals(names.size(), 2);
        assertEquals(names.get(0), "good1.txt");
        assertEquals(names.get(1), "good2.txt");
    }

    @Test
    public void iterateFromTempFile() throws Exception {
        byte[] archive = buildArchive("good/x", "../bad/y");
        Path tmp = Files.createTempFile("ziptest-", ".zip");
        try {
            Files.write(tmp, archive);
            try (FileOutputStream ignored = new FileOutputStream(tmp.toFile(), true)) {
                // round-trip through a real file to make sure file
                // I/O doesn't change the iterator behaviour
            }
            List<String> names;
            try (ZipInputStream zin = new ZipInputStream(Files.newInputStream(tmp))) {
                JavaUtilZipEntryIterator it = new JavaUtilZipEntryIterator(zin, TRIVIAL_FACTORY);
                names = new ArrayList<>();
                ArchiveEntry e;
                while ((e = it.nextEntry()) != null) {
                    names.add(e.getPath());
                }
            }
            assertEquals(names, List.of("good/x"));
        } finally {
            Files.deleteIfExists(tmp);
        }
    }
}
