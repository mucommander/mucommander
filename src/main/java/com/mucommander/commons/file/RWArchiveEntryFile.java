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


package com.mucommander.commons.file;

import com.mucommander.commons.io.ByteCounter;
import com.mucommander.commons.io.CounterOutputStream;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Represents a file entry inside a read-write archive. Read-write archives are characterized by
 * {@link AbstractArchiveFile#isWritable()} returning <code>false</code>.
 *
 * @see AbstractArchiveFile
 * @see ROArchiveEntryFile
 * @author Maxence Bernard
 */
public class RWArchiveEntryFile extends AbstractArchiveEntryFile {

    protected RWArchiveEntryFile(FileURL url, AbstractArchiveFile archiveFile, ArchiveEntry entry) {
        super(url, archiveFile, entry);
    }

    /**
     * Updates this entry's attributes in the archive and returns <code>true</code> if the update went OK.
     *
     * @return <code>true</code> if the attributes were successfully updated in the archive.
     */
    private boolean updateEntryAttributes() {
        try {
            ((AbstractRWArchiveFile)archiveFile).updateEntry(entry);
            return true;
        }
        catch(IOException e) {
            return false;
        }
    }


    /////////////////////////////////
    // AbstractFile implementation //
    /////////////////////////////////


    /**
     * @throws IOException if the entry does not exist within the archive
     */
    @Override
    public void changeDate(long lastModified) throws IOException, UnsupportedFileOperationException {
        if(!entry.exists())
            throw new IOException();

        long oldDate = entry.getDate();
        entry.setDate(lastModified);

        boolean success = updateEntryAttributes();
        if(!success) {
            // restore old date if attributes could not be updated
            entry.setDate(oldDate);
            throw new IOException();
        }
    }
    
    /**
     * Always returns {@link PermissionBits#FULL_PERMISSION_BITS}.
     */
    @Override
    public PermissionBits getChangeablePermissions() {
        // Todo: some writable archive implementations may not have full 'set' permissions support, or even no notion of permissions
        return PermissionBits.FULL_PERMISSION_BITS;
    }

    /**
     * Deletes this entry from the associated <code>AbstractArchiveFile</code>.
     * <p>
     * Throws a {@link UnsupportedFileOperationException} if if the underlying file does not support the required
     * read and write {@link FileOperation file operations}. Throws an <code>IOException</code> in any of the following
     * cases:
     * <ul>
     *  <li>if this entry does not exist in the archive</li>
     *  <li>if this entry is a non-empty directory</li>
     *  <li>if an I/O error occurred</li>
     * </ul>
     * </p>
     *
     * @throws IOException in any of the cases listed above.
     * @throws UnsupportedFileOperationException if the underlying archive file does not support the required read and
     * write {@link FileOperation file operations}.
     */
    @Override
    public void delete() throws IOException, UnsupportedFileOperationException {
        if(!entry.exists())
            throw new IOException();

        AbstractRWArchiveFile rwArchiveFile = (AbstractRWArchiveFile)archiveFile;

        // Throw an IOException if this entry is a non-empty directory
        if(isDirectory()) {
            ArchiveEntryTree tree = rwArchiveFile.getArchiveEntryTree();
            if(tree!=null) {
                DefaultMutableTreeNode node = tree.findEntryNode(entry.getPath());
                if(node!=null && node.getChildCount()>0)
                    throw new IOException();
            }
        }

        // Delete the entry in the archive file
        rwArchiveFile.deleteEntry(entry);

        // Non-existing entries are considered as zero-length regular files
        entry.setDirectory(false);
        entry.setSize(0);
        entry.setExists(false);
    }

    /**
     * Creates this entry as a directory in the associated <code>AbstractArchiveFile</code>.
     * <p>
     * Throws a {@link UnsupportedFileOperationException} if if the underlying file does not support the required
     * read and write {@link FileOperation file operations}. Throws an <code>IOException</code> if this entry
     * already exists in the archive or if an I/O error occurred.
     * </p>
     *
     * @throws IOException if this entry already exists in the archive or if an I/O error occurred.
     * @throws UnsupportedFileOperationException if the underlying archive file does not support the required read and
     * write {@link FileOperation file operations}.
     */
    @Override
    public void mkdir() throws IOException, UnsupportedFileOperationException {
        if(entry.exists())
            throw new IOException();

        AbstractRWArchiveFile rwArchivefile = (AbstractRWArchiveFile)archiveFile;
        // Update the ArchiveEntry
        entry.setDirectory(true);
        entry.setDate(System.currentTimeMillis());
        entry.setSize(0);

        // Add the entry to the archive file
        rwArchivefile.addEntry(entry);

        // The entry now exists
        entry.setExists(true);
    }

    /**
     * Returns an <code>OutputStream</code> that allows to write this entry's contents.
     * <p>
     * This method will create this entry as a regular file in the archive if it doesn't already exist, or replace
     * it if it already does.
     * </p>
     *
     * @throws IOException if an I/O error occurred
     * @throws UnsupportedFileOperationException if the underlying archive file does not support the required read and
     * write {@link FileOperation file operations}.
     */
    @Override
    public OutputStream getOutputStream() throws IOException, UnsupportedFileOperationException {
        if(entry.exists()) {
            try {
                delete();
            }
            catch(IOException e) {
                // Go ahead and try to add the file anyway
            }
        }

        // Update the ArchiveEntry's size as data gets written to the OutputStream
        OutputStream out = new CounterOutputStream(((AbstractRWArchiveFile)archiveFile).addEntry(entry),
                new ByteCounter() {
                    @Override
                    public synchronized void add(long nbBytes) {
                        entry.setSize(entry.getSize()+nbBytes);
                        entry.setDate(System.currentTimeMillis());
                    }
                });
        entry.setExists(true);

        return out;
    }

    @Override
    public void changePermissions(int permissions) throws IOException {
        if(!entry.exists())
            throw new IOException();

        FilePermissions oldPermissions = entry.getPermissions();
        FilePermissions newPermissions = new SimpleFilePermissions(permissions, oldPermissions.getMask());
        entry.setPermissions(newPermissions);

        boolean success = updateEntryAttributes();
        if(!success)        // restore old permissions if attributes could not be updated
            entry.setPermissions(oldPermissions);

        if(!success)
            throw new IOException();
    }
}
