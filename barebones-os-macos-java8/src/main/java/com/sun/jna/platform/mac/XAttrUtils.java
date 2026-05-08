/*
 * This file is part of muCommander, http://www.mucommander.com
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
package com.sun.jna.platform.mac;

import com.sun.jna.Memory;

/**
 * Utility methods that complement the JNA library.
 *
 * @author Arik Hadas
 */
public class XAttrUtils {
    /** The key of the spotlight comment section */
    public static final String COMMENT = "com.apple.metadata:kMDItemFinderComment";

    /**
     * Reads the content of section in file's metadata.
     *
     * @param path Path to a file
     * @param key Key of a metadata section
     * @return The value associated with the given key in the file's metadata
     */
    public static byte[] read(String path, String key) {
        long bufferLength = XAttr.INSTANCE.getxattr(path, key, null, 0, 0, 0);
        if (bufferLength <= 0)
            return null;

        Memory valueBuffer = new Memory(bufferLength);
        valueBuffer.clear();
        long valueLength = XAttr.INSTANCE.getxattr(path, key, valueBuffer, bufferLength, 0, 0);

        if (valueLength < 0)
            return null;

        return valueBuffer.getByteArray(0, (int)valueLength);
    }

    /**
     * Writes the content to section in file's metadata.
     *
     * @param path Path to a file
     * @param key Key of a metadata section
     * @param value Content to write to the metadata section
     */
    public static void write(String path, String key, byte[] value) {
        Memory valueBuffer = new Memory(value.length);
        valueBuffer.write(0, value, 0, value.length);
        XAttr.INSTANCE.setxattr(path, key, valueBuffer, valueBuffer.size(), 0, 0);
    }
}
