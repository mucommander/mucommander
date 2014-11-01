/**
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


package com.mucommander.commons.file.impl.tar.provider;

/**
 * This class provides static utility methods to work with byte streams.
 *
 * <p>-----------------------------------</p>
 * <p>This class is based off the <code>org.apache.tools.tar</code> package of the <i>Apache Ant</i> project. The Ant
 * code has been modified under the terms of the Apache License which you can find in the bundled muCommander license
 * file. It was forked at version 1.7.1 of Ant.</p>
 *
 * @author Apache Ant
 */
public class TarUtils {

    private static final int BYTE_MASK = 255;

    /**
     * Parse an octal string from a header buffer. This is used for the
     * file permission mode value.
     *
     * @param header The header buffer from which to parse.
     * @param offset The offset into the buffer from which to parse.
     * @param length The number of header bytes to parse.
     * @return The long value of the octal string.
     */
    public static long parseOctal(byte[] header, int offset, int length) {
        long    result = 0;
        boolean stillPadding = true;
        int     end = offset + length;

        for (int i = offset; i < end; ++i) {
            if (header[i] == 0) {
                break;
            }

            if (header[i] == (byte) ' ' || header[i] == '0') {
                if (stillPadding) {
                    continue;
                }

                if (header[i] == (byte) ' ') {
                    break;
                }
            }

            stillPadding = false;
            result = (result << 3) + (header[i] - '0');
        }

        return result;
    }

    /**
     * Parse an entry name from a header buffer.
     *
     * @param header The header buffer from which to parse.
     * @param offset The offset into the buffer from which to parse.
     * @param length The number of header bytes to parse.
     * @return The header's entry name.
     */
    public static StringBuffer parseName(byte[] header, int offset, int length) {
        StringBuffer result = new StringBuffer(length);
        int          end = offset + length;

        for (int i = offset; i < end; ++i) {
            if (header[i] == 0) {
                break;
            }

            result.append((char) header[i]);
        }

        return result;
    }

    /**
     * Determine the number of bytes in an entry name.
     *
     * @param name The header name from which to parse.
     * @param buf The buffer from which to parse.
     * @param offset The offset into the buffer from which to parse.
     * @param length The number of header bytes to parse.
     * @return The number of bytes in a header's entry name.
     */
    public static int getNameBytes(StringBuffer name, byte[] buf, int offset, int length) {
        int i;

        for (i = 0; i < length && i < name.length(); ++i) {
            buf[offset + i] = (byte) name.charAt(i);
        }

        for (; i < length; ++i) {
            buf[offset + i] = 0;
        }

        return offset + length;
    }

    /**
     * Parse an octal integer from a header buffer.
     *
     * @param value The header value
     * @param buf The buffer from which to parse.
     * @param offset The offset into the buffer from which to parse.
     * @param length The number of header bytes to parse.
     * @return The integer value of the octal bytes.
     */
    public static int getOctalBytes(long value, byte[] buf, int offset, int length) {
        int    idx = length - 1;

        buf[offset + idx] = 0;
        --idx;
        buf[offset + idx] = (byte) ' ';
        --idx;

        if (value == 0) {
            buf[offset + idx] = (byte) '0';
            --idx;
        } else {
            for (long val = value; idx >= 0 && val > 0; --idx) {
                buf[offset + idx] = (byte) ((byte) '0' + (byte) (val & 7));
                val = val >> 3;
            }
        }

        for (; idx >= 0; --idx) {
            buf[offset + idx] = (byte) ' ';
        }

        return offset + length;
    }

    /**
     * Parse an octal long integer from a header buffer.
     *
     * @param value The header value
     * @param buf The buffer from which to parse.
     * @param offset The offset into the buffer from which to parse.
     * @param length The number of header bytes to parse.
     * @return The long value of the octal bytes.
     */
    public static int getLongOctalBytes(long value, byte[] buf, int offset, int length) {
        byte[] temp = new byte[length + 1];

        getOctalBytes(value, temp, 0, length + 1);
        System.arraycopy(temp, 0, buf, offset, length);

        return offset + length;
    }

    /**
     * Parse the checksum octal integer from a header buffer.
     *
     * @param value The header value
     * @param buf The buffer from which to parse.
     * @param offset The offset into the buffer from which to parse.
     * @param length The number of header bytes to parse.
     * @return The integer value of the entry's checksum.
     */
    public static int getCheckSumOctalBytes(long value, byte[] buf, int offset, int length) {
        getOctalBytes(value, buf, offset, length);

        buf[offset + length - 1] = (byte) ' ';
        buf[offset + length - 2] = 0;

        return offset + length;
    }

    /**
     * Compute the checksum of a tar entry header.
     *
     * @param buf The tar entry's header buffer.
     * @return The computed checksum.
     */
    public static long computeCheckSum(byte[] buf) {
        long sum = 0;

        for (byte b : buf) {
            sum += BYTE_MASK & b;
        }

        return sum;
    }
}
