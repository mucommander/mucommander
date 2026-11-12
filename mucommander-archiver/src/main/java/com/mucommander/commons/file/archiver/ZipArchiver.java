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
import java.nio.charset.StandardCharsets;
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

        boolean isDirectory = isDirectoryEntry(file);
		
        // Create the entry and use the provided file's date
        ZipEntry entry = new ZipEntry(normalizePath(entryPath, isDirectory));
        // Use provided file's size and date
        long size = file.getSize();
        if(!isDirectory && size>=0) 	// Do not set size if file is directory or file size is unknown!
            entry.setSize(size);

        entry.setTime(file.getDate());
        entry.setUnixMode(getUnixMode(file));

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
        if (isLocalSymlink(file))
            return getSymlinkTargetStream(file);
        return super.getContentStream(file);
    }


    ///////////////////////////////////////////////////////////
    // Helper methods shared with the encrypted Zip archiver //
    ///////////////////////////////////////////////////////////

    /**
     * Returns <code>true</code> if the given file is a symbolic link on the local filesystem. Such files are stored
     * as Unix symlink entries, whose contents is the link's target.
     *
     * @param file the file to test
     * @return <code>true</code> if the given file is a local symbolic link
     */
    static boolean isLocalSymlink(AbstractFile file) {
        return LocalFile.SCHEMA.equals(file.getURL().getScheme()) && file.isSymlink();
    }

    /**
     * Returns <code>true</code> if the given file must be stored as a directory entry. Symbolic links to directories
     * are stored as file entries, consistently with ArchiveJob which doesn't recurse into them.
     *
     * @param file the file to test
     * @return <code>true</code> if the given file is a directory that is not a symbolic link
     */
    static boolean isDirectoryEntry(AbstractFile file) {
        return file.isDirectory() && !file.isSymlink();
    }

    /**
     * Returns the Unix mode (file type and permissions) to store in the Zip entry of the given file.
     *
     * @param file the file whose entry is being created
     * @return the Unix mode of the given file, as understood by {@link ZipEntry#setUnixMode(int)}
     */
    static int getUnixMode(AbstractFile file) {
        int unixMode = SimpleFilePermissions.padPermissions(file.getPermissions(), isDirectoryEntry(file) ?
                FilePermissions.DEFAULT_DIRECTORY_PERMISSIONS
                : FilePermissions.DEFAULT_FILE_PERMISSIONS).getIntValue();
        unixMode |= isLocalSymlink(file) ? UnixStat.LINK_FLAG : 0;
        return unixMode;
    }

    /**
     * Returns a stream containing the target of the given local symbolic link, which is written as the contents of
     * the symlink's entry within the archive.
     *
     * @param file a local symbolic link
     * @return a stream containing the link's target path
     * @throws IOException if the link's target could not be read
     */
    static InputStream getSymlinkTargetStream(AbstractFile file) throws IOException {
        Path path = Path.of(file.getAbsolutePath());
        return new ByteArrayInputStream(Files.readSymbolicLink(path).toString().getBytes(StandardCharsets.UTF_8));
    }
}
