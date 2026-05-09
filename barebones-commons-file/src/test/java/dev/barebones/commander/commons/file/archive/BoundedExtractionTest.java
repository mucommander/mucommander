/*
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 */
package dev.barebones.commander.commons.file.archive;

import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertThrows;

public class BoundedExtractionTest {

    private static ArchiveEntryIterator finite(List<ArchiveEntry> entries) {
        Iterator<ArchiveEntry> it = entries.iterator();
        return new ArchiveEntryIterator() {
            @Override public ArchiveEntry nextEntry() {
                return it.hasNext() ? it.next() : null;
            }
            @Override public void close() {}
        };
    }

    private static ArchiveEntry entry(String path, long size) {
        return new ArchiveEntry(path, false, 0L, size, true);
    }

    @Test
    public void passesThroughWhenWithinCaps() throws IOException {
        List<ArchiveEntry> entries = new ArrayList<>();
        entries.add(entry("a.txt", 100));
        entries.add(entry("b.txt", 200));
        BoundedExtraction it = BoundedExtraction.wrap(finite(entries), 1024, 1024, 100);

        assertEquals(it.nextEntry().getPath(), "a.txt");
        assertEquals(it.nextEntry().getPath(), "b.txt");
        assertNull(it.nextEntry());
    }

    @Test
    public void perEntrySizeCapTrips() {
        List<ArchiveEntry> entries = new ArrayList<>();
        entries.add(entry("ok.txt", 50));
        entries.add(entry("huge.bin", 10_000));
        BoundedExtraction it = BoundedExtraction.wrap(finite(entries), 1024, 1_000_000, 100);

        assertThrows(BoundedExtraction.DecompressionLimitExceededException.class, () -> {
            it.nextEntry();
            it.nextEntry();
        });
    }

    @Test
    public void cumulativeCapTrips() {
        List<ArchiveEntry> entries = new ArrayList<>();
        entries.add(entry("a", 600));
        entries.add(entry("b", 600));
        BoundedExtraction it = BoundedExtraction.wrap(finite(entries), 10_000, 1_000, 100);

        assertThrows(BoundedExtraction.DecompressionLimitExceededException.class, () -> {
            it.nextEntry();
            it.nextEntry();
        });
    }

    @Test
    public void entryCountCapTrips() {
        List<ArchiveEntry> entries = new ArrayList<>();
        for (int i = 0; i < 5; i++) entries.add(entry("e" + i, 1));
        BoundedExtraction it = BoundedExtraction.wrap(finite(entries), 1024, 1024, 3);

        assertThrows(BoundedExtraction.DecompressionLimitExceededException.class, () -> {
            for (int i = 0; i < 5; i++) it.nextEntry();
        });
    }

    @Test
    public void saturatingArithmeticOnMalformedSize() {
        // A malicious archive declares Long.MAX_VALUE for one entry.
        // The cumulative-size sum must NOT overflow into a small
        // negative number that bypasses the cap.
        List<ArchiveEntry> entries = new ArrayList<>();
        entries.add(entry("evil", Long.MAX_VALUE));
        BoundedExtraction it = BoundedExtraction.wrap(finite(entries), Long.MAX_VALUE, 1_000_000, 100);

        // Per-entry cap is Long.MAX_VALUE so entry passes per-entry.
        // Cumulative cap is 1_000_000 — entry's size > cap → trips.
        assertThrows(BoundedExtraction.DecompressionLimitExceededException.class,
            it::nextEntry);
    }

    @Test
    public void wrapWithDefaultsHasSensibleFloor() throws IOException {
        // A 1 KiB archive gets DEFAULT_CUMULATIVE_FLOOR (100 MiB)
        // because 1024 * 10 < 100 MiB.
        List<ArchiveEntry> entries = new ArrayList<>();
        entries.add(entry("a", 50 * 1024 * 1024)); // 50 MiB
        BoundedExtraction it = BoundedExtraction.wrapWithDefaults(finite(entries), 1024);
        // Should not throw (50 MiB < 100 MiB floor).
        assertEquals(it.nextEntry().getPath(), "a");
    }
}
