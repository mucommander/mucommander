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

package com.mucommander.file.impl.zip.provider;

/**
 * ZipEntryInfo is a C struct-like class that holds the information about an entry that is used for parsing and writing
 * the Zip file.
 *
 * @author Maxence Bernard
 */
final class ZipEntryInfo {

    /** Offset to the central file header */
    long centralHeaderOffset = -1;

    /** Length of the central file header */
    long centralHeaderLen = -1;

    /** Offset to the local file header */
    long headerOffset = -1;

    /** Offset to the start of file data */
    long dataOffset = -1;

    /** <code>true</code> if this entry has a data descriptor in the Zip file */
    boolean hasDataDescriptor;

    /** The encoding used for filename and comment fields */
    String encoding;

    /** The filename's bytes */
    byte filename[];

    /** The comment's bytes */
    byte comment[];
}
