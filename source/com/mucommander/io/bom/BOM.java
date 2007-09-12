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

package com.mucommander.io.bom;

/**
 * BOM represents a Byte-Order Mark, a byte sequence that can be found at the beginning of a Unicode text stream
 * which indicates the encoding of the text that follows.
 *
 * @see BOMInputStream
 * @author Maxence Bernard
 */
public class BOM {

    /** the byte sequence that identifies this BOM */
    private byte[] sig;

    /** the character encoding designated by this BOM */
    private String encoding;


    /**
     * Creates a new <code>BOM</code> instance identified by the given signature and designating the specified
     * character encoding.
     *
     * @param signature the byte sequence that identifies this BOM
     * @param encoding the character encoding designated by this BOM
     */
    public BOM(byte signature[], String encoding) {
        this.sig = signature;
        this.encoding = encoding;
    }

    /**
     * Returns the byte sequence that identifies this BOM at the beginning of a byte stream.
     *
     * @return the byte sequence that identifies this BOM at the beginning of a byte stream
     */
    public byte[] getSignature() {
        return sig;
    }

    /**
     * Returns the character encoding that this BOM denotes.
     *
     * @return the character encoding that this BOM denotes
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Returns <code>true</code> if this BOM's signature starts with the given byte sequence.
     *
     * @param bytes the byte sequence to compare against this BOM's signature
     * @return true if this BOM's signature starts with the given byte sequence
     */
    public boolean sigStartsWith(byte bytes[]) {
        int bytesLen = bytes.length;
        if(bytesLen>sig.length)
            return false;

        for(int i=0; i<bytesLen; i++) {
            if(bytes[i]!= sig[i])
                return false;
        }

        return true;
    }

    /**
     * Returns <code>true</code> if this BOM's signature matches the given byte sequence.
     *
     * @param bytes the byte sequence to compare against this BOM's signature
     * @return true if this BOM's signature matches the given byte sequence
     */
    public boolean sigEquals(byte bytes[]) {
        return bytes.length==sig.length && sigStartsWith(bytes);
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    /**
     * Returns <code>true</code> if and only if the given Object is a <code>BOM</code> instance with the same
     * signature as this instance.         *
     *
     * @param o the Object to test for equality
     * @return true if the specified Object is a BOM instance with the same signature as this instance
     */
    public boolean equals(Object o) {
        return (o instanceof BOM) && ((BOM)o).sigEquals(sig);
    }

    /**
     * Returns a String representation of this <code>BOM</code>.
     *
     * @return returns a String representation of this <code>BOM</code>.
     */
    public String toString() {
        String sigRep = "{";
        for(int i=0; i<sig.length; i++)
            sigRep += (0xFF&sig[i])+(i==sig.length-1?"}":", ");
        
        return super.toString()+", signature="+sigRep+", encoding="+encoding;
    }
}
