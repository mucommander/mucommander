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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * The TarOutputStream writes a UNIX tar archive as an OutputStream.
 * Methods are provided to put entries, and then write their contents
 * by writing to this stream using write().
 *
 * <p>-----------------------------------</p>
 * <p>This class is based off the <code>org.apache.tools.tar</code> package of the <i>Apache Ant</i> project. The Ant
 * code has been modified under the terms of the Apache License which you can find in the bundled muCommander license
 * file. It was forked at version 1.7.1 of Ant.</p>
 * 
 * @author Apache Ant, Maxence Bernard
 */
public class TarOutputStream extends FilterOutputStream {
    /** Fail if a long file name is required in the archive. */
    public static final int LONGFILE_ERROR = 0;

    /** Long paths will be truncated in the archive. */
    public static final int LONGFILE_TRUNCATE = 1;

    /** GNU tar extensions are used to store long file names in the archive. */
    public static final int LONGFILE_GNU = 2;

    protected boolean   debug;
    protected long      currSize;
    protected String    currName;
    protected long      currBytes;
    protected byte[]    oneBuf;
    protected byte[]    recordBuf;
    protected int       assemLen;
    protected byte[]    assemBuf;
    protected TarBuffer buffer;
    protected int       longFileMode = LONGFILE_ERROR;

    private boolean closed = false;

    /**
     * Constructor for TarInputStream.
     * @param os the output stream to use
     */
    public TarOutputStream(OutputStream os) {
        this(os, TarBuffer.DEFAULT_BLKSIZE, TarBuffer.DEFAULT_RCDSIZE);
    }

    /**
     * Constructor for TarInputStream.
     * @param os the output stream to use
     * @param blockSize the block size to use
     */
    public TarOutputStream(OutputStream os, int blockSize) {
        this(os, blockSize, TarBuffer.DEFAULT_RCDSIZE);
    }

    /**
     * Constructor for TarInputStream.
     * @param os the output stream to use
     * @param blockSize the block size to use
     * @param recordSize the record size to use
     */
    public TarOutputStream(OutputStream os, int blockSize, int recordSize) {
        super(os);

        this.buffer = new TarBuffer(os, blockSize, recordSize);
        this.debug = false;
        this.assemLen = 0;
        this.assemBuf = BufferPool.getByteArray(recordSize);
        this.recordBuf = BufferPool.getByteArray(recordSize);
        this.oneBuf = BufferPool.getByteArray(1);
    }

    /**
     * Set the long file mode.
     * This can be LONGFILE_ERROR(0), LONGFILE_TRUNCATE(1) or LONGFILE_GNU(2).
     * This specifies the treatment of long file names (names >= TarConstants.NAMELEN).
     * Default is LONGFILE_ERROR.
     * @param longFileMode the mode to use
     */
    public void setLongFileMode(int longFileMode) {
        this.longFileMode = longFileMode;
    }


    /**
     * Sets the debugging flag.
     *
     * @param debugF True to turn on debugging.
     */
    public void setDebug(boolean debugF) {
        this.debug = debugF;
    }

    /**
     * Sets the debugging flag in this stream's TarBuffer.
     *
     * @param debug True to turn on debugging.
     */
    public void setBufferDebug(boolean debug) {
        buffer.setDebug(debug);
    }

    /**
     * Ends the TAR archive without closing the underlying OutputStream.
     * The result is that the two EOF records of nulls are written.
     * @throws IOException on error
     */
    public void finish() throws IOException {
        // See Bugzilla 28776 for a discussion on this
        // http://issues.apache.org/bugzilla/show_bug.cgi?id=28776
        writeEOFRecord();
        writeEOFRecord();
    }

    /**
     * Ends the TAR archive and closes the underlying OutputStream.
     * This means that finish() is called followed by calling the
     * TarBuffer's close().
     * @throws IOException on error
     */
    @Override
    public void close() throws IOException {
        if (!closed) {
            try {
                finish();
            
                buffer.close();
            }
            finally {
                BufferPool.releaseByteArray(assemBuf);
                BufferPool.releaseByteArray(recordBuf);
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
     * Put an entry on the output stream. This writes the entry's
     * header record and positions the output stream for writing
     * the contents of the entry. Once this method is called, the
     * stream is ready for calls to write() to write the entry's
     * contents. Once the contents are written, closeEntry()
     * <B>MUST</B> be called to ensure that all buffered data
     * is completely written to the output stream.
     *
     * @param entry The TarEntry to be written to the archive.
     * @throws IOException on error
     */
    public void putNextEntry(TarEntry entry) throws IOException {
        if (entry.getName().length() >= TarConstants.NAMELEN) {

            if (longFileMode == LONGFILE_GNU) {
                // create a TarEntry for the LongLink, the contents
                // of which are the entry's name
                TarEntry longLinkEntry = new TarEntry(TarConstants.GNU_LONGLINK,
                                                      TarConstants.LF_GNUTYPE_LONGNAME);

                longLinkEntry.setSize(entry.getName().length() + 1);
                putNextEntry(longLinkEntry);
                write(entry.getName().getBytes());
                write(0);
                closeEntry();
            } else if (longFileMode != LONGFILE_TRUNCATE) {
                throw new RuntimeException("file name '" + entry.getName()
                                             + "' is too long ( > "
                                             + TarConstants.NAMELEN + " bytes)");
            }
        }

        entry.writeEntryHeader(recordBuf);
        buffer.writeRecord(recordBuf);

        currBytes = 0;

        if (entry.isDirectory()) {
            currSize = 0;
        } else {
            currSize = entry.getSize();
        }
        currName = entry.getName();
    }

    /**
     * Close an entry. This method MUST be called for all file
     * entries that contain data. The reason is that we must
     * buffer data written to the stream in order to satisfy
     * the buffer's record based writes. Thus, there may be
     * data fragments still being assembled that must be written
     * to the output stream before this entry is closed and the
     * next entry written.
     * @throws IOException on error
     */
    public void closeEntry() throws IOException {
        if (assemLen > 0) {
            for (int i = assemLen; i < assemBuf.length; ++i) {
                assemBuf[i] = 0;
            }

            buffer.writeRecord(assemBuf);

            currBytes += assemLen;
            assemLen = 0;
        }

        if (currBytes < currSize) {
            throw new IOException("entry '" + currName + "' closed at '"
                                  + currBytes
                                  + "' before the '" + currSize
                                  + "' bytes specified in the header were written");
        }
    }

    /**
     * Writes a byte to the current tar archive entry.
     *
     * This method simply calls read( byte[], int, int ).
     *
     * @param b The byte written.
     * @throws IOException on error
     */
    @Override
    public void write(int b) throws IOException {
        oneBuf[0] = (byte) b;

        write(oneBuf, 0, 1);
    }

    /**
     * Writes bytes to the current tar archive entry.
     *
     * This method simply calls write( byte[], int, int ).
     *
     * @param wBuf The buffer to write to the archive.
     * @throws IOException on error
     */
    @Override
    public void write(byte[] wBuf) throws IOException {
        write(wBuf, 0, wBuf.length);
    }

    /**
     * Writes bytes to the current tar archive entry. This method
     * is aware of the current entry and will throw an exception if
     * you attempt to write bytes past the length specified for the
     * current entry. The method is also (painfully) aware of the
     * record buffering required by TarBuffer, and manages buffers
     * that are not a multiple of recordsize in length, including
     * assembling records from small buffers.
     *
     * @param wBuf The buffer to write to the archive.
     * @param wOffset The offset in the buffer from which to get bytes.
     * @param numToWrite The number of bytes to write.
     * @throws IOException on error
     */
    @Override
    public void write(byte[] wBuf, int wOffset, int numToWrite) throws IOException {
        if ((currBytes + numToWrite) > currSize) {
            throw new IOException("request to write '" + numToWrite
                                  + "' bytes exceeds size in header of '"
                                  + currSize + "' bytes for entry '"
                                  + currName + "'");

            //
            // We have to deal with assembly!!!
            // The programmer can be writing little 32 byte chunks for all
            // we know, and we must assemble complete records for writing.
            // REVIEW Maybe this should be in TarBuffer? Could that help to
            // eliminate some of the buffer copying.
            //
        }

        if (assemLen > 0) {
            if ((assemLen + numToWrite) >= recordBuf.length) {
                int aLen = recordBuf.length - assemLen;

                System.arraycopy(assemBuf, 0, recordBuf, 0,
                                 assemLen);
                System.arraycopy(wBuf, wOffset, recordBuf,
                                 assemLen, aLen);
                buffer.writeRecord(recordBuf);

                currBytes += recordBuf.length;
                wOffset += aLen;
                numToWrite -= aLen;
                assemLen = 0;
            } else {
                System.arraycopy(wBuf, wOffset, assemBuf, assemLen,
                                 numToWrite);

                wOffset += numToWrite;
                assemLen += numToWrite;
                numToWrite -= numToWrite;
            }
        }

        //
        // When we get here we have EITHER:
        // o An empty "assemble" buffer.
        // o No bytes to write (numToWrite == 0)
        //
        while (numToWrite > 0) {
            if (numToWrite < recordBuf.length) {
                System.arraycopy(wBuf, wOffset, assemBuf, assemLen,
                                 numToWrite);

                assemLen += numToWrite;

                break;
            }

            buffer.writeRecord(wBuf, wOffset);

            int num = recordBuf.length;

            currBytes += num;
            numToWrite -= num;
            wOffset += num;
        }
    }

    /**
     * Write an EOF (end of archive) record to the tar archive.
     * An EOF record consists of a record of all zeros.
     */
    private void writeEOFRecord() throws IOException {
        for (int i = 0; i < recordBuf.length; ++i) {
            recordBuf[i] = 0;
        }

        buffer.writeRecord(recordBuf);
    }
}


