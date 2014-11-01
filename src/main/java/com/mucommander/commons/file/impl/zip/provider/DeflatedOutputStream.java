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


package com.mucommander.commons.file.impl.zip.provider;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Deflater;

/**
 * DeflatedOutputStream compresses data using the DEFLATED compression method.
 *
 * <p>--------------------------------------------------------------------------------------------------------------<br>
 * <br>
 * This class is based off the <code>org.apache.tools.zip</code> package of the <i>Apache Ant</i> project. The Ant
 * code has been modified under the terms of the Apache License which you can find in the bundled muCommander license
 * file. It was forked at version 1.7.0 of Ant.</p>
 *
 * @author Maxence Bernard
 */
public class DeflatedOutputStream extends ZipEntryOutputStream {

    /** Deflater instance that does the actual compression work */
    protected Deflater deflater;

    /** Buffer used to deflate data */
    protected byte[] buf;


    /**
     * Creates a new <code>DeflatedOutputStream</code> that writes compressed data to the given <code>OutputStream</code>
     * and automatically updates the supplied CRC32 checksum.
     *
     * @param out the OutputStream where the compressed data is sent to
     * @param deflater the Deflater that compresses data, reset before first use
     * @param buf the buffer used to deflate data
     */
    public DeflatedOutputStream(OutputStream out, Deflater deflater, byte buf[]) {
        super(out, ZipConstants.DEFLATED);

        this.deflater = deflater;
        this.buf = buf;

        deflater.reset();
    }

    /**
     * Writes next block of compressed data to the output stream.
     *
     * @throws java.io.IOException on error
     */
    protected void deflate() throws IOException {
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
    public void finishDeflate() throws IOException {
        deflater.finish();
        while (!deflater.finished()) {
            deflate();
        }
    }


    /////////////////////////////////////////
    // ZipEntryOutputStream implementation //
    /////////////////////////////////////////

    @Override
    public int getTotalIn() {
        return deflater.getTotalIn();
    }

    @Override
    public int getTotalOut() {
        return deflater.getTotalOut();
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
    @Override
    public void write(byte[] b, int offset, int length) throws IOException {
        if (length > 0) {
            if (!deflater.finished()) {
                deflater.setInput(b, offset, length);
                while (!deflater.needsInput()) {
                    deflate();
                }
            }
        }

        crc.update(b, offset, length);
    }

    /**
     * Completes writing the entry <b>without</b> closing the underlying <code>OutputStream</code>.
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        finishDeflate();
    }
}
