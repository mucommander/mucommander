/**
 * This file is part of muCommander, http://www.mucommander.com
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FilePermissions;
import com.mucommander.commons.file.SimpleFilePermissions;
import com.mucommander.commons.file.UnsupportedFileOperationException;
import com.mucommander.commons.file.archive.zip.provider.UnixStat;
import com.mucommander.commons.file.archive.zip.provider.ZipEntry;
import com.mucommander.commons.file.archive.zip.provider.ZipOutputStream;
import com.mucommander.commons.file.protocol.local.LocalFile;


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
    public OutputStream createEntry(String entryPath, AbstractFile file) throws IOException {
        // Start by closing current entry
        if(!firstEntry)
            zos.closeEntry();

        boolean isDirectory = file.isDirectory();
		
        // Create the entry and use the provided file's date
        ZipEntry entry = new ZipEntry(normalizePath(entryPath, isDirectory));
        // Use provided file's size and date
        long size = file.getSize();
        if(!isDirectory && size>=0) 	// Do not set size if file is directory or file size is unknown!
            entry.setSize(size);

        entry.setTime(file.getDate());
        int unixMode = SimpleFilePermissions.padPermissions(file.getPermissions(), isDirectory ?
                FilePermissions.DEFAULT_DIRECTORY_PERMISSIONS
                : FilePermissions.DEFAULT_FILE_PERMISSIONS).getIntValue();
        unixMode |= file.getURL().getScheme() == LocalFile.SCHEMA && file.isSymlink() ? UnixStat.LINK_FLAG : 0;
        entry.setUnixMode(unixMode);

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

    @Override
    public InputStream getContentStream(AbstractFile file) throws UnsupportedFileOperationException, IOException {
        if (file.getURL().getScheme() == LocalFile.SCHEMA && file.isSymlink()) {
            // we return the target of the link here so it will be
            // written to the "file" within the archive
            Path path = Path.of(file.getAbsolutePath());
            return new ByteArrayInputStream(Files.readSymbolicLink(path).toString().getBytes());
        }
        return super.getContentStream(file);
    }
}
