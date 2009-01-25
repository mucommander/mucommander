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

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

/**
 * A <code>Reader</code> that has a set limit to the number of characters that can be read from it before the EOF
 * is reached. The limit has no effect if it is set higher than the number of characters remaining in the
 * underlying reader.
 *
 * @author Maxence Bernard
 */
public class BoundedReader extends FilterReader {

    protected long totalRead;
    protected long allowedCharacters;
    protected IOException outOfBoundException;

    /**
     * Equivalent to {@link #BoundedReader(java.io.Reader, long, java.io.IOException)} called with a
     * {@link com.mucommander.io.StreamOutOfBoundException}.
     *
     * @param reader the reader to limit
     * @param allowedCharacters the total number of characters this reader allows to be read or skipped, <code>-1</code>
     * for no limitation
     */
    public BoundedReader(Reader reader, long allowedCharacters) {
        this(reader, allowedCharacters, new StreamOutOfBoundException(allowedCharacters));
    }

    /**
     * Creates a new <code>BounderReader</code> over the specified reader, allowing a maximum of
     * <code>allowedCharacters</code> to be read or skipped and throwing the specified <code>IOException</code> when
     * an attempt to read or skip beyond that is made.
     * If <code>allowedCharacters</code> is equal to <code>-1</code>, this reader is not bounded and acts as a normal
     * reader.
     *
     * @param reader the reader to bind
     * @param allowedCharacters the total number of characters this reader allows to be read or skipped, <code>-1</code>
     * for no limitation
     * @param outOfBoundException the IOException to throw when an attempt to read or skip beyond
     * <code>allowedCharacters</code> is made
     */
    public BoundedReader(Reader reader, long allowedCharacters, IOException outOfBoundException) {
        super(reader);

        this.allowedCharacters = allowedCharacters;
        this.outOfBoundException = outOfBoundException;
    }


    /**
     * Returns the total number of characters that this reader allows to be read, <code>-1</code> is this reader is
     * not bounded.
     *
     * @return the total number of characters that this reader allows to be read, <code>-1</code> is this reader is
     * not bounded
     */
    public long getAllowedCharacters() {
        return allowedCharacters;
    }

    /**
     * Returns the total number of characters that have been read or skipped thus far.
     *
     * @return the total number of characters that have been read or skipped thus far
     */
    public long getReadCounter() {
        return totalRead;
    }

    /**
     * Returns the remaining number of characters that this reader allows to be read, {@link Long#MAX_VALUE} if this
     * reader is not bounded.
     *
     * @return the remaining number of characters that this reader allows to be read, {@link Long#MAX_VALUE} if this
     * reader is not bounded.
     */
    public long getRemainingCharacters() {
        return allowedCharacters<=-1 ? Long.MAX_VALUE : allowedCharacters-totalRead;
    }


    ///////////////////////////
    // Reader implementation //
    ///////////////////////////

    public synchronized int read(char[] cbuf, int off, int len) throws IOException {
        int canRead = (int)Math.min(getRemainingCharacters(), len);
        if(canRead==0)
            throw outOfBoundException;

        int nbRead = in.read(cbuf, off, canRead);
        if(nbRead>0)
            totalRead += nbRead;

        return nbRead;

    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    public synchronized int read() throws IOException {
        if(getRemainingCharacters()==0)
            throw outOfBoundException;

        int i = in.read();
        totalRead++;

        return i;
    }

    public synchronized long skip(long n) throws IOException {
        if(n>getRemainingCharacters())
            throw outOfBoundException;

        long nbSkipped = in.skip(n);
        if(nbSkipped>0)
            totalRead += nbSkipped;

        return nbSkipped;
    }

    /**
     * Always returns <code>false</code>, even if the underlying reader supports it.
     *
     * @return always returns <code>false</code>, even if the underlying reader supports it
     */
    public boolean markSupported() {
        // Todo: in theory we could support mark/reset
        return false;
    }

    /**
     * Implemented as a no-op: the call is *not* delegated to the underlying reader.
     */
    public synchronized void mark(int readlimit) {
        // Todo: in theory we could support mark/reset
        // No-op
    }

    /**
     * Always throws an <code>IOException</code>: the call is *not* delegated to the underlying reader.
     */
    public synchronized void reset() throws IOException {
        // Todo: in theory we could support mark/reset
        // No-op
    }
}
