/*
 * Copyright 2006-2017 ICEsoft Technologies Canada Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS
 * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.icepdf.core.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Mark Collette
 * @since 2.0
 */
public class ConservativeSizingByteArrayOutputStream extends OutputStream {
    protected byte buf[];
    protected int count;

    /**
     * Creates a new byte array output stream, with the given initial
     * buffer capacity
     *
     * @param capacity The initial capacity
     * @throws IllegalArgumentException if capacity is negative
     */
    public ConservativeSizingByteArrayOutputStream(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Negative initial capacity: " + capacity);
        }
        buf = allocateByteArray(capacity);
        count = 0;
    }

    /**
     * Creates a new byte array output stream, with the given initial
     * buffer
     *
     * @param buffer The initial buffer
     * @throws IllegalArgumentException if capacity is negative
     */
    public ConservativeSizingByteArrayOutputStream(byte[] buffer) {
        if (buffer == null)
            throw new IllegalArgumentException("Initial buffer is null");
        else if (buffer.length == 0)
            throw new IllegalArgumentException("Initial buffer has zero length");
        buf = buffer;
        count = 0;
    }

    public synchronized void write(int b) throws IOException {
        int newCount = count + 1;
        if (newCount > buf.length)
            resizeArrayToFit(newCount);
        buf[count] = (byte) b;
        count = newCount;
    }

    public synchronized void write(byte b[], int off, int len) throws IOException {
        if ((off < 0) || (off >= b.length) || (len < 0) ||
                ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0)
            return;
        int newCount = count + len;
        if (newCount > buf.length)
            resizeArrayToFit(newCount);
        System.arraycopy(b, off, buf, count, len);
        count = newCount;
    }

    public synchronized void reset() {
        count = 0;
    }

    /**
     * Creates a newly allocated byte array. Its length is equal to the
     * current count of bytes in this output stream. The data bytes are
     * then copied into it.
     *
     * @return The current contents of this output stream, as a byte array.
     */
    public synchronized byte[] toByteArray() {
        byte newBuf[] = allocateByteArray(count);
        System.arraycopy(buf, 0, newBuf, 0, count);
        return newBuf;
    }

    /**
     * Returns the current size of the buffer.
     *
     * @return The number of valid bytes in this output stream.
     */
    public int size() {
        return count;
    }

    /**
     * Allows the caller to take ownership of this output stream's
     * byte array. Note that this output stream will then make
     * a new small buffer for itself and reset its size information,
     * meaning that you should call size() before this.
     */
    public synchronized byte[] relinquishByteArray() {
        byte[] returnBuf = buf;
        buf = new byte[64];
        count = 0;
        return returnBuf;
    }

    /**
     * @return true, if there was enough memory to trim buf; false otherwise
     */
    public boolean trim() {
        if (count == 0 && (buf == null || buf.length == 0))
            return true;
        if (count == buf.length)
            return true;

        byte newBuf[] = allocateByteArray(count);
        if (newBuf == null)
            return false;
        System.arraycopy(buf, 0, newBuf, 0, count);
        buf = null;
        buf = newBuf;
        return true;
    }

    protected void resizeArrayToFit(int newCount) {
        int steppedSize = buf.length;
        if (steppedSize == 0)
            steppedSize = 64;
        else if (steppedSize <= 1024)
            steppedSize *= 4;
        else if (steppedSize <= 4024)
            steppedSize *= 2;
        else if (steppedSize <= 2 * 1024 * 1024) {
            steppedSize *= 2;
            steppedSize &= (~0x0FFF);           // Fit on even 4KB pages
        } else if (steppedSize <= 4 * 1024 * 1024) {
            steppedSize = (steppedSize * 3) / 2;  // x 1.50
            steppedSize &= (~0x0FFF);           // Fit on even 4KB pages
        } else if (steppedSize <= 15 * 1024 * 1024) {
            steppedSize = (steppedSize * 5) / 4;  // x 1.25
            steppedSize &= (~0x0FFF);           // Fit on even 4KB pages
        } else {
            steppedSize = (steppedSize + (3 * 1024 * 1024));  // Go up in 3MB increments
            steppedSize &= (~0x0FFF);           // Fit on even 4KB pages
        }

        int newBufSize = Math.max(steppedSize, newCount);
        byte newBuf[] = allocateByteArray(newBufSize);
        System.arraycopy(buf, 0, newBuf, 0, count);
        buf = newBuf;
    }

    protected byte[] allocateByteArray(int size) {
        return new byte[size];
    }
}
