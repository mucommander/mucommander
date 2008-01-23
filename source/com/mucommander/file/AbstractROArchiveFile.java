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

package com.mucommander.file;

/**
 * <code>AbstractROArchiveFile</code> represents a read-only archive file. This class is abstract and implemented
 * by read-only archive files.
 *
 * <p>
 * <code>AbstractROArchiveFile</code> implementations only have to provide two methods:
 * <ul>
 *  <li>{@link #getEntries()} to list the entries contained by the archive in a flat, non hierarchical way
 *  <li>{@link #getEntryInputStream(ArchiveEntry)} to retrieve a particular entry's content.
 * </ul>
 * The {@link #isWritableArchive()} method is implemented to always returns <code>false</code>.
 * </p>
 *
 * @author Maxence Bernard
 */
public abstract class AbstractROArchiveFile extends AbstractArchiveFile {

    /**
     * Creates an AbstractROArchiveFile on top of the given file.
     *
     * @param file the file on top of which to create the archive
     */
    protected AbstractROArchiveFile(AbstractFile file) {
        super(file);
    }


    ////////////////////////////////////////
    // AbstractArchiveFile implementation //
    ////////////////////////////////////////

    /**
     * Returns <code>false</code>: <code>AbstractROArchiveFile</code> implementations are not capable of adding or
     * deleting entries.
     *
     * @return false
     */
    public final boolean isWritableArchive() {
        return false;
    }
}
