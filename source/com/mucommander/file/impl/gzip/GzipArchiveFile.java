/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (c) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with muCommander; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.mucommander.file.impl.gzip;

import com.mucommander.file.AbstractArchiveFile;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.ArchiveEntry;
import com.mucommander.file.SimpleArchiveEntry;

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
public class GzipArchiveFile extends AbstractArchiveFile {

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
        entries.add(new SimpleArchiveEntry("/"+name, getDate(), -1, false));
        return entries;
    }


    public InputStream getEntryInputStream(ArchiveEntry entry) throws IOException {
        return new GZIPInputStream(getInputStream());
    }
}
