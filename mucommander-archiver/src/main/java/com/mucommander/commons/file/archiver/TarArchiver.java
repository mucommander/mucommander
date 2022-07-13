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

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileAttributes;
import com.mucommander.commons.file.FilePermissions;
import com.mucommander.commons.file.SimpleFilePermissions;
import com.mucommander.commons.file.UnsupportedFileOperationException;
import com.mucommander.commons.file.archive.zip.provider.UnixStat;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarConstants;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;


/**
 * Archiver implementation using the Tar archive format.
 *
 * @author Maxence Bernard
 */
class TarArchiver extends Archiver {

    private TarArchiveOutputStream tos;
    private boolean firstEntry = true;

    protected TarArchiver(OutputStream outputStream) {
        super(outputStream);

        this.tos = new TarArchiveOutputStream(outputStream);
        // Specifies how to handle files which filename is > 100 chars (default is to fail!)
        this.tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
    }


    /////////////////////////////
    // Archiver implementation //
    /////////////////////////////

    @Override
    public OutputStream createEntry(String entryPath, FileAttributes attributes) throws IOException {
        // Start by closing current entry
        if(!firstEntry)
            tos.closeArchiveEntry();

        boolean isDirectory = attributes.isDirectory();
        boolean symlink = attributes.isSymlink();

        // Create the entry
        TarArchiveEntry entry = symlink ?
                new TarArchiveEntry(normalizePath(entryPath, isDirectory), TarConstants.LF_SYMLINK)
                : new TarArchiveEntry(normalizePath(entryPath, isDirectory));
        // Use provided file's size (required by TarArchiveOutputStream) and date
        long size = attributes.getSize();
        if(!isDirectory && !symlink && size>=0) // Do not set size if file is directory or file size is unknown!
            entry.setSize(size);

        // Set the entry's date and permissions
        entry.setModTime(attributes.getDate());
        entry.setMode(SimpleFilePermissions.padPermissions(attributes.getPermissions(), isDirectory
                ? FilePermissions.DEFAULT_DIRECTORY_PERMISSIONS
                : FilePermissions.DEFAULT_FILE_PERMISSIONS).getIntValue());
        if (symlink) {
            Path source = Path.of(attributes.getPath());
            Path target = Files.readSymbolicLink(source);
            entry.setLinkName(target.toString());
        }

        // Add the entry
        tos.putArchiveEntry(entry);

        if(firstEntry)
            firstEntry = false;
	
        // Return the OutputStream that allows to write to the entry, only if it isn't a directory 
        return isDirectory?null:tos;
    }


    @Override
    public void close() throws IOException {
        // Close current entry
        if(!firstEntry)
            tos.closeArchiveEntry();
		
        tos.close();
    }

    @Override
    public InputStream getContentStream(AbstractFile file) throws UnsupportedFileOperationException, IOException {
        if (file.isSymlink()) {
            return new ByteArrayInputStream(new byte[0]);
        }
        return super.getContentStream(file);
    }
}
