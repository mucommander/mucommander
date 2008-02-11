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

package com.mucommander.file.impl.zip.provider;

import com.mucommander.io.BufferPool;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.CRC32;
import java.util.zip.Deflater;


/**
 * ZipEntryOutputStream is a <code>OutputStream</code> that allows to write a Zip entry's data using one of the
 * supported compression methods: {@link #DEFLATED} or {@link #STORED}, and calculates the compressed and
 * uncompressed size and CRC of the data.
 *
 * <p>--------------------------------------------------------------------------------------------------------------<br>
 * <br>
 * This class is based off the <code>org.apache.tools.zip</code> package of the <i>Apache Ant</i> project. The Ant
 * code has been modified under the terms of the Apache License which you can find in the bundled muCommander license
 * file. It was forked at version 1.7.0 of Ant.</p>
 *
 * @author Apache Ant, Maxence Bernard
 */
public class ZipEntryOutputStream extends OutputStream implements ZipConstants {

    /** The underlying stream this ZipOutputStream writes zip-compressed data to */
    protected OutputStream out;

    /** Compression method (DEFLATED or STORED) */
    private int method;

    /** Compression level, used only for DEFLATED method */
    private int level = DEFAULT_DEFLATER_COMPRESSION;

    /** This Deflater object is used for output */
    private Deflater deflater;

    /** Buffer for the Deflater */
    protected byte[] buf;

    /** CRC instance to avoid parsing DEFLATED data twice */
    private CRC32 crc = new CRC32();

    /** Used for STORED method only: number of bytes in/out so far */
    private int storedCount;


    /**
     * Creates a new <code>ZipEntryOutputStream</code> that compresses the supplied data using the {@link #DEFLATED}
     * compression method and writes it to the given <code>OutputStream</code>.
     *
     * @param out the OutputStream where the compressed data is sent to
     */
    public ZipEntryOutputStream(OutputStream out) {
        this(out, DEFLATED);
    }

    /**
     * Creates a new <code>ZipEntryOutputStream</code> that compresses the supplied data using the specified compression
     * method and writes it to the given <code>OutputStream</code>.
     *
     * @param out the OutputStream where the compressed data is sent to
     * @param method the compression method, {@link #DEFLATED} or {@link #STORED}
     */
    public ZipEntryOutputStream(OutputStream out, int method) {
        this.out = out;
        this.method = method;

        // Use BufferPool to avoid excessive memory allocation and garbage collection.
        // /!\ For some unknown reason, having a larger buffer *hurts* performance.
        buf = BufferPool.getArrayBuffer(512);

        if(method == DEFLATED)
            deflater = new Deflater(level, true);
    }

    /**
     * Returns the compression level when the {@link #DEFLATED} compression method is used.
     *
     * @return the compression level for the DEFLATED compression method
     */
    public int getLevel() {
        return level;
    }

    /**
     * Sets the compression level when the {@link #DEFLATED} compression method is used.
     *
     * @param level the compression level for the DEFLATED compression method
     */
    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * Returns the compression method used for writing the supplied data.
     *
     * @return the compression method used for writing the supplied data
     */
    public int getMethod() {
        return method;
    }

    /**
     * Returns the uncompressed size of the data written so far.
     *
     * @return the uncompressed size of the data written so far
     */
    public int getTotalIn() {
        if(method == DEFLATED)
            return deflater.getTotalIn();
        else        // STORED
            return storedCount;
    }

    /**
     * Returns the compressed size of the data written so far.
     *
     * @return the compressed size of the data written so far
     */
    public int getTotalOut() {
        if(method == DEFLATED)
            return deflater.getTotalOut();
        else        // STORED
            return storedCount;
    }

    /**
     * Returns the CRC value of the data written so far.
     *
     * @return the CRC value of the data written so far.
     */
    public long getCrc() {
        return crc.getValue();
    }

    /**
     * Writes next block of compressed data to the output stream.
     *
     * @throws IOException on error
     */
    private void deflate() throws IOException {
        int len = deflater.deflate(buf, 0, buf.length);
        if (len > 0) {
            out.write(buf, 0, len);
        }
    }

    /**
     * Finishes writing the DEFLATED-compressed data.
     *
     * @throws IOException if an I/O occurred
     */
    protected void finishDeflate() throws IOException {
        deflater.finish();
        while (!deflater.finished()) {
            deflate();
        }
    }


    /////////////////////////////////
    // OutputStream implementation //
    /////////////////////////////////

    /**
     * Writes the given bytes to the Zip entry.
     *
     * @param b the byte array to write
     * @param offset the start position to write from
     * @param length the number of bytes to write
     * @throws java.io.IOException on error
     */
    public void write(byte[] b, int offset, int length) throws IOException {
        if (method == DEFLATED) {
            if (length > 0) {
                if (!deflater.finished()) {
                    deflater.setInput(b, offset, length);
                    while (!deflater.needsInput()) {
                        deflate();
                    }
                }
            }
        } else {        // STORED
            out.write(b, offset, length);
            storedCount += length;
        }

        crc.update(b, offset, length);
    }

    /**
     * Writes a single byte to the Zip entry.
     *
     * @param b the byte to write
     * @throws IOException on error
     */
    public void write(int b) throws IOException {
        byte[] array = new byte[1];
        array[0] = (byte) (b & 0xff);
        write(array, 0, 1);
    }

    /**
     * Flushes the underlying <code>OutputStream</code>.
     *
     * @throws IOException
     */
    public void flush() throws IOException {
        out.flush();
    }

    /**
     * Completes writing the entry <b>without</b> closing the underlying <code>OutputStream</code>.
     *
     * @throws IOException
     */
    public void close() throws IOException {
        if (method == DEFLATED)
            finishDeflate();

        if(buf!=null) {         // Only if close() has not already been called
            BufferPool.releaseArrayBuffer(buf);
            buf = null;
        }
    }
}
