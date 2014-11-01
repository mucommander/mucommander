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


package com.mucommander.commons.file.impl.lst;

import com.mucommander.commons.file.*;

import java.io.IOException;
import java.io.InputStream;

/**
 * IsoArchiveFile provides read-only access to archives in the LST format made popular by Total Commander.
 *
 * <p>Entries are parsed from the .lst file and can be read (an InputStream to them can be opened) if the file exists
 * locally.</p>
 *
 * <p>For reference, here's a short LST file:
 * <pre>
 * c:\
 * cygwin\	0	2006.10.2	19:35.2
 * cygwin.bat	57	2006.10.2	19:34.58
 * cygwin.ico	7022	2006.10.2	19:40.52
 * cygwin\bin\	0	2006.10.2	19:40.52
 * addftinfo.exe	67072	2002.12.16	10:3.24
 * afmtodit	8544	2002.12.16	10:3.22
 * apropos	1786	2005.5.4	2:12.50
 * ascii.exe	7168	2006.3.20	20:44.24
 * ash.exe	74240	2004.1.27	2:14.20
 * awk.exe	19	2006.10.2	19:34.4
 * </pre>
 * </p>
 *
 * @see com.mucommander.commons.file.impl.lst.LstFormatProvider
 * @author Maxence Bernard
 */
public class LstArchiveFile extends AbstractROArchiveFile {
    
    public LstArchiveFile(AbstractFile file) {
        super(file);
    }


    ////////////////////////////////////////
    // AbstractArchiveFile implementation //
    ////////////////////////////////////////

    @Override
    public ArchiveEntryIterator getEntryIterator() throws IOException, UnsupportedFileOperationException {
        return new LstArchiveEntryIterator(getInputStream());
    }

    @Override
    public InputStream getEntryInputStream(ArchiveEntry entry, ArchiveEntryIterator entryIterator) throws IOException, UnsupportedFileOperationException {
        // Will throw an IOException if the file designated by the entry doesn't exist 
        return FileFactory.getFile(((LstArchiveEntry)entry).getBaseFolder()+entry.getPath(), true).getInputStream();
    }
}
