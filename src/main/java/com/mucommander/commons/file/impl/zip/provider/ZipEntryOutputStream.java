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
import java.util.zip.CRC32;

/**
 * ZipEntryOutputStream is an abstract <code>OutputStream</code> used for compressing a Zip entry's data to an
 * underlying OutputStream.
 *
 * <p>The CRC32 checksum is calculated on-the-fly as data gets written to the stream, {@link #getCrc()} returns the
 * current checksum value. The {@link #getTotalIn()} and {@link #getTotalOut()} methods keep track of the uncompressed
 * and compressed of the supplied data.</p>
 *
 * <p>There currently are two implementations of this class:
 * <ul>
 *  <li>{@link com.mucommander.commons.file.impl.zip.provider.DeflatedOutputStream}: implements the DEFLATED compression method
 *  </li>
 *  <li>{@link com.mucommander.commons.file.impl.zip.provider.StoredOutputStream}: implements the STORED compression method
 * (i.e. no compression)</li>
 * </ul>
 * </p>
 *
 * <p>--------------------------------------------------------------------------------------------------------------<br>
 * <br>
 * This class is based off the <code>org.apache.tools.zip</code> package of the <i>Apache Ant</i> project. The Ant
 * code has been modified under the terms of the Apache License which you can find in the bundled muCommander license
 * file. It was forked at version 1.7.0 of Ant.</p>
 *
 * @author Maxence Bernard
 */
public abstract class ZipEntryOutputStream extends OutputStream {

    /** The underlying stream where the compressed data is sent */
    protected OutputStream out;

    /** Compression method (DEFLATED or STORED) */
    protected int method;

    /** The CRC32 instance that calculates the checksum */
    protected CRC32 crc = new CRC32();


    /**
     * Creates a new <code>EntryOutputStream</code> that writes compressed data to the given <code>OutputStream</code>
     * and automatically updates the supplied <code>CRC32</code> checksum.
     *
     * @param out the OutputStream where the compressed data is sent to
     * @param method the compression method, {@link ZipConstants#DEFLATED} or {@link ZipConstants#STORED}
     */
    public ZipEntryOutputStream(OutputStream out, int method) {
        this.out = out;
        this.method = method;
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
     * Returns the CRC value of the data written so far.
     *
     * @return the CRC value of the data written so far.
     */
    public long getCrc() {
        return crc.getValue();
    }


    /////////////////////////////////////////
    // Partial OutputStream implementation //
    /////////////////////////////////////////

    @Override
    public void write(int b) throws IOException {
        byte[] array = new byte[1];
        array[0] = (byte) (b & 0xff);
        write(array, 0, 1);
    }

    /**
     * Flushes the underlying <code>OutputStream</code>.
     */
    @Override
    public void flush() throws IOException {
        out.flush();
    }


    //////////////////////
    // Abstract methods //
    //////////////////////

    /**
     * Returns the uncompressed size of the data written so far.
     *
     * @return the uncompressed size of the data written so far
     */
    public abstract int getTotalIn();

    /**
     * Returns the compressed size of the data written so far.
     *
     * @return the compressed size of the data written so far
     */
    public abstract int getTotalOut();
}
