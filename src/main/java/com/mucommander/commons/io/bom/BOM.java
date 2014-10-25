/*
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

package com.mucommander.commons.io.bom;

import java.nio.charset.Charset;

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

    /** the character encoding denoted by this BOM */
    private String encoding;

    /** character encoding aliases that map onto this BOM */
    private String aliases[];

    /**
     * Creates a new <code>BOM</code> instance identified by the given signature and denoting the specified
     * character encoding.
     *
     * @param signature the byte sequence that identifies this BOM
     * @param encoding the character encoding denoted by this BOM
     * @param aliases character encoding aliases
     */
    BOM(byte signature[], String encoding, String[] aliases) {
        this.sig = signature;
        this.encoding = encoding;
        this.aliases = aliases;
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
     * Returns a set of character encoding aliases that map onto this BOM.
     *
     * @return a set of character encoding aliases that map onto this BOM
     */
    public String[] getAliases() {
        return aliases;
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


    ////////////////////
    // Static methods //
    ////////////////////

    /**
     * Returns a {@link BOM} instance for the specified encoding, <code>null</code> if the encoding doesn't
     * have a corresponding BOM (non-Unicode encoding). The search is case-insensitive.
     *
     * <p>All UTF encoding aliases are supported, in a BOM-neutral way: a BOM is always returned, regardless of
     * whether the particular encoding requires a BOM to be used or not. For instance,
     * <code>UTF-16LE</code> and <code>UnicodeLittleUnmarked</code> will both return the {@link BOMConstants#UTF16_LE_BOM}
     * BOM, even though by specification <code>UTF-16LE</code> and <code>UnicodeLittleUnmarked</code> should not
     * include a BOM in the data stream. Furthermore, when called with <code>UTF-16</code> and <code>UTF-32</code>,
     * the returned BOM will arbitrarily default to big endian and return {@link BOMConstants#UTF16_BE_BOM} and
     * {@link BOMConstants#UTF32_BE_BOM} respectively.
     *
     * @param encoding name of a character encoding
     * @return a {@link BOM} instance for the specified encoding, <code>null</code> if the encoding doesn't
     * have a corresponding BOM (non-Unicode encoding).
     */
    public static BOM getInstance(String encoding) {
        if(!Charset.isSupported(encoding))
            return null;

        Charset charset = Charset.forName(encoding);
        // Retrieve the charset's canonical name for aliases we may not know about
        encoding = charset.name();

        String[] aliases;

        for(int i=0; i<BOMConstants.SUPPORTED_BOMS.length; i++) {
            if(BOMConstants.SUPPORTED_BOMS[i].getEncoding().equalsIgnoreCase(encoding))
                return BOMConstants.SUPPORTED_BOMS[i];

            aliases = BOMConstants.SUPPORTED_BOMS[i].getAliases();
            for (String alias : aliases)
                if (alias.equalsIgnoreCase(encoding))
                    return BOMConstants.SUPPORTED_BOMS[i];
        }

        return null;
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
        StringBuilder out;

        out = new StringBuilder(super.toString());
        out.append(", signature=");
        for(int i=0; i < sig.length; i++) {
            out.append(0xFF&sig[i]);
            out.append((i==sig.length-1?"}":", "));
        }
        out.append(", encoding=");
        out.append(encoding);
        return out.toString();
    }
}
