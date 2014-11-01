/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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


package com.mucommander.commons.file.impl.tar.provider;

import com.mucommander.commons.io.BufferPool;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * The TarInputStream reads a UNIX tar archive as an InputStream.
 * methods are provided to position at each successive entry in
 * the archive, and the read each entry as a normal input stream
 * using read().
 *
 * <p>-----------------------------------</p>
 * <p>This class is based off the <code>org.apache.tools.tar</code> package of the <i>Apache Ant</i> project. The Ant
 * code has been modified under the terms of the Apache License which you can find in the bundled muCommander license
 * file. It was forked at version 1.7.1 of Ant.</p>
 * 
 * @author Apache Ant, Maxence Bernard
 */
public class TarInputStream extends InputStream {
    private static final int NAME_BUFFER_SIZE = 256;
    private static final int BYTE_MASK = 0xFF;

    protected boolean debug;
    protected boolean hasHitEOF;
    protected long entrySize;
    protected long entryOffset;
    protected byte[] recordBuf;
    protected byte[] nameBuf;
    protected int recordBufPos;
    protected int recordBufLeft;
    protected TarBuffer buffer;
    protected TarEntry currEntry;
    protected boolean closed;

    /**
     * This contents of this array is not used at all in this class,
     * it is only here to avoid repreated object creation during calls
     * to the no-arg read method.
     */
    protected byte[] oneBuf;

    /**
     * Creates a new <code>TarInputStream</code> over the specified input stream using the default block size and
     * record size and starting at the first entry.
     *
     * @param is the input stream providing the actual TAR data
     * @throws IOException if an error ocurred while initializing the stream
     */
    public TarInputStream(InputStream is) throws IOException  {
        this(is, TarBuffer.DEFAULT_BLKSIZE, TarBuffer.DEFAULT_RCDSIZE, 0);
    }


    /**
     * Creates a new <code>TarInputStream</code> over the specified input stream, starting at the specified
     * entry offset.
     *
     * @param is the input stream providing the actual TAR data
     * @param entryOffset offset from the start of the archive to an entry. Must be a multiple of recordSize, or
     * <code>0</code> to start at the first entry.
     * @throws IOException if an error ocurred while initializing the stream
     */
    public TarInputStream(InputStream is, long entryOffset) throws IOException {
        this(is, TarBuffer.DEFAULT_BLKSIZE, TarBuffer.DEFAULT_RCDSIZE, entryOffset);
    }


    /**
     * Creates a new <code>TarInputStream</code> over the specified input stream, using the specified
     * block size, record size and start offset.
     *
     * @param is the input stream to use
     * @param blockSize the block size to use
     * @param recordSize the record size to use
     * @param entryOffset offset from the start of the archive to an entry. Must be a multiple of recordSize, or
     * <code>0</code> to start at the first entry.
     * @throws IOException if an error ocurred while initializing the stream
     */
    public TarInputStream(InputStream is, int blockSize, int recordSize, long entryOffset) throws IOException {
        this.buffer = new TarBuffer(is, blockSize, recordSize);
        this.recordBuf = BufferPool.getByteArray(buffer.getRecordSize());
        this.nameBuf = BufferPool.getByteArray(NAME_BUFFER_SIZE);
        this.oneBuf = BufferPool.getByteArray(1);
        this.debug = false;
        this.hasHitEOF = false;

        if(entryOffset>0) {
            if((entryOffset%recordSize)!=0)
                throw new IllegalArgumentException("entryOffset ("+entryOffset+") is not a multiple of recordSize ("+recordSize+")");

            skipBytes(entryOffset);
        }

    }

    /**
     * Sets the debugging flag.
     *
     * @param debug True to turn on debugging.
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
        buffer.setDebug(debug);
    }

    /**
     * Closes this stream. Calls the TarBuffer's close() method.
     * @throws IOException on error
     */
    @Override
    public void close() throws IOException {
        if (!closed) {
            try {
                buffer.close();
            }
            finally {
                BufferPool.releaseByteArray(recordBuf);
                BufferPool.releaseByteArray(nameBuf);
                BufferPool.releaseByteArray(oneBuf);

                closed = true;
            }
        }
    }

    /**
     * Get the record size being used by this stream's TarBuffer.
     *
     * @return The TarBuffer record size.
     */
    public int getRecordSize() {
        return buffer.getRecordSize();
    }

    /**
     * Get the available data that can be read from the current
     * entry in the archive. This does not indicate how much data
     * is left in the entire archive, only in the current entry.
     * This value is determined from the entry's size header field
     * and the amount of data already read from the current entry.
     * Integer.MAX_VALUE is returen in case more than Integer.MAX_VALUE
     * bytes are left in the current entry in the archive.
     *
     * @return The number of available bytes for the current entry.
     * @throws IOException for signature
     */
    @Override
    public int available() throws IOException {
        if (entrySize - entryOffset > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) (entrySize - entryOffset);
    }

    /**
     * Since we do not support marking just yet, we return false.
     *
     * @return False.
     */
    @Override
    public boolean markSupported() {
        return false;
    }

    /**
     * Since we do not support marking just yet, we do nothing.
     *
     * @param markLimit The limit to mark.
     */
    @Override
    public void mark(int markLimit) {
    }

    /**
     * Since we do not support marking just yet, we do nothing.
     */
    @Override
    public void reset() {
    }


    /**
     * Reads a whole new record from the {@link TarBuffer} into the {@link #recordBuf record buffer} and resets
     * {@link #recordBufPos} and {@link #recordBufLeft} fields accordingly.
     *
     * @return <code>true</code> if the record has been read, <code>false</code> if EOF has been reached
     * @throws IOException on error
     */
    public boolean readRecord() throws IOException {
        boolean ret = buffer.readRecord(recordBuf);

        recordBufPos = 0;
        recordBufLeft = ret?recordBuf.length:0;

        return ret;
    }


    /**
     * Returns the current entry where this <code>TarInputStream</code> is currently positionned.
     *
     * @return the current entry where this <code>TarInputStream</code> is currently positionned
     */
    public TarEntry getCurrentEntry() {
        return currEntry;
    }

    /**
     * Get the next entry in this tar archive. This will skip
     * over any remaining data in the current entry, if there
     * is one, and place the input stream at the header of the
     * next entry, and read the header and instantiate a new
     * TarEntry from the header bytes and return that entry.
     * If there are no more entries in the archive, null will
     * be returned to indicate that the end of the archive has
     * been reached.
     *
     * @return The next TarEntry in the archive, or null.
     * @throws IOException on error
     */
    public TarEntry getNextEntry() throws IOException {
        if (hasHitEOF) {
            return null;
        }

        if (currEntry != null) {
            long numToSkip = entrySize - entryOffset;

            if (debug) {
                System.err.println("TarInputStream: SKIP currENTRY '"
                        + currEntry.getName() + "' SZ "
                        + entrySize + " OFF "
                        + entryOffset + "  skipping "
                        + numToSkip + " bytes");
            }

            if (numToSkip > 0) {
                skipBytes(numToSkip);
            }
        }

        // Read the header record
        if (!readRecord()) {
            if (debug) {
                System.err.println("READ NULL RECORD");
            }
            hasHitEOF = true;
        } else if (buffer.isEOFRecord(recordBuf)) {
            if (debug) {
                System.err.println("READ EOF RECORD");
            }
            hasHitEOF = true;
        }

        if (hasHitEOF) {
            currEntry = null;
        } else {
            currEntry = new TarEntry(recordBuf);

            // Offset of the current entry from the start of the archive,
            // allows to reposition the stream at the start of the entry
            currEntry.setOffset(buffer.getCurrentBlockNum()*buffer.getBlockSize()
                               + buffer.getCurrentRecordNum()*buffer.getRecordSize());

            if (debug) {
                System.err.println("TarInputStream: SET CURRENTRY '"
                        + currEntry.getName()
                        + "' size = "
                        + currEntry.getSize());
            }

            // Update the current entry offset and size
            entryOffset = 0;
            entrySize = currEntry.getSize();

            // Consume the rest of the record
            recordBufPos = 0;
            recordBufLeft = 0;
        }

        if (currEntry != null && currEntry.isGNULongNameEntry()) {
            // read in the name
            StringBuffer longName = new StringBuffer();
            int length;
            while ((length = read(nameBuf)) >= 0) {
                longName.append(new String(nameBuf, 0, length));
            }
            getNextEntry();
            if (currEntry == null) {
                // Bugzilla: 40334
                // Malformed tar file - long entry name not followed by entry
                return null;
            }
            // remove trailing null terminator
            if (longName.length() > 0
                && longName.charAt(longName.length() - 1) == 0) {
                longName.deleteCharAt(longName.length() - 1);
            }
            currEntry.setName(longName.toString());
        }

        return currEntry;
    }

    /**
     * Reads a byte from the current tar archive entry.
     *
     * This method simply calls read( byte[], int, int ).
     *
     * @return The byte read, or -1 at EOF.
     * @throws IOException on error
     */
    @Override
    public int read() throws IOException {
        int num = read(oneBuf, 0, 1);
        return num == -1 ? -1 : ((int) oneBuf[0]) & BYTE_MASK;
    }



    /**
     * Reads bytes from the current tar archive entry.
     *
     * This method is aware of the boundaries of the current
     * entry in the archive and will deal with them as if they
     * were this stream's start and EOF.
     *
     * @param buf The buffer into which to place bytes read.
     * @param offset The offset at which to place bytes read.
     * @param numToRead The number of bytes to read.
     * @return The number of bytes read, or -1 at EOF.
     * @throws IOException on error
     */
    @Override
    public int read(byte[] buf, int offset, int numToRead) throws IOException {
        int totalRead = 0;

        // Have we already reached the end of file/entry ?
        if (entryOffset >= entrySize) {
            return -1;
        }

        // Can't read more than the entry's size
        if ((numToRead + entryOffset) > entrySize) {
            numToRead = (int) (entrySize - entryOffset);
        }

        // Read data one record (at most) at a time. The record buffer is first emptied before reading a new record.
        while (numToRead > 0) {
            // If there is no more data left to read from the current record buffer,
            // read a new record  
            if(recordBufLeft<=0) {
                if (!readRecord()) {
                    // Unexpected EOF!
                    throw new EOFException("unexpected EOF with " + numToRead + " bytes unread");
                }
            }

            int sz = (numToRead > recordBufLeft)
                    ? recordBufLeft
                    : numToRead;

            System.arraycopy(recordBuf, recordBufPos, buf, offset, sz);

            recordBufPos += sz;
            recordBufLeft -= sz;

            totalRead += sz;
            numToRead -= sz;
            offset += sz;
            entryOffset += sz;
        }

        return totalRead;
    }

    /**
     * Skip bytes in the input buffer. This skips bytes in the
     * current entry's data, not the entire archive, and will
     * stop at the end of the current entry's data if the number
     * to skip extends beyond that point.
     *
     * @param numToSkip the number of bytes to skip.
     * @return the number actually skipped
     * @throws IOException on error
     */
    @Override
    public long skip(long numToSkip) throws IOException {
        // Have we already reached the end of file/entry ?
        if (entryOffset >= entrySize) {
            return -1;
        }

        // Can't read more than the entry's size
        if ((numToSkip + entryOffset) > entrySize) {
            numToSkip = (int) (entrySize - entryOffset);
        }

        return skipBytes(numToSkip);
    }

    /**
     * Skips the specified number of bytes, without checking for the current entry's boundaries.
     *
     * @param numToSkip the number of bytes to skip.
     * @return the number actually skipped
     * @throws IOException on error
     */
    private long skipBytes(long numToSkip) throws IOException {
        int totalSkipped = 0;

        int recordSize = buffer.getRecordSize();
        int blockSize = buffer.getBlockSize();

        while (numToSkip > 0) {
            // If the record buffer has some data left, empty it
            if(recordBufLeft>0) {
                int sz = (numToSkip > recordBufLeft)
                        ? recordBufLeft
                        : (int)numToSkip;

                recordBufPos += sz;
                recordBufLeft -= sz;

                totalSkipped += sz;
                numToSkip -= sz;
                entryOffset += sz;
            }
            // Skip a whole block if there are enough bytes left to skip, and if we are at the end of the current block
            else if(numToSkip>=blockSize && buffer.getCurrentRecordNum()==buffer.getRecordsPerBlock()-1) {
                if (!buffer.skipBlock()) {
                    // Unexpected EOF!
                    throw new EOFException("unexpected EOF with " + numToSkip + " bytes unskipped");
                }

                totalSkipped += blockSize;
                numToSkip -= blockSize;
                entryOffset += blockSize;
            }
            // Skip a whole record if there are enough bytes left to skip
            else if(numToSkip>=recordSize) {
                if (!buffer.skipRecord()) {
                    // Unexpected EOF!
                    throw new EOFException("unexpected EOF with " + numToSkip + " bytes unskipped");
                }

                totalSkipped += recordSize;
                numToSkip -= recordSize;
                entryOffset += recordSize;
            }
            // There is less than a record to skip -> read the record and skip
            else {
                if (!readRecord()) {
                    // Unexpected EOF!
                    throw new EOFException("unexpected EOF with " + numToSkip + " bytes unskipped");
                }

                // if(recordBufLeft>0) will be matched on the next loop
            }
        }

        return totalSkipped;
    }
}
