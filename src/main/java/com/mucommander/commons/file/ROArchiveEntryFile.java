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

import java.io.OutputStream;

/**
 * Represents a file entry inside a read-only archive. Read-only archives are characterized by
 * {@link AbstractArchiveFile#isWritable()} returning <code>false</code>.
 *
 * @see AbstractArchiveFile
 * @see RWArchiveEntryFile
 * @author Maxence Bernard
 */
public class ROArchiveEntryFile extends AbstractArchiveEntryFile {

    protected ROArchiveEntryFile(FileURL url, AbstractArchiveFile archiveFile, ArchiveEntry entry) {
        super(url, archiveFile, entry);
    }


    /////////////////////////////////
    // AbstractFile implementation //
    /////////////////////////////////

    /**
     * Always throws {@link UnsupportedFileOperationException} when called.
     *
     * @throws UnsupportedFileOperationException always
     */
    @Override
    @UnsupportedFileOperation
    public void changeDate(long lastModified) throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.CHANGE_DATE);
    }

    /**
     * Always return {@link PermissionBits#EMPTY_PERMISSION_BITS}.
     */
    @Override
    public PermissionBits getChangeablePermissions() {
        return PermissionBits.EMPTY_PERMISSION_BITS;
    }

    /**
     * Always throws {@link UnsupportedFileOperationException} when called.
     */
    @Override
    @UnsupportedFileOperation
    public void delete() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.DELETE);
    }

    /**
     * Always throws {@link UnsupportedFileOperationException} when called.
     */
    @Override
    @UnsupportedFileOperation
    public void mkdir() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.CREATE_DIRECTORY);
    }

    /**
     * Always throws {@link UnsupportedFileOperationException} when called.
     */
    @Override
    @UnsupportedFileOperation
    public OutputStream getOutputStream() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.WRITE_FILE);
    }

    /**
     * Always throws {@link UnsupportedFileOperationException} when called.
     */
    @Override
    @UnsupportedFileOperation
    public void changePermissions(int permissions) throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.CHANGE_PERMISSION);
    }

    /**
     * Always throws {@link UnsupportedFileOperationException} when called.
     */
    @Override
    @UnsupportedFileOperation
    public void changePermission(int access, int permission, boolean enabled) throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.CHANGE_PERMISSION);
    }

    /**
     * Always returns <code>0</code>.
     */
    @Override
    public long getFreeSpace() {
        return 0;
    }
}
