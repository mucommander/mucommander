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

import java.io.IOException;
import java.io.OutputStream;

/**
 * <code>RandomAccessOutputStream</code> is an <code>OutputStream</code> with random access.
 *
 * <b>Important:</b> <code>BufferedOutputStream</code> or any class wrapping a standard <code>OutputStream</code>
 * and using an internal buffer CANNOT be used with a <code>RandomAccessInputStream</code> if the {@link #seek(long)}
 * method is to be used. Doing so would corrupt the write buffer and yield to data inconsistencies.
 * {@link BufferedRandomOutputStream} provides safe buffering to RandomAccessOutputStream.
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
     * by the {@link #getOffset()} method is greater than <code>newLength</code> then the
     * offset will be equal to <code>newLength</code> after this method returns .</p>
     *
     * <p>If the present length of the file as returned by the {@link #getLength()} method is smaller than the 
     * <code>newLength</code> argument then the file will be extended. In this case, the contents of the extended
     * portion of the file are not defined.</p>
     *
     * @param newLength the new file's length
     * @throws IOException If an I/O error occurred while trying to change the file's length
     */
    public abstract void setLength(long newLength) throws IOException;
}
