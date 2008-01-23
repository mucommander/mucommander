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

package com.mucommander.io.bom;

import java.io.IOException;
import java.io.InputStream;

/**
 * <code>BOMInputStream</code> is an <code>InputStream</code> which provides support for Byte-Order Marks (BOM).
 * A BOM is a byte sequence found at the beginning of a Unicode text stream which indicates the encoding of the text
 * that follows.
 *
 * <p>
 * This class serves a dual purpose:<br>
 * 1) it allows to detect a BOM in the underlying stream and determine the encoding used by the stream:
 * the {@link BOM} instance returned by {@link #getBOM()} provides that information.<br>
 * 2) it allows to discard the BOM from a Unicode stream: the leading bytes corresponding to the BOM are swallowed by
 * the stream and never returned by the <code>read</code> methods.
 * </p>
 *
 *<p>
 * The following BOMs are supported by this class:
 * <ul>
 *  <li>{@link #UTF8_BOM UTF-8}</li>
 *  <li>{@link #UTF16_BE_BOM UTF-16 Big Endian}</li>
 *  <li>{@link #UTF16_LE_BOM UTF-16 Little Endian}</li>
 *  <li>{@link #UTF32_BE_BOM UTF-32 Big Endian}.</li>
 *  <li>{@link #UTF32_LE_BOM UTF-32 Little Endian}</li>
 * </ul>
 * Note that UTF-32 encodings (both Little and Big Endians) are usually <b>not</b> supported by Java runtimes
 * out of the box.
 * <p>
 *
 * @see BOMReader
 * @author Maxence Bernard
 */
public class BOMInputStream extends InputStream implements BOMConstants {

    /** The underlying InputStream that feeds bytes to this stream */
    private InputStream in;

    /** Contains the BOM that was detected in the stream, null if none was found */
    private BOM bom;

    /** Bytes that were swallowed by this stream when searching for a BOM, null if a BOM was found */
    private byte leadingBytes[];

    /** Current offset within the {@link #leadingBytes} array */
    private int leadingBytesOff;

    /** Contains the max signature length of supported BOMs */
    private final static int MAX_BOM_LENGTH;

    static {
        // Calculates MAX_BOM_LENGTH
        int maxLen = SUPPORTED_BOMS[0].getSignature().length;
        int len;
        for(int i=1; i<SUPPORTED_BOMS.length; i++) {
            len = SUPPORTED_BOMS[i].getSignature().length;
            if(len>maxLen)
                maxLen = len;
        }

        MAX_BOM_LENGTH = maxLen;
    }


    /**
     * Creates a new <code>BOMInputStream</code> and looks for a BOM at the beginning of the stream.
     *
     * @param in the underlying stream
     * @throws IOException if an error occurred while reading the given InputStream
     */
    public BOMInputStream(InputStream in) throws IOException {
        this.in = in;

        // Read up to MAX_BOM_LENGTH bytes
        byte bytes[] = new byte[MAX_BOM_LENGTH];
        int nbRead;
        int totalRead = 0;
        while((nbRead=in.read(bytes, totalRead, MAX_BOM_LENGTH-totalRead))!=-1 && (totalRead+=nbRead)<MAX_BOM_LENGTH);

        // Truncate the byte array if the stream ended before MAX_BOM_LENGTH
        if(totalRead<MAX_BOM_LENGTH) {
            byte tempBytes[] = new byte[totalRead];
            System.arraycopy(bytes, 0, tempBytes, 0, totalRead);
            bytes = tempBytes;
        }

        int bestMatchLength = 0;
        int bestMatchIndex = -1;
        BOM tempBom;
        byte[] tempBomSig;

        // Looks for the best (longest) signature match
        for(int i=0; i<SUPPORTED_BOMS.length; i++) {
            tempBom = SUPPORTED_BOMS[i];
            tempBomSig = tempBom.getSignature();
            if(tempBomSig.length>bestMatchLength && startsWith(bytes, tempBomSig)) {
                bestMatchIndex = i;
                bestMatchLength = tempBomSig.length;
            }
        }

        // Keep the bytes that do not correspond to a BOM to have the read methods return them
        if(bestMatchIndex!=-1) {
            bom = SUPPORTED_BOMS[bestMatchIndex];
            if(bestMatchLength<MAX_BOM_LENGTH) {
                leadingBytes = bytes;
                leadingBytesOff = bestMatchLength;
            }
        }
        else {
            leadingBytes = bytes;
            leadingBytesOff = 0;
        }
    }

    /**
     * Returns <code>true</code> if the first byte sequence starts with the second byte sequence.
     *
     * @param b1 first byte array to test
     * @param b2 second byte array to test
     * @return true if the first byte sequence starts with the second byte sequence.
     */
    private static boolean startsWith(byte b1[], byte b2[]) {
        int b1Len = b1.length;
        int b2Len = b2.length;
        if(b1Len<b2Len)
            return false;

        for(int i=0; i<b2Len; i++) {
            if(b2[i]!= b1[i])
                return false;
        }

        return true;
    }

    /**
     * Returns the {@link BOM} that was found at the beginning of the stream if there was one,
     * <code>null</code> otherwise.
     *
     * @return the BOM that was found at the beginning of the stream
     */
    public BOM getBOM() {
        return bom;
    }


    ////////////////////////////////
    // InputStream implementation //
    ////////////////////////////////

    public int read() throws IOException {
        if(leadingBytes==null)
            return in.read();

        int i = leadingBytes[leadingBytesOff++];

        if(leadingBytesOff>=leadingBytes.length)
            leadingBytes = null;

        return i;
    }

    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

    public int read(byte b[], int off, int len) throws IOException {
        if(leadingBytes==null)
            return in.read(b, off, len);

        int nbBytes = Math.min(leadingBytes.length-leadingBytesOff, len);
        System.arraycopy(leadingBytes, leadingBytesOff, b, off, nbBytes);

        leadingBytesOff += nbBytes;
        if(leadingBytesOff>=leadingBytes.length)
            leadingBytes = null;

        return nbBytes;
    }

    public void close() throws IOException {
        in.close();
    }
}
