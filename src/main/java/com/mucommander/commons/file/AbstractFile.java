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

import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.swing.Icon;

import com.mucommander.commons.file.compat.CompatURLStreamHandler;
import com.mucommander.commons.file.filter.FileFilter;
import com.mucommander.commons.file.filter.FilenameFilter;
import com.mucommander.commons.file.impl.ProxyFile;
import com.mucommander.commons.io.BufferPool;
import com.mucommander.commons.io.ChecksumInputStream;
import com.mucommander.commons.io.FileTransferException;
import com.mucommander.commons.io.RandomAccessInputStream;
import com.mucommander.commons.io.RandomAccessOutputStream;
import com.mucommander.commons.io.StreamUtils;

/**
 * <code>AbstractFile</code> is the superclass of all files.
 *
 * <p>AbstractFile classes should never be instantiated directly. Instead, the {@link FileFactory} <code>getFile</code>
 * methods should be used to get a file instance from a path or {@link FileURL} location.</p>
 *
 * @see com.mucommander.commons.file.FileFactory
 * @see com.mucommander.commons.file.impl.ProxyFile
 * @author Maxence Bernard
 */
public abstract class AbstractFile implements FileAttributes, PermissionTypes, PermissionAccesses {

    /** URL representing this file */
    protected FileURL fileURL;

    /** Default path separator */
    public final static String DEFAULT_SEPARATOR = "/";

    /** Size of the read/write buffer */
    // Note: raising buffer size from 8192 to 65536 makes a huge difference in SFTP read transfer rates but beyond
    // 65536, no more gain (not sure why).
    public final static int IO_BUFFER_SIZE = 65536;


    /**
     * Creates a new file instance with the given URL.
     *
     * @param url the FileURL instance that represents this file's location
     */
    protected AbstractFile(FileURL url) {
        this.fileURL = url;
    }



    /////////////////////////
    // Overridable methods //
    /////////////////////////

    /**
     * Returns the {@link FileURL} instance that represents this file's location.
     *
     * @return the FileURL instance that represents this file's location
     */
    public FileURL getURL() {
        return fileURL;
    }


    /**
     * Creates and returns a <code>java.net.URL</code> referring to the same location as the {@link FileURL} associated
     * with this <code>AbstractFile</code>.
     * The <code>java.net.URL</code> is created from the string representation of this file's <code>FileURL</code>.
     * Thus, any credentials this <code>FileURL</code> contains are preserved, but properties are lost.
     *
     * <p>The returned <code>URL</code> uses this {@link AbstractFile} to access the associated resource, via the
     * underlying <code>URLConnection</code> which delegates to this class.</p>
     *
     * <p>It is important to note that this method is provided for interoperability purposes, for the sole purpose of
     * connecting to APIs that require a <code>java.net.URL</code>.</p>
     *
     * @return a <code>java.net.URL</code> referring to the same location as this <code>FileURL</code>
     * @throws java.net.MalformedURLException if the java.net.URL could not parse the location of this FileURL
     */
    public URL getJavaNetURL() throws MalformedURLException {
        return new URL(null, getURL().toString(true), new CompatURLStreamHandler(this));
    }


    /**
     * Returns this file's name.
     *
     * <p>The returned name is the filename extracted from this file's <code>FileURL</code>
     * as returned by {@link FileURL#getFilename()}. If the filename is <code>null</code> (e.g. http://google.com), the
     * <code>FileURL</code>'s host will be returned instead. If the host is <code>null</code> (e.g. smb://), an empty
     * String will be returned. Thus, the returned name will never be <code>null</code>.</p>
     *
     * <p>This method should be overridden if a special processing (e.g. URL-decoding) needs to be applied to the
     * returned filename.</p>
     *
     * @return this file's name
     */
    public String getName() {
        String name = fileURL.getFilename();
        // If filename is null, use host instead
        if(name==null) {
            name = fileURL.getHost();
            // If host is null, return an empty string
            if(name==null)
                return "";
        }

        return name;
    }


    /**
     * Returns this file's extension, <code>null</code> if this file's name doesn't have an extension.
     *
     * <p>A filename has an extension if and only if:<br/>
     * - it contains at least one <code>.</code> character<br/>
     * - the last <code>.</code> is not the last character of the filename<br/>
     * - the last <code>.</code> is not the first character of the filename</p>
     *
     * @return this file's extension, <code>null</code> if this file's name doesn't have an extension
     */
    public String getExtension() {
        return getExtension(getName());
    }

    
    /**
     * Returns the absolute path to this file:
     * <ul>
     * <li>For local filesystems, the local file's path should be returned, and <b>not</b> a full URL with the scheme
     * and host parts (e.g. /path/to/file, not file://localhost/path/to/file)</li>
     * <li>For any other filesystems, the full URL including the protocol and host parts should be returned
     * (e.g. smb://192.168.1.1/root/blah)</li>
     * </ul>
     * <p>
     * This default implementation returns the string representation of this file's {@link #getURL() url}, without
     * the login and password parts. File implementations overridding this method should always return a path free of
     * any login and password, so that it can safely be displayed to the end user or stored, without risking to
     * compromise sensitive information.
     * </p>
     *
     * @return the absolute path to this file
     */
    public String getAbsolutePath() {
        return getURL().toString(false);
    }


    /**
     * Returns the canonical path to this file, resolving any symbolic links or '..' and '.' occurrences.
     *
     * <p>This implementation simply returns the value of {@link #getAbsolutePath()}, and thus should be overridden
     * if canonical path resolution is available.</p>
     *
     * @return the canonical path to this file
     */
    public String getCanonicalPath() {
        return getAbsolutePath();
    }

    /**
     * Returns an <code>AbstractFile</code> representing the canonical path of this file, or <code>this</code> if the
     * absolute and canonical path of this file are identical.<br/>
     * Note that the returned file may or may not exist, for example if this file is a symlink to a file that doesn't 
     * exist.
     *
     * @return an <code>AbstractFile representing the canonical path of this file, or this if the absolute and canonical
     * path of this file are identical.
     */
    public AbstractFile getCanonicalFile() {
        String canonicalPath = getCanonicalPath(false);
        if(canonicalPath.equals(getAbsolutePath(false)))
            return this;

        try {
            FileURL canonicalURL = FileURL.getFileURL(canonicalPath);
            canonicalURL.setCredentials(fileURL.getCredentials());

            return FileFactory.getFile(canonicalURL);
        }
        catch(IOException e) {
            return this;
        }
    }


    /**
     * Returns the path separator used by this file.
     *
     * <p>This default implementation returns the default separator "/", this method should be overridden if the path
     * separator used by the file implementation is different.</p>
     *
     * @return the path separator used by this file
     */
    public String getSeparator() {
        return DEFAULT_SEPARATOR;
    }


    /**
     * Returns <code>true</code> if this file is hidden.
     *
     * <p>This default implementation is solely based on the filename and returns <code>true</code> if this
     * file's name starts with '.'. This method should be overriden if the underlying filesystem has a notion 
     * of hidden files.</p>
     *
     * @return true if this file is hidden
     */	
    public boolean isHidden() {
        return getName().startsWith(".");
    }


    /**
     * Returns the root folder of this file, i.e. the top-level parent folder that has no parent folder. The returned
     * folder necessarily contains this file, directly or indirectly. If this file already is a root folder, the same
     * file will be returned.
     * <p>
     * This default implementation returns the file whose URL has the same scheme as this one, same credentials (if any),
     * and a path equal to <code>/</code>.
     * </p>
     *
     * @return the root folder that contains this file
     */
    public AbstractFile getRoot() {
        FileURL rootURL = (FileURL)getURL().clone();
        rootURL.setPath("/");

        return FileFactory.getFile(rootURL);
    }

    /**
     * Returns <code>true</code> if this file is a root folder.
     * <p>
     * This default implementation returns <code>true</code> if this file's URL path is <code>/</code>.
     * </p>
     *
     * @return <code>true</code> if this file is a root folder
     */
    public boolean isRoot() {
        return getURL().getPath().equals("/");
    }

    /**
     * Returns the volume on which this file is located, or <code>this</code> if this file is itself a volume.
     * The returned file may never be <code>null</code>. Furthermore, the returned file may not always
     * {@link #exists() exist}, for instance if the returned volume corresponds to a removable drive that's currently
     * unavailable. If the returned file does exist, it must always be a {@link #isDirectory() directory}.
     * In other words, archive files may not be considered as volumes.
     * <p>
     * The notion of volume may or may not have a meaning depending on the kind of fileystem. On local filesystems,
     * the notion of volume can be assimilated into that of <i>mount point</i> for UNIX-based OSes, or <i>drive</i>
     * for the Windows platform. Volumes may also have a meaning for certain network filesystems such as SMB, for which
     * shares can be considered as volumes. Filesystems that don't have a notion of volume should return the
     * {@link #getRoot() root folder}.
     * </p>
     * <p>
     * This default implementation returns this file's {@link #getRoot() root folder}. This method should be overridden
     * if this is not adequate.
     * </p>
     *
     * @return the volume on which this file is located.
     */
    public AbstractFile getVolume() {
        return getRoot();
    }

    /**
     * Returns an <code>InputStream</code> to read this file's contents, starting at the specified offset (in bytes).
     * A <code>java.io.IOException</code> is thrown if the file doesn't exist.
     *
     * <p>This implementation starts by checking whether the {@link FileOperation#RANDOM_READ_FILE} operation is
     * supported or not.
     * If it is, a {@link #getRandomAccessInputStream() random input stream} to this file is retrieved and used to seek
     * to the specified offset. If it's not, a regular {@link #getInputStream() input stream} is retrieved, and
     * {@link java.io.InputStream#skip(long)} is used to position the stream to the specified offset, which on most
     * <code>InputStream</code> implementations is very slow as it causes the bytes to be read and discarded.
     * For this reason, file implementations that do not provide random read access may want to override this method
     * if a more efficient implementation can be provided.</p>
     *
     * @param offset the offset in bytes from the beginning of the file, must be >0
     * @throws IOException if this file cannot be read or is a folder.
     * @throws UnsupportedFileOperationException if this method relies on a file operation that is not supported
     * or not implemented by the underlying filesystem.
     * @return an <code>InputStream</code> to read this file's contents, skipping the specified number of bytes
     */
    public InputStream getInputStream(long offset) throws IOException, UnsupportedFileOperationException {
        // Use a random access input stream when available
        if(isFileOperationSupported(FileOperation.RANDOM_READ_FILE)) {
            RandomAccessInputStream rais = getRandomAccessInputStream();
            rais.seek(offset);

            return rais;
        }

        InputStream in = getInputStream();

        // Skip exactly the specified number of bytes
        StreamUtils.skipFully(in, offset);

        return in;
    }
	

    /**
     * Copies the contents of the given <code>InputStream</code> to this file, appending or overwriting the file
     * if it exists. It is noteworthy that the provided <code>InputStream</code> will <b>not</b> be closed by this method.
     * 
     * <p>This method should be overridden by filesystems that do not offer a {@link #getOutputStream()}
     * implementation, but that can take an <code>InputStream</code> and use it to write the file.
     * For this reason, it is recommended to use this method to write a file, rather than copying streams manually using
     * {@link #getOutputStream()}</p>
     *
     * <p>The <code>length</code> parameter is optional. Setting its value help certain protocols which need to know
     * the length in advance. This is the case for instance for some HTTP-based protocols like Amazon S3, which require
     * the <code>Content-Length</code> header to be set in the request. Callers should thus set the length if it is
     * known.</p>
     *
     * <p>Read and write operations are buffered, with a buffer of {@link #IO_BUFFER_SIZE} bytes. For performance
     * reasons, this buffer is provided by {@link BufferPool}. Thus, there is no need to surround the InputStream
     * with a {@link java.io.BufferedInputStream}.</p>
     *
     * <p>Copy progress can optionally be monitored by supplying a {@link com.mucommander.commons.io.CounterInputStream}.</p>
     *
     * @param in the InputStream to read from
     * @param append if true, data written to the OutputStream will be appended to the end of this file. If false, any
     * existing data will be overwritten.
     * @param length length of the stream before EOF is reached, <code>-1</code> if unknown.
     * @throws FileTransferException if something went wrong while reading from the InputStream or writing to this file
     */
    public void copyStream(InputStream in, boolean append, long length) throws FileTransferException {
        OutputStream out;

        try {
            out = append?getAppendOutputStream():getOutputStream();
        }
        catch(IOException e) {
            // TODO: re-throw UnsupportedFileOperationException ? 
            throw new FileTransferException(FileTransferException.OPENING_DESTINATION);
        }

        try {
            StreamUtils.copyStream(in, out, IO_BUFFER_SIZE);
        }
        finally {
            // Close stream even if copyStream() threw an IOException
            try {
                out.close();
            }
            catch(IOException e) {
                throw new FileTransferException(FileTransferException.CLOSING_DESTINATION);
            }
        }
    }

    /**
     * Copies this file to a specified destination file, overwriting the destination if it exists. If this file is a
     * directory, any file or directory it contains will also be copied.
     *
     * <p>This method throws an {@link IOException} if the operation failed, for any of the following reasons:
     * <ul>
     *  <li>this file and the destination file are the same</li>
     *  <li>this file is a directory and a parent of the destination file (the operation would otherwise loop indefinitely)</li>
     *  <li>this file (or one if its children) cannot be read</li>
     *  <li>the destination file (or one of its children) can not be written</li>
     *  <li>an I/O error occurred</li>
     * </ul>
     * </p>
     *
     * <p>If this file supports the {@link FileOperation#COPY_REMOTELY} file operation, an attempt to perform a
     * {@link #copyRemotelyTo(AbstractFile) remote copy} of the file to the destination is made. If the operation isn't
     * supported or wasn't successful, the file is copied manually, by transferring its contents to the destination 
     * using {@link #copyRecursively(AbstractFile, AbstractFile)}.<br/>
     * In that case, no clean up is performed if an error occurs in the midst of a transfer: files that have been copied
     * (even partially) are left in the destination.<br/>
     * It is also worth noting that symbolic links are not copied to the destination when encountered: neither the link
     * nor the linked file is copied</p>
     *
     * @param destFile the destination file to copy this file to
     * @throws IOException in any of the error cases listed above
     */
    public final void copyTo(AbstractFile destFile) throws IOException {
        // First, try to perform a remote copy of the file if the operation is supported
        if(isFileOperationSupported(FileOperation.COPY_REMOTELY)) {
            try {
                copyRemotelyTo(destFile);
                // Operation was a success, all done.
                return;
            }
            catch(IOException e) {
                // Fail silently
            }
        }

        // Fall back to copying the file manually

        checkCopyPrerequisites(destFile, false);

        // Copy the file and its contents if the file is a directory
        copyRecursively(this, destFile);
    }

    /**
     * Moves this file to a specified destination file, overwriting the destination if it exists. If this file is a
     * directory, any file or directory it contains will also be moved.
     * After normal completion, this file will not exist anymore: {@link #exists()} will return <code>false</code>.
     *
     * <p>This method throws an {@link IOException} if the operation failed, for any of the following reasons:
     * <ul>
     *  <li>this file and the destination file are the same</li>
     *  <li>this file is a directory and a parent of the destination file (the operation would otherwise loop indefinitely)</li>
     *  <li>this file (or one if its children) cannot be read</li>
     *  <li>this file (or one of its children) cannot be written</li>
     *  <li>the destination file (or one of its children) can not be written</li>
     *  <li>an I/O error occurred</li>
     * </ul>
     * </p>
     *
     * <p>If this file supports the {@link FileOperation#RENAME} file operation, an attempt to
     * {@link #renameTo(AbstractFile) rename} the file to the destination is made. If the operation isn't supported
     * or wasn't successful, the file is moved manually, by transferring its contents to the destination using
     * {@link #copyTo(AbstractFile)} and then deleting the source.<br/>
     * In that case, deletion of the source occurs only after all files have been successfully transferred.
     * No clean up is performed if an error occurs in the midst of a transfer: files that have been copied
     * (even partially) are left in the destination.<br/>
     * It is also worth noting that symbolic links are not moved to the destination when encountered: neither the link
     * nor the linked file is moved, and the symlink file is deleted.</p>
     *
     * @param destFile the destination file to move this file to
     * @throws IOException in any of the error cases listed above
     */
    public final void moveTo(AbstractFile destFile) throws IOException {
        // First, try to rename the file if the operation is supported
        if(isFileOperationSupported(FileOperation.RENAME)) {
            try {
                renameTo(destFile);
                // Rename was a success, all done.
                return;
            }
            catch(IOException e) {
                // Fail silently
            }
        }

        // Fall back to moving the file manually

        copyTo(destFile);

        // Delete the source file and its contents now that it has been copied OK.
        // Note that the file won't be deleted if copyTo() failed (threw an IOException)
        try {
            deleteRecursively();
        }
        catch(IOException e) {
            throw new FileTransferException(FileTransferException.DELETING_SOURCE);
        }
    }

    /**
     * Creates this file as an empty, non-directory file. This method will fail (throw an <code>IOException</code>)
     * if this file already exists. Note that this method may not always yield a zero-byte file (see below).
     *
     * <p>This generic implementation simply creates a zero-byte file. {@link AbstractRWArchiveFile} implementations
     * may want to override this method so that it creates a valid archive with no entry. To illustrate, an empty Zip
     * file with proper headers is 22-byte long.</p>
     *
     * @throws IOException if the file could not be created, either because it already exists or because of an I/O error
     * @throws UnsupportedFileOperationException if this method relies on a file operation that is not supported
     * or not implemented by the underlying filesystem.
     */
    public void mkfile() throws IOException, UnsupportedFileOperationException {
        if(exists())
            throw new IOException();

        if(isFileOperationSupported(FileOperation.WRITE_FILE))
            getOutputStream().close();
        else
            copyStream(new ByteArrayInputStream(new byte[]{}), false, 0);
    }


    /**
     * Returns the children files that this file contains, filtering out files that do not match the specified FileFilter.
     * For this operation to be successful, this file must be 'browsable', i.e. {@link #isBrowsable()} must return
     * <code>true</code>.
     *
     * @param filter the FileFilter to be used to filter files out from the list, may be <code>null</code>
     * @return the children files that this file contains
     * @throws IOException if this operation is not possible (file is not browsable) or if an error occurred.
     * @throws UnsupportedFileOperationException if this method relies on a file operation that is not supported
     * or not implemented by the underlying filesystem.
     */
    public AbstractFile[] ls(FileFilter filter) throws IOException, UnsupportedFileOperationException {
        return filter==null?ls():filter.filter(ls());
    }


    /**
     * Returns the children files that this file contains, filtering out files that do not match the specified FilenameFilter.
     * For this operation to be successful, this file must be 'browsable', i.e. {@link #isBrowsable()} must return
     * <code>true</code>.
     *
     * <p>This default implementation filters out files *after* they have been created. This method
     * should be overridden if a more efficient implementation can be provided by subclasses.</p>
     *
     * @param filter the FilenameFilter to be used to filter out files from the list, may be <code>null</code>
     * @return the children files that this file contains
     * @throws IOException if this operation is not possible (file is not browsable) or if an error occurred.
     * @throws UnsupportedFileOperationException if this method relies on a file operation that is not supported
     * or not implemented by the underlying filesystem.
     */
    public AbstractFile[] ls(FilenameFilter filter) throws IOException, UnsupportedFileOperationException {
        return filter==null?ls():filter.filter(ls());
    }


    /**
     * Changes this file's permissions to the specified permissions int.
     * The permissions int should be constructed using the permission types and accesses defined in
     * {@link com.mucommander.commons.file.PermissionTypes} and {@link com.mucommander.commons.file.PermissionAccesses}.
     *
     * <p>Implementation note: the default implementation of this method calls sequentially {@link #changePermission(int, int, boolean)},
     * for each permission and access (that's a total 9 calls). This may affect performance on filesystems which need
     * to perform an I/O request to change each permission individually. In that case, and if the fileystem allows
     * to change all permissions at once, this method should be overridden.</p>
     *
     * @param permissions new permissions for this file
     * @throws IOException if the permissions couldn't be changed, either because of insufficient permissions or because
     * of an I/O error.
     * @throws UnsupportedFileOperationException if this method relies on a file operation that is not supported
     * or not implemented by the underlying filesystem.
     */
    public void changePermissions(int permissions) throws IOException, UnsupportedFileOperationException {
        int bitShift = 0;

        PermissionBits mask = getChangeablePermissions();
        for(int a=OTHER_ACCESS; a<=USER_ACCESS; a++) {
            for(int p=EXECUTE_PERMISSION; p<=READ_PERMISSION; p=p<<1) {
                if(mask.getBitValue(a, p))
                    changePermission(a, p, (permissions & (1<<bitShift))!=0);

                bitShift++;
            }
        }
    }

    /**
     * Returns a string representation of this file's permissions.
     *
     * <p>The first character is 'l' if this file is a symbolic link,'d' if it is a directory, '-' otherwise. Then
     * the string contains up to 3 character triplets, for each of the 'user', 'group' and 'other' access types, each
     * containing the following characters:
     * <ul>
     *  <li>'r' if this file has read permission, '-' otherwise
     *  <li>'w' if this file has write permission, '-' otherwise
     *  <li>'x' if this file has executable permission, '-' otherwise
     * </ul>
     * </p>
     *
     * <p>The first character triplet for 'user' access will always be added to the permissions. Then the 'group' and
     * 'other' triplets will only be added if at least one of the user permission bits is supported, as tested with
     * this file's permissions mask.
     * Here are a couple examples to illustrate:
     * <ul>
     *  <li>a directory for which the file permissions' mask is 0 will return the string <code>d---</code>, no matter
     * what permission values the FilePermissions returned by {@link #getPermissions()} contains</li>.
     *  <li>a regular file for which the file permissions' mask returns 777 (full permissions support) and which
     * has read/write/executable permissions for all three 'user', 'group' and 'other' access types will return
     * <code>-rwxrwxrwx</code></li>.
     * </ul>
     * </p>
     *
     * @return a string representation of this file's permissions
     */
    public String getPermissionsString() {
        FilePermissions permissions = getPermissions();
        int supportedPerms = permissions.getMask().getIntValue();

        String s = "";
        s += isSymlink()?'l':isDirectory()?'d':'-';

        int perms = permissions.getIntValue();

        int bitShift = USER_ACCESS *3;

        // Permissions go by triplets (rwx), there are 3 of them for respectively 'owner', 'group' and 'other' accesses.
        // The first one ('owner') will always be displayed, regardless of the permission bit mask. 'Group' and 'other'
        // will be displayed only if the permission mask contains information about them (at least one permission bit).
        for(int a=USER_ACCESS; a>=OTHER_ACCESS; a--) {

            if(a==USER_ACCESS || (supportedPerms & (7<<bitShift))!=0) {
                for(int p=READ_PERMISSION; p>=EXECUTE_PERMISSION; p=p>>1) {
                    if((perms & (p<<bitShift))==0)
                        s += '-';
                    else
                        s += p==READ_PERMISSION?'r':p==WRITE_PERMISSION?'w':'x';
                }
            }

            bitShift -= 3;
        }

        return s;
    }


    /**
     * Deletes this file. If the file is a directory, enclosing files are deleted recursively.
     * Symbolic links to directories are simply deleted, without deleting the contents of the linked directory.
     *
     * @throws IOException if an error occurred while deleting a file or listing a directory's contents
     * @throws UnsupportedFileOperationException if this method relies on a file operation that is not supported 
     * or not implemented by the underlying filesystem.
     */
    public void deleteRecursively() throws IOException, UnsupportedFileOperationException {
        deleteRecursively(this);
    }


    /**
     * Returns <code>true</code> if the specified file operation and corresponding method is supported by this
     * file implementation. See the {@link FileOperation} enum for a complete list of file operations and their
     * corresponding <code>AbstractFile</code> methods.
     * <p>
     * Note that even if <code>true</code> is returned, this doesn't ensure that the file operation will succeed:
     * additional conditions may be required for the operation to succeed and the corresponding method may throw an
     * <code>IOException</code> if those conditions are not met.
     * </p>
     *
     * @param op a file operation
     * @return <code>true</code> if the specified file operation is supported by this filesystem.
     * @see FileOperation
     */
    public boolean isFileOperationSupported(FileOperation op) {
        return isFileOperationSupported(op, getClass());
    }


    ///////////////////
    // Final methods //
    ///////////////////

    /**
     * Returns <code>true</code> if this file is browsable. A file is considered browsable if it contains children files
     * that can be retrieved by calling the <code>ls()</code> methods. Archive files will usually return
     * <code>true</code>, as will directories (directories are always browsable).
     *
     * @return true if this file is browsable
     */
    public final boolean isBrowsable() {
        return isDirectory() || isArchive();
    }

    /**
     * Returns the name of the file without its extension.
     *
     * <p>A filename has an extension if and only if:<br/>
     * - it contains at least one <code>.</code> character<br/>
     * - the last <code>.</code> is not the last character of the filename<br/>
     * - the last <code>.</code> is not the first character of the filename<br/>
     * If this file has no extension, its full name is returned.</p>
     *
     * @return this file's name, without its extension.
     * @see    #getName()
     * @see    #getExtension()
     */
    public final String getNameWithoutExtension() {
        String name;
        int    position;

        name     = getName();
        position = name.lastIndexOf('.');

        if((position<=0) || (position == name.length() - 1))
            return name;

        return name.substring(0, position);
    }

    /**
     * Shorthand for {@link #getAbsolutePath()}.
     *
     * @return the value returned by {@link #getAbsolutePath()}.
     */
    public final String getPath() {
        return getAbsolutePath();
    }

    /**
     * Returns the absolute path to this file.
     * A separator character will be appended to the returned path if <code>true</code> is passed.
     *
     * @param appendSeparator if true, a separator will be appended to the returned path
     * @return the absolute path to this file
     */
    public final String getAbsolutePath(boolean appendSeparator) {
        String path = getAbsolutePath();
        return appendSeparator?addTrailingSeparator(path): removeTrailingSeparator(path);
    }


    /**
     * Returns the canonical path to this file, resolving any symbolic links or '..' and '.' occurrences.
     * A separator character will be appended to the returned path if <code>true</code> is passed.
     *
     * @param appendSeparator if true, a separator will be appended to the returned path
     * @return the canonical path to this file
     */
    public final String getCanonicalPath(boolean appendSeparator) {
        String path = getCanonicalPath();
        return appendSeparator?addTrailingSeparator(path): removeTrailingSeparator(path);
    }


    /**
     * Returns a child of this file, whose path is the concatenation of this file's path and the given relative path.
     * Although this method does not enforce it, the specified path should be relative, i.e. should not start with
     * a separator.<br/>
     * An <code>IOException</code> may be thrown if the child file could not be instantiated but the returned file
     * instance should never be <code>null</code>.
     *
     * @param relativePath the child's path, relative to this file's path
     * @return an AbstractFile representing the requested child file, never null
     * @throws IOException if the child file could not be instantiated
     */
    public final AbstractFile getChild(String relativePath) throws IOException {
        FileURL childURL = (FileURL)getURL().clone();
        childURL.setPath(addTrailingSeparator(childURL.getPath())+ relativePath);

        return FileFactory.getFile(childURL, true);
    }

    /**
     * Convenience method that acts as {@link #getChild(String)} except that it does not throw {@link IOException} but
     * returns <code>null</code> if the child could not be instantiated.
     *
     * @param relativePath the child's path, relative to this file's path
     * @return an AbstractFile representing the requested child file, <code>null</code> if it could not be instantiated
     */
    public final AbstractFile getChildSilently(String relativePath) {
        try {
            return getChild(relativePath);
        }
        catch(IOException e) {
            return null;
        }
    }

    /**
     * Returns a direct child of this file, whose path is the concatenation of this file's path and the given filename.
     * An <code>IOException</code> will be thrown in any of the following cases:
     * <ul>
     *  <li>if the filename contains one or several path separator (the file would not be a direct child)</li>
     *  <li>if the child file could not be instantiated</li>
     * </ul>
     * This method never returns <<code>null</code>.
     *
     * <p>Although {@link #getChild} can be used to retrieve a direct child file, this method should be favored because
     * it allows to use this file instance as the parent of the returned child file.</p>
     *
     * @param filename the name of the child file to be created
     * @return an AbstractFile representing the requested direct child file, never null
     * @throws IOException in any of the cases listed above
     */
    public final AbstractFile getDirectChild(String filename) throws IOException {
        if(filename.indexOf(getSeparator())!=-1)
            throw new IOException();

        AbstractFile childFile = getChild(filename);

        // Use this file as the child's parent, it avoids creating a new AbstractFile instance when getParent() is called
        childFile.setParent(this);

        return childFile;
    }


    /**
     * Convenience method that creates a directory as a direct child of this directory.
     * This method will fail if this file is not a directory.
     *
     * @param name name of the directory to create
     * @throws IOException if the directory could not be created, either because the file already exists or for any
     * other reason.
     * @throws UnsupportedFileOperationException if this method relies on a file operation that is not supported
     * or not implemented by the underlying filesystem.
     */
    public final void mkdir(String name) throws IOException, UnsupportedFileOperationException {
        getChild(name).mkdir();
    }


    /**
     * Creates this file as a directory and any parent directory that does not already exist. This method will fail
     * (throw an <code>IOException</code>) if this file already exists. It may also fail because of an I/O error ;
     * in this case, this method will not remove the parent directories it has created (if any).
     *
     * @throws IOException if this file already exists or if an I/O error occurred.
     * @throws UnsupportedFileOperationException if this method relies on a file operation that is not supported
     * or not implemented by the underlying filesystem.
     */
    public final void mkdirs() throws IOException, UnsupportedFileOperationException {
        AbstractFile parent;
        if(((parent=getParent())!=null) && !parent.exists())
            parent.mkdirs();

        mkdir();
    }


    /**
     * Convenience method that creates a file as a direct child of this directory.
     * This method will fail if this file is not a directory.
     *
     * @param name name of the file to create
     * @throws IOException if the file could not be created, either because the file already exists or for any
     * other reason.
     * @throws UnsupportedFileOperationException if this method relies on a file operation that is not supported
     * or not implemented by the underlying filesystem.
     */
    public final void mkfile(String name) throws IOException, UnsupportedFileOperationException {
        getChild(name).mkfile();
    }


    /**
     * Returns the immediate ancestor of this <code>AbstractFile</code> if it has one, <code>this</code> otherwise:
     * <ul>
     *  <li>if this file is a {@link ProxyFile}, returns the return value of {@link ProxyFile#getProxiedFile()}
     *  <li>if this file is not a <code>ProxyFile</code>, returns <code>this</code>
     * </ul>
     *
     * @return the immediate ancestor of this <code>AbstractFile</code> if it has one, <code>this</code> otherwise
     */
    public final AbstractFile getAncestor() {
        if(this instanceof ProxyFile)
            return ((ProxyFile)this).getProxiedFile();

        return this;
    }

    /**
     * Returns the first ancestor of this file that is an instance of the given Class or of a subclass of it,
     * or <code>this</code> if this instance's class matches those criteria. Returns <code>null</code> if this
     * file has no such ancestor.
     * <br>
     * Note that this method will always return <code>this</code> if <code>AbstractFile.class</code> is specified.
     *
     * @param abstractFileClass a Class corresponding to an AbstractFile subclass
     * @return the first ancestor of this file that is an instance of the given Class or of a subclass of the given
     * Class, or <code>this</code> if this instance's class matches those criteria. Returns <code>null</code> if this
     * file has no such ancestor.
     */
    public final <T extends AbstractFile> T getAncestor(Class<T> abstractFileClass) {
    	AbstractFile ancestor = this;
        AbstractFile lastAncestor;

        do {
            if(abstractFileClass.isAssignableFrom(ancestor.getClass()))
                return (T) ancestor;

            lastAncestor = ancestor;
            ancestor = ancestor.getAncestor();
        }
        while(lastAncestor!=ancestor);

        return null;
    }

    /**
     * Iterates through the ancestors returned by {@link #getAncestor()} until the top-most ancestor is reached and
     * returns it. If this file has no ancestor, <code>this</code> will be returned.
     *
     * @return returns the top-most ancestor of this file, <code>this</code> if this file has no ancestor
     */
    public final AbstractFile getTopAncestor() {
        AbstractFile topAncestor = this;
        while(topAncestor.hasAncestor())
            topAncestor = topAncestor.getAncestor();

        return topAncestor;
    }

    /**
     * Returns <code>true</code> if this <code>AbstractFile</code> has an ancestor, i.e. if this file is a
     * {@link ProxyFile}, <code>false</code> otherwise.
     *
     * @return <code>true</code> if this <code>AbstractFile</code> has an ancestor, <code>false</code> otherwise.
     */
    public final boolean hasAncestor() {
        return this instanceof ProxyFile;
    }

    /**
     * Returns <code>true</code> if this file is or has an ancestor (immediate or not) that is an instance of the given
     * <code>Class</code> or of a subclass of the <code>Class</code>. Note that the specified must correspond to an
     * <code>AbstractFile</code> subclass. Specifying any other Class will always yield to this method returning
     * <code>false</code>. Also note that this method will always return <code>true</code> if
     * <code>AbstractFile.class</code> is specified.
     *
     * @param abstractFileClass a Class corresponding to an AbstractFile subclass
     * @return <code>true</code> if this file has an ancestor (immediate or not) that is an instance of the given Class
     * or of a subclass of the given Class.
     */
    public final boolean hasAncestor(Class<? extends AbstractFile> abstractFileClass) {
        AbstractFile ancestor = this;
        AbstractFile lastAncestor;

        do {
            if(abstractFileClass.isAssignableFrom(ancestor.getClass()))
                return true;

            lastAncestor = ancestor;
            ancestor = ancestor.getAncestor();
        }
        while(lastAncestor!=ancestor);

        return false;
    }


    /**
     * Returns <code>true</code> if this file is a parent folder of the given file, or if the two files are equal.
     *
     * @param file the AbstractFile to test
     * @return true if this file is a parent folder of the given file, or if the two files are equal
     */
    public final boolean isParentOf(AbstractFile file) {
        return isBrowsable() && file.getCanonicalPath(true).startsWith(getCanonicalPath(true));
    }

    /**
     * Convenience method that returns the parent {@link AbstractArchiveFile} that contains this file. If this file
     * is an {@link AbstractArchiveFile} or an ancestor of {@link AbstractArchiveFile}, <code>this</code> is returned.
     * If this file is neither contained by an archive nor is an archive, <code>null</code> is returned.
     *
     * <p>
     * <b>Important note:</b> the returned {@link AbstractArchiveFile}, if any, may not necessarily be an
     * archive, as specified by {@link #isArchive()}. This is the case for files that were resolved as
     * {@link AbstractArchiveFile} instances based on their path, but that do not yet exist or were created as
     * directories. On the contrary, an existing archive will necessarily return a non-null value.
     * </p>
     *
     * @return the parent {@link AbstractArchiveFile} that contains this file
     */
    public final AbstractArchiveFile getParentArchive() {
        if(hasAncestor(AbstractArchiveFile.class))
            return getAncestor(AbstractArchiveFile.class);
        else if(hasAncestor(AbstractArchiveEntryFile.class))
            return getAncestor(AbstractArchiveEntryFile.class).getArchiveFile();

        return null;
    }


    /**
     * Returns an icon representing this file, using the default {@link com.mucommander.commons.file.icon.FileIconProvider}
     * registered in {@link FileFactory}. The specified preferred resolution will be used as a hint, but the returned
     * icon may have different dimension; see {@link com.mucommander.commons.file.icon.FileIconProvider#getFileIcon(AbstractFile, java.awt.Dimension)}
     * for full details.
     * This method may return <code>null</code> if the JVM is running on a headless environment.
     *
     * @param preferredResolution the preferred icon resolution
     * @return an icon representing this file, <code>null</code> if the JVM is running on a headless environment
     * @see com.mucommander.commons.file.FileFactory#getDefaultFileIconProvider()
     * @see com.mucommander.commons.file.icon.FileIconProvider#getFileIcon(AbstractFile, java.awt.Dimension)
     */
    public final Icon getIcon(Dimension preferredResolution) {
        return FileFactory.getDefaultFileIconProvider().getFileIcon(this, preferredResolution);
    }

    /**
     * Returns an icon representing this file, using the default {@link com.mucommander.commons.file.icon.FileIconProvider}
     * registered in {@link FileFactory}. The default preferred resolution for the icon is 16x16 pixels.
     * This method may return <code>null</code> if the JVM is running on a headless environment.
     *
     * @return an icon representing this file, <code>null</code> if the JVM is running on a headless environment
     * @see com.mucommander.commons.file.FileFactory#getDefaultFileIconProvider()
     * @see com.mucommander.commons.file.icon.FileIconProvider#getFileIcon(AbstractFile, java.awt.Dimension)
     */
    public final Icon getIcon() {
        // Note: the Dimension object is created here instead of returning a final static field, because creating
        // a Dimension object triggers the AWT and Swing classes loading. Since these classes are not
        // needed in a headless environment, we want them to be loaded only if strictly necessary.
        return getIcon(new java.awt.Dimension(16, 16));
    }


    /**
     * Returns a checksum of this file (also referred to as <i>hash</i> or <i>digest</i>) calculated by reading this
     * file's contents and feeding the bytes to the given <code>MessageDigest</code>, until EOF is reached.
     *
     * <p>The checksum is returned as an hexadecimal string, such as "6d75636f0a". The length of this string depends on
     * the kind of algorithm.</p>
     *
     * <p>Note: this method does not reset the <code>MessageDigest</code> after the checksum has been calculated.</p>
     *
     * @param algorithm the algorithm to use for calculating the checksum
     * @return this file's checksum, as an hexadecimal string
     * @throws IOException if an I/O error occurred while calculating the checksum
     * @throws NoSuchAlgorithmException if the specified algorithm does not correspond to any MessageDigest registered
     * with the Java Cryptography Extension.
     * @throws UnsupportedFileOperationException if this method relies on a file operation that is not supported
     * or not implemented by the underlying filesystem.
     */
    public final String calculateChecksum(String algorithm) throws IOException, NoSuchAlgorithmException, UnsupportedFileOperationException {
        return calculateChecksum(MessageDigest.getInstance(algorithm));
    }

    /**
     * Returns a checksum of this file (also referred to as <i>hash</i> or <i>digest</i>) calculated by reading this
     * file's contents and feeding the bytes to the given <code>MessageDigest</code>, until EOF is reached.
     *
     * <p>The checksum is returned as an hexadecimal string, such as "6d75636f0a". The length of this string depends on
     * the kind of <code>MessageDigest</code>.</p>
     *
     * <p>Note: this method does not reset the <code>MessageDigest</code> after the checksum has been calculated.</p>
     *
     * @param messageDigest the MessageDigest to use for calculating the checksum
     * @return this file's checksum, as an hexadecimal string
     * @throws IOException if an I/O error occurred while calculating the checksum
     * @throws UnsupportedFileOperationException if this method relies on a file operation that is not supported
     * or not implemented by the underlying filesystem.
     */
    public final String calculateChecksum(MessageDigest messageDigest) throws IOException, UnsupportedFileOperationException {
        InputStream in = getInputStream();

        try {
            return calculateChecksum(in, messageDigest);
        }
        finally {
            in.close();
        }
    }


    /**
     * Tests if the given path contains a trailing separator, and if not, adds one to the returned path.
     * The separator used is the one returned by {@link #getSeparator()}.
     *
     * @param path the path for which to add a trailing separator
     * @return the path with a trailing separator
     */
    public final String addTrailingSeparator(String path) {
        // Even though getAbsolutePath() is not supposed to return a trailing separator, root folders ('/', 'c:\' ...)
        // are exceptions that's why we still have to test if path ends with a separator
        String separator = getSeparator();
        if(!path.endsWith(separator))
            return path+separator;
        return path;
    }

    /**
     * Tests if the given path contains a trailing separator, and if it does, removes it from the returned path.
     * The separator used is the one returned by {@link #getSeparator()}.
     *
     * @param path the path for which to remove the trailing separator
     * @return the path free of a trailing separator
     */
    protected final String removeTrailingSeparator(String path) {
        // Remove trailing slash if path is not '/' or trailing backslash if path does not end with ':\'
        // (Reminder: C: is C's current folder, while C:\ is C's root)
        String separator = getSeparator();
        if(path.endsWith(separator)
           && !((separator.equals("/") && path.length()==1) || (separator.equals("\\") && path.charAt(path.length()-2)==':')))
            path = path.substring(0, path.length()-1);
        return path;
    }


    /**
     * Checks the prerequisites of a copy (or move) operation.
     * Throws a {@link FileTransferException} if any of the following conditions are true, does nothing otherwise:
     * <ul>
     *   <li>this file does not exist</li>
     *   <li>this file and the destination file are the same, unless <code>allowCaseVariations</code> is <code>true</code>
     * and the destination filename is a case variation of the source</li>
     *   <li>this file is a parent of the destination file</li>
     * </ul>
     *
     * @param destFile the destination file to copy this file to
     * @param allowCaseVariations prevents throwing an exception if both file names are a case variation of one another
     * @throws FileTransferException in any of the cases listed above, use {@link FileTransferException#getReason()} to
     * know the reason.
     */
    protected final void checkCopyPrerequisites(AbstractFile destFile, boolean allowCaseVariations) throws FileTransferException {
        boolean isAllowedCaseVariation = false;

        // Throw an exception of a specific kind if the source and destination files refer to the same file
        boolean filesEqual = this.equalsCanonical(destFile);
        if(filesEqual) {
            // If case variations are allowed and the destination filename is a case variation of the source,
            // do not throw an exception.
            if(allowCaseVariations) {
                String sourceFileName = getName();
                String destFileName = destFile.getName();
                if(sourceFileName.equalsIgnoreCase(destFileName) && !sourceFileName.equals(destFileName))
                    isAllowedCaseVariation = true;
            }

            if(!isAllowedCaseVariation)
                throw new FileTransferException(FileTransferException.SOURCE_AND_DESTINATION_IDENTICAL);
        }

        // Throw an exception if source is a parent of destination
        if(!filesEqual && isParentOf(destFile))      // Note: isParentOf(destFile) returns true if both files are equal
            throw new FileTransferException(FileTransferException.SOURCE_PARENT_OF_DESTINATION);

        // Throw an exception if the source file does not exist
        if(!exists())
            throw new FileTransferException(FileTransferException.FILE_NOT_FOUND);
    }

    /**
     * Checks the prerequisites of a {@link #copyRemotelyTo(AbstractFile)} operation.
     * This method starts by verifying the following requirements and throws an <code>IOException</code> if one of them
     * isn't met:
     * <ul>
     *   <li>both files' schemes are equal</li>
     *   <li>both files' {@link #getTopAncestor() top ancestors} are equal</li>
     *   <li>both files' hosts are equal, or <code>allowDifferentHosts</code> is <code>true</code></li>
     * </ul>
     * If all those requirements are met, {@link #checkCopyPrerequisites(AbstractFile, boolean)} is called with the
     * destination file and <code>allowCaseVariations</code> flag to perform prerequisites verifications.
     *
     * @param destFile the destination file to copy this file to
     * @param allowCaseVariations prevents throwing an exception if both file names are a case variation of one another
     * @param allowDifferentHosts prevents throwing an exception if both files have the same host
     * @throws FileTransferException in any of the cases listed above, use {@link FileTransferException#getReason()} to
     * know the reason.
     * @see #checkCopyPrerequisites(AbstractFile, boolean)
     */
    protected final void checkCopyRemotelyPrerequisites(AbstractFile destFile, boolean allowCaseVariations, boolean allowDifferentHosts) throws IOException, FileTransferException {
        if(!fileURL.schemeEquals(fileURL)
        || !destFile.getTopAncestor().getClass().equals(getTopAncestor().getClass())
        || (!allowDifferentHosts && !destFile.getURL().hostEquals(fileURL)))
            throw new IOException();

        checkCopyPrerequisites(destFile, allowCaseVariations);
    }

    /**
     * Checks the prerequisites of a {@link #renameTo(AbstractFile)} operation.
     * This method starts by verifying the following requirements and throws an <code>IOException</code> if one of them
     * isn't met:
     * <ul>
     *   <li>both files' schemes are equal</li>
     *   <li>both files' {@link #getTopAncestor() top ancestors} are equal</li>
     *   <li>both files' hosts are equal, or <code>allowDifferentHosts</code> is <code>true</code></li>
     * </ul>
     * If all those requirements are met, {@link #checkCopyPrerequisites(AbstractFile, boolean)} is called with the
     * destination file and <code>allowCaseVariations</code> flag to perform further prerequisites verifications.
     *
     * @param destFile the destination file to copy this file to
     * @param allowCaseVariations prevents throwing an exception if both file names are a case variation of one another
     * @param allowDifferentHosts prevents throwing an exception if both files have the same host
     * @throws FileTransferException in any of the cases listed above, use {@link FileTransferException#getReason()} to
     * know the reason.
     * @see #checkCopyPrerequisites(AbstractFile, boolean)
     */
    protected final void checkRenamePrerequisites(AbstractFile destFile, boolean allowCaseVariations, boolean allowDifferentHosts) throws IOException, FileTransferException {
        checkCopyRemotelyPrerequisites(destFile, allowCaseVariations, allowDifferentHosts);
    }

    /**
     * Copies the source file to the destination one and recurses on directory contents.
     * This method assumes that the destination file does not exists, this must be checked prior to calling this method.
     * Symbolic links are skipped when encountered: neither the link nor the linked file are copied.
     *
     * @param sourceFile the file to copy
     * @param destFile the destination file
     * @throws FileTransferException if an error occurred while copying the file
     */
    protected final void copyRecursively(AbstractFile sourceFile, AbstractFile destFile) throws FileTransferException {
        if(sourceFile.isSymlink())
            return;

        if(sourceFile.isDirectory()) {
            try {
                destFile.mkdir();
            }
            catch(IOException e) {
                throw new FileTransferException(FileTransferException.WRITING_DESTINATION);
            }

            AbstractFile children[];
            try {
                children = sourceFile.ls();
            }
            catch(IOException e) {
                throw new FileTransferException(FileTransferException.READING_SOURCE);
            }

            AbstractFile destChild;
            for (AbstractFile child : children) {
                try {
                    destChild = destFile.getDirectChild(child.getName());
                }
                catch (IOException e) {
                    throw new FileTransferException(FileTransferException.OPENING_DESTINATION);
                }

                copyRecursively(child, destChild);
            }
        }
        else {
            InputStream in;

            try {
                in = sourceFile.getInputStream();
            }
            catch(IOException e) {
                throw new FileTransferException(FileTransferException.OPENING_SOURCE);
            }

            try {
                destFile.copyStream(in, false, sourceFile.getSize());
            }
            finally {
                // Close stream even if copyStream() threw an IOException
                try {
                    in.close();
                }
                catch(IOException e) {
                    throw new FileTransferException(FileTransferException.CLOSING_SOURCE);
                }
            }
        }
    }

    /**
     * Deletes the given file. If the file is a directory, enclosing files are deleted recursively.
     * Symbolic links to directories are simply deleted, without deleting the contents of the linked directory.
     *
     * @param file the file to delete
     * @throws IOException if an error occurred while deleting a file or listing a directory's contents
     * @throws UnsupportedFileOperationException if this method relies on a file operation that is not supported
     * or not implemented by the underlying filesystem.
     */
    protected final void deleteRecursively(AbstractFile file) throws IOException, UnsupportedFileOperationException {
        if(file.isDirectory() && !file.isSymlink()) {
            AbstractFile children[] = file.ls();
            for (AbstractFile child : children)
                deleteRecursively(child);
        }

        file.delete();
    }

    /**
     * Convenience method that calls {@link #changePermissions(int)} with the given permissions' int value.
     *
     * @param permissions new permissions for this file
     * @throws IOException if the permissions couldn't be changed, either because of insufficient permissions or because
     * of an I/O error.
     * @throws UnsupportedFileOperationException if this method relies on a file operation that is not supported
     * or not implemented by the underlying filesystem.
     */
    public final void changePermissions(FilePermissions permissions) throws IOException, UnsupportedFileOperationException {
        changePermissions(permissions.getIntValue());
    }

    /**
     * This method is a shorthand for {@link #importPermissions(AbstractFile, FilePermissions)} called with
     * {@link FilePermissions#DEFAULT_DIRECTORY_PERMISSIONS} if this file is a directory or
     * {@link FilePermissions#DEFAULT_FILE_PERMISSIONS} if this file is a regular file.
     *
     * @param sourceFile the file from which to import permissions
     * @throws IOException if the permissions couldn't be changed, either because of insufficient permissions or because
     * of an I/O error.
     * @throws UnsupportedFileOperationException if this method relies on a file operation that is not supported
     * or not implemented by the underlying filesystem.
     */
    public final void importPermissions(AbstractFile sourceFile) throws IOException, UnsupportedFileOperationException {
        importPermissions(sourceFile,isDirectory()
                ? FilePermissions.DEFAULT_DIRECTORY_PERMISSIONS
                : FilePermissions.DEFAULT_FILE_PERMISSIONS);
    }

    /**
     * Imports the given source file's permissions, overwriting this file's permissions. Only the bits that are
     * supported by the source file (as reported by the permissions' mask) are preserved. Other bits are be
     * set to those of the specified default permissions.
     * See {@link SimpleFilePermissions#padPermissions(FilePermissions, FilePermissions)} for more information about
     * permissions padding.
     *
     * @param sourceFile the file from which to import permissions
     * @param defaultPermissions default permissions to use
     * @throws IOException if the permissions couldn't be changed, either because of insufficient permissions or because
     * of an I/O error.
     * @throws UnsupportedFileOperationException if this method relies on a file operation that is not supported
     * or not implemented by the underlying filesystem.
     * @see SimpleFilePermissions#padPermissions(FilePermissions, FilePermissions)
     */
    public final void importPermissions(AbstractFile sourceFile, FilePermissions defaultPermissions) throws IOException, UnsupportedFileOperationException {
        changePermissions(SimpleFilePermissions.padPermissions(sourceFile.getPermissions(), defaultPermissions).getIntValue());
    }


    ////////////////////
    // Static methods //
    ////////////////////

    /**
     * Returns <code>true</code> if the specified file operation and corresponding method is supported by the
     * given <code>AbstractFile</code> implementation.<br>
     * See the {@link FileOperation} enum for a complete list of file operations and their corresponding
     * <code>AbstractFile</code> methods.
     *
     * @param op a file operation
     * @param c the file implementation to test
     * @return <code>true</code> if the specified file operation is supported by this filesystem.
     * @see FileOperation
     */
    public static boolean isFileOperationSupported(FileOperation op, Class<? extends AbstractFile> c) {
        return !op.getCorrespondingMethod(c).isAnnotationPresent(UnsupportedFileOperation.class);
    }

    /**
     * Returns the given filename's extension, <code>null</code> if the filename doesn't have an extension.
     *
     * <p>A filename has an extension if and only if:<br/>
     * - it contains at least one <code>.</code> character<br/>
     * - the last <code>.</code> is not the last character of the filename<br/>
     * - the last <code>.</code> is not the first character of the filename</p>
     *
     * <p>
     * The returned extension (if any) is free of any extension separator character (<code>.</code>). For instance,
     * this method will return <code>"ext"</code> for a file named <code>"name.ext"</code>, <b>not</b> <code>".ext"</code>.
     * </p>
     *
     * @param filename a filename, not a full path
     * @return the given filename's extension, <code>null</code> if the filename doesn't have an extension
     */
    public static String getExtension(String filename) {
        int lastDotPos = filename.lastIndexOf('.');

        int len;
        if(lastDotPos<=0 || lastDotPos==(len=filename.length())-1)
            return null;

        return filename.substring(lastDotPos+1, len);
    }


    /**
     * Returns the given filename without its extension (base name). if the filename doesn't have an extension, returns the filename as received
     *
     * <p>A filename has an extension if and only if:<br/>
     * - it contains at least one <code>.</code> character<br/>
     * - the last <code>.</code> is not the last character of the filename<br/>
     * - the last <code>.</code> is not the first character of the filename</p>
     *
     * @return the file's base name - without its extension, if the filename doesn't have an extension returns the filename as received
     */
    public String getBaseName() { 
    	String fileName = getName(); 
    	int lastDotPos = fileName.lastIndexOf('.');
    	 
         if(lastDotPos<=0 || lastDotPos==fileName.length()-1)
             return fileName;
         
         return fileName.substring(0, lastDotPos);
    }
    
    /**
     * Returns the checksum (also referred to as <i>hash</i> or <i>digest</i>) of the given <code>InputStream</code>
     * calculated by reading the stream and feeding the bytes to the given <code>MessageDigest</code> until EOF is
     * reached.
     *
     * <p><b>Important:</b> this method does not close the <code>InputStream</code>, and does not reset the
     * <code>MessageDigest</code> after the checksum has been calculated.</p>
     *
     * @param in the InputStream for which to calculate the checksum
     * @param messageDigest the MessageDigest to use for calculating the checksum
     * @return the given InputStream's checksum, as an hexadecimal string
     * @throws IOException if an I/O error occurred while calculating the checksum
     */
    public static String calculateChecksum(InputStream in, MessageDigest messageDigest) throws IOException {
        ChecksumInputStream cin = new ChecksumInputStream(in, messageDigest);
        try {
            StreamUtils.readUntilEOF(cin);
            return cin.getChecksumString();
        }
        catch(IOException e) {
            throw new FileTransferException(FileTransferException.READING_SOURCE);
        }
    }

    
    ////////////////////////
    // Overridden methods //
    ////////////////////////

    /**
     * Tests a file for equality by comparing both files' {@link #getURL() URL}. Returns <code>true</code> if the URL
     * of this file and the specified one are equal according to {@link FileURL#equals(Object, boolean, boolean)} called
     * with credentials and properties comparison enabled.
     *
     * <p>
     * Unlike {@link #equalsCanonical(Object)}, this method <b>is not</b> allowed to perform I/O operations and block
     * the caller thread.
     * </p>
     *
     * @param o the object to compare against this instance
     * @return Returns <code>true</code> if the URL of this file and the specified one are equal
     * @see FileURL#equals(Object, boolean, boolean)
     * @see #equalsCanonical(Object)
     */
    public boolean equals(Object o) {
        if(o==null || !(o instanceof AbstractFile))
            return false;

        return getURL().equals(((AbstractFile)o).getURL(), true, true);
    }

    /**
     * Tests a file for equality by comparing both files' {@link #getCanonicalPath() canonical path}.
     * Returns <code>true</code> if the canonical path of this file and the specified one are equal.
     *
     * <p>It is noteworthy that this method uses <code>java.lang.String#equals(Object)</code> to compare paths, which
     * in some rare cases may return <code>false</code> for non-ascii/Unicode paths that have the same written
     * representation but are not equal according to <code>java.lang.String#equals(Object)</code>. Handling such cases
     * would require a locale-aware String comparison which is not an option here.</p>
     *
     * <p>It is also worth noting that hostnames are not resolved, which means this method does not consider
     * a hostname and its corresponding IP address as being equal.</p>
     *
     * <p>Unlike {@link #equals(Object)}, this method <b>is</b> allowed to perform I/O operations and block
     * the caller thread.</p>
     *
     * @param o the object to compare against this instance
     * @return <code>true</code> if the canonical path of this file and the specified one are equal.
     * @see #equals(Object)
     */
    public boolean equalsCanonical(Object o) {
        if(o==null || !(o instanceof AbstractFile))
            return false;

        // TODO: resolve hostnames ?

        return getCanonicalPath(false).equals(((AbstractFile)o).getCanonicalPath(false));
    }

    /**
     * Returns the hashCode of this file's {@link #getURL() URL}.
     *
     * @return the hashCode of this file's {@link #getURL() URL}.
     */
    public int hashCode() {
        return getURL().hashCode();
    }

    /**
     * Returns a String representation of this file. The returned String is this file's path as returned by
     * {@link #getAbsolutePath()}.
     */
    public String toString() {
        return getAbsolutePath();
    }


    //////////////////////
    // Abstract methods //
    //////////////////////

    /**
     * Returns this file's last modified date, in milliseconds since the epoch (00:00:00 GMT, January 1, 1970).
     *
     * @return this file's last modified date, in milliseconds since the epoch (00:00:00 GMT, January 1, 1970)
     */
    public abstract long getDate();

    /**
     * Changes this file's last modified date to the specified one. Throws an <code>IOException</code> if the date
     * couldn't be changed, either because of insufficient permissions or because of an I/O error.
     *
     * <p>This {@link FileOperation#CHANGE_DATE file operation} may or may not be supported by the underlying filesystem
     * -- {@link #isFileOperationSupported(FileOperation)} can be called to find out if it is. If the operation isn't
     * supported, a {@link UnsupportedFileOperation} will be thrown when this method is called.</p>
     *
     * @param lastModified last modified date, in milliseconds since the epoch (00:00:00 GMT, January 1, 1970)
     * @throws IOException if the date couldn't be changed, either because of insufficient permissions or because of
     * an I/O error.
     * @throws UnsupportedFileOperationException if this operation is not supported by the underlying filesystem,
     * or is not implemented.
     */
    public abstract void changeDate(long lastModified) throws IOException, UnsupportedFileOperationException;

    /**
     * Returns this file's size in bytes, <code>0</code> if this file doesn't exist, <code>-1</code> if the size is
     * undetermined.
     *
     * @return this file's size in bytes, 0 if this file doesn't exist, -1 if the size is undetermined
     */
    public abstract long getSize();
	
    /**
     * Returns this file's parent, <code>null</code> if it doesn't have one.
     *
     * @return this file's parent, <code>null</code> if it doesn't have one
     */
    public abstract AbstractFile getParent();
	
    /**
     * Sets this file's parent. <code>null</code> can be specified if this file doesn't have a parent.
     *
     * @param parent the new parent of this file
     */
    public abstract void setParent(AbstractFile parent);

    /**
     * Returns <code>true</code> if this file exists.
     *
     * @return <code>true</code> if this file exists
     */
    public abstract boolean exists();

    /**
     * Returns this file's permissions, as a {@link FilePermissions} object. Note that this file may only support
     * certain permission bits, use the {@link com.mucommander.commons.file.FilePermissions#getMask() permission mask} to find
     * out which bits are supported.
     *
     * <p>This method may return permissions for which none of the bits are supported, but may never return
     * <code>null</code>.</p>
     *
     * @return this file's permissions, as a FilePermissions object
     */
    public abstract FilePermissions getPermissions();

    /**
     * Returns a bit mask describing the permission bits that can be changed on this file when calling
     * {@link #changePermission(int, int, boolean)} and {@link #changePermissions(int)}.
     *
     * @return a bit mask describing the permission bits that can be changed on this file
     */
    public abstract PermissionBits getChangeablePermissions();

    /**
     * Changes the specified permission bit.
     *
     * <p>This {@link FileOperation#CHANGE_PERMISSION file operation} may or may not be supported by the underlying filesystem
     * -- {@link #isFileOperationSupported(FileOperation)} can be called to find out if it is. If the operation isn't
     * supported, a {@link UnsupportedFileOperation} will be thrown when this method is called.</p>
     *
     * @param access see {@link PermissionTypes} for allowed values
     * @param permission see {@link PermissionAccesses} for allowed values
     * @param enabled true to enable the flag, false to disable it
     * @throws IOException if the permission couldn't be changed, either because of insufficient permissions or because
     * of an I/O error.
     * @throws UnsupportedFileOperationException if this operation is not supported by the underlying filesystem,
     * or is not implemented.
     * @see #getChangeablePermissions()
     */
    public abstract void changePermission(int access, int permission, boolean enabled) throws IOException, UnsupportedFileOperationException;

    /**
     * Returns information about the owner of this file. The kind of information that is returned is implementation-dependant.
     * It may typically be a username (e.g. 'bob') or a user ID (e.g. '501').
     * If the owner information is not available to the <code>AbstractFile</code> implementation (cannot be retrieved or
     * the filesystem doesn't have any notion of owner) or not available for this particular file, <code>null</code>
     * will be returned.
     *
     * @return information about the owner of this file
     */
    public abstract String getOwner();

    /**
     * Returns <code>true</code> if this file implementation is able to return some information about file owners, not
     * necessarily for all files or this file in particular but at least for some of them. In other words, a
     * <code>true</code> return value doesn't mean that {@link #getOwner()} will necessarily return a non-null value,
     * but rather that there is a chance that it does.
     *
     * @return true if this file implementation is able to return information about file owners  
     */
    public abstract boolean canGetOwner();

    /**
     * Returns information about the group this file belongs to. The kind of information that is returned is implementation-dependant.
     * It may typically be a group name (e.g. 'www-data') or a group ID (e.g. '501').
     * If the group information is not available to the <code>AbstractFile</code> implementation (cannot be retrieved or
     * the filesystem doesn't have any notion of owner) or not available for this particular file, <code>null</code>
     * will be returned.
     *
     * @return information about the owner of this file
     */
    public abstract String getGroup();

    /**
     * Returns <code>true</code> if this file implementation is able to return some information about file groups, not
     * necessarily for all files or this file in particular but at least for some of them. In other words, a
     * <code>true</code> return value doesn't mean that {@link #getGroup()} will necessarily return a non-null value,
     * but rather that there is a chance that it does.
     *
     * @return true if this file implementation is able to return information about file groups
     */
    public abstract boolean canGetGroup();

    /**
     * Returns <code>true</code> if this file is a directory, <code>false</code> in any of the following cases:
     * <ul>
     *  <li>this file does not exist</li>
     *  <li>this file is a regular file</li>
     *  <li>this file is an {@link #isArchive() archive}</li>
     * </ul> 
     *
     * @return <code>true</code> if this file is a directory, <code>false</code> in any of the cases listed above
     */
    public abstract boolean isDirectory();

    /**
     * Returns <code>true</code> if this file is an archive.
     * <p>
     * An archive is a file container that can be {@link #isBrowsable() browsed}.  Archive files may not be
     * {@link #isDirectory() directories}, and vice-versa.
     * </p>.
     *
     * @return <code>true</code> if this file is an archive.
     */
    public abstract boolean isArchive();

    /**
     * Returns <code>true</code> if this file is a symbolic link. Symbolic links need to be handled with special care,
     * especially when manipulating files recursively.
     *
     * @return <code>true</code> if this file is a symbolic link
     */
    public abstract boolean isSymlink();

    /**
     * Returns <code>true</code> if this file is a system file.
     * Note that system file attribute depends on the OS, so we can know it only for local files:
     * - For MAC OS, {@link MacOsSystemFolder} defines the group of system files
     * - On Windows, files has special attribute that mark them as system files
     *
     * @return <code>true</code> if this file is a system file
     */
    public abstract boolean isSystem();
	
    /**
     * Returns the children files that this file contains. For this operation to be successful, this file must be
     * 'browsable', i.e. {@link #isBrowsable()} must return <code>true</code>.
     * This method may return a zero-length array if it has no children but may never return <code>null</code>.
     *
     * <p>This {@link FileOperation#LIST_CHILDREN file operation} may or may not be supported by the underlying filesystem
     * -- {@link #isFileOperationSupported(FileOperation)} can be called to find out if it is. If the operation isn't
     * supported, a {@link UnsupportedFileOperation} will be thrown when this method is called.</p>
     *
     * @return the children files that this file contains
     * @throws IOException if this operation is not possible (file is not browsable) or if an error occurred.
     * @throws UnsupportedFileOperationException if this operation is not supported by the underlying filesystem,
     * or is not implemented.
     */
    public abstract AbstractFile[] ls() throws IOException, UnsupportedFileOperationException;

    /**
     * Creates this file as a directory. This method will fail (throw an <code>IOException</code>) if this file
     * already exists.
     *
     * <p>This {@link FileOperation#CREATE_DIRECTORY file operation} may or may not be supported by the underlying filesystem
     * -- {@link #isFileOperationSupported(FileOperation)} can be called to find out if it is. If the operation isn't
     * supported, a {@link UnsupportedFileOperation} will be thrown when this method is called.</p>
     *
     * @throws IOException if the directory could not be created, either because this file already exists or for any
     * other reason.
     * @throws UnsupportedFileOperationException if this operation is not supported by the underlying filesystem,
     * or is not implemented.
     */
    public abstract void mkdir() throws IOException, UnsupportedFileOperationException;

    /**
     * Returns an <code>InputStream</code> to read the contents of this file.
     * Throws an <code>IOException</code> in any of the following cases:
     * <ul>
     *  <li>this file does not exist</li>
     *  <li>this file is a directory</li>
     *  <li>this file cannot be read</li>
     *  <li>an I/O error occurs</li>
     * </ul>
     * This method may never return <code>null</code>.
     *
     * <p>This {@link FileOperation#READ_FILE file operation} may or may not be supported by the underlying filesystem
     * -- {@link #isFileOperationSupported(FileOperation)} can be called to find out if it is. If the operation isn't
     * supported, a {@link UnsupportedFileOperation} will be thrown when this method is called.</p>
     *
     * @return an <code>InputStream</code> to read the contents of this file
     * @throws IOException in any of the cases listed above
     * @throws UnsupportedFileOperationException if this operation is not supported by the underlying filesystem,
     * or is not implemented.
     */
    public abstract InputStream getInputStream() throws IOException, UnsupportedFileOperationException;

    /**
     * Returns an <code>OuputStream</code> to write the contents of this file, overwriting the existing contents, if any.
     * This file will be created as a zero-byte file if it does not yet exist.
     * <p>
     * This method may throw an <code>IOException</code> in any of the following cases, but may never return
     * <code>null</code>:
     * <ul>
     *   <li>this file is a directory</li>
     *   <li>this file cannot be written</li>
     *   <li>an I/O error occurs</li>
     * </ul>
     * </p>
     *
     * <p>This {@link FileOperation#WRITE_FILE file operation} may or may not be supported by the underlying filesystem
     * -- {@link #isFileOperationSupported(FileOperation)} can be called to find out if it is. If the operation isn't
     * supported, a {@link UnsupportedFileOperation} will be thrown when this method is called.</p>
     *
     * @return an <code>OuputStream</code> to write the contents of this file
     * @throws IOException in any of the cases listed above
     * @throws UnsupportedFileOperationException if this operation is not supported by the underlying filesystem,
     * or is not implemented.
     */
    public abstract OutputStream getOutputStream() throws IOException, UnsupportedFileOperationException;

    /**
     * Returns an <code>OuputStream</code> to write the contents of this file, appending the existing contents, if any.
     * This file will be created as a zero-byte file if it does not yet exist.
     * <p>
     * This method may throw an <code>IOException</code> in any of the following cases, but may never return
     * <code>null</code>:
     * <ul>
     *   <li>this file is a directory</li>
     *   <li>this file cannot be written</li>
     *   <li>an I/O error occurs</li>
     * </ul>
     * </p>
     *
     * <p>This {@link FileOperation#APPEND_FILE file operation} may or may not be supported by the underlying filesystem
     * -- {@link #isFileOperationSupported(FileOperation)} can be called to find out if it is. If the operation isn't
     * supported, a {@link UnsupportedFileOperation} will be thrown when this method is called.</p>
     *
     * @return an <code>OuputStream</code> to write the contents of this file
     * @throws IOException in any of the cases listed above
     * @throws UnsupportedFileOperationException if this operation is not supported by the underlying filesystem,
     * or is not implemented.
     */
    public abstract OutputStream getAppendOutputStream() throws IOException, UnsupportedFileOperationException;

    /**
     * Returns a {@link RandomAccessInputStream} to read the contents of this file with random access.
     * Throws an <code>IOException</code> in any of the following cases:
     * <ul>
     *  <li>this file does not exist</li>
     *  <li>this file is a directory</li>
     *  <li>this file cannot be read</li>
     *  <li>an I/O error occurs</li>
     * </ul>
     * This method may never return <code>null</code>.
     *
     * <p>This {@link FileOperation#RANDOM_READ_FILE file operation} may or may not be supported by the underlying filesystem
     * -- {@link #isFileOperationSupported(FileOperation)} can be called to find out if it is. If the operation isn't
     * supported, a {@link UnsupportedFileOperation} will be thrown when this method is called.</p>
     *
     * @return a <code>RandomAccessInputStream</code> to read the contents of this file with random access
     * @throws IOException in any of the cases listed above
     * @throws UnsupportedFileOperationException if this operation is not supported by the underlying filesystem,
     * or is not implemented.
     */
    public abstract RandomAccessInputStream getRandomAccessInputStream() throws IOException, UnsupportedFileOperationException;

    /**
     * Returns a {@link RandomAccessOutputStream} to write the contents of this file with random access.
     * This file will be created as a zero-byte file if it does not yet exist.
     * Throws an <code>IOException</code> in any of the following cases:
     * <ul>
     *  <li>this file is a directory</li>
     *  <li>this file cannot be written</li>
     *  <li>an I/O error occurs</li>
     * </ul>
     * This method may never return <code>null</code>.
     *
     * <p>This {@link FileOperation#RANDOM_WRITE_FILE file operation} may or may not be supported by the underlying filesystem
     * -- {@link #isFileOperationSupported(FileOperation)} can be called to find out if it is. If the operation isn't
     * supported, a {@link UnsupportedFileOperation} will be thrown when this method is called.</p>
     *
     * @return a <code>RandomAccessOutputStream</code> to write the contents of this file with random access
     * @throws IOException in any of the cases listed above
     * @throws UnsupportedFileOperationException if this operation is not supported by the underlying filesystem,
     * or is not implemented.
     */
    public abstract RandomAccessOutputStream getRandomAccessOutputStream() throws IOException, UnsupportedFileOperationException;

    /**
     * Deletes this file and this file only (does not recurse on folders).
     * Throws an <code>IOException</code> in any of the following cases:
     * <ul>
     *  <li>if this file does not exist</li>
     *  <li>if this file is a non-empty directory</li>
     *  <li>if this file could not be deleted, for example because of insufficient permissions</li>
     *  <li>if an I/O error occurred</li>
     * </ul>
     *
     * <p>This {@link FileOperation#DELETE file operation} may or may not be supported by the underlying filesystem
     * -- {@link #isFileOperationSupported(FileOperation)} can be called to find out if it is. If the operation isn't
     * supported, a {@link UnsupportedFileOperation} will be thrown when this method is called.</p>
     *
     * @throws IOException if this file does not exist or could not be deleted
     * @throws UnsupportedFileOperationException if this operation is not supported by the underlying filesystem,
     * or is not implemented.
     */
    public abstract void delete() throws IOException, UnsupportedFileOperationException;

    /**
     * Renames this file to a specified destination file, overwriting the destination if it exists. If this file is a
     * directory, any file or directory it contains will also be moved.
     * After normal completion, this file will not exist anymore: {@link #exists()} will return <code>false</code>.
     *
     * <p>This method throws an {@link IOException} if the operation failed, for any of the following reasons:
     * <ul>
     *  <li>this file and the destination file are the same</li>
     *  <li>this file is a directory and a parent of the destination file (the operation would otherwise loop indefinitely)</li>
     *  <li>this file cannot be read</li>
     *  <li>this file cannot be written</li>
     *  <li>the destination file can not be written</li>
     *  <li>an I/O error occurred</li>
     * </ul>
     * </p>
     *
     * <p>This {@link FileOperation#RENAME file operation} may or may not be supported by the underlying filesystem
     * -- {@link #isFileOperationSupported(FileOperation)} can be called to find out if it is. If the operation isn't
     * supported, a {@link UnsupportedFileOperation} will be thrown when this method is called.</p>
     *
     * @param destFile file to rename this file to
     * @throws IOException in any of the error cases listed above
     * @throws UnsupportedFileOperationException if this operation is not supported by the underlying filesystem,
     * or is not implemented.
     */
    public abstract void renameTo(AbstractFile destFile) throws IOException, UnsupportedFileOperationException;

    /**
     * Remotely copies this file to a specified destination file, overwriting the destination if it exists.
     * If this file is a directory, any file or directory it contains will also be copied.
     *
     * <p>This method differs from {@link #copyTo(AbstractFile)} in that it performs a server-to-server copy of the
     * file(s), without having the file's contents go through to the local process. This operation should only be
     * implemented if it offers a performance advantage over a regular client-driven copy like
     * {@link #copyTo(AbstractFile)}, or if {@link FileOperation#WRITE_FILE} is not supported (output streams cannot be
     * retrieved) and thus a regular copy cannot succeed.</p>.
     *
     * <p>This method throws an {@link IOException} if the operation failed, for any of the following reasons:
     * <ul>
     *  <li>this file and the destination file are the same</li>
     *  <li>this file is a directory and a parent of the destination file (the operation would otherwise loop indefinitely)</li>
     *  <li>this file (or one if its children) cannot be read</li>
     *  <li>the destination file (or one of its children) can not be written</li>
     *  <li>an I/O error occurred</li>
     * </ul>
     * </p>
     *
     * <p>The behavior in the case of an error occurring in the midst of the transfer is unspecified: files that have
     * been copied (even partially) may or may not be left in the destination.<p/>
     *
     * @param destFile the destination file to copy this file to
     * @throws IOException in any of the error cases listed above
     * @throws UnsupportedFileOperationException if this operation is not supported by the underlying filesystem,
     * or is not implemented.
     */
    public abstract void copyRemotelyTo(AbstractFile destFile) throws IOException, UnsupportedFileOperationException;

    /**
     * Returns the free space (in bytes) on the disk/volume where this file is, <code>-1</code> if this information is
     * not available.
     *
     * <p>This {@link FileOperation#GET_FREE_SPACE file operation} may or may not be supported by the underlying filesystem
     * -- {@link #isFileOperationSupported(FileOperation)} can be called to find out if it is. If the operation isn't
     * supported, a {@link UnsupportedFileOperation} will be thrown when this method is called.</p>
     *
     * @return the free space (in bytes) on the disk/volume where this file is, <code>-1</code> if this information is
     * not available.
     * @throws IOException if an I/O error occurred
     * @throws UnsupportedFileOperationException if this operation is not supported by the underlying filesystem,
     * or is not implemented.
     */
    public abstract long getFreeSpace() throws IOException, UnsupportedFileOperationException;

    /**
     * Returns the total space (in bytes) of the disk/volume where this file is.
     *
     * <p>This {@link FileOperation#GET_TOTAL_SPACE file operation} may or may not be supported by the underlying filesystem
     * -- {@link #isFileOperationSupported(FileOperation)} can be called to find out if it is. If the operation isn't
     * supported, a {@link UnsupportedFileOperation} will be thrown when this method is called.</p>
     *
     * @return the total space (in bytes) of the disk/volume where this file is
     * @throws IOException if an I/O error occurred
     * @throws UnsupportedFileOperationException if this operation is not supported by the underlying filesystem,
     * or is not implemented.
     */
    public abstract long getTotalSpace() throws IOException, UnsupportedFileOperationException;

    /**
     * Returns the file Object of the underlying API providing access to the filesystem. The returned Object may expose
     * filesystem-specific functionalities that are not available in <code>AbstractFile</code>. Note however that the
     * returned Object type may change over time, if the underlying API used to provide access to the filesystem
     * changes, so this method should be used only as a last resort.
     *
     * <p>If the implemented filesystem has no such Object, <code>null</code> is returned.</p>
     *
     * @return the file Object of the underlying API providing access to the filesystem, <code>null</code> if there
     * is none
     */
    public abstract Object getUnderlyingFileObject();
}
