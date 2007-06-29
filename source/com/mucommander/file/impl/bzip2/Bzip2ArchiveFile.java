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

package com.mucommander.file.impl.bzip2;

import com.mucommander.file.AbstractArchiveFile;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.ArchiveEntry;
import com.mucommander.file.SimpleArchiveEntry;
import org.apache.tools.bzip2.CBZip2InputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

/**
 * Bzip2ArchiveFile provides read-only access to archives in the Bzip2 format.
 *
 * <p>Bzip2 support is provided by the <code>Apache Ant</code> library distributed in the Apache Software License.
 *
 * @author Maxence Bernard
 */
public class Bzip2ArchiveFile extends AbstractArchiveFile {

    /**
     * Creates a BzipArchiveFile on top of the given file.
     */
    public Bzip2ArchiveFile(AbstractFile file) {
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
			
            // Remove the 'bz2' or 'tbz2' extension from the entry's name
            if(extension.equals("tbz2"))
                name = name.substring(0, name.length()-4)+"tar";
            else if(extension.equals("bz2"))
                name = name.substring(0, name.length()-4);
        }

        Vector entries = new Vector();
        entries.add(new SimpleArchiveEntry("/"+name, getDate(), -1, false));
        return entries;
    }


    public InputStream getEntryInputStream(ArchiveEntry entry) throws IOException {
        try { return new CBZip2InputStream(getInputStream()); }
        catch(Exception e) {
            // CBZip2InputStream is known to throw NullPointerException if file is not properly Bzip2-encoded
            // so we need to catch those and throw them as IOException
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Exception caught while creating CBZip2InputStream:"+e+", throwing IOException"); 

            throw new IOException();
        }
    }
}
