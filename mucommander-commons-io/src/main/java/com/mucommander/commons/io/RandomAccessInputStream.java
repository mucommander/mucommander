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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * <code>RandomAccessInputStream</code> is an <code>InputStream</code> with random access.
 * <p>
 * The following <code>java.io.InputStream</code> methods are overridden to provide an improved implementation:
 * <ul>
 * <li>{@link #mark(int)}</li>
 * <li>{@link #reset()}</li>
 * <li>{@link #markSupported()}</li>
 * <li>{@link #skip(long)}</li>
 * <li>{@link #available()}</li>
 * </ul>
 * <p>
 * <b>Important:</b> <code>BufferedInputStream</code> or any wrapper <code>InputStream</code> class that uses a read buffer
 * CANNOT be used with a <code>RandomAccessInputStream</code> if the {@link #seek(long)} method is to be used. Doing so
 * would corrupt the read buffer and yield to data inconsistencies.
 *
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public abstract class RandomAccessInputStream extends InputStream implements RandomAccess {
    /**
     * Logger used by this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RandomAccessInputStream.class);

    /**
     * The last offset set by {@link #mark(int)}
     */
    private long markOffset;


    /**
     * Creates a new RandomAccessInputStream.
     */
    public RandomAccessInputStream() {
    }


    /**
     * Reads <code>b.length</code> bytes from this file into the byte array, starting at the current file pointer.
     * This method reads repeatedly from the file until the requested number of bytes are read. This method blocks until
     * the requested number of bytes are read, the end of the stream is detected, or an exception is thrown.
     *
     * @param b the buffer into which the data is read.
     * @throws java.io.EOFException if this file reaches the end before reading all the bytes.
     * @throws IOException          if an I/O error occurs.
     */
    public void readFully(byte b[]) throws IOException {
        StreamUtils.readFully(this, b, 0, b.length);
    }

    /**
     * Reads exactly <code>len</code> bytes from this file into the byte array, starting at the current file pointer.
     * This method reads repeatedly from the file until the requested number of bytes are read. This method blocks until
     * the requested number of bytes are read, the end of the stream is detected, or an exception is thrown.
     *
     * @param b   the buffer into which the data is read.
     * @param off the start offset of the data.
     * @param len the number of bytes to read.
     * @throws java.io.EOFException if this file reaches the end before reading all the bytes.
     * @throws IOException          if an I/O error occurs.
     */
    public void readFully(byte b[], int off, int len) throws IOException {
        StreamUtils.readFully(this, b, off, len);
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    /**
     * Skips (up to) the specified number of bytes and returns the number of bytes effectively skipped.
     * The exact given number of bytes will be skipped as long as the current offset as returned by {@link #getOffset()}
     * plus the number of bytes to skip doesn't exceed the length of this stream as returned by {@link #getLength()}.
     * If it does, all the remaining bytes will be skipped so that the offset of this stream will be positionned to
     * {@link #getLength()}.
     * Returns <code>-1</code> if the offset is already positionned to the end of the stream when this method is called.
     *
     * @param n number of bytes to skip
     * @return the number of bytes that have effectively been skipped, -1 if the offset is already positionned to the
     * end of the stream when this method is called
     * @throws IOException if something went wrong
     */
    @Override
    public long skip(long n) throws IOException {
        if (n <= 0)
            return 0;

        long offset = getOffset();
        long length = getLength();

        // Return -1 if the offset is already at the end of the stream
        if (offset >= length)
            return -1;

        // Makes sure not to go beyond the end of the stream
        long newOffset = offset + n;
        if (newOffset > length)
            newOffset = length;

        // Seek to the new offset
        seek(newOffset);

        // Return the actual number of bytes skipped
        return (int) (newOffset - offset);
    }

    /**
     * Return the number of bytes that are available for reading, that is: {@link #getLength()} - {@link #getOffset()} - 1.
     * Since <code>InputStream.available()</code> returns an int and this method overrides it, a maximum of
     * <code>Integer.MAX_VALUE</code> can be returned, even if this stream has more bytes available.
     *
     * @return the number of bytes that are available for reading.
     * @throws IOException if something went wrong
     */
    @Override
    public int available() throws IOException {
        return (int) (getLength() - getOffset() - 1);
    }

    /**
     * Overrides <code>InputStream.mark()</code> to provide a working implementation of the method. The given readLimit
     * is simply ignored, the stream can be repositionned using {@link #reset()} with no limit on the number of bytes
     * read after <code>mark()</code> has been called.
     *
     * @param readLimit this parameter has no effect and is simply ignored
     */
    @Override
    public synchronized void mark(int readLimit) {
        try {
            this.markOffset = getOffset();
        } catch (IOException e) {
            LOGGER.info("Caught exception", e);
        }
    }

    /**
     * Overrides <code>InputStream.mark()</code> to provide a working implementation of the method.
     *
     * @throws IOException if something went wrong
     */
    @Override
    public synchronized void reset() throws IOException {
        seek(this.markOffset);
    }

    /**
     * Always returns <code>true</code>: {@link #mark(int)} and {@link #reset()} methods are supported.
     */
    @Override
    public boolean markSupported() {
        return true;
    }


    //////////////////////
    // Abstract methods //
    //////////////////////

    /**
     * Reads up to <code>len</code> bytes of data from this file into an array of bytes. This method blocks until at
     * least one byte of input is available.
     *
     * @param b   the buffer into which the data is read
     * @param off the start offset of the data
     * @param len the maximum number of bytes read
     * @return the total number of bytes read into the buffer, or -1 if there is no more data because the end of the
     * file has been reached.
     * @throws IOException if an I/O error occurs
     */
    @Override
    public abstract int read(byte b[], int off, int len) throws IOException;

    /**
     * Closes this stream and releases any system resources associated with the stream.
     * A closed stream cannot perform input operations and cannot be reopened.
     *
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public abstract void close() throws IOException;
}
