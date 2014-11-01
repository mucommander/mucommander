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

import com.mucommander.commons.file.filter.FileFilter;
import com.mucommander.commons.file.filter.FilenameFilter;
import com.mucommander.commons.io.ByteUtils;
import com.mucommander.commons.io.RandomAccessInputStream;
import com.mucommander.commons.io.RandomAccessOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * <code>AbstractArchiveEntryFile</code> represents a file entry inside an archive.
 * An <code>AbstractArchiveEntryFile</code> is always associated with an {@link ArchiveEntry} object which contains
 * information about the entry (name, size, date, ...) and with an {@link AbstractArchiveFile} which acts as an entry
 * repository and provides operations such as listing a directory entry's files, adding or removing entries
 * (if the archive is writable), etc...
 *
 * <p>
 * <code>AbstractArchiveEntryFile</code> implements {@link com.mucommander.commons.file.AbstractFile} by delegating methods to
 * the <code>ArchiveEntry</code> and <code>AbstractArchiveFile</code> instances.
 * <code>AbstractArchiveEntryFile</code> is agnostic to the actual archive format. In other words, there is no need to
 * extend this class for a particular archive format, <code>ArchiveEntry</code> and <code>AbstractArchiveFile</code>
 * provide a generic framework that isolates from the archive format's specifics.
 * </p>
 * <p>
 * This class is abstract (as the name implies) and implemented by two subclasses:
 * <ul>
 *   <li>{@link ROArchiveEntryFile}: represents an entry inside a read-only archive</li>
 *   <li>{@link RWArchiveEntryFile}: represents an entry inside a {@link AbstractArchiveFile#isWritable() read-write} archive</li>
 * </ul>
 * </p>
 *
 * @see AbstractArchiveFile
 * @see ArchiveEntry
 * @author Maxence Bernard
 */
public abstract class AbstractArchiveEntryFile extends AbstractFile {

    /** The archive file that contains this entry */
    protected AbstractArchiveFile archiveFile;

    /** This entry file's parent, can be the archive file itself if this entry is located at the top level */
    protected AbstractFile parent;

    /** The ArchiveEntry object that contains information about this entry */
    protected ArchiveEntry entry;


    /**
     * Creates a new AbstractArchiveEntryFile.
     *
     * @param url the FileURL instance that represents this file's location
     * @param archiveFile the AbstractArchiveFile instance that contains this entry
     * @param entry the ArchiveEntry object that contains information about this entry
     */
    protected AbstractArchiveEntryFile(FileURL url, AbstractArchiveFile archiveFile, ArchiveEntry entry) {
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
            path = path.replace("/", separator);

        return path;
    }


    /////////////////////////////////
    // AbstractFile implementation //
    /////////////////////////////////

    @Override
    public long getDate() {
        return entry.getDate();
    }

    @Override
    public long getSize() {
        return entry.getSize();
    }
	
    @Override
    public boolean isDirectory() {
        return entry.isDirectory();
    }

    @Override
    public boolean isArchive() {
        // Archive entries files may be wrapped by archive files but they are not archive files per se
        return false;
    }

    @Override
    public AbstractFile[] ls() throws IOException, UnsupportedFileOperationException {
        return archiveFile.ls(this, null, null);
    }

    @Override
    public AbstractFile[] ls(FilenameFilter filter) throws IOException, UnsupportedFileOperationException {
        return archiveFile.ls(this, filter, null);
    }
	
    @Override
    public AbstractFile[] ls(FileFilter filter) throws IOException, UnsupportedFileOperationException {
        return archiveFile.ls(this, null, filter);
    }

    @Override
    public AbstractFile getParent() {
        return parent;
    }
	
    @Override
    public void setParent(AbstractFile parent) {
        this.parent = parent;	
    }

    /**
     * Returns <code>true</code> if this entry exists within the archive file.
     *
     * @return true if this entry exists within the archive file
     */
    @Override
    public boolean exists() {
        return entry.exists();
    }
	
    @Override
    public FilePermissions getPermissions() {
        // Return the entry's permissions
        return entry.getPermissions();
    }

    @Override
    public void changePermission(int access, int permission, boolean enabled) throws IOException, UnsupportedFileOperationException {
        changePermissions(ByteUtils.setBit(getPermissions().getIntValue(), (permission << (access*3)), enabled));
    }

    @Override
    public String getOwner() {
        return entry.getOwner();
    }

    @Override
    public boolean canGetOwner() {
        return entry.getOwner()!=null;
    }

    @Override
    public String getGroup() {
        return entry.getGroup();
    }

    @Override
    public boolean canGetGroup() {
        return entry.getGroup()!=null;
    }

    /**
     * Always returns <code>false</code>.
     */
    @Override
    public boolean isSymlink() {
        return false;
    }

    /**
     * Always returns <code>false</code>.
     */
    @Override
    public boolean isSystem() {
        return false;
    }

    /**
     * Delegates to the archive file's {@link AbstractArchiveFile#getFreeSpace()} method.
     *
     * @throws IOException if an I/O error occurred
     * @throws UnsupportedFileOperationException if the underlying archive file does not support
     * {@link FileOperation#GET_FREE_SPACE} operations.
     */
    @Override
    public long getFreeSpace() throws IOException, UnsupportedFileOperationException {
        return archiveFile.getFreeSpace();
    }

    /**
     * Delegates to the archive file's {@link AbstractArchiveFile#getTotalSpace()} method.
     *
     * @throws IOException if an I/O error occurred
     * @throws UnsupportedFileOperationException if the underlying archive file does not support
     * {@link FileOperation#GET_TOTAL_SPACE} operations.
     */
    @Override
    public long getTotalSpace() throws IOException, UnsupportedFileOperationException {
        return archiveFile.getTotalSpace();
    }

    /**
     * Delegates to the archive file's {@link AbstractArchiveFile#getEntryInputStream(ArchiveEntry,ArchiveEntryIterator)}}
     * method.
     *
     * @throws UnsupportedFileOperationException if the underlying archive file does not support
     * {@link FileOperation#READ_FILE} operations.
     */
    @Override
    public InputStream getInputStream() throws IOException, UnsupportedFileOperationException {
        return archiveFile.getEntryInputStream(entry, null);
    }

    /**
     * Always throws an {@link UnsupportedFileOperationException}: append is not available for archive entries.
     *
     * @throws UnsupportedFileOperationException always
     */
    @Override
    @UnsupportedFileOperation
    public OutputStream getAppendOutputStream() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.APPEND_FILE);
    }

    /**
     * Always throws an {@link UnsupportedFileOperationException}: random read access is not available for archive
     * entries.
     *
     * @throws UnsupportedFileOperationException always
     */
    @Override
    @UnsupportedFileOperation
    public RandomAccessInputStream getRandomAccessInputStream() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.RANDOM_READ_FILE);
    }

    /**
     * Always throws an {@link UnsupportedFileOperationException}: random write access is not available for archive
     * entries.
     *
     * @throws UnsupportedFileOperationException always
     */
    @Override
    @UnsupportedFileOperation
    public RandomAccessOutputStream getRandomAccessOutputStream() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.RANDOM_WRITE_FILE);
    }

    /**
     * Always throws an {@link UnsupportedFileOperationException} when called.
     *
     * @throws UnsupportedFileOperationException always
     */
    @Override
    @UnsupportedFileOperation
    public void copyRemotelyTo(AbstractFile destFile) throws UnsupportedFileOperationException {
        // TODO: we could consider adding remote copy support to RWArchiveEntryFile
        throw new UnsupportedFileOperationException(FileOperation.COPY_REMOTELY);
    }

    /**
     * Always throws an {@link UnsupportedFileOperationException} when called.
     *
     * @throws UnsupportedFileOperationException always
     */
    @Override
    @UnsupportedFileOperation
    public void renameTo(AbstractFile destFile) throws UnsupportedFileOperationException {
        // TODO: we could consider adding renaming support to RWArchiveEntryFile
        throw new UnsupportedFileOperationException(FileOperation.RENAME);
    }

    /**
     * Returns the same ArchiveEntry instance as {@link #getEntry()}.
     */
    @Override
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
    @Override
    public String getSeparator() {
        return archiveFile.getSeparator();
    }

    /**
     * This method is overridden to use the archive file's absolute path as the base path of this entry file.
     */
    @Override
    public String getAbsolutePath() {
        // Use the archive file's absolute path and append the entry's relative path to it
        return archiveFile.getAbsolutePath(true)+getRelativeEntryPath();
    }

    /**
     * This method is overridden to use the archive file's canonical path as the base path of this entry file.
     */
    @Override
    public String getCanonicalPath() {
        // Use the archive file's canonical path and append the entry's relative path to it
        return archiveFile.getCanonicalPath(true)+getRelativeEntryPath();
    }

    /**
     * This method is overridden to return the archive's root folder.
     */
    @Override
    public AbstractFile getRoot() {
        return archiveFile.getRoot();
    }

    /**
     * This method is overridden to blindly return <code>false</code>, an archive entry cannot be a root folder.
     *
     * @return <code>false</code>, always
     */
    @Override
    public boolean isRoot() {
        return false;
    }

    /**
     * This method is overridden to return the archive's volume folder.
     */
    @Override
    public AbstractFile getVolume() {
        return archiveFile.getVolume();
    }
}
