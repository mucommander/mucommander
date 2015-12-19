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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * The TarBuffer class implements the tar archive concept
 * of a buffered input stream. This concept goes back to the
 * days of blocked tape drives and special io devices. In the
 * Java universe, the only real function that this class
 * performs is to ensure that files have the correct "block"
 * size, or other tars will complain.
 * <p>
 * You should never have a need to access this class directly.
 * TarBuffers are created by Tar IO Streams.
 * <p>-----------------------------------</p>
 * <p>This class is based off the <code>org.apache.tools.tar</code> package of the <i>Apache Ant</i> project. The Ant
 * code has been modified under the terms of the Apache License which you can find in the bundled muCommander license
 * file. It was forked at version 1.7.1 of Ant.</p>
 * 
 * @author Apache Ant, Maxence Bernard
 */
public class TarBuffer {

    /** Default record size */
    public static final int DEFAULT_RCDSIZE = (512);

    /** Default block size */
    public static final int DEFAULT_BLKSIZE = (DEFAULT_RCDSIZE * 20);

    private InputStream     inStream;
    private OutputStream    outStream;
    private byte[]          blockBuffer;
    private int             currBlkIdx;
    private int             currRecIdx;
    private int             blockSize;
    private int             recordSize;
    private int             recsPerBlock;
    private boolean         debug;

    /**
     * Constructor for a TarBuffer on an input stream.
     * @param inStream the input stream to use
     */
    public TarBuffer(InputStream inStream) {
        this(inStream, TarBuffer.DEFAULT_BLKSIZE, TarBuffer.DEFAULT_RCDSIZE);
    }

    /**
     * Constructor for a TarBuffer on an input stream.
     * @param inStream the input stream to use
     * @param blockSize the block size to use
     * @param recordSize the record size to use
     */
    public TarBuffer(InputStream inStream, int blockSize, int recordSize) {
        this.inStream = inStream;
        this.outStream = null;

        this.initialize(blockSize, recordSize);
    }

    /**
     * Constructor for a TarBuffer on an output stream.
     * @param outStream the output stream to use
     */
    public TarBuffer(OutputStream outStream) {
        this(outStream, TarBuffer.DEFAULT_BLKSIZE, TarBuffer.DEFAULT_RCDSIZE);
    }

    /**
     * Constructor for a TarBuffer on an output stream.
     * @param outStream the output stream to use
     * @param blockSize the block size to use
     * @param recordSize the record size to use
     */
    public TarBuffer(OutputStream outStream, int blockSize, int recordSize) {
        this.inStream = null;
        this.outStream = outStream;

        this.initialize(blockSize, recordSize);
    }

    /**
     * Initialization common to all constructors.
     *
     * @param blockSize the block size to use
     * @param recordSize the record size to use
     */
    private void initialize(int blockSize, int recordSize) {
        this.debug = false;
        this.blockSize = blockSize;
        this.recordSize = recordSize;
        this.recsPerBlock = (this.blockSize / this.recordSize);
        this.blockBuffer = BufferPool.getByteArray(this.blockSize);

        if (this.inStream != null) {
            this.currBlkIdx = -1;
            this.currRecIdx = this.recsPerBlock;
        } else {
            this.currBlkIdx = 0;
            this.currRecIdx = 0;
        }
    }

    /**
     * Get the TAR Buffer's block size. Blocks consist of multiple records.
     * @return the block size
     */
    public int getBlockSize() {
        return this.blockSize;
    }

    /**
     * Get the TAR Buffer's record size.
     * @return the record size
     */
    public int getRecordSize() {
        return this.recordSize;
    }

    /**
     * Set the debugging flag for the buffer.
     *
     * @param debug If true, print debugging output.
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * Determine if an archive record indicate End of Archive. End of
     * archive is indicated by a record that consists entirely of null bytes.
     *
     * @param record The record data to check.
     * @return true if the record data is an End of Archive
     */
    public boolean isEOFRecord(byte[] record) {
        for (int i = 0, sz = getRecordSize(); i < sz; ++i) {
            if (record[i] != 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * Skip over a record on the input stream.
     *
     * @return <code>true</code> if the record has been skipped, <code>false</code> if EOF has been reached
     * @throws IOException on error
     */
    public boolean skipRecord() throws IOException {
        if (debug) {
            System.err.println("SkipRecord: recIdx = " + currRecIdx
                               + " blkIdx = " + currBlkIdx);
        }

        if (inStream == null) {
            throw new IOException("reading (via skip) from an output buffer");
        }

        if (currRecIdx >= recsPerBlock) {
            if (!readBlock()) {
                return false;
            }
        }

        currRecIdx++;
        return true;
    }


    /**
     * Read a record from the input stream and stores it into the specified buffer.
     *
     * @param recordBuf the buffer into which the record will be stored. Its length must be {@link #getRecordSize()}.
     * @return <code>true</code> if the record has been read, <code>false</code> if EOF has been reached
     * @throws IOException on error
     */
    public boolean readRecord(byte[] recordBuf) throws IOException {
        if (debug) {
            System.err.println("ReadRecord: recIdx = " + currRecIdx
                               + " blkIdx = " + currBlkIdx);
        }

        if(recordBuf.length!=recordSize)
            throw new IOException("specified record buffer doesn't match record size: "+recordSize);

        if (inStream == null) {
            throw new IOException("reading from an output buffer");
        }

        if (currRecIdx >= recsPerBlock) {
            if (!readBlock()) {
                return false;
            }
        }

        System.arraycopy(blockBuffer,
                         (currRecIdx * recordSize), recordBuf, 0,
                         recordSize);

        currRecIdx++;

        return true;
    }

    /**
     * Read a block from the input stream and stores it into the block buffer.
     *
     * @return true if a block was read, false if EOF was reached
     * @throws IOException on error
     */
    private boolean readBlock() throws IOException {
        if (debug) {
            System.err.println("ReadBlock: blkIdx = " + currBlkIdx);
        }

        if (inStream == null) {
            throw new IOException("reading from an output buffer");
        }

        currRecIdx = 0;

        int offset = 0;
        int bytesNeeded = blockSize;

        while (bytesNeeded > 0) {
            long numBytes = inStream.read(blockBuffer, offset,
                                               bytesNeeded);

            //
            // NOTE
            // We have fit EOF, and the block is not full!
            //
            // This is a broken archive. It does not follow the standard
            // blocking algorithm. However, because we are generous, and
            // it requires little effort, we will simply ignore the error
            // and continue as if the entire block were read. This does
            // not appear to break anything upstream. We used to return
            // false in this case.
            //
            // Thanks to 'Yohann.Roussel@alcatel.fr' for this fix.
            //
            if (numBytes == -1) {
                if (offset == 0) {
                    // Ensure that we do not read gigabytes of zeros
                    // for a corrupt tar file.
                    // See http://issues.apache.org/bugzilla/show_bug.cgi?id=39924
                    return false;
                }
                // However, just leaving the unread portion of the buffer dirty does
                // cause problems in some cases.  This problem is described in
                // http://issues.apache.org/bugzilla/show_bug.cgi?id=29877
                //
                // The solution is to fill the unused portion of the buffer with zeros.

                Arrays.fill(blockBuffer, offset, offset + bytesNeeded, (byte) 0);

                break;
            }

            offset += numBytes;
            bytesNeeded -= numBytes;

            if (numBytes != blockSize) {
                if (debug) {
                    System.err.println("ReadBlock: INCOMPLETE READ "
                                       + numBytes + " of " + blockSize
                                       + " bytes read.");
                }
            }
        }

        currBlkIdx++;

        return true;
    }

    /**
     * Skip over a block on the input stream.
     *
     * @return true if a block was read, false if EOF was reached
     * @throws IOException on error
     */
    public boolean skipBlock() throws IOException {
        int bytesToSkip = blockSize;

        while (bytesToSkip > 0) {
            long numBytes = inStream.skip(bytesToSkip);
            // Adopt the same 'generous' behavior as #readBlock(), i.e. allow a premature EOF only if at least
            // a byte was properly skipped.
            if(numBytes==-1) {
                return bytesToSkip != blockSize;
            }

            bytesToSkip -= numBytes;
        }

        currBlkIdx++;
        currRecIdx = recsPerBlock;

        return true;
    }


    /**
     * Get the current block number, zero based.
     *
     * @return The current zero based block number.
     */
    public int getCurrentBlockNum() {
        return currBlkIdx;
    }

    /**
     * Sets the current block number, zero based.
     *
     * @param blockNum the current block number, zero based
     */
    public void setCurrentBlockNum(int blockNum) {
        this.currBlkIdx = blockNum;
    }

    /**
     * Get the current record number, within the current block, zero based.
     * Thus, current offset = (currentBlockNum * recsPerBlk) + currentRecNum.
     *
     * @return The current zero based record number.
     */
    public int getCurrentRecordNum() {
        return currRecIdx - 1;
    }


    /**
     * Returns the number of records per block.
     *
     * @return the number of records per block
     */
    public int getRecordsPerBlock() {
        return recsPerBlock;
    }

    /**
     * Write an archive record to the archive.
     *
     * @param record The record data to write to the archive.
     * @throws IOException on error
     */
    public void writeRecord(byte[] record) throws IOException {
        if (debug) {
            System.err.println("WriteRecord: recIdx = " + currRecIdx
                               + " blkIdx = " + currBlkIdx);
        }

        if (outStream == null) {
            throw new IOException("writing to an input buffer");
        }

        if (record.length != recordSize) {
            throw new IOException("record to write has length '"
                                  + record.length
                                  + "' which is not the record size of '"
                                  + recordSize + "'");
        }

        if (currRecIdx >= recsPerBlock) {
            writeBlock();
        }

        System.arraycopy(record, 0, blockBuffer,
                         (currRecIdx * recordSize),
                         recordSize);

        currRecIdx++;
    }

    /**
     * Write an archive record to the archive, where the record may be
     * inside of a larger array buffer. The buffer must be "offset plus
     * record size" long.
     *
     * @param buf The buffer containing the record data to write.
     * @param offset The offset of the record data within buf.
     * @throws IOException on error
     */
    public void writeRecord(byte[] buf, int offset) throws IOException {
        if (debug) {
            System.err.println("WriteRecord: recIdx = " + currRecIdx
                               + " blkIdx = " + currBlkIdx);
        }

        if (outStream == null) {
            throw new IOException("writing to an input buffer");
        }

        if ((offset + recordSize) > buf.length) {
            throw new IOException("record has length '" + buf.length
                                  + "' with offset '" + offset
                                  + "' which is less than the record size of '"
                                  + recordSize + "'");
        }

        if (currRecIdx >= recsPerBlock) {
            writeBlock();
        }

        System.arraycopy(buf, offset, blockBuffer,
                         (currRecIdx * recordSize),
                         recordSize);

        currRecIdx++;
    }

    /**
     * Write a TarBuffer block to the archive.
     */
    private void writeBlock() throws IOException {
        if (debug) {
            System.err.println("WriteBlock: blkIdx = " + currBlkIdx);
        }

        if (outStream == null) {
            throw new IOException("writing to an input buffer");
        }

        outStream.write(blockBuffer, 0, blockSize);
        outStream.flush();

        currRecIdx = 0;
        currBlkIdx++;
    }

    /**
     * Flush the current data block if it has any data in it.
     */
    private void flushBlock() throws IOException {
        if (debug) {
            System.err.println("TarBuffer.flushBlock() called.");
        }

        if (outStream == null) {
            throw new IOException("writing to an input buffer");
        }

        if (currRecIdx > 0) {
            writeBlock();
        }
    }

    /**
     * Close the TarBuffer. If this is an output buffer, also flush the
     * current block before closing.
     * @throws IOException on error
     */
    public void close() throws IOException {
        if (debug) {
            System.err.println("TarBuffer.closeBuffer().");
        }

        try {
            if (outStream != null) {
                flushBlock();

                if (outStream != System.out
                        && outStream != System.err) {
                    outStream.close();

                    outStream = null;
                }
            } else if (inStream != null) {
                if (inStream != System.in) {
                    inStream.close();

                    inStream = null;
                }
            }
        }
        finally {
            if(blockBuffer!=null) {
                BufferPool.releaseByteArray(blockBuffer);
                blockBuffer = null;
            }
        }
    }
}
