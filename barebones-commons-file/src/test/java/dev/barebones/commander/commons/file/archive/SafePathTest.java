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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;

public class SafePathTest {

    @Test
    public void plainEntryRoundTrips() throws Exception {
        assertEquals(SafePath.validate("foo/bar.txt"), "foo/bar.txt");
        assertEquals(SafePath.validate("a/b/c/d.bin"), "a/b/c/d.bin");
        assertEquals(SafePath.validate("single"), "single");
    }

    @Test
    public void leadingSlashRejected() {
        // Absolute-looking paths are rejected: a naive extractor
        // calling root.resolve("/foo") would silently escape the
        // extraction root.
        assertThrows(SafePath.UnsafeEntryNameException.class,
            () -> SafePath.validate("/foo/bar"));
        assertThrows(SafePath.UnsafeEntryNameException.class,
            () -> SafePath.validate("///foo"));
        assertThrows(SafePath.UnsafeEntryNameException.class,
            () -> SafePath.validate("/"));
    }

    @Test
    public void parentEscapeRejected() {
        assertThrows(SafePath.UnsafeEntryNameException.class,
            () -> SafePath.validate("../etc/passwd"));
        assertThrows(SafePath.UnsafeEntryNameException.class,
            () -> SafePath.validate("foo/../bar"));
        assertThrows(SafePath.UnsafeEntryNameException.class,
            () -> SafePath.validate("foo/bar/.."));
    }

    /** The classic zip-slip vector: leading slash + parent traversal. */
    @Test
    public void absolutePlusTraversalRejected() {
        assertThrows(SafePath.UnsafeEntryNameException.class,
            () -> SafePath.validate("/../etc/passwd"));
        assertThrows(SafePath.UnsafeEntryNameException.class,
            () -> SafePath.validate("/var/../../etc/passwd"));
    }

    @Test
    public void backslashRejected() {
        assertThrows(SafePath.UnsafeEntryNameException.class,
            () -> SafePath.validate("foo\\bar"));
        assertThrows(SafePath.UnsafeEntryNameException.class,
            () -> SafePath.validate("..\\..\\windows\\system32"));
    }

    @Test
    public void windowsDrivePrefixRejected() {
        assertThrows(SafePath.UnsafeEntryNameException.class,
            () -> SafePath.validate("C:foo"));
        assertThrows(SafePath.UnsafeEntryNameException.class,
            () -> SafePath.validate("a:relative"));
    }

    @Test
    public void nulByteRejected() {
        assertThrows(SafePath.UnsafeEntryNameException.class,
            () -> SafePath.validate("foo\0bar"));
    }

    @Test
    public void nullAndEmptyRejected() {
        assertThrows(SafePath.UnsafeEntryNameException.class,
            () -> SafePath.validate(null));
        assertThrows(SafePath.UnsafeEntryNameException.class,
            () -> SafePath.validate(""));
    }

    /**
     * Single-dot segments are LEGAL — `./foo` is just `foo`.
     * The validator must NOT reject these; only `..` is dangerous.
     */
    @Test
    public void singleDotIsAllowed() throws Exception {
        assertEquals(SafePath.validate("./foo"), "./foo");
        assertEquals(SafePath.validate("foo/./bar"), "foo/./bar");
    }
}
