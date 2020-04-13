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
import java.io.InputStream;

/**
 * This InputStream implementation allows an array of byte[]'s to be
 * read.
 *
 * @since 5.0
 */
public class ByteDoubleArrayInputStream extends InputStream {

    /**
     * An array of bytes that was provided
     * by the creator of the stream. Elements <code>buf[0]</code>
     * through <code>buf[count-1]</code> are the
     * only bytes that can ever be read from the
     * stream;  element <code>buf[pos]</code> is
     * the next byte to be read.
     */
    protected byte buf[][];

    protected int bufOffset[];

    /**
     * The index of the next character to read from the input stream buffer.
     * This value should always be nonnegative
     * and not larger than the value of <code>count</code>.
     * The next byte to be read from the input stream buffer
     * will be <code>buf[pos]</code>.
     */
    protected int pos;

    protected int posIndex;

    /**
     * The currently marked position in the stream.
     * ByteArrayInputStream objects are marked at position zero by
     * default when constructed.  They may be marked at another
     * position within the buffer by the <code>mark()</code> method.
     * The current buffer position is set to this point by the
     * <code>reset()</code> method.
     * <p/>
     * If no mark has been set, then the value of mark is the offset
     * passed to the constructor (or 0 if the offset was not supplied).
     *
     * @since JDK1.1
     */
    protected int mark = 0;
    protected int markIndex = 0;

    /**
     * The index one greater than the last valid character in the input
     * stream buffer.
     * This value should always be nonnegative
     * and not larger than the length of <code>buf</code>.
     * It  is one greater than the position of
     * the last byte within <code>buf</code> that
     * can ever be read  from the input stream buffer.
     */
    protected int count;

    /**
     * Creates a <code>ByteArrayInputStream</code>
     * so that it  uses <code>buf</code> as its
     * buffer array.
     * The buffer array is not copied.
     * The initial value of <code>pos</code>
     * is <code>0</code> and the initial value
     * of  <code>count</code> is the length of
     * <code>buf</code>.
     *
     * @param buf the input buffer.
     */
    public ByteDoubleArrayInputStream(byte buf[][]) {
        this.buf = buf;
        this.pos = 0;
        this.posIndex = 0;
        bufOffset = new int[buf.length];
        for (int i = 0; i < buf.length; i++) {
            bufOffset[i] = this.count;
            this.count += buf[i].length;
        }
    }

    /**
     * Reads the next byte of data from this input stream. The value
     * byte is returned as an <code>int</code> in the range
     * <code>0</code> to <code>255</code>. If no byte is available
     * because the end of the stream has been reached, the value
     * <code>-1</code> is returned.
     * <p/>
     * This <code>read</code> method
     * cannot block.
     *
     * @return the next byte of data, or <code>-1</code> if the end of the
     *         stream has been reached.
     */
    public synchronized int read() {
        float posOffset = bufOffset[posIndex] + pos;
        if (posOffset < count) {
            if (posOffset < bufOffset[posIndex] + buf[posIndex].length) {
                return (buf[posIndex][pos++] & 0xff);
            } else {
                posIndex++;
                pos = 0;
                return (buf[posIndex][pos++] & 0xff);
            }
        } else {
            return -1;
        }
    }

    /**
     * Reads up to <code>len</code> bytes of data into an array of bytes
     * from this input stream.
     * If <code>pos</code> equals <code>count</code>,
     * then <code>-1</code> is returned to indicate
     * end of file. Otherwise, the  number <code>k</code>
     * of bytes read is equal to the smaller of
     * <code>len</code> and <code>count-pos</code>.
     * If <code>k</code> is positive, then bytes
     * <code>buf[pos]</code> through <code>buf[pos+k-1]</code>
     * are copied into <code>b[off]</code>  through
     * <code>b[off+k-1]</code> in the manner performed
     * by <code>System.arraycopy</code>. The
     * value <code>k</code> is added into <code>pos</code>
     * and <code>k</code> is returned.
     * <p/>
     * This <code>read</code> method cannot block.
     *
     * @param b   the buffer into which the data is read.
     * @param off the start offset of the data.
     * @param len the maximum number of bytes read.
     * @return the total number of bytes read into the buffer, or
     *         <code>-1</code> if there is no more data because the end of
     *         the stream has been reached.
     */
    public synchronized int read(byte b[], int off, int len) {
        if (b == null) {
            throw new NullPointerException();
        } else if ((off < 0) || (off > b.length) || (len < 0) ||
                ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        }
        if (posIndex >= buf.length) {
            return -1;
        }

        int posOffset = bufOffset[posIndex] + pos;
        if (posOffset >= count) {
            return -1;
        }
        if (posOffset + len > count) {
            len = count - posOffset;
        }
        if (len <= 0) {
            return 0;
        }
        // check if the current posIndex can handle the len copy
        if (pos + len < buf[posIndex].length) {
            System.arraycopy(buf[posIndex], pos, b, off, len);
            pos += len;
        } else {
            // we need to copy to the end of the current posIndex and then move to the next array
            int newLength = len;
            int partialOffset = buf[posIndex].length - pos;
            while (newLength > 0) {
                System.arraycopy(buf[posIndex], pos, b, off, partialOffset);
                off += partialOffset;
                newLength -= partialOffset;
                pos += partialOffset;
                if (newLength == 0) {
                    break;
                }
                // setup the next buffer
                posIndex++;
                pos = 0;
                if (pos + newLength < buf[posIndex].length) {
                    partialOffset = newLength;
                } else {
                    partialOffset = buf[posIndex].length - pos;
                }
            }
        }
        return len;
    }

    /**
     * Skips <code>n</code> bytes of input from this input stream. Fewer
     * bytes might be skipped if the end of the input stream is reached.
     * The actual number <code>k</code>
     * of bytes to be skipped is equal to the smaller
     * of <code>n</code> and  <code>count-pos</code>.
     * The value <code>k</code> is added into <code>pos</code>
     * and <code>k</code> is returned.
     *
     * @param n the number of bytes to be skipped.
     * @return the actual number of bytes skipped.
     */
    public synchronized long skip(long n) {
        if (pos + n > count) {
            n = count - pos;
        }
        if (n < 0) {
            return 0;
        }
        if (pos + n < bufOffset[posIndex]) {
            pos += n;
        } else {
            long partialOffset = (bufOffset[posIndex] - pos);
            while (n > 0) {
                n -= partialOffset;
                posIndex++;
                if (pos + n < bufOffset[posIndex]) {
                    partialOffset = n;
                } else {
                    partialOffset = (bufOffset[posIndex] - pos);
                }
            }
        }
        return n;
    }

    /**
     * Returns the number of bytes that can be read from this input
     * stream without blocking.
     * The value returned is
     * <code>count&nbsp;- pos</code>,
     * which is the number of bytes remaining to be read from the input buffer.
     *
     * @return the number of bytes that can be read from the input stream
     *         without blocking.
     */
    public synchronized int available() {
        return count - (bufOffset[posIndex] + pos);
    }

    /**
     * Tests if this <code>InputStream</code> supports mark/reset. The
     * <code>markSupported</code> method of <code>ByteArrayInputStream</code>
     * always returns <code>true</code>.
     */
    public boolean markSupported() {
        return true;
    }

    /**
     * Set the current marked position in the stream.
     * ByteArrayInputStream objects are marked at position zero by
     * default when constructed.  They may be marked at another
     * position within the buffer by this method.
     * <p/>
     * If no mark has been set, then the value of the mark is the
     * offset passed to the constructor (or 0 if the offset was not
     * supplied).
     * <p/>
     * <p> Note: The <code>readAheadLimit</code> for this class
     * has no meaning.
     */
    public void mark(int readAheadLimit) {
        mark = pos;
        markIndex = posIndex;
    }

    /**
     * Resets the buffer to the marked position.  The marked position
     * is 0 unless another position was marked or an offset was specified
     * in the constructor.
     */
    public synchronized void reset() {
        pos = mark;
        posIndex = markIndex;
    }

    /**
     * Closing a <tt>ByteArrayInputStream</tt> has no effect. The methods in
     * this class can be called after the stream has been closed without
     * generating an <tt>IOException</tt>.
     * <p/>
     */
    public void close() throws IOException {
    }

}
