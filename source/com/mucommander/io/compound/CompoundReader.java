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

package com.mucommander.io.compound;

import java.io.IOException;
import java.io.Reader;

/**
 * <code>CompoundReader</code> concatenates several readers into one. It can operate in two modes:
 * <dl>
 *   <dt>Merged</dt>
 *   <dd>the compound reader acts as a single, global reader, merging the contents of underlying readers. The
 * compound reader can be read just like a regular <code>Reader</code> -- readers are advanced automatically as EOF
 * of individual readers are reached.</dd>
 *
 *   <dt>Unmerged</dt>
 *   <dd>the compound reader has be advanced manually. EOF are signaled individually for each underlying reader.
 * After EOF is reached, the current reader has to be advanced to the next one using {@link #advanceReader()}.</dd>
 * <dl>
 *
 * <p>
 * This class is abstract, with a single method to implement: {@link #getNextReader()}.
 * See {@link IteratorCompoundReader} for an <code>Iterator</code>-backed implementation.
 * </p>
 *
 * @see IteratorCompoundReader, CompoundInputStream
 * @author Maxence Bernard
 */
public abstract class CompoundReader extends Reader {

    /** True if this CompoundReader operates in 'merged' mode */
    private boolean merged;

    /** The Reader that's currently being processed */
    private Reader currentReader;

    /** Used by {@link #read()} */
    private char oneCharBuf[];

    /** <code>true</code> if the global EOF has been reached */
    private boolean globalEOFReached;


    /**
     * Creates a new <code>CompoundReader</code> operating in the specified mode.
     *
     * @param merged <code>true</code> if the readers should be merged, acting as a single reader, or considered
     * as separate readers that have to be {@link #advanceReader() advanced manually}.
     */
    public CompoundReader(boolean merged) {
        this.merged = merged;
    }

    /**
     * Returns:
     * <ul>
     *   <li><code>true</code> if this reader acts as a single, global input reader, merging the contents of underlying
     * readers. In this mode, the compound reader can be read just like a regular Reader -- readers are advanced
     * automatically as EOF of individual readers are reached.</li>
     *   <li><code>false</code> if this reader has be advanced manually. In this mode, EOF are signaled individually
     * for each underlying reader. After EOF has been reached, the current reader has to be advanced to the next one
     * using {@link #advanceReader()}.</li>
     * </ul>
     *
     * @return <code>true</code> if this reader acts as a global reader, <code>false</code> if this reader has
     * to be advanced manually.
     */
    public boolean isMerged() {
        return merged;
    }

    /**
     * Returns the <code>Reader</code> this compound reader is currently reading, <code>null</code> if this reader
     * hasn't read anything yet, or if it has no underlying reader to read.
     *
     * @return <code>Reader</code> this compound reader is currently reading
     */
    public Reader getCurrentReader() {
        return currentReader;
    }

    /**
     * Closes the current reader, if any. This method has no effect if there is no current reader.
     *
     * @throws IOException if an error occurred while closing the current reader.
     */
    public void closeCurrentReader() throws IOException {
        if(currentReader!=null)
            currentReader.close();
    }

    /**
     * Advances the current reader to the next one, causing subsequent calls to <code>Reader</code>
     * methods to operate on the new reader. Returns <code>true</code> if there was a next reader, <code>false</code>
     * otherwise.
     * <p>
     * Note: the current reader (if any) will be closed by this method.
     * </p>
     *
     * @return <code>true</code> if there was a next reader, <code>false</code> otherwise
     * @throws IOException if an error occurred while trying to advancing the current reader. This
     * <code>CompoundReader</code> can't be used after that and must be closed.
     */
    public boolean advanceReader() throws IOException {
        // Return immediately (don't close the reader) if this method is global EOF has already been reached
        if(globalEOFReached)
            return false;

        // Close the current reader
        if(currentReader !=null) {
            try {
                closeCurrentReader();
            }
            catch(IOException e) {
                // Fail silently
            }
        }

        // Try to advance the current Reader to the next
        try {
            currentReader = getNextReader();
        }
        catch(IOException e) {
            // Can't recover from this, this is the end of this stream
            globalEOFReached = true;
            throw e;
        }

        if(currentReader==null) {
            // Global EOF reached
            globalEOFReached = true;
            return false;
        }
        return true;
    }


    /**
     * Checks the current reader and returns <code>true</code> if the current reader is in a state where it can be
     * accessed, <code>false</code> if global EOF has been reached.
     *
     * @return <code>true</code> if the current reader is in a state where it can be accessed, <code>false</code> if
     * global EOF has been reached.
     * @throws IOException if an error occurred while trying to advancing the current reader.
     */
    private boolean checkReader() throws IOException {
        if(globalEOFReached)
            return true;

        if(currentReader ==null)
            if(!advanceReader())
                return true;

        return false;
    }


    //////////////////////
    // Abstract methods //
    //////////////////////

    /**
     * Returns the next <code>Reader</code>, <code>null</code> if there is none.
     * <p>
     * Before calling this method, {@link #advanceReader()} closes the current reader (if any). In other words,
     * implementations do not have to worry about closing previously-returned readers.
     * </p>
     *
     * @return the next <code>Reader</code>, <code>null</code> if there is none.
     * @throws IOException if an error occurred while retrieving the next reader 
     */
    public abstract Reader getNextReader() throws IOException;


    ///////////////////////////
    // Reader implementation //
    ///////////////////////////

    /**
     * Delegates to {@link #read(char[], int, int)} with a 1-char buffer.
     */
    @Override
    public int read() throws IOException {
        if(oneCharBuf ==null)
            oneCharBuf = new char[1];

        int ret = read(oneCharBuf, 0, 1);

        return ret<=0?ret:oneCharBuf[0];
    }

    /**
     * Delegates to {@link #read(char[], int, int)} with a <code>0</code> offset and the whole buffer's length.
     */
    @Override
    public int read(char[] c) throws IOException {
        return read(c, 0, c.length);
    }

    /**
     * Reads up to <code>len-off</code> characters and stores them in the specified buffer, starting at <code>off</code>.
     * Returns the number of characters that were actually read, or <code>-1</code> to signal:
     * <ul>
     *   <li>if {@link #isMerged()} is enabled, the end of the compound reader as a whole</li>
     *   <li>if {@link #isMerged ()} is disabled, the end of the current reader, which may or may not coincide
     * with the end of the reader as a whole.</li>
     * </ul>
     */
    @Override
    public int read(char[] c, int off, int len) throws IOException {
        if(checkReader())
            return -1;

        int ret = currentReader.read(c, off, len);

        if(ret==-1) {
            // read the next reader
            if(merged) {
                if(!advanceReader())
                    return -1;      // Global EOF reached

                // Recurse
                return read(c, off, len);
            }

            return -1;
        }

        return ret;
    }

    /**
     * Skips up to <code>n</code> characters and returns the number of characters that were actually skipped, or
     * <code>-1</code> to signal:
     * <ul>
     *   <li>if {@link #isMerged()} is enabled, the end of the compound reader as a whole</li>
     *   <li>if {@link #isMerged ()} is disabled, the end of the current reader, which may or may not coincide
     * with the end of the reader as a whole.</li>
     * </ul>
     */
    @Override
    public long skip(long n) throws IOException {
        if(checkReader())
            return -1;

        long ret = currentReader.skip(n);

        if(ret==-1) {
            // read the next reader
            if(merged) {
                if(!advanceReader())
                    return -1;      // Global EOF reached

                return currentReader.skip(n);
            }

            return -1;
        }

        return ret;
    }

    /**
     * Closes the current <code>Reader</code> and this <code>CompoundReader</code> a whole.
     * The current reader can no longer be advanced after this method has been called.
     *
     * @throws IOException if the current reader could not be closed.
     */
    @Override
    public void close() throws IOException {
        try {
            if(currentReader!=null)
                closeCurrentReader();
        }
        finally {
            globalEOFReached = true;
        }
    }

    /**
     * Delegates to the current <code>Reader</code>.
     */
    @Override
    public boolean ready() throws IOException {
        return !checkReader() && currentReader.ready();
    }

    /**
     * Delegates to the current <code>Reader</code>.
     */
    @Override
    public void mark(int readlimit) throws IOException {
        if(!checkReader())
            currentReader.mark(readlimit);
    }

    /**
     * Delegates to the current <code>Reader</code>.
     */
    @Override
    public void reset() throws IOException {
        if(!checkReader())
            currentReader.reset();
    }

    /**
     * Delegates to the current <code>Reader</code>.
     */
    @Override
    public boolean markSupported() {
        try {
            return !checkReader() && currentReader.markSupported();
        }
        catch(IOException e) {
            return false;
        }
    }
}