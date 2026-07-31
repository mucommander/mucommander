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
package com.mucommander.desktop.macos;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

/**
 * JNA interface to macOS extended attributes (xattr) functions.
 * Provides access to getxattr and setxattr system calls.
 *
 * @author Arik Hadas
 */
public interface MacXAttr extends Library {
    MacXAttr INSTANCE = Native.load("c", MacXAttr.class);

    /**
     * Get an extended attribute value.
     *
     * @param path path to the file
     * @param name attribute name
     * @param value buffer to receive the attribute value (or null to query size)
     * @param size size of the value buffer
     * @param position byte offset within the attribute (typically 0)
     * @param options options flags (typically 0)
     * @return number of bytes in the attribute value, or -1 on error
     */
    long getxattr(String path, String name, Pointer value, long size, int position, int options);

    /**
     * Set an extended attribute value.
     *
     * @param path path to the file
     * @param name attribute name
     * @param value buffer containing the attribute value
     * @param size size of the value
     * @param position byte offset within the attribute (typically 0)
     * @param options options flags (typically 0)
     * @return 0 on success, -1 on error
     */
    int setxattr(String path, String name, Pointer value, long size, int position, int options);
}
