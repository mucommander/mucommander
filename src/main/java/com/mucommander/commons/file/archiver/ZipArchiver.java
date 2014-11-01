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

package com.mucommander.commons.file.archiver;

import com.mucommander.commons.file.FileAttributes;
import com.mucommander.commons.file.FilePermissions;
import com.mucommander.commons.file.SimpleFilePermissions;
import com.mucommander.commons.file.impl.zip.provider.ZipEntry;
import com.mucommander.commons.file.impl.zip.provider.ZipOutputStream;

import java.io.IOException;
import java.io.OutputStream;
//import java.util.zip.ZipEntry;
//import java.util.zip.ZipOutputStream;


/**
 * Archiver implementation using the Zip archive format.
 *
 * @author Maxence Bernard
 */
class ZipArchiver extends Archiver {

    private ZipOutputStream zos;
    private boolean firstEntry = true;



    protected ZipArchiver(OutputStream outputStream) {
        super(outputStream);

        this.zos = new ZipOutputStream(outputStream);
    }


    /**
     * Overrides Archiver's no-op setComment method as Zip supports archive comment.
     */
    @Override
    public void setComment(String comment) {
        zos.setComment(comment);
    } 
	

    /////////////////////////////
    // Archiver implementation //
    /////////////////////////////

    @Override
    public OutputStream createEntry(String entryPath, FileAttributes attributes) throws IOException {
        // Start by closing current entry
        if(!firstEntry)
            zos.closeEntry();

        boolean isDirectory = attributes.isDirectory();
		
        // Create the entry and use the provided file's date
        ZipEntry entry = new ZipEntry(normalizePath(entryPath, isDirectory));
        // Use provided file's size and date
        long size = attributes.getSize();
        if(!isDirectory && size>=0) 	// Do not set size if file is directory or file size is unknown!
            entry.setSize(size);

        entry.setTime(attributes.getDate());
        entry.setUnixMode(SimpleFilePermissions.padPermissions(attributes.getPermissions(), isDirectory
                    ? FilePermissions.DEFAULT_DIRECTORY_PERMISSIONS
                    : FilePermissions.DEFAULT_FILE_PERMISSIONS).getIntValue());

        // Add the entry
        zos.putNextEntry(entry);

        if(firstEntry)
            firstEntry = false;
		
        // Return the OutputStream that allows to write to the entry, only if it isn't a directory 
        return isDirectory?null:zos;
    }


    @Override
    public void close() throws IOException {
        zos.close();
    }
}
