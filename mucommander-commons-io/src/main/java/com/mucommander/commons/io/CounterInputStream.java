/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2016 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.commons.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * An InputStream that keeps track of the number of bytes that have been read from it. Bytes that are skipped (using
 * {@link #skip(long)} are by default accounted for, {@link #setCountSkippedBytes(boolean)} can be used to change this.
 * <p>
 * <p>The actual number of bytes can be retrieved from the {@link ByteCounter} instance returned by {@link #getCounter()}.
 * The {@link #CounterInputStream(InputStream, ByteCounter)} constructor can be used to specify an existing
 * ByteCounter instance instead of creating a new one. The ByteCounter will always remain accessible, even
 * after this stream has been closed.
 *
 * @author Maxence Bernard
 * @see ByteCounter
 */
public class CounterInputStream extends InputStream {

    /**
     * Underlying InputStream
     */
    private final InputStream in;

    /**
     * Byte counter
     */
    private final ByteCounter counter;

    /**
     * Should skipped bytes be accounted for ? (enabled by default)
     */
    private boolean countSkippedBytes = true;


    /**
     * Creates a new CounterInputStream using the specified InputStream. A new {@link ByteCounter} will be created.
     *
     * @param in the underlying InputStream the data will be read from
     */
    public CounterInputStream(InputStream in) {
        this.in = in;
        this.counter = new ByteCounter();
    }

    /**
     * Creates a new CounterInputStream using the specified InputStream and {@link ByteCounter}.
     * The provided <code>ByteCounter</code> will NOT be reset, whatever value it contains will be kept.
     *
     * @param in the underlying InputStream the data will be read from
     */
    public CounterInputStream(InputStream in, ByteCounter counter) {
        this.in = in;
        this.counter = counter;
    }


    /**
     * Returns the ByteCounter that holds the number of bytes that have been read (and optionally skipped) from this
     * InputStream.
     */
    public ByteCounter getCounter() {
        return this.counter;
    }


    /**
     * Specifies whether or not skipped bytes (using {@link #skip(long)} should be accounted for.
     * This is by default enabled, bytes that are skipped are added to the ByteCounter.
     *
     * @param countSkippedBytes if true, skipped bytes will be accounted for, the ByteCounter will be increased
     *                          by the number of skipped bytes
     */
    public void setCountSkippedBytes(boolean countSkippedBytes) {
        this.countSkippedBytes = countSkippedBytes;
    }

    /**
     * Returns true if skipped bytes (using {@link #skip(long)} are accounted for.
     * This is by default enabled, bytes that are skipped are added to the ByteCounter.
     */
    public boolean getCountSkippedBytes() {
        return countSkippedBytes;
    }


    ////////////////////////////////
    // InputStream implementation //
    ////////////////////////////////

    @Override
    public int read() throws IOException {
        int i = in.read();
        if (i > 0)
            counter.add(1);

        return i;
    }


    @Override
    public int read(byte b[]) throws IOException {
        int nbRead = in.read(b);
        if (nbRead > 0)
            counter.add(nbRead);

        return nbRead;
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException {
        int nbRead = in.read(b, off, len);
        if (nbRead > 0)
            counter.add(nbRead);

        return nbRead;
    }

    @Override
    public long skip(long n) throws IOException {
        long nbSkipped = in.skip(n);

        // Count skipped bytes only if this has been enabled
        if (countSkippedBytes && nbSkipped > 0)
            counter.add(nbSkipped);

        return nbSkipped;
    }


    @Override
    public int available() throws IOException {
        return in.available();
    }


    @Override
    public void close() throws IOException {
        in.close();
    }


    @Override
    public void mark(int readLimit) {
        in.mark(readLimit);
    }


    @Override
    public boolean markSupported() {
        return in.markSupported();
    }


    @Override
    public void reset() throws IOException {
        in.reset();
    }
}
