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

package com.mucommander.file.impl.zip;

import com.mucommander.file.AbstractArchiveFile;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.ArchiveEntry;
import com.mucommander.file.impl.local.LocalFile;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
// do not import java.util.zip.ZipEntry as it would create class name conflicts !

/**
 * ZipArchiveFile provides read-only access to archives in the Zip format.
 *
 * <p>Zip support is provided by the <code>java.util.zip</code> API.
 *
 * @author Maxence Bernard
 */
public class ZipArchiveFile extends AbstractArchiveFile {

    /**
     * Creates a new ZipArchiveFile.
     */
    public ZipArchiveFile(AbstractFile file) {
        super(file);
    }


    ////////////////////////////////////////
    // AbstractArchiveFile implementation //
    ////////////////////////////////////////
	
    public Vector getEntries() throws IOException {
        // Load all zip entries
        Vector entries = new Vector();
		
        // If the underlying file is a local file, use the ZipFile.getEntries() method as it 
        // is *way* faster than using ZipInputStream to iterate over the entries.
        // Note: under Mac OS X at least, ZipFile.getEntries() method is native
        if(file instanceof LocalFile) {
            ZipFile zf = null;
            try {
                zf = new ZipFile(getAbsolutePath());
                Enumeration entriesEnum = zf.entries();
                while(entriesEnum.hasMoreElements())
                    entries.add(new ZipEntry((java.util.zip.ZipEntry)entriesEnum.nextElement()));
            }
            finally {
                // Should not be necessary as we don't retrieve any entry InputStream, but just in case
                if(zf!=null)
                    zf.close();
            }
        }
        else {
            ZipInputStream zin = null;
            try {
                // works but it is *way* slower
                zin = new ZipInputStream(file.getInputStream());
                java.util.zip.ZipEntry entry;
                while ((entry=zin.getNextEntry())!=null) {
                    entries.add(new ZipEntry(entry));
                }
            }
            finally {
                if(zin!=null)
                    zin.close();
            }
        }

        return entries;
    }


    public InputStream getEntryInputStream(ArchiveEntry entry) throws IOException {
        // If the underlying file is a local file, use the ZipFile.getInputStream() method as it
        // is *way* faster than using ZipInputStream and looking for the entry
        if (file instanceof LocalFile) {
//            return new ZipFile(getAbsolutePath()).getInputStream((java.util.zip.ZipEntry)entry.getEntry());
            final ZipFile zf = new ZipFile(getAbsolutePath());

            // ZipFile needs to be explicitly closed when the entry InputStream is closed, otherwise ZipFile
            // will keep an InputStream open until it is garbage collected / finalized
            return new FilterInputStream(zf.getInputStream((java.util.zip.ZipEntry)entry.getEntry())) {
                public void close() throws IOException {
                    super.close();
                    zf.close();
                }
            };
        }
        // works but it is *way* slower
        else {
            ZipInputStream zin = new ZipInputStream(file.getInputStream());
            java.util.zip.ZipEntry tempEntry;
            String entryPath = entry.getPath();
            // Iterate until we find the entry we're looking for
            while ((tempEntry=zin.getNextEntry())!=null)
                if (tempEntry.getName().equals(entryPath)) // That's the one, return it
                    return zin;
            return null;
        }
    }
}
