/*
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 */
package dev.barebones.commander.commons.file.archive;

import java.io.IOException;

/**
 * Single source of truth for "is this archive entry name safe to
 * use as a filesystem path?" Called by every format module's
 * iterator before building an {@link ArchiveEntry}. Rejects:
 *
 * <ul>
 *   <li><b>Absolute paths</b> — leading {@code /} or {@code \},
 *       Windows drive prefixes ({@code C:}, {@code A:}). A naive
 *       extractor doing {@code root.resolve(entryName)} would
 *       silently escape the extraction root for absolute paths.</li>
 *   <li><b>Parent escapes</b> — any {@code ..} path segment.</li>
 *   <li><b>Backslashes</b> — Windows path separators are normalised
 *       in archive specs to forward slashes; a backslash in an
 *       entry name on Windows would create a directory boundary
 *       the validator never sees.</li>
 *   <li><b>NUL bytes</b> — would terminate a C-string in a
 *       downstream native call.</li>
 *   <li><b>Empty / null</b> names.</li>
 * </ul>
 *
 * On success returns the entry name unchanged. On rejection throws
 * {@link UnsafeEntryNameException} with the original name and the
 * rejection reason — callers should skip the entry, not abort the
 * whole archive.
 */
public final class SafePath {

    private SafePath() {
    }

    /**
     * Validates the entry name and returns it unchanged on success.
     *
     * @throws UnsafeEntryNameException if the name fails any rule.
     */
    public static String validate(String entryName) throws UnsafeEntryNameException {
        if (entryName == null) {
            throw new UnsafeEntryNameException("(null)", "entry name is null");
        }
        if (entryName.isEmpty()) {
            throw new UnsafeEntryNameException(entryName, "entry name is empty");
        }
        if (entryName.indexOf('\0') >= 0) {
            throw new UnsafeEntryNameException(entryName, "entry name contains NUL byte");
        }
        if (entryName.indexOf('\\') >= 0) {
            throw new UnsafeEntryNameException(entryName,
                "entry name contains backslash (Windows separator); archive specs use '/'");
        }
        if (entryName.charAt(0) == '/') {
            throw new UnsafeEntryNameException(entryName,
                "entry name is absolute (leading '/')");
        }
        // Drive prefix at the start (e.g. "C:foo", "C:\\foo") — the colon
        // must be at index 1 and preceded by an ASCII letter.
        if (entryName.length() >= 2
                && entryName.charAt(1) == ':'
                && isAsciiLetter(entryName.charAt(0))) {
            throw new UnsafeEntryNameException(entryName,
                "entry name has a Windows drive prefix");
        }
        // Reject any '..' path segment.
        for (String seg : entryName.split("/")) {
            if ("..".equals(seg)) {
                throw new UnsafeEntryNameException(entryName,
                    "entry name contains a '..' parent-escape segment");
            }
        }
        return entryName;
    }

    private static boolean isAsciiLetter(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
    }

    /**
     * Thrown when {@link SafePath#validate} rejects an entry name.
     * Extends {@link IOException} so it propagates through archive
     * iterators that already declare {@code throws IOException}.
     */
    public static final class UnsafeEntryNameException extends IOException {
        private final String entryName;

        public UnsafeEntryNameException(String entryName, String reason) {
            super("unsafe archive entry name '" + entryName + "': " + reason);
            this.entryName = entryName;
        }

        public String entryName() {
            return entryName;
        }
    }
}
