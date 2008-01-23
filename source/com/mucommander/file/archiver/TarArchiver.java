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


package com.mucommander.file.archiver;

import com.mucommander.file.AbstractFile;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarOutputStream;

import java.io.IOException;
import java.io.OutputStream;


/**
 * Archiver implementation using the Tar archive format.
 *
 * @author Maxence Bernard
 */
class TarArchiver extends Archiver {

    private TarOutputStream tos;
    private boolean firstEntry = true;

    protected TarArchiver(OutputStream outputStream) {
        super(outputStream);

        this.tos = new TarOutputStream(outputStream);
        // Specifies how to handle files which filename is > 100 chars (default is to fail!)
        this.tos.setLongFileMode(TarOutputStream.LONGFILE_GNU);
    }


    /////////////////////////////
    // Archiver implementation //
    /////////////////////////////

    public OutputStream createEntry(String entryPath, AbstractFile file) throws IOException {
        // Start by closing current entry
        if(!firstEntry)
            tos.closeEntry();

        boolean isDirectory = file.isDirectory();
		
        // Create the entry
        TarEntry entry = new TarEntry(normalizePath(entryPath, isDirectory));
        // Use provided file's size (required by TarOutputStream) and date
        long size = file.getSize();
        if(!isDirectory && size>=0)		// Do not set size if file is directory or file size is unknown!
            entry.setSize(size);

        // Set the entry's date and permissions
        entry.setModTime(file.getDate());
        entry.setMode(file.getPermissions());

        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("creating entry, name="+entry.getName()+" isDirectory="+entry.isDirectory()+" size="+entry.getSize()+" modTime="+entry.getModTime());
		
        // Add the entry
        tos.putNextEntry(entry);

        if(firstEntry)
            firstEntry = false;
	
        // Return the OutputStream that allows to write to the entry, only if it isn't a directory 
        return isDirectory?null:tos;
    }


    public void close() throws IOException {
        // Close current entry
        if(!firstEntry)
            tos.closeEntry();
		
        tos.close();
    }
}
