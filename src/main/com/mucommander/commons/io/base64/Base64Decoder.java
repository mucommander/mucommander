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

import java.io.*;

/**
 * <code>Base64Decoder</code> provides methods to ease the decoding of strings and byte arrays in base64.
 * The {@link Base64InputStream} class is used under the hood to perform the actual base64 decoding.
 *
 * @see Base64InputStream
 * @author Maxence Bernard
 */
public abstract class Base64Decoder {

    /**
     * Shorthand for {@link #decodeAsBytes(String, Base64Table)} invoked with {@link Base64Table#STANDARD_TABLE}.
     *
     * @param s a Base64-encoded String
     * @return the decoded string as a byte array
     * @throws java.io.IOException if the given String isn't properly Base64-encoded
     */
    public static byte[] decodeAsBytes(String s) throws IOException {
        return decodeAsBytes(s, Base64Table.STANDARD_TABLE);
    }

    /**
     * Decodes the given Base64-encoded string and returns the result as a byte array.
     * Throws an <code>IOException</code> if the String isn't properly Base64-encoded.
     *
     * @param s a Base64-encoded String
     * @param table the table to use to decode data
     * @return the decoded string as a byte array
     * @throws java.io.IOException if the given String isn't properly Base64-encoded
     */
    public static byte[] decodeAsBytes(String s, Base64Table table) throws IOException {
        byte[] b = s.getBytes();

        if(b.length%4 != 0) {
            // Base64 encoded data must come in a multiple of 4 bytes, throw an IOException if it's not the case
            throw new IOException("Byte array length is not a multiple of 4");
        }

        Base64InputStream bin = new Base64InputStream(new ByteArrayInputStream(b), table);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        int i;

        try {
            while((i=bin.read())!=-1)
                bout.write(i);

            return bout.toByteArray();
        }
        finally {
            bin.close();
        }
    }

    /**
     * Decodes the given Base64-encoded string and returns the result as a String. The specified encoding is used for
     * transforming the decoded bytes into a String. Throws an <code>IOException</code> if the String isn't properly
     * Base64-encoded, or if the encoding is not supported by the Java runtime.
     *
     * @param s a Base64-encoded String
     * @param encoding the character encoding to use for transforming the decoded bytes into a String
     * @param table the table to use to decode data
     * @return the decoded String
     * @throws UnsupportedEncodingException if the specified encoding is not supported by the Java runtime
     * @throws java.io.IOException if the given String isn't properly Base64-encoded
     */
    public static String decode(String s, String encoding, Base64Table table) throws UnsupportedEncodingException, IOException {
        InputStreamReader isr = new InputStreamReader(new ByteArrayInputStream(decodeAsBytes(s, table)), encoding);
        StringBuffer sb = new StringBuffer();
        int i;

        try {
            while((i=isr.read())!=-1)
                sb.append((char)i);

            return sb.toString();
        }
        finally {
            isr.close();
        }
    }

    /**
     * Shorthand for {@link #decode(String, String, Base64Table)} invoked with <code>UTF-8</code> encoding and
     * {@link Base64Table#STANDARD_TABLE}.
     *
     * @param s a Base64-encoded String
     * @return the decoded String
     * @throws java.io.IOException if the given String isn't properly Base64-encoded
     */
    public static String decode(String s) throws IOException {
        return decode(s, "UTF-8", Base64Table.STANDARD_TABLE);
    }
}
