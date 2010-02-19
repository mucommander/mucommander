/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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
 * <code>BoundedInputStream</code> is an InputStream that has a set limit to the number of bytes that can be read or
 * skipped from it. What happens when the limit is reached is controlled at creation time: <code>read</code> and
 * <code>skip</code> methods can either throw a {@link StreamOutOfBoundException} or simply return <code>-1</code>.
 *
 * <p>The limit has no effect if it is set to a value that is higher than the number of bytes remaining in the
 * underlying stream.</p>
 *
 * <p>This class is particularly useful for reading archives that are a concatenation of files, tarballs for instance.</p>
 *
 * @author Maxence Bernard
 * @see BoundedReader
 * @see BoundedOutputStream
 * @see StreamOutOfBoundException
 */
public class BoundedInputStream extends FilterInputStream implements Bounded {

    protected long totalRead;
    protected long allowedBytes;
    protected boolean throwStreamOutOfBoundException;

    /**
     * Creates a new <code>BoundedInputStream</code> over the specified stream, allowing a maximum of
     * <code>allowedBytes</code> to be read or skipped. If <code>allowedBytes</code> is equal to <code>-1</code>, this
     * stream is not bounded and acts as a normal stream.
     *
     * <p>If the <code>throwStreamOutOfBoundException</code> parameter is <code>true</code>, <code>read</code> and
     * <code>skip</code> methods will throw a {@link StreamOutOfBoundException} when an attempt to read or skip beyond
     * that limit is made. If <code>false</code>, <code>-1</code> will be returned.</p>
     *
     * @param in the stream to be bounded
     * @param allowedBytes the total number of bytes that are allowed to be read or skipped, <code>-1</code> for no limit
     * @param throwStreamOutOfBoundException <code>true</code> to throw when an attempt to read or skip beyond the byte
     * limit is made, <code>false</code> to simply return <code>-1</code>
     */
    public BoundedInputStream(InputStream in, long allowedBytes, boolean throwStreamOutOfBoundException) {
        super(in);

        this.allowedBytes = allowedBytes;
        this.throwStreamOutOfBoundException = throwStreamOutOfBoundException;
    }


    ////////////////////////////
    // Bounded implementation //
    ////////////////////////////

    public long getAllowedBytes() {
        return allowedBytes;
    }

    public long getProcessedBytes() {
        return totalRead;
    }

    public long getRemainingBytes() {
        return allowedBytes<=-1?Long.MAX_VALUE:allowedBytes-totalRead;
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    @Override
    public synchronized int read() throws IOException {
        if(getRemainingBytes()==0) {
            if(throwStreamOutOfBoundException)
                throw new StreamOutOfBoundException(allowedBytes);

            return -1;
        }

        int i = in.read();
        totalRead++;

        return i;
    }

    @Override
    public synchronized int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public synchronized int read(byte b[], int off, int len) throws IOException {
        int canRead = (int)Math.min(getRemainingBytes(), len);
        if(canRead==0) {
            if(throwStreamOutOfBoundException)
                throw new StreamOutOfBoundException(allowedBytes);

            return -1;
        }

        int nbRead = in.read(b, off, canRead);
        if(nbRead>0)
            totalRead += nbRead;

        return nbRead;
    }

    @Override
    public synchronized long skip(long n) throws IOException {
        int canSkip = (int)Math.min(getRemainingBytes(), n);
        if(canSkip==0) {
            if(throwStreamOutOfBoundException)
                throw new StreamOutOfBoundException(allowedBytes);

            return -1;
        }

        long nbSkipped = in.skip(canSkip);
        if(nbSkipped>0)
            totalRead += nbSkipped;

        return nbSkipped;
    }

    @Override
    public synchronized int available() throws IOException {
        return Math.min(in.available(), (int)getRemainingBytes());
    }

    // Methods not implemented

    /**
     * Always returns <code>false</code>, even if the underlying stream supports it.
     *
     * @return always returns <code>false</code>, even if the underlying stream supports it
     */
    @Override
    public boolean markSupported() {
        // Todo: in theory we could support mark/reset
        return false;
    }

    /**
     * Implemented as a no-op: the call is *not* delegated to the underlying stream.
     */
    @Override
    public synchronized void mark(int readlimit) {
        // Todo: in theory we could support mark/reset
        // No-op
    }

    /**
     * Always throws an <code>IOException</code>: the call is *not* delegated to the underlying stream.
     */
    @Override
    public synchronized void reset() throws IOException {
        // Todo: in theory we could support mark/reset
        // No-op
    }
}
