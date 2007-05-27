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
     * Writes <code>len</code> bytes from the specified byte array starting at offset <code>off</code> to this file.
     * 
     * @param b the data to write
     * @param off the start offset in the data array
     * @param len the number of bytes to write
     * @throws IOException if an I/O error occurs
     */
    public abstract void write(byte b[], int off, int len) throws IOException;

    /**
     * Sets the length of the file.
     *
     * <p>If the present length of the file as returned by the {@link #getLength()} method is greater than the
     * <code>newLength</code> argument then the file will be truncated. In this case, if the file offset as returned
     * by the {@link #getOffset()} method is greater than <code>newLength</code> then after this method returns the
     * offset will be equal to <code>newLength</code>.</p>
     *
     * <p>If the present length of the file as returned by the {@link #getLength()} method is smaller than the 
     * <code>newLength</code> argument then the file will be extended. In this case, the contents of the extended
     * portion of the file are not defined.</p>
     *
     * @return <code>true</code> if the file's length could be changed to the new length, <code>false</code> if the
     * operation is not supported.
     * @throw IOException If an I/O error occurred while trying to change the file's length
     */
    public abstract boolean setLength(long newLength) throws IOException;
}