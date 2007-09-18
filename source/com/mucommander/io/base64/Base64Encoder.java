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

package com.mucommander.io.base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * <code>Base64Encoder</code> provides a convenience method, {@link #encode(String)}, which allows to easily
 * base64-encode a String using {@link Base64OutputStream} under the hood.
 *
 * @see Base64OutputStream
 * @author Maxence Bernard
 */
public abstract class Base64Encoder {

    /**
     * Base64-encodes the given String and returns the encoded String.
     *
     * @param s the String to encode as base64
     * @return the base64-encoded String
     */
    public static String encode(String s) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        Base64OutputStream out64 = new Base64OutputStream(bout, false);

        try {
            int len = s.length();
            for(int i=0; i<len; i++)
                out64.write(s.charAt(i));

            out64.writePadding();

            return new String(bout.toByteArray());
        }
        catch(IOException e) {
            // Should never happen
            return null;
        }
        finally {
            try { out64.close(); }
            catch(IOException e) {}
        }
    }
}
