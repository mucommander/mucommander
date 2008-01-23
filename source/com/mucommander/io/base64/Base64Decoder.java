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

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * <code>Base64Decoder</code> provides a convenience method, {@link #decode(String)}, which allows to easily decode
 * a base64-encoded String using {@link Base64InputStream} under the hood.
 *
 * @see Base64InputStream
 * @author Maxence Bernard
 */
public abstract class Base64Decoder {

    /**
     * Decodes the given Base64-encoded String and returns it decoded.
     * Throws an <code>IOException</code> if the given String wasn't properly Base64-encoded, or if an
     * <code>IOException</code> occurred while reading the underlying InputStream.
     *
     * @param  s a Base64-encoded String
     * @return the decoded String
     * @throws java.io.IOException if the given String wasn't properly Base64-encoded, or if an IOException occurred 
     * while accessing the underlying InputStream.
     */
    public static String decode(String s) throws IOException {
        Base64InputStream in64 = new Base64InputStream(new ByteArrayInputStream(s.getBytes()));

        try {
            StringBuffer sb = new StringBuffer();
            int i;
            while((i=in64.read())!=-1)
                sb.append((char)i);

            return sb.toString();
        }
        finally {
            in64.close();
        }
    }
}
