/*
 * This file is part of muCommander, http://www.mucommander.com
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

import java.io.*;

/**
 * This class provides convenience static methods that operate on streams. All read/write buffers are allocated using
 * {@link BufferPool} for memory efficiency reasons.
 *
 * @author Maxence Bernard
 */
public class StreamUtils {

    /**
     * This method is a shorthand for {@link #copyStream(java.io.InputStream, java.io.OutputStream, int)} called with a
     * {@link BufferPool#getDefaultBufferSize() default buffer size}.
     *
     * @param in the InputStream to read from
     * @param out the OutputStream to write to
     * @return the number of bytes that were copied
     * @throws FileTransferException if something went wrong while reading from or writing to one of the provided streams
     */
    public static long copyStream(InputStream in, OutputStream out) throws FileTransferException {
        return copyStream(in, out, BufferPool.getDefaultBufferSize());
    }

    /**
     * This method is a shorthand for {@link #copyStream(java.io.InputStream, java.io.OutputStream, int, long)} called
     * with a {@link Long#MAX_VALUE}.
     *
     * @param in the InputStream to read from
     * @param out the OutputStream to write to
     * @param bufferSize size of the buffer to use, in bytes
     * @return the number of bytes that were copied
     * @throws FileTransferException if something went wrong while reading from or writing to one of the provided streams
     */
    public static long copyStream(InputStream in, OutputStream out, int bufferSize) throws FileTransferException {
        return copyStream(in, out, bufferSize, Long.MAX_VALUE);
    }
    
    /**
     * Shorthand for {@link #copyStream(InputStream, OutputStream, byte[], long)} called with a buffer of the specified
     * size retrieved from {@link BufferPool}.
     *
     * @param in the InputStream to read from
     * @param out the OutputStream to write to
     * @param bufferSize size of the buffer to use, in bytes
     * @param length number of bytes to copy from InputStream
     * @return the number of bytes that were copied
     * @throws FileTransferException if something went wrong while reading from or writing to one of the provided streams
     */
    public static long copyStream(InputStream in, OutputStream out, int bufferSize, long length) throws FileTransferException {
        // Use BufferPool to reuse any available buffer of the same size
        byte buffer[] = BufferPool.getByteArray(bufferSize);
        try {
            return copyStream(in, out, buffer, length);
        }
        finally {
            // Make the buffer available for further use
            BufferPool.releaseByteArray(buffer);
        }
    }
    
    /**
     * Copies up to <code>length</code> bytes from the given <code>InputStream</code> to the specified
     * </code>OutputStream</code>, less if the end-of-file was reached before that. 
     * This method does *NOT* close any of the given streams.
     *
     * <p>Read and write operations use the specified buffer, making the use of a <code>BufferedInputStream</code>
     * unnecessary. A <code>BufferedOutputStream</code> also isn't necessary, unless this method
     * is called repeatedly with the same <code>OutputStream</code> and with potentially small <code>InputStream</code>
     * (smaller than the buffer's size): in this case, providing a <code>BufferedOutputStream</code> will further
     * improve performance by grouping calls to the underlying <code>OutputStream</code> write method.</p>
     *
     * <p>Copy progress can optionally be monitored by supplying a {@link com.mucommander.commons.io.CounterInputStream} and/or
     * {@link com.mucommander.commons.io.CounterOutputStream}.</p>
     *
     * @param in the InputStream to read from
     * @param out the OutputStream to write to
     * @param buffer buffer to use for copying
     * @param length number of bytes to copy from InputStream
     * @return the number of bytes that were copied
     * @throws FileTransferException if something went wrong while reading from or writing to one of the provided streams
     */
    public static long copyStream(InputStream in, OutputStream out, byte[] buffer, long length) throws FileTransferException {
        // Copies the InputStream's content to the OutputStream chunk by chunk
        int nbRead;
        long totalRead = 0;

        while(length>0) {
            try {
                nbRead = in.read(buffer, 0, (int)Math.min(buffer.length, length));	// the result of min will be int
            }
            catch(IOException e) {
                throw new FileTransferException(FileTransferError.READING_SOURCE);
            }

            if(nbRead==-1)
                break;

            try {
                out.write(buffer, 0, nbRead);
            }
            catch(IOException e) {
                throw new FileTransferException(FileTransferError.WRITING_DESTINATION, totalRead);
            }

            length -= nbRead;
            totalRead += nbRead;
        }

        return totalRead;
    }

    /**
     * This method is a shorthand for {@link #transcode(java.io.InputStream, String, java.io.OutputStream, String, int)}
     * called with a {@link BufferPool#getDefaultBufferSize() default buffer size}.
     *
     * @param in the InputStream to read from
     * @param inCharset the source charset
     * @param out the OutputStream to write to
     * @param outCharset the destination charset
     * @return the number of bytes that were transcoded
     * @throws FileTransferException if something went wrong while reading from or writing to one of the provided streams
     * @throws UnsupportedEncodingException if any of the two charsets are not supported by the JVM
     */
    public static long transcode(InputStream in, String inCharset, OutputStream out, String outCharset) throws FileTransferException, UnsupportedEncodingException {
        return transcode(in, inCharset, out, outCharset, BufferPool.getDefaultBufferSize());
    }

    /**
     * Converts a stream from a charset to another, copying the contents of the given <code>InputStream</code> to the
     * <code>OutputStream</code>. A {@link java.io.UnsupportedEncodingException} is thrown if any of the two charsets
     * are not supported by the JVM.
     *
     * <p>Apart from the transcoding part, this method operates exactly like {@link #copyStream(java.io.InputStream, java.io.OutputStream, int)}.
     * In particular, none of the given streams are closed.</p>
     *
     * @param in the InputStream to read the data from
     * @param inCharset the source charset
     * @param out the OutputStream to write to
     * @param outCharset the destination charset
     * @param bufferSize size of the buffer to use, in bytes
     * @return the number of bytes that were transcoded
     * @throws FileTransferException if something went wrong while reading from or writing to one of the provided streams
     * @throws UnsupportedEncodingException if any of the two charsets are not supported by the JVM
     * @see #copyStream(java.io.InputStream, java.io.OutputStream, int)
     * @see java.nio.charset.Charset#isSupported(String)
     */
    public static long transcode(InputStream in, String inCharset, OutputStream out, String outCharset, int bufferSize) throws FileTransferException, UnsupportedEncodingException {
        InputStreamReader isr = new InputStreamReader(in, inCharset);
        OutputStreamWriter osw = new OutputStreamWriter(out, outCharset);

        // Use BufferPool to reuse any available buffer of the same size
        char buffer[] = BufferPool.getCharArray(bufferSize);
        try {
            // Copies the InputStreamReader's content to the OutputStreamWriter chunk by chunk
            int nbRead;
            long totalRead = 0;

            while(true) {
                try {
                    nbRead = isr.read(buffer, 0, buffer.length);
                }
                catch(IOException e) {
                    throw new FileTransferException(FileTransferError.READING_SOURCE);
                }

                if(nbRead==-1)
                    break;

                try {
                    osw.write(buffer, 0, nbRead);
                    // Let's not forget to flush as the writer will *not* be closed (to avoid closing the OutputStream)
                    osw.flush();
                }
                catch(IOException e) {
                    throw new FileTransferException(FileTransferError.WRITING_DESTINATION);
                }

                totalRead += nbRead;
            }

            return totalRead;
        }
        finally {
            // Make the buffer available for further use
            BufferPool.releaseCharArray(buffer);
        }
    }

    /**
     * This method is a shorthand for {@link #fillWithConstant(java.io.OutputStream, byte, long, int)} called with a
     * {@link BufferPool#getDefaultBufferSize default buffer size}.
     *
     * @param out the OutputStream to write to
     * @param value the byte constant to write len times
     * @param len number of bytes to write
     * @throws java.io.IOException if an error occurred while writing
     */
    public static void fillWithConstant(OutputStream out, byte value, long len) throws IOException {
        fillWithConstant(out, value, len, BufferPool.getDefaultBufferSize());
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
        byte buffer[] = BufferPool.getByteArray(bufferSize);

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
            BufferPool.releaseByteArray(buffer);
        }
    }

    /**
     * This method is a shorthand for {@link #copyChunk(RandomAccessInputStream, RandomAccessOutputStream, long, long, long, int)}
     * called with a {@link BufferPool#getDefaultBufferSize default buffer size}.
     *
     * @param rais the source stream
     * @param raos the destination stream
     * @param srcOffset offset to the beginning of the chunk in the source stream
     * @param destOffset offset to the beginning of the chunk in the destination stream
     * @param length number of bytes to copy
     * @throws java.io.IOException if an error occurred while copying data
     */
    public static void copyChunk(RandomAccessInputStream rais, RandomAccessOutputStream raos, long srcOffset, long destOffset, long length) throws IOException {
        copyChunk(rais, raos, srcOffset, destOffset, length, BufferPool.getDefaultBufferSize());
    }

    /**
     * Copies a chunk of data from the given {@link com.mucommander.commons.io.RandomAccessInputStream} to the specified
     * {@link com.mucommander.commons.io.RandomAccessOutputStream}.
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
        byte buffer[] = BufferPool.getByteArray(bufferSize);

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
            BufferPool.releaseByteArray(buffer);
        }
    }


    /**
     * This method is a shorthand for {@link #readFully(java.io.InputStream, byte[], int, int)}.
     *
     * @param in the InputStream to read from
     * @param b the buffer into which the stream data is copied
     * @return the same byte array that was passed, returned only for convenience
     * @throws java.io.EOFException if EOF is reached before all bytes have been read
     * @throws IOException if an I/O error occurs
     */
    public static byte[] readFully(InputStream in, byte b[]) throws EOFException, IOException {
        return readFully(in, b, 0, b.length);
    }

    /**
     * Reads exactly <code>len</code> bytes from the <code>InputStream</code> and copies them into the byte array,
     * starting at position <code>off</code>.
     *
     * <p>This method calls the <code>read()</code> method of the given stream until the requested number of bytes have
     * been skipped, or throws an {@link EOFException} if the end of file has been reached prematurely.</p>
     *
     * @param in the InputStream to read from
     * @param b the buffer into which the stream data is copied
     * @param off specifies where the copy should start in the buffer
     * @param len the number of bytes to read
     * @return the same byte array that was passed, returned only for convenience
     * @throws java.io.EOFException if EOF is reached before all bytes have been read
     * @throws IOException if an I/O error occurs
     */
    public static byte[] readFully(InputStream in, byte b[], int off, int len) throws EOFException, IOException {
        if(len>0) {
            int totalRead = 0;
            do {
                int nbRead = in.read(b, off + totalRead, len - totalRead);
                if (nbRead < 0)
                    throw new EOFException();
                totalRead += nbRead;
            }
            while (totalRead < len);
        }

        return b;
    }

    /**
     * Skips exactly <code>n</code>bytes from the given InputStream.
     *
     * <p>This method calls the <code>skip()</code> method of the given stream until the requested number of bytes have
     * been skipped, or throws an {@link EOFException} if the end of file has been reached prematurely.</p>
     *
     * @param in the InputStream to skip bytes from
     * @param n the number of bytes to skip
     * @throws java.io.EOFException if the EOF is reached before all bytes have been skipped
     * @throws java.io.IOException if an I/O error occurs
     */
    public static void skipFully(InputStream in, long n) throws IOException {
        if(n<=0)
            return;

        do {
            long nbSkipped = in.skip(n);
            if(nbSkipped==0)
                throw new EOFException();

            n -= nbSkipped;
        } while(n>0);
    }

    /**
     * This method is a shorthand for {@link #readUpTo(java.io.InputStream, byte[], int, int) readUpTo(in, b, 0, b.length)}.
     *
     * @param in the InputStream to read from
     * @param b the buffer into which the stream data is copied
     * @return the number of bytes that have been read, can be less than len if EOF has been reached prematurely
     * @throws IOException if an I/O error occurs
     */
    public static int readUpTo(InputStream in, byte b[]) throws IOException {
        return readUpTo(in, b, 0, b.length);
    }

    /**
     * Reads up to <code>len</code> bytes from the <code>InputStream</code> and copies them into the byte array,
     * starting at position <code>off</code>.
     *
     * <p>This method differs from {@link #readFully(java.io.InputStream, byte[], int, int)} in that it does not throw
     * a <code>java.io.EOFException</code> if the end of stream is reached before all bytes have been read. In that
     * case (and in that case only), the number of bytes returned by this method will be lower than <code>len</code>.
     * </p>
     *
     * @param in the InputStream to read from
     * @param b the buffer into which the stream data is copied
     * @param off specifies where the copy should start in the buffer
     * @param len the number of bytes to read
     * @return the number of bytes that have been read, can be less than len if EOF has been reached prematurely
     * @throws IOException if an I/O error occurs
     */
    public static int readUpTo(InputStream in, byte b[], int off, int len) throws IOException {
        int totalRead = 0;
        if(len>0) {
            do {
                int nbRead = in.read(b, off + totalRead, len - totalRead);
                if (nbRead < 0)
                    break;
                totalRead += nbRead;
            }
            while (totalRead < len);
        }

        return totalRead;
    }


    /**
     * This method is a shorthand for {@link #readUntilEOF(java.io.InputStream, int)} called with a
     * {@link BufferPool#getDefaultBufferSize default buffer size}.
     *
     * @param in the InputStream to read
     * @throws IOException if an I/O error occurs
     */
    public static void readUntilEOF(InputStream in) throws IOException {
        readUntilEOF(in, BufferPool.getDefaultBufferSize());
    }

    /**
     * This method reads the given InputStream until the End Of File is reached, discarding all the data that is read
     * in the process. It is noteworthy that this method does <b>not</b> close the stream.
     *
     * @param in the InputStream to read
     * @param bufferSize size of the read buffer
     * @throws IOException if an I/O error occurs
     */
    public static void readUntilEOF(InputStream in, int bufferSize) throws IOException {
        // Use BufferPool to avoid excessive memory allocation and garbage collection
        byte buffer[] = BufferPool.getByteArray(bufferSize);

        try {
            int nbRead;
            while(true) {
                nbRead = in.read(buffer, 0, buffer.length);

                if(nbRead==-1)
                    break;
            }
        }
        finally {
            BufferPool.releaseByteArray(buffer);
        }
    }
}
