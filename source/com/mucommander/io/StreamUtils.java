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
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This class provides convience static methods that operate on streams.
 *
 * @author Maxence Bernard
 */
public class StreamUtils {

    /**
     * Convience method that calls {@link #copyStream(java.io.InputStream, java.io.OutputStream, int)} with a
     * {@link BufferPool#DEFAULT_BUFFER_SIZE default buffer size}.
     */
    public static void copyStream(InputStream in, OutputStream out) throws FileTransferException {
        copyStream(in, out, BufferPool.DEFAULT_BUFFER_SIZE);
    }

    /**
     * Copies the contents of the given <code>InputStream</code> to the specified </code>OutputStream</code>
     * and throws an IOException if something went wrong. This method does *NOT* close the streams.
     *
     * <p>Read and write operations are buffered, using a buffer of the specified size (in bytes). For performance
     * reasons, this buffer is provided by {@link BufferPool}. There is no need to provide a
     * <code>BufferedInputStream</code>. A <code>BufferedOutputStream</code> also isn't necessary, unless this method
     * is called repeatedly with the same <code>OutputStream</code> and with potentially small <code>InputStream</code>
     * (smaller than the buffer's size): in this case, providing a <code>BufferedOutputStream</code> will further
     * improve performance by grouping calls to the underlying <code>OutputStream</code> write method.</p>
     *
     * <p>Copy progress can optionally be monitored by supplying a {@link com.mucommander.io.CounterInputStream} and/or
     * {@link com.mucommander.io.CounterOutputStream}.</p>
     *
     * @param in the InputStream to read from
     * @param out the OutputStream to write to
     * @param bufferSize size of the buffer to use, in bytes
     * @throws FileTransferException if something went wrong while reading from or writing to one of the provided streams
     */
    public static void copyStream(InputStream in, OutputStream out, int bufferSize) throws FileTransferException {
        // Use BufferPool to reuse any available buffer of the same size
        byte buffer[] = BufferPool.getBuffer(bufferSize);
        try {
            // Copies the InputStream's content to the OutputStream chunks by chunks
            int nbRead;

            while(true) {
                try {
                    nbRead = in.read(buffer, 0, buffer.length);
                }
                catch(IOException e) {
                    throw new FileTransferException(FileTransferException.READING_SOURCE);
                }

                if(nbRead==-1)
                    break;

                try {
                    out.write(buffer, 0, nbRead);
                }
                catch(IOException e) {
                    throw new FileTransferException(FileTransferException.WRITING_DESTINATION);
                }
            }
        }
        finally {
            // Make the buffer available for further use
            BufferPool.releaseBuffer(buffer);
        }
    }


    /**
     * Convience method that calls {@link #fillWithConstant(java.io.OutputStream, byte, long, int)} with a
     * {@link BufferPool#DEFAULT_BUFFER_SIZE default buffer size}.
     */
    public static void fillWithConstant(OutputStream out, byte value, long len) throws IOException {
        fillWithConstant(out, value, len, BufferPool.DEFAULT_BUFFER_SIZE);
    }

    /**
     * Writes the specified byte constant <code>len</code> times to the given <code>OutputStream</code>.
     * This method does *NOT* close the stream when it is finished.
     *
     * @param out the OutputStream to write to
     * @param value the byte constant to write len times
     * @param len number of bytes to write
     * @param bufferSize size of the buffer to use, in bytes
     * @throws java.io.IOException if an error occurred while writing
     */
    public static void fillWithConstant(OutputStream out, byte value, long len, int bufferSize) throws IOException {
        // Use BufferPool to avoid excessive memory allocation and garbage collection
        byte buffer[] = BufferPool.getBuffer(bufferSize);

        // Fill the buffer with the constant byte value, not necessary if the value is zero
        if(value!=0) {
            for(int i=0; i<bufferSize; i++)
                buffer[i] = value;
        }

        try {
            long remaining = len;
            int nbWrite;
            while(remaining>0) {
                nbWrite = (int)(remaining>bufferSize?bufferSize:remaining);
                out.write(buffer, 0, nbWrite);
                remaining -= nbWrite;
            }
        }
        finally {
            BufferPool.releaseBuffer(buffer);
        }
    }

    /**
     * Convience method that calls {@link #copyChunk(RandomAccessInputStream, RandomAccessOutputStream, long, long, long, int)}
     * with a {@link BufferPool#DEFAULT_BUFFER_SIZE default buffer size}.
     */
    public static void copyChunk(RandomAccessInputStream rais, RandomAccessOutputStream raos, long srcOffset, long destOffset, long length) throws IOException {
        copyChunk(rais, raos, srcOffset, destOffset, length, BufferPool.DEFAULT_BUFFER_SIZE);
    }

    /**
     * Copies a chunk of data from the given {@link com.mucommander.io.RandomAccessInputStream} to the specified
     * {@link com.mucommander.io.RandomAccessOutputStream}.
     *
     * @param rais the source stream
     * @param raos the destination stream
     * @param srcOffset offset to the beginning of the chunk in the source stream
     * @param destOffset offset to the beginning of the chunk in the destination stream
     * @param length number of bytes to copy
     * @param bufferSize size of the buffer to use, in bytes
     * @throws java.io.IOException if an error occurred while copying data
     */
    public static void copyChunk(RandomAccessInputStream rais, RandomAccessOutputStream raos, long srcOffset, long destOffset, long length, int bufferSize) throws IOException {
        rais.seek(srcOffset);
        raos.seek(destOffset);

        // Use BufferPool to avoid excessive memory allocation and garbage collection
        byte buffer[] = BufferPool.getBuffer(bufferSize);

        try {
            long remaining = length;
            int nbBytes;
            while(remaining>0) {
                nbBytes = (int)(remaining<bufferSize?remaining:bufferSize);
                rais.readFully(buffer, 0, nbBytes);
                raos.write(buffer, 0, nbBytes);
                remaining -= nbBytes;
            }
        }
        finally {
            BufferPool.releaseBuffer(buffer);
        }
    }
}
