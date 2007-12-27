/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
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

/**
 * BufferedRandomOutputStream is a buffered output stream for {@link RandomAccessOutputStream} which, unlike a regular
 * <code>java.io.BufferedOutputStream</code>, makes it safe to seek in the underlying <code>RandomAccessOutputStream</code>.
 *
 * <p>This class uses {@link BufferPool} to create the internal buffer, to avoid excessive memory allocation and
 * garbage collection. The buffer is released when this stream is closed.</p>
 *
 * @author Maxence Bernard
 */
public class BufferedRandomOutputStream extends RandomAccessOutputStream {

    /** The underlying random access output stream */
    private RandomAccessOutputStream raos;

    /** The buffer where written bytes are accumulated before being sent to the underlying output stream */
    private byte buffer[];

    /** The current number of bytes waiting to be flushed to the underlying output stream */
    private int count;

    /** The default buffer size if none is specified */
    public final static int DEFAULT_BUFFER_SIZE = 65536;


    /**
     * Creates a new <code>BufferedRandomOutputStream</code> on top of the given {@link RandomAccessOutputStream}.
     * An internal buffer of {@link #DEFAULT_BUFFER_SIZE} bytes is created.
     *
     * @param raos the underlying RandomAccessOutputStream used by this buffered output stream
     */
    public BufferedRandomOutputStream(RandomAccessOutputStream raos) {
        this(raos, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Creates a new <code>BufferedRandomOutputStream</code> on top of the given {@link RandomAccessOutputStream}.
     * An internal buffer of the specified size is created.
     *
     * @param raos the underlying RandomAccessOutputStream used by this buffered output stream
     * @param size size of the buffer in bytes
     */
    public BufferedRandomOutputStream(RandomAccessOutputStream raos, int size) {
        this.raos = raos;
        this.buffer = BufferPool.getBuffer(size);
    }

    /**
     * Flushes the internal buffer.
     *
     * @throws IOException if an error occurs
     */
    private void flushBuffer() throws IOException {
        if (count > 0) {
            raos.write(buffer, 0, count);
            count = 0;
        }
    }


    /////////////////////////////////////////////
    // RandomAccessOutputStream implementation //
    /////////////////////////////////////////////

    /**
     * Writes the specified byte to this buffered output stream.
     *
     * @param b the byte to be written
     * @throws IOException if an I/O error occurs
     */
    public synchronized void write(int b) throws IOException {
        if (count >= buffer.length)
            flushBuffer();

        buffer[count++] = (byte)b;
    }

    /**
     * Writes the specified byte array to this buffered output stream.
     *
     * @param b the bytes to be written
     * @throws IOException if an I/O error occurs
     */
    public synchronized void write(byte b[]) throws IOException {
        write(b, 0, b.length);
    }

    /**
     * Writes <code>len</code> bytes from the specified byte array starting at offset <code>off</code> to this
     * buffered output stream.
     *
     * <p>Usually this method stores bytes from the given array into this
     * stream's buffer, flushing the buffer to the underlying output stream as
     * needed. However, if the requested data length is equal or larger than this stream's
     * buffer, then this method will flush the buffer and write the
     * bytes directly to the underlying output stream. Thus redundant
     * <code>RandomBufferedOutputStream</code>s will not copy data unnecessarily.</p>
     *
     * @param b the data.
     * @param off the start offset in the data.
     * @param len the number of bytes to write.
     * @throws IOException if an I/O error occurs.
     */
    public synchronized void write(byte b[], int off, int len) throws IOException {
        if (len >= buffer.length) {
            /* If the request length exceeds the size of the output buffer,
            flush the output buffer and then write the data directly.
            In this way buffered streams will cascade harmlessly. */
            flushBuffer();
            raos.write(b, off, len);
            return;
        }

        if (len > buffer.length - count)
            flushBuffer();

        System.arraycopy(b, off, buffer, count, len);
        count += len;
    }

    /**
     * Flushes this buffered output stream. This forces any buffered
     * output bytes to be written out to the underlying output stream.
     *
     * @throws IOException if an I/O error occurs.
     */
    public synchronized void flush() throws IOException {
        flushBuffer();
        raos.flush();
    }

    public synchronized long getOffset() throws IOException {
        // Add the buffered byte count
        return raos.getOffset() + count;
    }

    public synchronized void seek(long offset) throws IOException {
        // Flush any buffered bytes before seeking, otherwise buffered bytes would be written at the wrong offset
        flush();

        raos.seek(offset);
    }

    public synchronized long getLength() throws IOException {
        // Anticipate if the file is to be expanded by the bytes awaiting in the buffer
        return Math.max(raos.getLength(), getOffset());
    }

    public synchronized void setLength(long newLength) throws IOException {
        // Flush before changing the file's length, otherwise the behavior of setLength() would be modified, especially
        // when truncating the file
        flush();

        raos.setLength(newLength);
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    /**
     * This method is overridden to release the internal buffer when this stream is closed.
     */
    public synchronized void close() throws IOException {
        if(buffer!=null) {      // buffer is null if close() was already called
            try {
                flush();
            }
            catch(IOException e) {
                // Continue anyway
            }

            // Release the buffer
            BufferPool.releaseBuffer(buffer);
            buffer = null;
        }

        raos.close();
    }

    /**
     * This method is overridden to release the internal buffer if {@link #close()} has not been called, to avoid any
     * memory leak.
     */
    protected void finalize() throws Throwable {
        // If this stream hasn't been closed, release the buffer before finalizing the object
        if(buffer!=null)
            BufferPool.releaseBuffer(buffer);

        super.finalize();
    }
}
