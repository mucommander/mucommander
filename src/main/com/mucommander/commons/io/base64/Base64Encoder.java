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

package com.mucommander.commons.io.base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * <code>Base64Encoder</code> provides methods to ease the encoding of strings and byte arrays in base64.
 * The {@link Base64OutputStream} class is used under the hood to perform the actual base64 encoding.
 *
 * @see Base64OutputStream
 * @author Maxence Bernard
 */
public abstract class Base64Encoder {

    /**
     * Shorthand for {@link #encode(byte[], Base64Table)} invoked with {@link Base64Table#STANDARD_TABLE}.
     *
     * @param b bytes to base64-encode
     * @return the base64-encoded String
     */
    public static String encode(byte[] b) {
        return encode(b, 0, b.length, Base64Table.STANDARD_TABLE);
    }

    /**
     * Base64-encodes the given byte array using {@link Base64Table#STANDARD_TABLE} using the given Base64 table
     * and returns the result.
     *
     * @param b bytes to base64-encode
     * @param table the table to use to encode data
     * @return the base64-encoded String
     */
    public static String encode(byte[] b, Base64Table table) {
        return encode(b, 0, b.length, table);
    }

    /**
     * Shorthand for {@link #encode(byte[], int, int, Base64Table)} invoked with {@link Base64Table#STANDARD_TABLE}.
     *
     * @param b bytes to base64-encode
     * @param off position to the first byte in the array to be encoded
     * @param len number of bytes in the array to encode
     * @return the base64-encoded String
     */
    public static String encode(byte[] b, int off, int len) {
        return encode(b, off, len, Base64Table.STANDARD_TABLE);
    }

    /**
     * Base64-encodes the given byte array, from off to len, and returns the result.
     *
     * @param b bytes to base64-encode
     * @param off position to the first byte in the array to be encoded
     * @param len number of bytes in the array to encode
     * @param table the table to use to encode data
     * @return the base64-encoded String
     */
    public static String encode(byte[] b, int off, int len, Base64Table table) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        Base64OutputStream out64 = new Base64OutputStream(bout, false, table);

        try {
            out64.write(b, off, len);
            out64.writePadding();

            return new String(bout.toByteArray());
        }
        catch(IOException e) {
            // Should never happen
            return null;
        }
        finally {
            try { out64.close(); }
            catch(IOException e) {
                // Should never happen
            }
        }
    }

    /**
     * Shorthand for {@link #encode(String, String, Base64Table)} invoked with <code>UTF-8</code> encoding and
     * {@link Base64Table#STANDARD_TABLE}.
     *
     * @param s the String to base64-encode
     * @return the base64-encoded String
     */
    public static String encode(String s) {
        try {
            return encode(s, "UTF-8", Base64Table.STANDARD_TABLE);
        }
        catch(UnsupportedEncodingException e) {
            // Should never happen, UTF-8 is necessarily supported by the Java runtime
            return null;
        }
    }

    /**
     * Base64-encodes the given String and returns result. The specified encoding is used for tranforming
     * the string into bytes.
     *
     * @param s the String to base64-encode
     * @param encoding the character encoding to use for transforming the string into bytes
     * @param table the table to use to encode data
     * @return the base64-encoded String
     * @throws UnsupportedEncodingException if the specified encoding is not supported by the Java runtime
     */
    public static String encode(String s, String encoding, Base64Table table) throws UnsupportedEncodingException {
        return encode(s.getBytes(encoding), table);
    }
}
