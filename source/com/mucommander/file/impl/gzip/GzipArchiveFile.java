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

package com.mucommander.file.impl.gzip;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.AbstractROArchiveFile;
import com.mucommander.file.ArchiveEntry;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

/**
 * GzipArchiveFile provides read-only access to archives in the Gzip format.
 *
 * <p>Gzip support is provided by the <code>java.util.zip.GZIPInputStream</code> class.
 *
 * @author Maxence Bernard
 */
public class GzipArchiveFile extends AbstractROArchiveFile {

    /**
     * Creates a GzipArchiveFile on top of the given file.
     */
    public GzipArchiveFile(AbstractFile file) {
        super(file);
    }


    ////////////////////////////////////////
    // AbstractArchiveFile implementation //
    ////////////////////////////////////////
	
    public Vector getEntries() throws IOException {
        String extension = getExtension();
        String name = getName();
		
        if(extension!=null) {
            extension = extension.toLowerCase();
			
            // Remove the 'gz' or 'tgz' extension from the entry's name
            if(extension.equals("tgz"))
                name = name.substring(0, name.length()-3)+"tar";
            else if(extension.equals("gz"))
                name = name.substring(0, name.length()-3);
        }

        Vector entries = new Vector();
        entries.add(new ArchiveEntry("/"+name, false, getDate(), -1));
        return entries;
    }


    public InputStream getEntryInputStream(ArchiveEntry entry) throws IOException {
        return new GZIPInputStream(getInputStream());
    }
}
