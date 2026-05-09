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
import java.util.Objects;

/**
 * Wraps an {@link ArchiveEntryIterator} and enforces three caps that
 * defend against decompression bombs:
 *
 * <ul>
 *   <li><b>Per-entry size cap</b> — any single entry whose declared
 *       uncompressed size exceeds this value is rejected. Default
 *       1 GiB. Bombs that exfil their full payload via one giant
 *       entry are caught here.</li>
 *   <li><b>Cumulative size cap</b> — the sum of declared
 *       uncompressed sizes across all entries seen so far. When
 *       exceeded, iteration stops with {@link DecompressionLimitExceededException}.
 *       Default 10 × the archive's compressed size (and at least
 *       100 MiB), so a tiny archive can still expand to a sane
 *       working size, but a 10 KiB → 100 GiB bomb is caught.</li>
 *   <li><b>Entry count cap</b> — number of entries returned.
 *       Default 100,000. Catches "million tiny files" attacks
 *       that exhaust per-file state in the file-table view.</li>
 * </ul>
 *
 * Caps are advisory ceilings, not parsing-time enforced — an
 * archive whose declared sizes lie can still surprise the
 * extractor at write time. The extraction-write path enforces a
 * mirror cap in the actual byte loop.
 */
public final class BoundedExtraction implements ArchiveEntryIterator {

    public static final long DEFAULT_PER_ENTRY_SIZE = 1L << 30;          // 1 GiB
    public static final long DEFAULT_CUMULATIVE_FLOOR = 100L * (1 << 20); // 100 MiB
    public static final int DEFAULT_ENTRY_COUNT = 100_000;
    public static final int DEFAULT_EXPANSION_RATIO = 10;

    private final ArchiveEntryIterator delegate;
    private final long maxPerEntry;
    private final long maxCumulative;
    private final int maxEntryCount;

    private long cumulative;
    private int count;

    private BoundedExtraction(ArchiveEntryIterator delegate,
                              long maxPerEntry, long maxCumulative,
                              int maxEntryCount) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.maxPerEntry = maxPerEntry;
        this.maxCumulative = maxCumulative;
        this.maxEntryCount = maxEntryCount;
    }

    /**
     * Wraps {@code delegate} with default caps based on the
     * archive's compressed size:
     * <pre>
     *   maxCumulative = max(DEFAULT_CUMULATIVE_FLOOR,
     *                       compressedSize × DEFAULT_EXPANSION_RATIO)
     * </pre>
     */
    public static BoundedExtraction wrapWithDefaults(ArchiveEntryIterator delegate,
                                                     long compressedSize) {
        long cumulative = Math.max(DEFAULT_CUMULATIVE_FLOOR,
            compressedSize * (long) DEFAULT_EXPANSION_RATIO);
        return new BoundedExtraction(delegate,
            DEFAULT_PER_ENTRY_SIZE, cumulative, DEFAULT_ENTRY_COUNT);
    }

    /**
     * Wraps {@code delegate} with explicit caps. Use a negative
     * value to disable a particular cap (only intended for tests).
     */
    public static BoundedExtraction wrap(ArchiveEntryIterator delegate,
                                         long maxPerEntry, long maxCumulative,
                                         int maxEntryCount) {
        return new BoundedExtraction(delegate, maxPerEntry, maxCumulative, maxEntryCount);
    }

    @Override
    public ArchiveEntry nextEntry() throws IOException {
        ArchiveEntry entry = delegate.nextEntry();
        if (entry == null) {
            return null;
        }

        if (maxEntryCount >= 0 && ++count > maxEntryCount) {
            throw new DecompressionLimitExceededException(
                "archive entry count exceeded cap of " + maxEntryCount);
        }

        long size = entry.getSize();
        if (size > 0) {
            if (maxPerEntry >= 0 && size > maxPerEntry) {
                throw new DecompressionLimitExceededException(
                    "archive entry '" + entry.getPath() + "' declares size " + size
                    + " bytes, exceeds per-entry cap of " + maxPerEntry);
            }
            // Saturating add: don't overflow on a malicious size = Long.MAX_VALUE.
            long next = cumulative + size;
            if (next < cumulative) {
                next = Long.MAX_VALUE;
            }
            cumulative = next;
            if (maxCumulative >= 0 && cumulative > maxCumulative) {
                throw new DecompressionLimitExceededException(
                    "archive cumulative declared size exceeded cap of " + maxCumulative
                    + " bytes (last entry: '" + entry.getPath() + "')");
            }
        }
        return entry;
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    /** Thrown when one of the {@link BoundedExtraction} caps is hit. */
    public static final class DecompressionLimitExceededException extends IOException {
        public DecompressionLimitExceededException(String message) {
            super(message);
        }
    }
}
