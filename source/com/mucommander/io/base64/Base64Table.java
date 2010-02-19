/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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

package com.mucommander.io.base64;

/**
 * This class represents an immutable Base64 encoding/decoding table. It provides the correspondance between encoded
 * and decoded characters (and vice-versa), and the character to use for padding.
 * <p>
 * A number of common Base64 tables are provided as public static final fields of this class:
 * <ul>
 *   <li>{@link #STANDARD_TABLE}</li>
 *   <li>{@link #URL_SAFE_TABLE}</li>
 *   <li>{@link #FILENAME_SAFE_TABLE}</li>
 *   <li>{@link #REGEXP_SAFE_TABLE}</li>
 * </ul>
 * </p>
 *
 * {@link
 *
 * @author Maxence Bernard
 */
public class Base64Table {

    /** Encoding table, 64 bytes long */
    protected byte[] encodingTable;

    /** Decoding table, 256 bytes long */
    protected int[] decodingTable;

    /** Padding character used */
    protected byte paddingChar;

    /** The standard Base64 table, using '+' and '/' for non-alphanumerical characters, and '=' for padding */
    public final static Base64Table STANDARD_TABLE = createTable((byte)'+', (byte)'/', (byte)'=');

    /** An URL-safe Base64 table, using '-' and '_' for non-alphanumerical characters, and '.' for padding */
    public final static Base64Table URL_SAFE_TABLE = createTable((byte)'-', (byte)'_', (byte)'.');

    /** A filename-safe Base64 table, using '+' and '-' for non-alphanumerical characters, and '=' for padding */
    public final static Base64Table FILENAME_SAFE_TABLE = createTable((byte)'+', (byte)'-', (byte)'=');

    /** A regexp-safe Base64 table, using '!' and '-' for non-alphanumerical characters, and '=' for padding */
    public final static Base64Table REGEXP_SAFE_TABLE = createTable((byte)'!', (byte)'-', (byte)'=');

    /**
     * Creates a base64 table using A–Z, a–z, and 0–9 for the first 62 values, the two specified characters at
     * position 62 and 63 in the table, and the specified padding character.
     *
     * @param char62 ASCII character to use at position 62 of the table
     * @param char63 ASCII character to use at position 63 of the table
     * @param paddingChar ASCII character to use for padding
     * @throws IllegalArgumentException if one specified characters are the same or alphanumerical characters
     * @return a base64 table
     */
    public static Base64Table createTable(byte char62, byte char63, byte paddingChar) throws IllegalArgumentException {
        byte[] table = new byte[] {
            'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P',
            'Q','R','S','T','U','V','W','X','Y','Z','a','b','c','d','e','f',
            'g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v',
            'w','x','y','z','0','1','2','3','4','5','6','7','8','9',char62,char63
        };

        return new Base64Table(table, paddingChar);
    }


    /**
     * Creates a new <code>Base64Table</code> using the specified character table and padding character.
     *
     * <p>An <code>IllegalArgumentException</code> if the specified table is not 64 bytes long, contains duplicate
     * values, or if the specified padding character is present in the table.</p>
     *
     * <p>The given byte array is cloned before being stored, to avoid any side effect that could be caused by the
     * byte array being modified inadvertently after this constructor is called.</p>
     *
     * @param table the base64 character table. The array must be 64 bytes long and must not contain any duplicate values.
     * @param paddingChar the ASCII character used for padding. This character must not already be used in the table.
     * @throws IllegalArgumentException if the specified table is not 64 bytes long, contains duplicate values, or
     * if the specified padding character is present in the table.
     */
    public Base64Table(byte[] table, byte paddingChar) throws IllegalArgumentException {
        // Basic length check
        if(table==null || table.length!=64)
            throw new IllegalArgumentException("Base64 table is not 64 bytes long");

        // Create the decoding table and initialize all values to -1
        this.decodingTable = new int[256];
        char c;
        for(c=0; c<256; c++)
            decodingTable[c] = -1;

        // Fill the decoding table and snsure that characters are used only once
        byte val;
        for(int i=0; i<64; i++) {
            val= table[i];
            if(decodingTable[val]!=-1)
                throw new IllegalArgumentException("Base64 table contains duplicate values");

            decodingTable[val] = i;
        }

        // Ensure that the padding character is not already used in the table
        if(decodingTable[paddingChar]!=-1)
            throw new IllegalArgumentException("Padding char is already used in Base64 table");

        this.paddingChar = paddingChar;

        // Clone the byte array so that it cannot be altered externally
        this.encodingTable = new byte[64];
        System.arraycopy(table, 0, this.encodingTable, 0, 64);
    }


    /**
     * Returns the base64 encoding table. The return array should not be modified.
     *
     * @return the base64 encoding table.
     */
    byte[] getEncodingTable() {
        return encodingTable;
    }

    /**
     * Returns the base64 decoding table, containing byte values with the exception of <code>-1</code> which indicates
     * the character is not used. The return array should not be modified.
     *
     * @return the base64 decoding table.
     */
    int[] getDecodingTable() {
        return decodingTable;
    }

    /**
     * Returns the ASCII character used for padding.
     *
     * @return the ASCII character used for padding.
     */
    public byte getPaddingChar() {
        return paddingChar;
    }
}
