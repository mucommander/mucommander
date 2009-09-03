/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2009 Maxence Bernard
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

package com.mucommander.file;

import com.mucommander.file.filter.FileFilter;
import com.mucommander.file.filter.FilenameFilter;
import com.mucommander.io.*;
import com.mucommander.util.StringUtils;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * <code>ArchiveEntryFile</code> represents a file entry inside an archive. An ArchiveEntryFile is always associated with an
 * {@link ArchiveEntry} object which contains information about the entry (name, size, date, ...) and with an
 * {@link AbstractArchiveFile} which acts as an entry repository and provides operations such as listing a directory
 * entry's files, adding or removing entries (if the archive is writable), etc...
 *
 * <p>
 * <code>ArchiveEntryFile</code> implements {@link com.mucommander.file.AbstractFile} by delegating methods to the
 * <code>ArchiveEntry</code> and <code>AbstractArchiveFile</code> instances.
 * <code>ArchiveEntryFile</code> is agnostic to the actual archive format. In other words, there is no need to extend
 * this class for a particular archive format, <code>ArchiveEntry</code> and <code>AbstractArchiveFile</code> provide a
 * general framework that isolates from the archive format's specifics.
 * </p>
 *
 * @author Maxence Bernard
 */
public class ArchiveEntryFile extends AbstractFile {

    /** The archive file that contains this entry */
    protected AbstractArchiveFile archiveFile;

    /** This entry file's parent, can be the archive file itself if this entry is located at the top level */
    protected AbstractFile parent;

    /** The ArchiveEntry object that contains information about this entry */
    protected ArchiveEntry entry;


    /**
     * Creates a new ArchiveEntryFile.
     *
     * @param url the FileURL instance that represents this file's location
     * @param archiveFile the AbstractArchiveFile instance that contains this entry
     * @param entry the ArchiveEntry object that contains information about this entry
     */
    protected ArchiveEntryFile(FileURL url, AbstractArchiveFile archiveFile, ArchiveEntry entry) {
        super(url);
        this.archiveFile = archiveFile;
        this.entry = entry;
    }
	
	
    /**
     * Returns the ArchiveEntry instance that contains information about the archive entry (path, size, date, ...).
     *
     * @return the ArchiveEntry instance that contains information about the archive entry (path, size, date, ...)
     */
    public ArchiveEntry getEntry() {
        return entry;
    }

    /**
     * Returns the {@link AbstractArchiveFile} that contains the entry represented by this file.
     *
     * @return the AbstractArchiveFile that contains the entry represented by this file
     */
    public AbstractArchiveFile getArchiveFile() {
        return archiveFile;
    }


    /**
     * Returns the relative path of this entry, with respect to the archive file. The path separator of the returned
     * path is the one returned by {@link #getSeparator()}. As a relative path, the returned path does not start
     * with a separator character.
     *
     * @return the relative path of this entry, with respect to the archive file.
     */
    public String getRelativeEntryPath() {
        String path = entry.getPath();

        // Replace all occurrences of the entry's separator by the archive file's separator, only if the separator is
        // not "/" (i.e. the entry path separator).
        String separator = getSeparator();
        if(!separator.equals("/"))
            path = StringUtils.replaceCompat(path, "/", separator);

        return path;
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

    public long getDate() {
        return entry.getDate();
    }

    /**
     * Returns <code>true</code> only if the archive file that contains this entry is writable.
     */
    public boolean canChangeDate() {
        return archiveFile.isWritable();
    }

    /**
     * Always returns <code>false</code> only if the archive file that contains this entry is not writable.
     */
    public boolean changeDate(long lastModified) {
        if(!(entry.exists() && archiveFile.isWritable()))
            return false;

        long oldDate = entry.getDate();
        entry.setDate(lastModified);

        boolean success = updateEntryAttributes();
        if(!success)        // restore old date if attributes could not be updated
            entry.setDate(oldDate);

        return success;
    }

    public long getSize() {
        return entry.getSize();
    }
	
    public boolean isDirectory() {
        return entry.isDirectory();
    }

    public boolean isArchive() {
        // Archive entries files may be wrapped by archive files but they are not archive files per se
        return false;
    }

    public AbstractFile[] ls() throws IOException {
        return archiveFile.ls(this, null, null);
    }

    public AbstractFile[] ls(FilenameFilter filter) throws IOException {
        return archiveFile.ls(this, filter, null);
    }
	
    public AbstractFile[] ls(FileFilter filter) throws IOException {
        return archiveFile.ls(this, null, filter);
    }

    public AbstractFile getParent() {
        return parent;
    }
	
    public void setParent(AbstractFile parent) {
        this.parent = parent;	
    }

    /**
     * Returns <code>true</code> if this entry exists within the archive file.
     *
     * @return true if this entry exists within the archive file
     */
    public boolean exists() {
        return entry.exists();
    }
	
    public FilePermissions getPermissions() {
        // Return the entry's permissions
        return entry.getPermissions();
    }

    /**
     * Returns {@link PermissionBits#FULL_PERMISSION_BITS} or {@link PermissionBits#EMPTY_PERMISSION_BITS}, depending
     * on whether the archive that contains this entry is writable or not.
     */
    public PermissionBits getChangeablePermissions() {
        // Todo: some writable archive implementations may not have full 'set' permissions support, or even no notion of permissions
        return archiveFile.isWritable()?PermissionBits.FULL_PERMISSION_BITS:PermissionBits.EMPTY_PERMISSION_BITS;
    }

    /**
     * Always returns <code>false</code> only if the archive file that contains this entry is not writable.
     */
    public boolean changePermission(int access, int permission, boolean enabled) {
        return changePermissions(ByteUtils.setBit(getPermissions().getIntValue(), (permission << (access*3)), enabled));
    }

    public String getOwner() {
        return entry.getOwner();
    }

    public boolean canGetOwner() {
        return entry.getOwner()!=null;
    }

    public String getGroup() {
        return entry.getGroup();
    }

    public boolean canGetGroup() {
        return entry.getGroup()!=null;
    }

    /**
     * Always returns <code>false</code>.
     */
    public boolean isSymlink() {
        return false;
    }

    /**
     * Deletes this entry from the associated <code>AbstractArchiveFile</code> if it is writable (as reported by
     * {@link com.mucommander.file.AbstractArchiveFile#isWritable()}).
     * Throws an <code>IOException</code> in any of the following cases:
     * <ul>
     *  <li>if the associated <code>AbstractArchiveFile</code> is not writable</li>
     *  <li>if this entry does not exist in the archive</li>
     *  <li>if this entry is a non-empty directory</li>
     *  <li>if an I/O error occurred</li>
     * </ul>
     *
     * @throws IOException in any of the cases listed above.
     */
    public void delete() throws IOException {
        if(entry.exists() && archiveFile.isWritable()) {
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
        else
            throw new IOException();
    }

    /**
     * Creates this entry as a directory in the associated <code>AbstractArchiveFile</code> if the archive is
     * writable (as reported by {@link com.mucommander.file.AbstractArchiveFile#isWritable()}).
     * Throws an <code>IOException</code> if it isn't, if this entry already exists in the archive or if an I/O error
     * occurred.
     *
     * @throws IOException if the associated archive file is not writable, if this entry already exists in the archive,
     * or if an I/O error occurred
     */
    public void mkdir() throws IOException {
        if(!entry.exists() && archiveFile.isWritable()) {
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
        else
            throw new IOException();
    }

    /**
     * Delegates to the archive file's {@link AbstractArchiveFile#getFreeSpace()} method.
     */
    public long getFreeSpace() {
        return archiveFile.getFreeSpace();
    }

    /**
     * Delegates to the archive file's {@link AbstractArchiveFile#getTotalSpace()} method.
     */
    public long getTotalSpace() {
        return archiveFile.getTotalSpace();
    }

    /**
     * Delegates to the archive file's {@link AbstractArchiveFile#getEntryInputStream(ArchiveEntry,ArchiveEntryIterator)}}
     * method.
     */
    public InputStream getInputStream() throws IOException {
        return archiveFile.getEntryInputStream(entry, null);
    }

    /**
     * Returns an <code>OutputStream</code> that allows to write this entry's contents if the archive is
     * writable (as reported by {@link com.mucommander.file.AbstractArchiveFile#isWritable()}).
     * Throws an <code>IOException</code> if it isn't or if an I/O error occurred.
     *
     * <p>
     * This method will create this entry as a regular file in the archive if it doesn't already exist, or replace
     * it if it already does.
     * </p>
     *
     * @throws IOException if the associated archive file is not writable, if this entry already exists in the archive,
     * or if an I/O error occurred
     */
    public OutputStream getOutputStream(boolean append) throws IOException {
        if(archiveFile.isWritable()) {
            if(append)
                throw new IOException("Can't append to an existing archive entry");

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
                        public synchronized void add(long nbBytes) {
                            entry.setSize(entry.getSize()+nbBytes);
                            entry.setDate(System.currentTimeMillis());
                        }
                    });
            entry.setExists(true);

            return out;
        }
        else
            throw new IOException();
    }

    /**
     * Always returns <code>false</code>: random read access is not available for archive entries.
     */
    public boolean hasRandomAccessInputStream() {
        return false;
    }

    /**
     * Always throws an <code>IOException</code>: random read access is not available for archive entries.
     */
    public RandomAccessInputStream getRandomAccessInputStream() throws IOException {
        throw new IOException();
    }

    /**
     * Always returns <code>false</code>: random write access is not available for archive entries.
     */
    public boolean hasRandomAccessOutputStream() {
        return false;
    }

    /**
     * Always throws an <code>IOException</code>: random write access is not available for archive entries.
     */
    public RandomAccessOutputStream getRandomAccessOutputStream() throws IOException {
        throw new IOException();
    }

    /**
     * Returns the same ArchiveEntry instance as {@link #getEntry()}.
     */
    public Object getUnderlyingFileObject() {
        return entry;
    }

    
    ////////////////////////
    // Overridden methods //
    ////////////////////////

    /**
     * This method is overridden to return the separator of the {@link #getArchiveFile() archive file} that contains
     * this entry.
     *
     * @return the separator of the archive file that contains this entry
     */
    public String getSeparator() {
        return archiveFile.getSeparator();
    }

    /**
     * This method is overridden to use the archive file's absolute path as the base path of this entry file.
     */
    public String getAbsolutePath() {
        // Use the archive file's absolute path and append the entry's relative path to it
        return archiveFile.getAbsolutePath(true)+getRelativeEntryPath();
    }

    /**
     * This method is overridden to use the archive file's canonical path as the base path of this entry file.
     */
    public String getCanonicalPath() {
        // Use the archive file's canonical path and append the entry's relative path to it
        return archiveFile.getCanonicalPath(true)+getRelativeEntryPath();
    }

    /**
     * This method is overridden to return the archive's root folder.
     */
    public AbstractFile getRoot() {
        return archiveFile.getRoot();
    }

    /**
     * This method is overridden to blindly return <code>false</code>, an archive entry cannot be a root folder.
     *
     * @return <code>false</code>, always
     */
    public boolean isRoot() {
        return false;
    }

    /**
     * This method is overridden to return the archive's volume folder.
     */
    public AbstractFile getVolume() {
        return archiveFile.getVolume();
    }

    /**
     * Always returns <code>false</code> only if the archive file that contains this entry is not writable.
     */
    public boolean changePermissions(int permissions) {
        if(!(entry.exists() && archiveFile.isWritable()))
            return false;

        FilePermissions oldPermissions = entry.getPermissions();
        FilePermissions newPermissions = new SimpleFilePermissions(permissions, oldPermissions.getMask());
        entry.setPermissions(newPermissions);

        boolean success = updateEntryAttributes();
        if(!success)        // restore old permissions if attributes could not be updated
            entry.setPermissions(oldPermissions);

        return success;
    }

    public int getMoveToHint(AbstractFile destFile) {
        if(archiveFile.isWritable())
            return SHOULD_NOT_HINT;

        return MUST_NOT_HINT;
    }
}
