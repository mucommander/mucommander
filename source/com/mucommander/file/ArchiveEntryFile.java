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

package com.mucommander.file;

import com.mucommander.file.filter.FileFilter;
import com.mucommander.file.filter.FilenameFilter;
import com.mucommander.io.ByteCounter;
import com.mucommander.io.CounterOutputStream;
import com.mucommander.io.RandomAccessInputStream;
import com.mucommander.io.RandomAccessOutputStream;

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

    /** True if this entry exists in the archive */
    protected boolean exists;


    /**
     * Creates a new ArchiveEntryFile.
     *
     * @param url the FileURL instance that represents this file's location
     * @param archiveFile the AbstractArchiveFile instance that contains this entry
     * @param entry the ArchiveEntry object that contains information about this entry
     * @param exists true if this entry exists in the archive
     */
    protected ArchiveEntryFile(FileURL url, AbstractArchiveFile archiveFile, ArchiveEntry entry, boolean exists) {
        super(url);
        this.archiveFile = archiveFile;
        this.entry = entry;
        this.exists = exists;
    }
	
	
    /**
     * Returns the ArchiveEntry instance that contains information about the archive entry (name, size, date, ...).
     *
     * @return the ArchiveEntry instance that contains information about the archive entry (name, size, date, ...)
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


    /////////////////////////////////
    // AbstractFile implementation //
    /////////////////////////////////

    public long getDate() {
        return entry.getDate();
    }

    /**
     * Always returns <code>false</code>: date of entries cannot be modified.
     */
    public boolean canChangeDate() {
        return false;
    }

    /**
     * Always returns <code>false</code>: date of entries cannot be modified.
     */
    public boolean changeDate(long lastModified) {
        return false;
    }

    public long getSize() {
        return entry.getSize();
    }
	
    public boolean isDirectory() {
        return entry.isDirectory();
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
        return exists;
    }
	
    public boolean getPermission(int access, int permission) {
        return (getPermissions() & (permission << (access*3))) != 0;
    }

    /**
     * Always returns <code>false</code>: permissions of entries cannot be changed.
     */
    public boolean setPermission(int access, int permission, boolean enabled) {
        return false;
    }

    public boolean canGetPermission(int access, int permission) {
        // Use entry's permissions mask
        return (entry.getPermissionsMask() & (permission << (access*3))) != 0;
    }

    /**
     * Always returns <code>false</code>: permissions of entries cannot be changed.
     */
    public boolean canSetPermission(int access, int permission) {
        return false;
    }

    /**
     * Always returns <code>false</code>.
     */
    public boolean isSymlink() {
        return false;
    }

    /**
     * Deletes this entry from the associated <code>AbstractArchiveFile</code> if it is writable (as reported by
     * {@link com.mucommander.file.AbstractArchiveFile#isWritableArchive()}).
     * Throws an <code>IOException</code> if it isn't, if this entry does not exist in the archive, or if an I/O error
     * occurred.
     *
     * @throws IOException if the associated archive file is not writable, if this entry does not exist in the archive,
     * or if an I/O error occurred
     */
    public void delete() throws IOException {
        if(exists && archiveFile.isWritableArchive()) {
            AbstractRWArchiveFile rwArchiveFile = (AbstractRWArchiveFile)archiveFile;

            // Delete the entry in the archive file
            rwArchiveFile.deleteEntry(entry);

            // Non-existing entries are considered as zero-length regular files
            entry.setDirectory(false);
            entry.setSize(0);
            exists = false;
        }
        else
            throw new IOException();
    }

    /**
     * Creates this entry as a directory in the associated <code>AbstractArchiveFile</code> if the archive is
     * writable (as reported by {@link com.mucommander.file.AbstractArchiveFile#isWritableArchive()}).
     * Throws an <code>IOException</code> if it isn't, if this entry already exists in the archive or if an I/O error
     * occurred.
     *
     * @throws IOException if the associated archive file is not writable, if this entry already exists in the archive,
     * or if an I/O error occurred
     */
    public void mkdir() throws IOException {
        if(!exists && archiveFile.isWritableArchive()) {
            AbstractRWArchiveFile rwArchivefile = (AbstractRWArchiveFile)archiveFile;
            // Update the ArchiveEntry
            entry.setDirectory(true);
            entry.setDate(System.currentTimeMillis());
            entry.setSize(0);

            // Add the entry to the archive file
            rwArchivefile.addEntry(entry);

            // The entry now exists
            exists = true;
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
     * Delegates to the archive file's {@link AbstractArchiveFile#getEntryInputStream(ArchiveEntry)}} method.
     */
    public InputStream getInputStream() throws IOException {
        return archiveFile.getEntryInputStream(entry);
    }

    /**
     * Returns an <code>OutputStream</code> that allows to write this entry's contents if the archive is
     * writable (as reported by {@link com.mucommander.file.AbstractArchiveFile#isWritableArchive()}).
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
        if(archiveFile.isWritableArchive()) {
            if(append)
                throw new IOException("Can't append to an existing archive entry");

            if(exists) {
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
                        }
                    });
            exists = true;

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

    /**
     * Always returns <code>false</code>: archive entries cannot run processes.
     */
    public boolean canRunProcess() {
        return false;
    }

    /**
     * Always throws an <code>IOException</code>: archive entries cannot run processes.
     */
    public com.mucommander.process.AbstractProcess runProcess(String[] tokens) throws IOException {
        throw new IOException();
    }

    
    ////////////////////////
    // Overridden methods //
    ////////////////////////

    public int getPermissions() {
        // Return entry's permissions mask
        return entry.getPermissions();
    }
    
    public String getSeparator() {
        return archiveFile.getSeparator();
    }

    public int getMoveToHint(AbstractFile destFile) {
        if(archiveFile.isWritableArchive())
            return SHOULD_NOT_HINT;

        return MUST_NOT_HINT;
    }
}
