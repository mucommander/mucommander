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

package com.mucommander.file.impl;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileURL;
import com.mucommander.file.filter.FileFilter;
import com.mucommander.file.filter.FilenameFilter;
import com.mucommander.io.FileTransferException;
import com.mucommander.io.RandomAccessInputStream;
import com.mucommander.io.RandomAccessOutputStream;
import com.mucommander.process.AbstractProcess;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * ProxyFile is an {@link AbstractFile} that acts as a proxy between the class that extends it
 * and the proxied <code>AbstractFile</code> instance specified to the constructor.
 * All <code>AbstractFile</code> methods (abstract of not) are proxied and delegated to the proxied file.
 * The {@link #getProxiedFile()} method allows to retrieve the proxied file.
 *
 * <p>This class is useful for wrapper files, such as {@link com.mucommander.file.AbstractArchiveFile archive files}, that need
 * an existing <code>AbstractFile</code> instance (the proxied file) to provide additional functionalities.
 * By overriding/implementing every <code>AbstractFile</code> methods, <code>ProxyFile</code> ensures that
 * all <code>AbstractFile</code> methods can be safely used, even if they are overridden by the proxied
 * instance's class.
 *
 * <p><b>Implementation note:</b> the <code>java.lang.reflect.Proxy</code> class can unfortunately not be
 * used as it only works with interfaces (not abstract class). There doesn't seem to be any dynamic way to
 * proxy method invocations, so any modifications made to {@link com.mucommander.file.AbstractFile} must be also
 * reflected in <code>ProxyFile</code>.
 *
 * @see com.mucommander.file.AbstractArchiveFile
 * @author Maxence Bernard
 */
public abstract class ProxyFile extends AbstractFile {

    /** Proxied file */
    protected AbstractFile file;


    /**
     * Creates a new ProxyFile using the given file to delegate AbstractFile method calls to.
     *
     * @param file the file to be proxied
     */
    public ProxyFile(AbstractFile file) {
        super(file.getURL());
        this.file = file;
    }


    /**
     * Returns the <code>AbstractFile</code> instance proxied by this </code>ProxyFile</code>.
     */
    public AbstractFile getProxiedFile() {
        return file;
    }


    /////////////////////////////////
    // AbstractFile implementation //
    /////////////////////////////////

    public long getDate() {
        return file.getDate();
    }

    public boolean changeDate(long lastModified) {
        return file.changeDate(lastModified);
    }

    public long getSize() {
        return file.getSize();
    }

    public AbstractFile getParent() {
        return file.getParent();
    }

    public void setParent(AbstractFile parent) {
        file.setParent(parent);
    }

    public boolean exists() {
        return file.exists();
    }

    public boolean getPermission(int access, int permission) {
        return file.getPermission(access, permission);
    }

    public boolean setPermission(int access, int permission, boolean enabled) {
        return file.setPermission(access, permission, enabled);
    }

    public boolean canGetPermission(int access, int permission) {
        return file.canGetPermission(access, permission);
    }

    public boolean canSetPermission(int access, int permission) {
        return file.canSetPermission(access, permission);
    }

    public boolean isDirectory() {
        return file.isDirectory();
    }

    public boolean isSymlink() {
        return file.isSymlink();
    }

    public AbstractFile[] ls() throws IOException {
        return file.ls();
    }

    public void mkdir() throws IOException {
        file.mkdir();
    }

    public InputStream getInputStream() throws IOException {
        return file.getInputStream();
    }

    public OutputStream getOutputStream(boolean append) throws IOException {
        return file.getOutputStream(append);
    }

    public boolean hasRandomAccessInputStream() {
        return file.hasRandomAccessInputStream();
    }

    public RandomAccessInputStream getRandomAccessInputStream() throws IOException {
        return file.getRandomAccessInputStream();
    }

    public boolean hasRandomAccessOutputStream() {
        return file.hasRandomAccessOutputStream();
    }

    public RandomAccessOutputStream getRandomAccessOutputStream() throws IOException {
        return file.getRandomAccessOutputStream();
    }

    public void delete() throws IOException {
        file.delete();
    }

    public long getFreeSpace() {
        return file.getFreeSpace();
    }

    public long getTotalSpace() {
        return file.getTotalSpace();
    }

    public Object getUnderlyingFileObject() {
        return file.getUnderlyingFileObject();
    }

    public boolean canRunProcess() {
        return file.canRunProcess();
    }

    public AbstractProcess runProcess(String[] tokens) throws IOException {
        return file.runProcess(tokens);
    }

    
    ////////////////////////
    // Overridden methods //
    ////////////////////////

    public FileURL getURL() {
        return file.getURL();
    }

    public String getName() {
        return file.getName();
    }

    public String getExtension() {
        return file.getExtension();
    }

    public String getAbsolutePath() {
        return file.getAbsolutePath();
    }

    public String getCanonicalPath() {
        return file.getCanonicalPath();
    }

    public String getSeparator() {
        return file.getSeparator();
    }

    public boolean isParentOf(AbstractFile f) {
        return file.isParentOf(f);
    }

    public boolean isBrowsable() {
        return file.isBrowsable();
    }

    public boolean isHidden() {
        return file.isHidden();
    }

    public int getPermissions() {
        return file.getPermissions();
    }

    public boolean setPermissions(int permissions) {
        return file.setPermissions(permissions);
    }

    public int getPermissionGetMask() {
        return file.getPermissionGetMask();
    }

    public int getPermissionSetMask() {
        return file.getPermissionSetMask();
    }

    public String getPermissionsString() {
        return file.getPermissionsString();
    }

    public AbstractFile getRoot() {
        return file.getRoot();
    }

    public boolean isRoot() {
        return file.isRoot();
    }

    public InputStream getInputStream(long offset) throws IOException {
        return file.getInputStream(offset);
    }

    public void copyStream(InputStream in, boolean append) throws FileTransferException {
        file.copyStream(in, append);
    }

    public boolean copyTo(AbstractFile destFile) throws FileTransferException {
        return file.copyTo(destFile);
    }

    public int getCopyToHint(AbstractFile destFile) {
        return file.getCopyToHint(destFile);
    }

    public boolean moveTo(AbstractFile destFile) throws FileTransferException {
        return file.moveTo(destFile);
    }

    public int getMoveToHint(AbstractFile destFile) {
        return file.getMoveToHint(destFile);
    }

    public AbstractFile[] ls(FileFilter filter) throws IOException {
        return file.ls(filter);
    }

    public AbstractFile[] ls(FilenameFilter filter) throws IOException {
        return file.ls(filter);
    }

    public void mkfile() throws IOException {
        file.mkfile();
    }

    public boolean equals(Object f) {
        return file.equals(f);
    }

    public String toString() {
        return file.toString();
    }
}
