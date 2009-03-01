/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2009 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An InputStream that has a set limit to the number of bytes that can be read from it before the EOF is reached.
 * The limit has no effect if it is set higher than the number of bytes remaining in the underlying stream.
 *
 * <p>This class is particularly useful for reading archives that are a concatenation of files, tarballs for instance.</p>
 *
 * @author Maxence Bernard
 * @see StreamOutOfBoundException
 */
public class BoundedInputStream extends FilterInputStream {

    protected long totalRead;
    protected long allowedBytes;
    protected IOException outOfBoundException;

    /**
     * Equivalent to {@link #BoundedInputStream(java.io.InputStream, long, java.io.IOException)} called with a
     * <code>null</code> <code>IOException</code>.
     *
     * @param in the stream to bind
     * @param allowedBytes the total number of bytes this stream allows to be read or skipped, <code>-1</code> for no limitation
     */
    public BoundedInputStream(InputStream in, long allowedBytes) {
        this(in, allowedBytes, null);
    }

    /**
     * Creates a new <code>BoundedInputStream</code> over the specified stream, allowing a maximum of
     * <code>allowedBytes</code> to be read or skipped. If <code>allowedBytes</code> is equal to <code>-1</code>, this
     * stream is not bounded and acts as a normal stream.
     * <p>
     * The specified <code>IOException</code> will be thrown when an attempt to read or skip beyond that is made.
     * If it is <code>null</code>, read and skip methods will return <code>-1</code> instead of throwing an
     * <code>IOException</code>.
     * </p>
     *
     * @param in the stream to bind
     * @param allowedBytes the total number of bytes this stream allows to be read or skipped, <code>-1</code> for no limitation
     * @param streamOutOfBoundException the IOException to throw when an attempt to read or skip beyond <code>allowedBytes</code>
     * is made, <code>null</code> to return -1 instead
     * @see StreamOutOfBoundException
     */
    public BoundedInputStream(InputStream in, long allowedBytes, IOException streamOutOfBoundException) {
        super(in);

        this.allowedBytes = allowedBytes;
        this.outOfBoundException = streamOutOfBoundException;
    }


    /**
     * Returns the total number of bytes that this stream allows to be read, <code>-1</code> is this stream is
     * not bounded.
     *
     * @return the total number of bytes that this stream allows to be read, <code>-1</code> is this stream is
     * not bounded
     */
    public long getAllowedBytes() {
        return allowedBytes;
    }

    /**
     * Returns the total number of bytes that have been read or skipped thus far.
     *
     * @return the total number of bytes that have been read or skipped thus far
     */
    public long getReadCounter() {
        return totalRead;
    }

    /**
     * Returns the remaining number of bytes that this stream allows to be read, {@link Long#MAX_VALUE} if this stream
     * is not bounded.
     *
     * @return the remaining number of bytes that this stream allows to be read, {@link Long#MAX_VALUE} if this stream
     * is not bounded.
     */
    public long getRemainingBytes() {
        return allowedBytes<=-1?Long.MAX_VALUE:allowedBytes-totalRead;
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    public synchronized int read() throws IOException {
        if(getRemainingBytes()==0) {
            if(outOfBoundException==null)
                return -1;

            throw outOfBoundException;
        }

        int i = in.read();
        totalRead++;

        return i;
    }

    public synchronized int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

    public synchronized int read(byte b[], int off, int len) throws IOException {
        int canRead = (int)Math.min(getRemainingBytes(), len);
        if(canRead==0) {
            if(outOfBoundException==null)
                return -1;

            throw outOfBoundException;
        }

        int nbRead = in.read(b, off, canRead);
        if(nbRead>0)
            totalRead += nbRead;

        return nbRead;
    }

    public synchronized long skip(long n) throws IOException {
        int canSkip = (int)Math.min(getRemainingBytes(), n);
        if(canSkip==0) {
            if(outOfBoundException==null)
                return -1;

            throw outOfBoundException;
        }

        long nbSkipped = in.skip(canSkip);
        if(nbSkipped>0)
            totalRead += nbSkipped;

        return nbSkipped;
    }

    public synchronized int available() throws IOException {
        return Math.min(in.available(), (int)getRemainingBytes());
    }

    /**
     * Always returns <code>false</code>, even if the underlying stream supports it.
     *
     * @return always returns <code>false</code>, even if the underlying stream supports it
     */
    public boolean markSupported() {
        // Todo: in theory we could support mark/reset
        return false;
    }

    /**
     * Implemented as a no-op: the call is *not* delegated to the underlying stream.
     */
    public synchronized void mark(int readlimit) {
        // Todo: in theory we could support mark/reset
        // No-op
    }

    /**
     * Always throws an <code>IOException</code>: the call is *not* delegated to the underlying stream.
     */
    public synchronized void reset() throws IOException {
        // Todo: in theory we could support mark/reset
        // No-op
    }
}
