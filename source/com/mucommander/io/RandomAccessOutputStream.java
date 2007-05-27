package com.mucommander.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * <code>RandomAccessOutputStream</code> is an <code>OutputStream</code> with random access.
 *
 * <b>Important:</b> <code>BufferedOutputStream</code> or any wrapper <code>OutputStream</code> class that uses a write
 * buffer CANNOT be used with a <code>RandomAccessInputStream</code> if the {@link #seek(long)} method is to be used.
 * Doing so would corrupt the write buffer and yield to data inconsistencies.
 *
 * @author Maxence Bernard
 */
public abstract class RandomAccessOutputStream extends OutputStream implements RandomAccess {

    /**
     * Creates a new RandomAccessOutputStream.
     */
    public RandomAccessOutputStream() {
    }


    //////////////////////
    // Abstract methods //
    //////////////////////

    /**
     * Writes <code>b.length</code> bytes from the specified byte array to this file, starting at the current file offset.
     *
     * @param b the data to write
     * @throws IOException if an I/O error occurs
     */
    public abstract void write(byte b[]) throws IOException;

    /**
     * Writes <code>len bytes</code> from the specified byte array starting at offset <code>off</code> to this file.
     * 
     * @param b the data to write
     * @param off the start offset in the data array
     * @param len the number of bytes to write
     * @throws IOException if an I/O error occurs
     */
    public abstract void write(byte b[], int off, int len) throws IOException;
}