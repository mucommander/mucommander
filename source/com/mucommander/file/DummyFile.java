/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
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
import com.mucommander.process.AbstractProcess;

import java.io.IOException;
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
    public long getDate() {
        return 0;
    }

    /**
     * Implementation notes: always returns <code>false</code>.
     */
    public boolean canChangeDate() {
        return false;
    }

    /**
     * Implementation notes: always returns <code>false</code>.
     */
    public boolean changeDate(long lastModified) {
        return false;
    }

    /**
     * Implementation notes: always returns <code>-1</code>.
     */
    public long getSize() {
        return -1;
    }

    /**
     * Implementation notes: always returns <code>null</code>.
     */
    public AbstractFile getParent() throws IOException {
        return null;
    }

    /**
     * Implementation notes: no-op, does nothing with the specified parent.
     */
    public void setParent(AbstractFile parent) {
    }

    /**
     * Implementation notes: always returns <code>false</code>.
     */
    public boolean exists() {
        return false;
    }

    /**
     * Implementation notes: always returns <code>false</code>.
     */
    public boolean getPermission(int access, int permission) {
        return false;
    }

    /**
     * Implementation notes: always returns <code>false</code>.
     */
    public boolean setPermission(int access, int permission, boolean enabled) {
        return false;
    }

    /**
     * Implementation notes: always returns <code>false</code>.
     */
    public boolean canGetPermission(int access, int permission) {
        return false;
    }

    /**
     * Implementation notes: always returns <code>false</code>.
     */
    public boolean canSetPermission(int access, int permission) {
        return false;
    }

    /**
     * Implementation notes: always returns <code>null</code>.
     */
    public String getOwner() {
        return null;
    }

    /**
     * Implementation notes: always returns <code>false</code>.
     */
    public boolean canGetOwner() {
        return false;
    }

    /**
     * Implementation notes: always returns <code>null</code>.
     */
    public String getGroup() {
        return null;
    }

    /**
     * Implementation notes: always returns <code>false</code>.
     */
    public boolean canGetGroup() {
        return false;
    }

    /**
     * Implementation notes: always returns <code>false</code>.
     */
    public boolean isDirectory() {
        return false;
    }

    /**
     * Implementation notes: always returns <code>false</code>.
     */
    public boolean isSymlink() {
        return false;
    }

    /**
     * Implementation notes: always throws an exception.
     */
    public AbstractFile[] ls() throws IOException {
        throw new IOException();
    }

    /**
     * Implementation notes: always throws an exception.
     */
    public void mkdir() throws IOException {
        throw new IOException();
    }

    /**
     * Implementation notes: always throws an exception.
     */
    public InputStream getInputStream() throws IOException {
        throw new IOException();
    }

    /**
     * Implementation notes: always throws an exception.
     */
    public OutputStream getOutputStream(boolean append) throws IOException {
        throw new IOException();
    }

    /**
     * Implementation notes: always returns <code>false</code>.
     */
    public boolean hasRandomAccessInputStream() {
        return false;
    }

    /**
     * Implementation notes: always throws an exception.
     */
    public RandomAccessInputStream getRandomAccessInputStream() throws IOException {
        throw new IOException();
    }

    /**
     * Implementation notes: always returns <code>false</code>.
     */
    public boolean hasRandomAccessOutputStream() {
        return false;
    }

    /**
     * Implementation notes: always throws an exception.
     */
    public RandomAccessOutputStream getRandomAccessOutputStream() throws IOException {
        throw new IOException();
    }

    /**
     * Implementation notes: always throws an exception.
     */
    public void delete() throws IOException {
        throw new IOException();
    }

    /**
     * Implementation notes: always returns <code>-1</code>.
     */
    public long getFreeSpace() {
        return -1;
    }

    /**
     * Implementation notes: always returns <code>-1</code>.
     */
    public long getTotalSpace() {
        return -1;
    }

    /**
     * Implementation notes: always returns <code>null</code>.
     */
    public Object getUnderlyingFileObject() {
        return null;
    }

    /**
     * Implementation notes: always returns <code>false</code>.
     */
    public boolean canRunProcess() {
        return false;
    }

    /**
     * Implementation notes: always throws an exception.
     */
    public AbstractProcess runProcess(String[] tokens) throws IOException {
        throw new IOException();
    }
}
