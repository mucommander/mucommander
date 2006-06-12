package com.mucommander.io;

import java.io.InputStream;
import java.io.IOException;

/**
 * <code>RandomAccessInputStream</code> is an <code>InputStream</code> with random access.
 *
 * <b>Important:</b> <code>BufferedInputStream</code> or any wrapper <code>InputStream</code> class that uses a read buffer
 * CANNOT be used with a <code>RandomAccessInputStream</code> if the {@link #seek(long)} method is to be used. Doing so
 * would corrupt the read buffer and yield to data inconsistencies.
 *
 * @author Maxence Bernard
 */
public abstract class RandomAccessInputStream extends InputStream {

    private int markOffset;


    /**
     * Creates a new RandomAccessInputStream.
     */
    public RandomAccessInputStream() {
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
     *
     * @param n number of bytes to skip
     * @return the number of bytes that have effectively been skipped
     * @throws IOException if something went wrong
     */
    public long skip(long n) throws IOException {
        long offset = getOffset();

        if(offset+n >= getLength()) {
            seek(getLength()-1);
            return getLength() - offset - 1;
        }

        seek(n);
        return n;
    }


    /**
     * Return the number of bytes that are available for reading, that is: {@link #getLength()} - {@link #getOffset()} - 1.
     * Since <code>InputStream.available()</code> returns an int and this method overrides it, a maximum of
     * <code>Integer.MAX_VALUE</code> can be returned, even if this stream has more bytes available.
     *
     * @return the number of bytes that are available for reading.
     * @throws IOException if something went wrong
     */
    public int available() throws IOException {
        return (int)(getLength() - getOffset() - 1);
    }


    /**
     * Overrides <code>InputStream.mark()</code> to provide a working implementation of the method. The given readLimit
     * is simply ignored, the stream can be repositionned using {@link #reset()} with no limit on the number of bytes
     * read after <code>mark()</code> has been called.
     *
     * @param readLimit this parameter has no effect and is simply ignored
     */
    public synchronized void mark(int readLimit) {
        this.markOffset = readLimit;
    }


    /**
     * Overrides <code>InputStream.mark()</code> to provide a working implementation of the method.
     *
     * @throws IOException if something went wrong
     */
    public synchronized void reset() throws IOException {
        seek(this.markOffset);
    }


    /**
     * Always returns <code>true</code>: {@link #mark(int)} and {@link #reset()} methods are supported.
     */
    public boolean markSupported() {
        return true;
    }

    //////////////////////
    // Abstract methods //
    //////////////////////

    public abstract int read(byte b[]) throws IOException;

    public abstract int read(byte b[], int off, int len) throws IOException;

    public abstract void close() throws IOException;

    public abstract long getOffset() throws IOException;

    public abstract long getLength() throws IOException;

    public abstract void seek(long pos) throws IOException;
}
