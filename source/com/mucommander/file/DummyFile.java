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

import com.mucommander.io.RandomAccessInputStream;
import com.mucommander.io.RandomAccessOutputStream;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * This class is an implementation of <code>AbstractFile</code> which implements all methods as no-op (that do nothing)
 * that return default values. It makes it easy to quickly create a <code>AbstractFile</code> implementation by simply
 * overridding the methods that are needed, for example as an anonymous class inside a method.
 *
 * <p>This class should NOT be subclassed for proper AbstractFile implementations. It should only be used in certain
 * circumstances that require creating a quick AbstractFile implementation where only a few methods will be used.</p>
 *
 * @author Maxence Bernard
 */
public class DummyFile extends AbstractFile {

    public DummyFile(FileURL url) {
        super(url);
    }


    /////////////////////////////////
    // AbstractFile implementation //
    /////////////////////////////////

    /**
     * Implementation notes: always returns <code>0</code>.
     */
    @Override
    public long getDate() {
        return 0;
    }

    /**
     * Implementation notes: always returns <code>false</code>.
     */
    @Override
    public boolean canChangeDate() {
        return false;
    }

    /**
     * Implementation notes: always returns <code>false</code>.
     */
    @Override
    public boolean changeDate(long lastModified) {
        return false;
    }

    /**
     * Implementation notes: always returns <code>-1</code>.
     */
    @Override
    public long getSize() {
        return -1;
    }

    /**
     * Implementation notes: always returns <code>null</code>.
     */
    @Override
    public AbstractFile getParent() {
        return null;
    }

    /**
     * Implementation notes: no-op, does nothing with the specified parent.
     */
    @Override
    public void setParent(AbstractFile parent) {
    }

    /**
     * Implementation notes: always returns <code>false</code>.
     */
    @Override
    public boolean exists() {
        return false;
    }

    /**
     * Implementation notes: always returns {@link FilePermissions#EMPTY_FILE_PERMISSIONS}.
     */
    @Override
    public FilePermissions getPermissions() {
        return FilePermissions.EMPTY_FILE_PERMISSIONS;
    }

    /**
     * Implementation notes: returns {@link PermissionBits#EMPTY_PERMISSION_BITS}, none of the permission bits can be
     * changed.
     */
    @Override
    public PermissionBits getChangeablePermissions() {
        return PermissionBits.EMPTY_PERMISSION_BITS;
    }

    /**
     * Implementation notes: always returns <code>false</code>.
     */
    @Override
    public boolean changePermission(int access, int permission, boolean enabled) {
        return false;
    }

    /**
     * Implementation notes: always returns <code>null</code>.
     */
    @Override
    public String getOwner() {
        return null;
    }

    /**
     * Implementation notes: always returns <code>false</code>.
     */
    @Override
    public boolean canGetOwner() {
        return false;
    }

    /**
     * Implementation notes: always returns <code>null</code>.
     */
    @Override
    public String getGroup() {
        return null;
    }

    /**
     * Implementation notes: always returns <code>false</code>.
     */
    @Override
    public boolean canGetGroup() {
        return false;
    }

    /**
     * Implementation notes: always returns <code>false</code>.
     */
    @Override
    public boolean isDirectory() {
        return false;
    }

    /**
     * Implementation notes: always returns <code>false</code>.
     */
    @Override
    public boolean isArchive() {
        return false;
    }

    /**
     * Implementation notes: always returns <code>false</code>.
     */
    @Override
    public boolean isSymlink() {
        return false;
    }

    /**
     * Implementation notes: always throws an {@link UnsupportedFileOperationException}.
     *
     * @throws UnsupportedFileOperationException always
     */
    @Override
    public AbstractFile[] ls() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException();
    }

    /**
     * Implementation notes: always throws an {@link UnsupportedFileOperationException}.
     *
     * @throws UnsupportedFileOperationException always
     */
    @Override
    public void mkdir() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException();
    }

    /**
     * Implementation notes: always throws an {@link UnsupportedFileOperationException}.
     *
     * @throws UnsupportedFileOperationException always
     */
    @Override
    public InputStream getInputStream() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException();
    }

    /**
     * Implementation notes: always throws an {@link UnsupportedFileOperationException}.
     *
     * @throws UnsupportedFileOperationException always
     */
    @Override
    public OutputStream getOutputStream(boolean append) throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException();
    }

    /**
     * Implementation notes: always returns <code>false</code>.
     */
    @Override
    public boolean hasRandomAccessInputStream() {
        return false;
    }

    /**
     * Implementation notes: always throws an {@link UnsupportedFileOperationException}.
     *
     * @throws UnsupportedFileOperationException always
     */
    @Override
    public RandomAccessInputStream getRandomAccessInputStream() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException();
    }

    /**
     * Implementation notes: always returns <code>false</code>.
     */
    @Override
    public boolean hasRandomAccessOutputStream() {
        return false;
    }

    /**
     * Implementation notes: always throws an {@link UnsupportedFileOperationException}.
     *
     * @throws UnsupportedFileOperationException always
     */
    @Override
    public RandomAccessOutputStream getRandomAccessOutputStream() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException();
    }

    /**
     * Implementation notes: always throws an {@link UnsupportedFileOperationException}.
     *
     * @throws UnsupportedFileOperationException always
     */
    @Override
    public void delete() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException();
    }

    /**
     * Implementation notes: always returns <code>-1</code>.
     */
    @Override
    public long getFreeSpace() {
        return -1;
    }

    /**
     * Implementation notes: always returns <code>-1</code>.
     */
    @Override
    public long getTotalSpace() {
        return -1;
    }

    /**
     * Implementation notes: always returns <code>null</code>.
     */
    @Override
    public Object getUnderlyingFileObject() {
        return null;
    }
}
