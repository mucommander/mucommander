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

package com.mucommander.io;

import java.io.IOException;

/**
 * RandomAccess provides a common interface to random access streams, whether they be input or output streams.
 *
 * @author Maxence Bernard
 */
public interface RandomAccess {

    /**
     * Closes the random access stream and releases any system resources associated with the stream.
     * A closed stream cannot perform input or output operations and cannot be reopened.
     *
     * @throws IOException if an I/O error occurs
     */
    public abstract void close() throws IOException;

    /**
     * Returns the offset (in bytes) from the beginning of the file at which the next read or write occurs.
     *
     * @return the offset (in bytes) from the beginning of the file at which the next read or write occurs
     * @throws IOException if an I/O error occurs.
     */
    public abstract long getOffset() throws IOException;

    /**
     * Returns the length of the file, in bytes.
     *
     * @return the length of the file, in bytes
     * @throws IOException if an I/O error occurs
     */
    public abstract long getLength() throws IOException;

    /**
     * Sets the offset, measured from the beginning of the file, at which the next read or write occurs.
     * The offset may be set beyond the end of the file. Setting the offset beyond the end of the file does not change
     * the file length. The file length will change only by writing after the offset has been set beyond the end of the
     * file.
     *
     * @param offset the new offset position, measured in bytes from the beginning of the file
     * @throws IOException if an I/O error occurs
     */
    public abstract void seek(long offset) throws IOException;

}
