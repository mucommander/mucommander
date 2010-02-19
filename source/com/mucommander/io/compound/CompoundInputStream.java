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
import java.io.InputStream;

/**
 * <code>CompoundInputStream</code> concatenates several input streams into one. It can operate in two modes:
 * <dl>
 *   <dt>Merged</dt>
 *   <dd>the compound stream acts as a single, global input stream, merging the contents of underlying streams. The
 * compound stream can be read just like a regular <code>InputStream</code> -- streams are advanced automatically as EOF
 * of individual streams are reached.</dd>
 *
 *   <dt>Unmerged</dt>
 *   <dd>the compound stream has be advanced manually. EOF are signaled individually for each underlying stream.
 * After EOF is reached, the current stream has to be advanced to the next one using {@link #advanceInputStream()}.</dd>
 * </dl>
 *
 * <p>
 * This class is abstract, with a single method to implement: {@link #getNextInputStream()}.
 * See {@link IteratorCompoundInputStream} for an <code>Iterator</code>-backed implementation.
 * </p>
 *
 * @see IteratorCompoundInputStream, CompoundReader
 * @author Maxence Bernard
 */
public abstract class CompoundInputStream extends InputStream {

    /** True if this CompoundInputStream operates in 'merged' mode */
    private boolean merged;

    /** The InputStream that's currently being processed */
    private InputStream currentIn;

    /** Used by {@link #read()} */
    private byte oneByteBuf[];

    /** <code>true</code> if the global EOF has been reached */
    private boolean globalEOFReached;


    /**
     * Creates a new <code>CompoundInputStream</code> operating in the specified mode.
     *
     * @param merged <code>true</code> if the streams should be merged, acting as a single stream, or considered
     * as separate streams that have to be {@link #advanceInputStream() advanced manually}.
     */
    public CompoundInputStream(boolean merged) {
        this.merged = merged;
    }

    /**
     * Returns:
     * <ul>
     *   <li><code>true</code> if this stream acts as a single, global input stream, merging the contents of underlying
     * streams. In this mode, the compound stream can be read just like a regular InputStream -- streams are advanced
     * automatically as EOF of individual streams are reached.</li>
     *   <li><code>false</code> if this stream has be advanced manually. In this mode, EOF are signaled individually
     * for each underlying stream. After EOF has been reached, the current stream has to be advanced to the next one
     * using {@link #advanceInputStream()}.</li>
     * </ul>
     *
     * @return <code>true</code> if this stream acts as a global input stream, <code>false</code> if this stream has
     * to be advanced manually.
     */
    public boolean isMerged() {
        return merged;
    }

    /**
     * Returns the <code>InputStream</code> this compound stream is currently reading, <code>null</code> if this stream
     * hasn't read anything yet, or if it has no underlying stream to read.
     *
     * @return <code>InputStream</code> this compound stream is currently reading
     */
    public InputStream getCurrentInputStream() {
        return currentIn;
    }

    /**
     * Closes the current input stream, if any. This method has no effect if there is no current input stream.
     *
     * @throws IOException if an error occurred while closing the current input stream.
     */
    public void closeCurrentInputStream() throws IOException {
        if(currentIn!=null)
            currentIn.close();
    }

    /**
     * Tries to advances the current stream to the next one, causing subsequent calls to <code>InputStream</code>
     * methods to operate on the new stream. Returns <code>true</code> if there was a next stream, <code>false</code>
     * otherwise.
     * <p>
     * Note: the current stream (if any) will be closed by this method.
     * </p>
     *
     * @return <code>true</code> if there was a next stream, <code>false</code> otherwise
     * @throws IOException if an error occurred while trying to advancing the current stream. This
     * <code>CompoundInputStream</code> can't be used after that and must be closed.
     */
    public boolean advanceInputStream() throws IOException {
        // Return immediately (don't close the stream) if this method is global EOF has already been reached
        if(globalEOFReached)
            return false;

        // Close the current stream
        if(currentIn!=null) {
            try {
                closeCurrentInputStream();
            }
            catch(IOException e) {
                // Fail silently
            }
        }

        // Try to advance the current InputStream to the next
        try {
            currentIn = getNextInputStream();
        }
        catch(IOException e) {
            // Can't recover from this, this is the end of this stream
            globalEOFReached = true;
            throw e;
        }

        if(currentIn==null) {
            // Global EOF reached
            globalEOFReached = true;
            return false;
        }
        return true;
    }


    /**
     * Checks the current stream and returns <code>true</code> if the current stream is in a state where it can be
     * accessed, <code>false</code> if global EOF has been reached.
     *
     * @return <code>true</code> if the current stream is in a state where it can be accessed, <code>false</code> if
     * global EOF has been reached.
     * @throws IOException if an error occurred while trying to advancing the current stream.
     */
    private boolean checkStream() throws IOException {
        if(globalEOFReached)
            return true;

        if(currentIn==null)
            if(!advanceInputStream())
                return true;

        return false;
    }


    //////////////////////
    // Abstract methods //
    //////////////////////

    /**
     * Returns the next <code>InputStream</code>, <code>null</code> if there is none.
     * <p>
     * Before calling this method, {@link #advanceInputStream()} closes the current stream (if any). In other words,
     * implementations do not have to worry about closing previously-returned streams.
     * </p>
     *
     * @return the next <code>InputStream</code>, <code>null</code> if there is none.
     * @throws IOException if an error occurred while retrieving the next input stream 
     */
    public abstract InputStream getNextInputStream() throws IOException;


    ////////////////////////////////
    // InputStream implementation //
    ////////////////////////////////

    /**
     * Delegates to {@link #read(byte[], int, int)} with a 1-byte buffer.
     */
    @Override
    public int read() throws IOException {
        if(oneByteBuf==null)
            oneByteBuf = new byte[1];

        int ret = read(oneByteBuf, 0, 1);

        return ret<=0?ret:oneByteBuf[0];
    }

    /**
     * Delegates to {@link #read(byte[], int, int)} with a <code>0</code> offset and the whole buffer's length.
     */
    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    /**
     * Reads up to <code>len-off</code> bytes and stores them in the specified byte buffer, starting at <code>off</code>.
     * Returns the number of bytes that were actually read, or <code>-1</code> to signal:
     * <ul>
     *   <li>if {@link #isMerged()} is <code>true</code>, the end of the compound stream as a whole</li>
     *   <li>if {@link #isMerged ()} is <code>false</code>, the end of the current stream, which may or may not coincide
     * with the end of the stream as a whole.</li>
     * </ul>
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if(checkStream())
            return -1;

        int ret = currentIn.read(b, off, len);

        if(ret==-1) {
            // read the next stream
            if(merged) {
                if(!advanceInputStream())
                    return -1;      // Global EOF reached

                // Recurse
                return read(b, off, len);
            }

            return -1;
        }

        return ret;
    }

    /**
     * Skips up to <code>n</code> bytes and returns the number of bytes that were actually skipped, or <code>-1</code>
     * to signal:
     * <ul>
     *   <li>if {@link #isMerged()} is enabled, the end of the compound stream as a whole</li>
     *   <li>if {@link #isMerged ()} is disabled, the end of the current stream, which may or may not coincide
     * with the end of the stream as a whole.</li>
     * </ul>
     */
    @Override
    public long skip(long n) throws IOException {
        if(checkStream())
            return -1;

        long ret = currentIn.skip(n);

        if(ret==-1) {
            // read the next stream
            if(merged) {
                if(!advanceInputStream())
                    return -1;      // Global EOF reac  hed

                return currentIn.skip(n);
            }

            return -1;
        }

        return ret;
    }

    /**
     * Closes the current <code>InputStream</code> and this <code>CompoundInputStream</code> a whole.
     * The current stream can no longer be advanced after this method has been called.
     *
     * @throws IOException if the current stream could not be closed.  
     */
    @Override
    public void close() throws IOException {
        try {
            if(currentIn!=null)
                closeCurrentInputStream();
        }
        finally {
            globalEOFReached = true;
        }
    }

    /**
     * Delegates to the current <code>InputStream</code>.
     */
    @Override
    public int available() throws IOException {
        if(checkStream())
            return 0;

        return currentIn.available();
    }

    /**
     * Delegates to the current <code>InputStream</code>.
     */
    @Override
    public void mark(int readlimit) {
        try {
            if(!checkStream())
                currentIn.mark(readlimit);
        }
        catch(IOException e) {
            // Can't throw an IOException here unfortunately, fail silently
        }
    }

    /**
     * Delegates to the current <code>InputStream</code>.
     */
    @Override
    public void reset() throws IOException {
        if(!checkStream())
            currentIn.reset();
    }

    /**
     * Delegates to the current <code>InputStream</code>.
     */
    @Override
    public boolean markSupported() {
        try {
            return !checkStream() && currentIn.markSupported();
        }
        catch(IOException e) {
            return false;
        }
    }
}