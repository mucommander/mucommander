/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2009 Maxence Bernard
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

package com.mucommander.file.impl.ar;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.AbstractROArchiveFile;
import com.mucommander.file.ArchiveEntry;
import com.mucommander.file.ArchiveEntryIterator;
import com.mucommander.io.BoundedInputStream;

import java.io.IOException;
import java.io.InputStream;

/**
 * ArArchiveFile provides read-only access to archives in the unix AR format. Both the BSD and GNU variants (which adds
 * support for extended filenames) are supported.
 *
 * @see com.mucommander.file.impl.ar.ArFormatProvider
 * @author Maxence Bernard
 */
public class ArArchiveFile extends AbstractROArchiveFile {

    /**
     * Creates a ArArchiveFile around the given file.
     *
     * @param file the underlying file to wrap this archive file around
     */
    public ArArchiveFile(AbstractFile file) {
        super(file);
    }


    ////////////////////////////////////////
    // AbstractArchiveFile implementation //
    ////////////////////////////////////////

    public ArchiveEntryIterator getEntryIterator() throws IOException {
        return new ArArchiveEntryIterator(getInputStream());
    }

    public InputStream getEntryInputStream(ArchiveEntry entry) throws IOException {
        InputStream in = getInputStream();
        ArchiveEntryIterator iterator = new ArArchiveEntryIterator(getInputStream());

        ArchiveEntry currentEntry;
        while((currentEntry = iterator.nextEntry())!=null) {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("currentEntry="+currentEntry.getName()+" entry="+entry.getName());

            if(currentEntry.getName().equals(entry.getName())) {
                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("found entry "+entry.getName());
                return new BoundedInputStream(in, entry.getSize());
            }
        }

        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("entry not found, throwing IOException");

        // Entry not found, should not normally happen
        throw new IOException();
    }
}
