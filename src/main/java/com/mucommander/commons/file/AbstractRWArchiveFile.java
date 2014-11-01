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

import java.io.IOException;
import java.io.OutputStream;

/**
 * <code>AbstractRWArchiveFile</code> represents a read-write archive file. This class is abstract and implemented by
 * all read-write archive files.
 * In addition to the read-only operations defined by {@link com.mucommander.commons.file.AbstractArchiveFile}, it provides
 * abstract methods for adding and deleting entries from the archive.
 *
 * The {@link #isWritable()} method impletemented by this class always returns <code>true</code>. However,
 * write operations may not always be available depending on the underlying file (e.g. if random file access is
 * required). In that case, {@link #isWritable ()} should be overridden to return <code>true</code> only when
 * write operations are available.
 *
 * @author Maxence Bernard
 */
public abstract class AbstractRWArchiveFile extends AbstractArchiveFile {

    /**
     * Creates an AbstractRWArchiveFile on top of the given file.
     *
     * @param file the file on top of which to create the archive
     */
    protected AbstractRWArchiveFile(AbstractFile file) {
        super(file);
    }


    ////////////////////////////////////////
    // AbstractArchiveFile implementation //
    ////////////////////////////////////////

    /**
     * Returns <code>true</code>: <code>AbstractRWArchiveFile</code> implementations are by definition capable of adding
     * or deleting entries. This method should be overridden if the implementation is capable of providing write access
     * only under certain conditions, for example if it requires random access to the proxied archive file which may not
     * always be available depending on the underlying file. If that is the case, this method should return
     * <code>true</code> only when all conditions for providing write operations are met.
     *
     * @return <code>true</code>, always
     */
    @Override
    public boolean isWritable() {
        return true;
    }


    //////////////////////
    // Abstract methods //
    //////////////////////

    /**
     * Adds the given entry to the archive and returns an <code>OutputStream</code> to write the entry's contents
     * if the entry is a regular file, <code>null</code> if the entry is a directory.
     * Throws an <code>IOException</code> if the entry already exists in the archive or if an I/O error occurs.
     *
     * @param entry the entry to add to the archive
     * @return an OutputStream to write the entry's contents if the entry is a regular file, null if the entry is a directory
     * @throws IOException if the entry already exists in the archive or if an I/O error occurs
     * @throws UnsupportedFileOperationException if {@link FileOperation#WRITE_FILE} operations are not supported by
     * the underlying file protocol.
     */
    public abstract OutputStream addEntry(ArchiveEntry entry) throws IOException, UnsupportedFileOperationException;

    /**
     * Deletes the specified entry from the archive. Throws an <code>IOException</code> if the entry doesn't exist
     * in the archive or if an I/O error occurs.
     *
     * @param entry the entry to delete from the archive
     * @throws IOException if the entry doesn't exist in the archive or if an I/O error occurs
     * @throws UnsupportedFileOperationException if {@link FileOperation#WRITE_FILE} operations are not supported by
     * the underlying file protocol.
     */
    public abstract void deleteEntry(ArchiveEntry entry) throws IOException, UnsupportedFileOperationException;

    /**
     * Updates the specified entry in the archive with the attributes containted in the {@link ArchiveEntry} object.
     * Throws an <code>IOException</code> if the entry doesn't exist in the archive or if an I/O error occurs.
     *
     * <p>This methods can be used to update the entry's date and permissions for instance.</p>
     *
     * @param entry the entry to update in the archive
     * @throws IOException if the entry doesn't exist in the archive or if an I/O error occurs
     * @throws UnsupportedFileOperationException if {@link FileOperation#WRITE_FILE} operations are not supported by
     * the underlying file protocol.
     */
    public abstract void updateEntry(ArchiveEntry entry) throws IOException, UnsupportedFileOperationException;

    /**
     * Processes the archive file to leave it in an optimal form. This method should be called after a writable archive
     * has been modified (entries added or removed).
     *
     * <p>The actual effect of this method on the archive file depends on the kind of archive. It may be implemented
     * as a no-op if there is no use for it.
     * To illustrate, in the case of a {@link com.mucommander.commons.file.impl.zip.ZipArchiveFile}, this method removes chunks
     * of free space that are left when entries are deleted.</p>
     *
     * @throws IOException if an I/O error occurs
     * @throws UnsupportedFileOperationException if {@link FileOperation#WRITE_FILE} operations are not supported by 
     * the underlying file protocol.
     */
    public abstract void optimizeArchive() throws IOException, UnsupportedFileOperationException;
}
