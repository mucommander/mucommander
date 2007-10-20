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

package com.mucommander.file.impl.bzip2;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.AbstractROArchiveFile;
import com.mucommander.file.ArchiveEntry;
import org.apache.tools.bzip2.CBZip2InputStream;

import java.io.BufferedInputStream;
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
public class Bzip2ArchiveFile extends AbstractROArchiveFile {

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
        entries.add(new ArchiveEntry("/"+name, false, getDate(), -1));
        return entries;
    }


    public InputStream getEntryInputStream(ArchiveEntry entry) throws IOException {
        try {
            InputStream in = getInputStream();

            // Skips the 2 magic bytes 'BZ', as required by CBZip2InputStream. Quoted from CBZip2InputStream's Javadoc:
            // "Although BZip2 headers are marked with the magic 'Bz'. this constructor expects the next byte in the
            // stream to be the first one after the magic.  Thus callers have to skip the first two bytes. Otherwise
            // this constructor will throw an exception."
            // Note: the return value of read() is unchecked. In the unlikely event that EOF is reached in the first
            // 2 bytes, CBZip2InputStream will throw an IOException.
            in.read();
            in.read();

            // Quoted from CBZip2InputStream's Javadoc:
            // "CBZip2InputStream reads bytes from the compressed source stream via the single byte {@link java.io.InputStream#read()
            // read()} method exclusively. Thus you should consider to use a buffered source stream."
            return new CBZip2InputStream(new BufferedInputStream(in));
        }
        catch(Exception e) {
            // CBZip2InputStream is known to throw NullPointerException if file is not properly Bzip2-encoded
            // so we need to catch those and throw them as IOException
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Exception caught while creating CBZip2InputStream:"+e+", throwing IOException"); 

            throw new IOException();
        }
    }
}
