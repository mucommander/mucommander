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

package com.mucommander.io;

/**
 * This class provides convenience static methods that operate on bits and bytes.
 *
 * @author Maxence Bernard
 */
public class ByteUtils {

    /**
     * Sets/unsets a bit in the given integer.
     *
     * @param i the permission int
     * @param bit the bit to set
     * @param enabled true to enable the bit, false to disable it
     * @return the modified permission int
     */
    public static int setBit(int i, int bit, boolean enabled) {
        if(enabled)
            i |= bit;
        else
            i &= ~bit;

        return i;
    }

    /**
     * Returns an hexadecimal string representation of the given byte array, where each byte is represented by two
     * hexadecimal characters and padded with a zero if its value is comprised between 0 and 15 (inclusive).
     * As an example, this method will return "6d75636f0a" when called with the byte array {109, 117, 99, 111, 10}.
     *
     * @param bytes the array of bytes for which to get an hexadecimal string representation
     * @return an hexadecimal string representation of the given byte array
     */
    public static String toHexString(byte bytes[]) {
        StringBuffer sb = new StringBuffer();

        int bytesLen = bytes.length;
        String hexByte;
        for(int i=0; i<bytesLen; i++) {
            hexByte = Integer.toHexString(bytes[i] & 0xFF);
            if(hexByte.length()==1)
                sb.append('0');
            sb.append(hexByte);
        }

        return sb.toString();
    }
}
