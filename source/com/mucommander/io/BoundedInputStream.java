/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
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
 * @author Maxence Bernard
 */
public class BoundedInputStream extends FilterInputStream {

    protected long totalRead;
    protected long allowedBytes;
    protected IOException outOfBoundException;

    /**
     * Equivalent to {@link #BoundedInputStream(java.io.InputStream, long, java.io.IOException)} called with a
     * {@link com.mucommander.io.BoundedInputStream.StreamOutOfBoundException}.
     *
     * @param in the stream to bound
     * @param allowedBytes the total number of bytes this stream allows to be read or skipped, <code>-1</code> for no limitation
     */
    public BoundedInputStream(InputStream in, long allowedBytes) {
        this(in, allowedBytes, new StreamOutOfBoundException(allowedBytes));
    }

    /**
     * Creates a new <code>BoundedInputStream</code> over the specified stream, allowing a maximum of
     * <code>allowedBytes</code> to be read or skipped and throwing the specified <code>IOException</code> when
     * an attempt to read or skip beyond that is made.
     * If <code>allowedBytes</code> is equal to <code>-1</code>, this stream is not bounded and acts as a normal stream.
     *
     * @param in the stream to bound
     * @param allowedBytes the total number of bytes this stream allows to be read or skipped, <code>-1</code> for no limitation
     * @param streamOutOfBoundException the IOException to throw when an attempt to read or skip beyond <code>allowedBytes</code> is made
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
        if(getRemainingBytes()==0)
            throw outOfBoundException;

        int i = in.read();
        totalRead++;

        return i;
    }

    public synchronized int read(byte b[]) throws IOException {
        if(b.length>getRemainingBytes())
            throw outOfBoundException;

        int nbRead = in.read(b);
        if(nbRead>0)
            totalRead += nbRead;

        return nbRead;
    }

    public synchronized int read(byte b[], int off, int len) throws IOException {
        if(len>getRemainingBytes())
            throw outOfBoundException;

        int nbRead = in.read(b, off, len);
        if(nbRead>0)
            totalRead += nbRead;

        return nbRead;
    }

    public synchronized long skip(long n) throws IOException {
        if(n>getRemainingBytes())
            throw outOfBoundException;

        long nbSkipped = in.skip(n);
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


    ///////////////////
    // Inner classes //
    ///////////////////

    public static class StreamOutOfBoundException extends IOException {
        public StreamOutOfBoundException(long limit) {
            super("Attempt to read out of bounds, limit="+limit);
        }
    }
}
