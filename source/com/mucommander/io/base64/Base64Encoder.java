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

package com.mucommander.io.base64;

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
     * Base64-encodes the given byte array and returns the result. The specified encoding is used for tranforming
     * the string into bytes.
     *
     * @param b the String to base64-encode
     * @return the base64-encoded String
     */
    public static String encode(byte[] b) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        Base64OutputStream out64 = new Base64OutputStream(bout, false);

        try {
            out64.write(b);
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
     * Shorthand for {@link #encode(String, String)} invoked with UTF-8 encoding.
     *
     * @param s the String to base64-encode
     * @return the base64-encoded String
     */
    public static String encode(String s) {
        try {
            return encode(s, "UTF-8");
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
     * @return the base64-encoded String
     * @throws UnsupportedEncodingException if the specified encoding is not supported by the Java runtime
     */
    public static String encode(String s, String encoding) throws UnsupportedEncodingException {
        return encode(s.getBytes(encoding));
    }
}
