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


package com.mucommander.commons.file.impl;

import com.mucommander.commons.file.*;
import com.mucommander.commons.file.filter.FileFilter;
import com.mucommander.commons.file.filter.FilenameFilter;
import com.mucommander.commons.io.FileTransferException;
import com.mucommander.commons.io.RandomAccessInputStream;
import com.mucommander.commons.io.RandomAccessOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * ProxyFile is an {@link AbstractFile} that acts as a proxy between the class that extends it
 * and the proxied <code>AbstractFile</code> instance specified to the constructor.
 * All <code>AbstractFile</code> public methods (abstract or not) are delegated to the proxied file.
 * The {@link #getProxiedFile()} method allows to retrieve the proxied file instance.
 *
 * <p>This class is useful for wrapper files, such as {@link com.mucommander.commons.file.AbstractArchiveFile archive files},
 * that provide additional functionalities over an existing <code>AbstractFile</code> instance (the proxied file).
 * By implementing/overriding every <code>AbstractFile</code> methods, <code>ProxyFile</code> ensures that
 * all <code>AbstractFile</code> methods can safely be used, even if they are overridden by the proxied
 * file instance's class.
 *
 * <p><b>Implementation note:</b> the <code>java.lang.reflect.Proxy</code> class can unfortunately not be
 * used as it only works with interfaces (not abstract class). There doesn't seem to be any dynamic way to
 * proxy method invocations, so any modifications made to {@link com.mucommander.commons.file.AbstractFile} must be also
 * reflected in <code>ProxyFile</code>.
 *
 * @see com.mucommander.commons.file.AbstractArchiveFile
 * @author Maxence Bernard
 */
public abstract class ProxyFile extends AbstractFile {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyFile.class);

    /** The proxied file instance */
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
     *
     * @return the <code>AbstractFile</code> instance proxied by this </code>ProxyFile</code>
     */
    public AbstractFile getProxiedFile() {
        return file;
    }


    /////////////////////////////////
    // AbstractFile implementation //
    /////////////////////////////////

    @Override
    public long getDate() {
        return file.getDate();
    }

    @Override
    public void changeDate(long lastModified) throws IOException, UnsupportedFileOperationException {
        file.changeDate(lastModified);
    }

    @Override
    public long getSize() {
        return file.getSize();
    }

    @Override
    public AbstractFile getParent() {
        return file.getParent();
    }

    @Override
    public void setParent(AbstractFile parent) {
        file.setParent(parent);
    }

    @Override
    public boolean exists() {
        return file.exists();
    }

    @Override
    public void changePermission(int access, int permission, boolean enabled) throws IOException, UnsupportedFileOperationException {
        file.changePermission(access, permission, enabled);
    }

    @Override
    public String getOwner() {
        return file.getOwner();
    }

    @Override
    public boolean canGetOwner() {
        return file.canGetOwner();
    }

    @Override
    public String getGroup() {
        return file.getGroup();
    }

    @Override
    public boolean canGetGroup() {
        return file.canGetGroup();
    }

    @Override
    public boolean isDirectory() {
        return file.isDirectory();
    }

    @Override
    public boolean isSymlink() {
        return file.isSymlink();
    }

    @Override
    public boolean isSystem() {
        return file.isSystem();
    }

    @Override
    public AbstractFile[] ls() throws IOException, UnsupportedFileOperationException {
        return file.ls();
    }

    @Override
    public void mkdir() throws IOException, UnsupportedFileOperationException {
        file.mkdir();
    }

    @Override
    public InputStream getInputStream() throws IOException, UnsupportedFileOperationException {
        return file.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException, UnsupportedFileOperationException {
        return file.getOutputStream();
    }

    @Override
    public OutputStream getAppendOutputStream() throws IOException, UnsupportedFileOperationException {
        return file.getAppendOutputStream();
    }

    @Override
    public RandomAccessInputStream getRandomAccessInputStream() throws IOException, UnsupportedFileOperationException {
        return file.getRandomAccessInputStream();
    }

    @Override
    public RandomAccessOutputStream getRandomAccessOutputStream() throws IOException, UnsupportedFileOperationException {
        return file.getRandomAccessOutputStream();
    }

    @Override
    public void delete() throws IOException, UnsupportedFileOperationException {
        file.delete();
    }

    @Override
    public void copyRemotelyTo(AbstractFile destFile) throws IOException, UnsupportedFileOperationException {
        file.copyRemotelyTo(destFile);
    }

    @Override
    public void renameTo(AbstractFile destFile) throws IOException, UnsupportedFileOperationException {
        file.renameTo(destFile);
    }

    @Override
    public long getFreeSpace() throws IOException, UnsupportedFileOperationException {
        return file.getFreeSpace();
    }

    @Override
    public long getTotalSpace() throws IOException, UnsupportedFileOperationException {
        return file.getTotalSpace();
    }

    @Override
    public Object getUnderlyingFileObject() {
        return file.getUnderlyingFileObject();
    }

    
    /////////////////////////////////////
    // Overridden AbstractFile methods //
    /////////////////////////////////////

    @Override
    public final boolean isFileOperationSupported(FileOperation op) {
        Class<? extends AbstractFile> thisClass = getClass();
        Method opMethod = op.getCorrespondingMethod(thisClass);
        // If the method corresponding to the file operation has been overridden by this class (a ProxyFile subclass),
        // check the presence of the UnsupportedFileOperation annotation in this class.
        try {
            if(!thisClass.getMethod(opMethod.getName(), opMethod.getParameterTypes()).getDeclaringClass().equals(ProxyFile.class))
                return AbstractFile.isFileOperationSupported(op, thisClass);
        }
        catch(Exception e) {
            // Should never happen, unless AbstractFile method signatures have changed and FileOperation has not been updated
            LOGGER.warn("Exception caught, this should not have happened", e);
        }

        // Otherwise, check for the presence of the UnsupportedFileOperation annotation in the wrapped AbstractFile.
        return file.isFileOperationSupported(op);
    }

    @Override
    public FileURL getURL() {
        return file.getURL();
    }

    @Override
    public URL getJavaNetURL() throws MalformedURLException {
        return file.getJavaNetURL();
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public String getExtension() {
        return file.getExtension();
    }

    @Override
    public String getBaseName() {
    	return file.getBaseName();
    }
    
    @Override
    public String getAbsolutePath() {
        return file.getAbsolutePath();
    }

    @Override
    public String getCanonicalPath() {
        return file.getCanonicalPath();
    }

    @Override
    public AbstractFile getCanonicalFile() {
        return file.getCanonicalFile();
    }

    @Override
    public String getSeparator() {
        return file.getSeparator();
    }

    @Override
    public boolean isArchive() {
        return file.isArchive();
    }

    @Override
    public boolean isHidden() {
        return file.isHidden();
    }

    @Override
    public FilePermissions getPermissions() {
        return file.getPermissions();
    }

    @Override
    public void changePermissions(int permissions) throws IOException, UnsupportedFileOperationException {
        file.changePermissions(permissions);
    }

    @Override
    public PermissionBits getChangeablePermissions() {
        return file.getChangeablePermissions();
    }

    @Override
    public String getPermissionsString() {
        return file.getPermissionsString();
    }

    @Override
    public AbstractFile getRoot() {
        return file.getRoot();
    }

    @Override
    public boolean isRoot() {
        return file.isRoot();
    }

    @Override
    public AbstractFile getVolume() {
        return file.getVolume();
    }

    @Override
    public InputStream getInputStream(long offset) throws IOException, UnsupportedFileOperationException {
        return file.getInputStream(offset);
    }

    @Override
    public void copyStream(InputStream in, boolean append, long length) throws FileTransferException {
        file.copyStream(in, append, length);
    }

    @Override
    public AbstractFile[] ls(FileFilter filter) throws IOException, UnsupportedFileOperationException {
        return file.ls(filter);
    }

    @Override
    public AbstractFile[] ls(FilenameFilter filter) throws IOException, UnsupportedFileOperationException {
        return file.ls(filter);
    }

    @Override
    public void mkfile() throws IOException, UnsupportedFileOperationException {
        file.mkfile();
    }

    @Override
    public void deleteRecursively() throws IOException, UnsupportedFileOperationException {
        file.deleteRecursively();
    }

    public boolean equals(Object f) {
        return file.equals(f);
    }

    @Override
    public boolean equalsCanonical(Object f) {
        return file.equalsCanonical(f);
    }

    public int hashCode() {
        return file.hashCode();
    }

    public String toString() {
        return file.toString();
    }
}
