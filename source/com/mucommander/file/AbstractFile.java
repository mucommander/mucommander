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

import com.mucommander.file.compat.CompatURLStreamHandler;
import com.mucommander.file.filter.FileFilter;
import com.mucommander.file.filter.FilenameFilter;
import com.mucommander.file.impl.ProxyFile;
import com.mucommander.io.*;
import com.mucommander.process.AbstractProcess;
import com.mucommander.runtime.OsFamilies;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

/**
 * <code>AbstractFile</code> is the superclass of all files.
 *
 * <p>AbstractFile classes should never be instanciated directly. Instead, the {@link FileFactory} <code>getFile</code>
 * methods should be used to get a file instance from a path or {@link FileURL} location.</p>
 *
 * @see com.mucommander.file.FileFactory
 * @see com.mucommander.file.impl.ProxyFile
 * @author Maxence Bernard
 */
public abstract class AbstractFile implements FilePermissions {

    /** URL representing this file */
    protected FileURL fileURL;

    /** Default path separator */
    public final static String DEFAULT_SEPARATOR = "/";

    /** Indicates {@link #copyTo(AbstractFile)}/{@link #moveTo(AbstractFile)} *should* be used to copy/move the file (e.g. more efficient) */
    public final static int SHOULD_HINT = 0;
    /** Indicates {@link #copyTo(AbstractFile)}/{@link #moveTo(AbstractFile)} *should not* be used to copy/move the file (default) */
    public final static int SHOULD_NOT_HINT = 1;
    /** Indicates {@link #copyTo(AbstractFile)}/{@link #moveTo(AbstractFile)} *must* be used to copy/move the file (e.g. no other way to do so) */
    public final static int MUST_HINT = 2;
    /** Indicates {@link #copyTo(AbstractFile)}/{@link #moveTo(AbstractFile)} *must not* be used to copy/move the file (e.g. not implemented) */
    public final static int MUST_NOT_HINT = 3;

    /** Size of the read/write buffer */
    // Note: raising buffer size from 8192 to 65536 makes a huge difference in SFTP read transfer rates but beyond
    // 65536, no more gain (not sure why).
    public final static int IO_BUFFER_SIZE = 65536;

    /** Pattern matching Windows drive root folders, e.g. C:\ */
    protected final static Pattern windowsDriveRootPattern = Pattern.compile("^[a-zA-Z]{1}[:]{1}[\\\\]{1}$");


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
     * <li>For local files, the path is returned without the protocol and host parts (i.e. without file://localhost)
     * <li>For any other file protocol, the full URL including the protocol and host parts is returned (e.g. smb://192.168.1.1/root/blah)
     * </ul>
     *
     * <p>The returned path will always be free of any login and password and thus can be safely displayed or stored.</p>
     *
     * @return the absolute path to this file
     */
    public String getAbsolutePath() {
        FileURL fileURL = getURL();

        // For local files: return file's path 'sans' the protocol and host parts
        if(fileURL.getProtocol().equals(FileProtocols.FILE))
            return fileURL.getPath();

        // For any other file protocols: return the full URL that includes the protocol and host parts
        return fileURL.toString(false);
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
            FileURL canonicalURL = new FileURL(canonicalPath);
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
     * Returns <code>true</code> if this file is browsable. A file is considered browsable if it contains children files
     * that can be exposed by calling the <code>ls()</code> methods. {@link AbstractArchiveFile} implementations will
     * usually return <code>true</code>, as will directories (directories are always browsable).
     *
     * @return true if this file is browsable
     */
    public boolean isBrowsable() {
        return isDirectory() || (this instanceof AbstractArchiveFile);
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
     * Returns the root folder that contains this file either as a direct or an indirect child. If this file is already
     * a root folder (has no parent), <code>this</code> is returned.
     *
     * @return the root folder that contains this file
     * @throws IOException if the root file or one parent file could not be instanciated
     */
    public AbstractFile getRoot() throws IOException {
        AbstractFile parent;
        AbstractFile child = this; 
        while((parent=child.getParent())!=null && !parent.equals(child)) {
            child = parent;
        }
		
        return child;
    }


    /**
     * Returns <code>true</code> if this file is a root folder.
     *
     * <p>This default implementation characterizes root folders in the following way:
     * <ul>
     *  <li>For local files under Windows: if the path corresponds a drive's root ('C:\' for instance)
     *  <li>For local files under other OS: if the path is "/"
     *  <li>For any other file kinds: if the FileURL's path is '/'
     * </ul>
     * </p>
     *
     * @return <code>true</code> if this file is a root folder
     */
    public boolean isRoot() {
        String path = fileURL.getPath();

        if(fileURL.getProtocol().equals(FileProtocols.FILE))
            return OsFamilies.WINDOWS.isCurrent()?windowsDriveRootPattern.matcher(path).matches():path.equals("/");
        else
            return path.equals("/");
    }


    /**
     * Returns an <code>InputStream</code> to read this file's contents, starting at the specified offset (in bytes).
     * A <code>java.io.IOException</code> is thrown if the file doesn't exist. 
     *
     * <p>This method should be overridden whenever possible to provide a more efficient implementation, as this
     * default implementation uses {@link java.io.InputStream#skip(long)} which may just read bytes and discards them, 
     * which is very slow.</p>
     *
     * @param offset the offset in bytes from the beginning of the file, must be >0
     * @throws IOException if this file cannot be read or is a folder.
     * @return an <code>InputStream</code> to read this file's contents, skipping the specified number of bytes
     */
    public InputStream getInputStream(long offset) throws IOException {
        InputStream in = getInputStream();
		
        // Call InputStream.skip() until the specified number of bytes have been skipped
        long nbSkipped = 0;
        long n;
        while(nbSkipped<offset) {
            n = in.skip(offset-nbSkipped);
            if(n>0)
                nbSkipped += n;
        }

        return in;
    }
	

    /**
     * Copies the contents of the given <code>InputStream</code> to this file. The provided <code>InputStream</code>
     * will *NOT* be closed by this method.
     * 
     * <p>This method should be overridden by file protocols that do not offer a {@link #getOutputStream(boolean)}
     * implementation, but that can take an <code>InputStream</code> and use it to write the file.</p>
     *
     * <p>Read and write operations are buffered, with a buffer of {@link #IO_BUFFER_SIZE} bytes. For performance
     * reasons, this buffer is provided by {@link BufferPool}. There is no need to provide a BufferedInputStream.</p>
     *
     * <p>Copy progress can optionally be monitored by supplying a {@link com.mucommander.io.CounterInputStream}.</p>
     *
     * @param in the InputStream to read from
     * @param append if true, data written to the OutputStream will be appended to the end of this file. If false, any existing data will be overwritten.
     * @throws FileTransferException if something went wrong while reading from the InputStream or writing to this file
     */
    public void copyStream(InputStream in, boolean append) throws FileTransferException {
        OutputStream out;

        try {
            out = getOutputStream(append);
        }
        catch(IOException e) {
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
     * Checks the prerequisites of a copy (or move) operation.
     * Throws a {@link FileTransferException} in any of the following conditions are true, does nothing otherwise:
     * <ul>
     *   <li>this file does not exist</li>
     *   <li>the destination file exists</li>
     *   <li>this file and the destination file are the same, unless <code>allowCaseVariations</code> is <code>true</code>
     * and the destination filename is a case variation of the source</li>
     *   <li>this file is a parent of the destination file</li>
     * </ul>
     *
     * @param destFile the destination file to copy this file to
     * @param allowCaseVariations if true and the destination file is a case variation of source, no exception will be thrown
     * @throws FileTransferException in any of the cases listed above, use {@link FileTransferException#getReason()} to
     * know the reason.
     */
    protected final void checkCopyPrerequisites(AbstractFile destFile, boolean allowCaseVariations) throws FileTransferException {
        boolean isAllowedCaseVariation = false;

        // Throw an exception of a specific kind if the source and destination files are identical
        boolean filesEqual = this.equals(destFile);
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

        // Throw an exception if the destination file exists
        if(destFile.exists() && !isAllowedCaseVariation)
            throw new FileTransferException(FileTransferException.DESTINATION_EXISTS);
    }


    /**
     * Copies this file to a specified destination file. If this file is a directory, any file or directory
     * it contains will also be copied. An exception will be thrown if the destination file exists.
     *
     * <p>This method returns <code>true</code> if the operation was successfully completed, <code>false</code> if the
     * operation could not be performed because of unsatisfied conditions (not an error).
     * A {@link FileTransferException} if the operation was attempted but failed for any of the following reasons:
     * <ul>
     *  <li>the destination file exists</li>
     *  <li>this file and the destination file are the same</li>
     *  <li>this file is a directory and a parent of the destination file (operation would otherwise loop indefinitely)</li>
     *  <li>this file (or one if its child) could not be read</li>
     *  <li>the destination file (or one of its child) could not be written</li>
     *  <li>an I/O error occurred</li>
     * </ul>
     * </p>
     *
     * <p>This generic implementation will always attempt to copy files, thus either return <code>true</code> or
     * throw an exception, but will never return <code>false</code>. Symbolic links are skipped when encountered:
     * neither the link nor the linked file is copied. Also noteworthy is that no clean up is performed if an error
     * occurs in the midst of a transfer: files that have been copied (even partially) are left in the destination.</p>
     *
     * <p>This method should be overridden by filesystems which are able to provide a more efficient implementation --
     * in particular, network-based filesystems that can perform a server-to-server copy.</p>
     *
     * @param destFile the destination file to copy this file to
     * @return true if the operation could be successfully be completed, false if the operation could not be performed
     * because of unsatisfied conditions (not an error)
     * @throws FileTransferException in any of the cases listed above, use {@link FileTransferException#getReason()} to
     * know the reason.
     */
    public boolean copyTo(AbstractFile destFile) throws FileTransferException {
        checkCopyPrerequisites(destFile, false);

        // Copy the file and its contents if the file is a directory
        copyRecursively(this, destFile);

        return true;
    }

    /**
     * Returns a hint that indicates whether the {@link #copyTo(AbstractFile)} method should be used to
     * copy this file to the specified destination file, rather than copying the file 'manually', using
     * {@link #copyStream(InputStream, boolean)}, or {@link #getInputStream()} and {@link #getOutputStream(boolean)}.
     *
     * <p>Potential returned values are:
     * <ul>
     * <li>{@link #SHOULD_HINT} if copyTo() should be preferred (more efficient)
     * <li>{@link #SHOULD_NOT_HINT} if the file should rather be copied using copyStream()
     * <li>{@link #MUST_HINT} if the file can only be copied using copyTo(), that's the case when getOutputStream() or copyStream() is not implemented
     * <li>{@link #MUST_NOT_HINT} if the file can only be copied using copyStream()
     * </ul>
     * </p>
     *
     * <p>This default implementation returns {@link #SHOULD_NOT_HINT} as some granularity is lost when using
     *  <code>copyTo()</code> making it impossible to monitor progress when copying a file.
     * This method should be overridden when <code>copyTo()</code> should be favored over <code>copyStream()</code>.</p>
     *
     * @param destFile the destination file that is considered being copied
     * @return the hint int indicating whether the {@link #copyTo(AbstractFile)} method should be used
     */
    public int getCopyToHint(AbstractFile destFile) {
        return SHOULD_NOT_HINT;
    }


    /**
     * Moves this file to a specified destination file. If this file is a directory, any file or directory
     * it contains will also be moved. An exception will be thrown if the destination file exists.
     * After normal completion, this file will not exist anymore: {@link #exists()} will return <code>false</code>.
     *
     * <p>This method returns <code>true</code> if the operation was successfully completed, <code>false</code> if the
     * operation could not be performed because of unsatisfied conditions (not an error).
     * A {@link FileTransferException} if the operation was attempted but failed for any of the following reasons:
     * <ul>
     *  <li>the destination file exists</li>
     *  <li>this file and the destination file are the same</li>
     *  <li>this file is a directory and a parent of the destination file (operation would otherwise loop indefinitely)</li>
     *  <li>this file (or one if its child) could not be read</li>
     *  <li>this file )or one of its child) could not be written</li>
     *  <li>the destination file (or one of its children) could not be written</li>
     *  <li>an I/O error occurred</li>
     * </ul>
     * </p>
     *
     * <p>This generic implementation will always attempt to move files, thus either return <code>true</code> or
     * throw an exception, but will never return <code>false</code>.
     * Symbolic links are not moved to the destination when encountered: neither the link nor the linked file is moved,
     * and the symlink file is deleted.</p>
     *
     * <p>This implementation first copies the file and it contents (if any) and then deletes it. Deletion occurs only
     * after all files have been successfully copied. Also noteworthy is that no clean up is performed if an error
     * occurs in the midst of a transfer: files that have been copied (even partially) are left in the destination.</p>
     *
     * <p>This method should be overridden by filesystems which are able to provide a more efficient implementation --
     * in particular, network-based filesystems that can perform remote renaming.</p>
     *
     * @param destFile the destination file to move this file to
     * @return true if the operation could be successfully be completed, false if the operation could not be performed
     * because of unsatisfied conditions (not an error)
     * @throws FileTransferException in any of the cases listed above, use {@link FileTransferException#getReason()} to
     * know the reason.
     */
    public boolean moveTo(AbstractFile destFile) throws FileTransferException {
        checkCopyPrerequisites(destFile, false);

        // Copy the file and its contents if the file is a directory
        copyRecursively(this, destFile);

        // Note: the above code is the same as #copyTo(), but we don't want to avoid using #copyTo() so that both
        // moveTo() and copyTo() can be overridden separately.

        // Delete the source file and its contents now that it has been copied OK.
        // Note that the file won't be deleted if copyTo() failed (threw an IOException)
        try {
            deleteRecursively();
            return true;
        }
        catch(IOException e) {
            throw new FileTransferException(FileTransferException.DELETING_SOURCE);
        }
    }


    /**
     * Returns a hint that indicates whether the {@link #moveTo(AbstractFile)} method should be used to
     * move this file to the specified destination file, rather than moving the file using
     * {@link #copyStream(InputStream, boolean)} or {@link #getInputStream()} and {@link #getOutputStream(boolean)}.
     *
     * <p>Potential returned values are:
     * <ul>
     * <li>{@link #SHOULD_HINT} if copyTo() should be preferred (more efficient)
     * <li>{@link #SHOULD_NOT_HINT} if the file should rather be copied using copyStream()
     * <li>{@link #MUST_HINT} if the file can only be copied using copyTo(), that's the case when getOutputStream() or copyStream() is not implemented
     * <li>{@link #MUST_NOT_HINT} if the file can only be copied using copyStream()
     * </ul>
     * </p>
     *
     * <p>This default implementation returns {@link #SHOULD_HINT} if both this file and the specified destination file
     * use the same protocol and are located on the same host, {@link #SHOULD_NOT_HINT} otherwise.
     * This method should be overridden to return {@link #SHOULD_NOT_HINT} if the underlying file protocol doesn't not
     * allow direct move/renaming without copying the contents of the source (this) file.</p>
     *
     * @param destFile the destination file that is considered being copied
     * @return the hint int indicating whether the {@link #moveTo(AbstractFile)} method should be used
     */
    public int getMoveToHint(AbstractFile destFile) {
        // Return SHOULD_NOT if protocols differ
        if(!fileURL.getProtocol().equals(destFile.fileURL.getProtocol()))
          return SHOULD_NOT_HINT;

        // Are both fileURL's hosts equal ?
        // This test is a bit complicated because each of the hosts can potentially be null (e.g. smb://)
        String host = fileURL.getHost();
        String destHost = destFile.fileURL.getHost();
        boolean hostsEqual = host==null?(destHost==null||destHost.equals(host)):host.equals(destHost);

        // Return SHOULD_NOT if hosts differ
        if(!hostsEqual)
            return SHOULD_NOT_HINT;

        // Return SHOULD only if both files use the same AbstractFile class (not taking into account proxies).
        return destFile.getTopAncestor().getClass().equals(getTopAncestor().getClass())?SHOULD_HINT:SHOULD_NOT_HINT;
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
     */
    public void mkfile() throws IOException {
        if(exists())
            throw new IOException();

        getOutputStream(false).close();
    }


    /**
     * Returns the children files that this file contains, filtering out files that do not match the specified FileFilter.
     * For this operation to be successful, this file must be 'browsable', i.e. {@link #isBrowsable()} must return
     * <code>true</code>.
     *
     * @param filter the FileFilter to be used to filter files out from the list, may be <code>null</code>
     * @return the children files that this file contains
     * @throws IOException if this operation is not possible (file is not browsable) or if an error occurred.
     */
    public AbstractFile[] ls(FileFilter filter) throws IOException {
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
     */
    public AbstractFile[] ls(FilenameFilter filter) throws IOException {
        return filter==null?ls():filter.filter(ls());
    }


    /**
     * Returns this file's permissions as an int, UNIX octal style.
     * The permissions can be compared against {@link #READ_PERMISSION}, {@link #WRITE_PERMISSION}, {@link #EXECUTE_PERMISSION}
     * and {@link #USER_ACCESS}, {@link #GROUP_ACCESS} and {@link #OTHER_ACCESS} masks.
     *
     * <p>Implementation note: the default implementation of this method calls sequentially {@link #getPermission(int, int)} for
     * each permission and access (that's a total of 9 calls). This may affect performance on filesystems which need to perform
     * an I/O request to retrieve each permission individually. In that case, and if the fileystem allows to retrieve all
     * permissions at once, this method should be overridden.</p>
     *
     * @return permissions as an int, UNIX octal style.
     */
    public int getPermissions() {
        int bitShift = 0;
        int perms = 0;

        for(int a=OTHER_ACCESS; a<= USER_ACCESS; a++) {
            for(int p=EXECUTE_PERMISSION; p<=READ_PERMISSION; p=p<<1) {
                if(canGetPermission(a, p) && getPermission(a, p))
                    perms |= (1<<bitShift);

                bitShift++;
            }
        }

        return perms;
    }


    /**
     * Changes this file's permissions to the specified permissions int and returns <code>true</code> if
     * the operation was successful, <code>false</code> if at least one of the file permissions could not be changed.
     * The permissions int should be created using {@link #READ_PERMISSION}, {@link #WRITE_PERMISSION}, {@link #EXECUTE_PERMISSION}
     * and {@link #USER_ACCESS}, {@link #GROUP_ACCESS} and {@link #OTHER_ACCESS} masks combined with logical OR.
     *
     * <p>Implementation note: the default implementation of this method calls sequentially {@link #setPermission(int, int, boolean)},
     * for each permission and access (that's a total 9 calls). This may affect performance on filesystems which need
     * to perform an I/O request to change each permission individually. In that case, and if the fileystem allows
     * to change all permissions at once, this method should be overridden.</p>
     *
     * @param permissions the new permissions this file should have
     * @return true if the operation was successful, false if at least one of the file permissions could not be changed
     */
    public boolean setPermissions(int permissions) {
        int bitShift = 0;
        boolean success = true;

        for(int a=OTHER_ACCESS; a<= USER_ACCESS; a++) {
            for(int p=EXECUTE_PERMISSION; p<=READ_PERMISSION; p=p<<1) {
                if(canSetPermission(a, p))
                    success = setPermission(a, p, (permissions & (1<<bitShift))!=0) && success;

                bitShift++;
            }
        }

        return success;
    }


    /**
     * Returns a mask describing the permission bits that the filesystem can read and which can be returned by
     * {@link #getPermission(int, int)} and {@link #getPermissions()}. This allows to determine which permissions are
     * meaningful. 0 is returned if no permission can be read or if the filesystem doesn't have a notion of permissions,
     * 777 if all permissions can be read.
     *
     * <p>Implementation note: the default implementation of this method calls sequentially {@link #canGetPermission(int, int)},
     * for each permission and access (that's a total 9 calls). This method should be overridden if a more efficient
     * implementation can be provided. Usually, file permissions support is the same for all files on a filesystem. If
     * that is the case, this method should be overridden to return a static permission mask.</p>
     *
     * @return a bit mask describing the permission bits that the filesystem can read
     */
    public int getPermissionGetMask() {
        int bitShift = 0;
        int permsMask = 0;

        for(int a=OTHER_ACCESS; a<= USER_ACCESS; a++) {
            for(int p=EXECUTE_PERMISSION; p<=READ_PERMISSION; p=p<<1) {
                if(canGetPermission(a, p))
                    permsMask |= (1<<bitShift);

                bitShift++;
            }
        }

        return permsMask;
    }


    /**
     * Returns a mask describing the permission bits that can be changed by the filesystem when calling
     * {@link #setPermission(int, int, boolean)} and {@link #setPermissions(int)}.
     * 0 is returned if no permission can be set or if the filesystem doesn't have a notion of permissions,
     * 777 if all permissions can be set.
     *
     * <p>Implementation note: the default implementation of this method calls sequentially {@link #canGetPermission(int, int)},
     * for each permission and access (that's a total 9 calls). This method should be overridden if a more efficient
     * implementation can be provided. Usually, file permissions support is the same for all files on a filesystem. If
     * that is the case, this method should be overridden to return a static permission mask.</p>
     *
     * @return a bit mask describing the permission bits that the filesystem can change
     */
    public int getPermissionSetMask() {
        int bitShift = 0;
        int permsMask = 0;

        for(int a=OTHER_ACCESS; a<= USER_ACCESS; a++) {
            for(int p=EXECUTE_PERMISSION; p<=READ_PERMISSION; p=p<<1) {
                if(canSetPermission(a, p))
                    permsMask |= (1<<bitShift);

                bitShift++;
            }
        }

        return permsMask;
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
     * <p>The first character triplet for 'user' access will always be added to the permissions. Then the 'group' and 'other'
     * triplets will only be added if at least one of the user permission bits can be retrieved, as tested with
     * {@link #getPermissionGetMask()}.
     * Here are a couple examples to illustrate:
     * <ul>
     *  <li>a directory for which {@link #getPermissionGetMask()} returns 0 will return the string "d----", no matter
     * what {@link #getPermissions()} returns.
     *  <li>a regular file for which {@link #getPermissionGetMask()} returns 777 (full permissions support) and which
     * has read/write/executable permissions for all three 'user', 'group' and 'other' access types will return "-rwxrwxrwx".
     * </ul>
     * </p>
     *
     * @return a string representation of this file's permissions
     */
    public String getPermissionsString() {
        int availPerms = getPermissionGetMask();

        String s = "";
        s += isSymlink()?'l':isDirectory()?'d':'-';

        int perms = getPermissions();

        int bitShift = USER_ACCESS *3;

        // Permissions go by triplets (rwx), there are 3 of them for respectively 'owner', 'group' and 'other' accesses.
        // The first one ('owner') will always be displayed, regardless of the permission bit mask. 'Group' and 'other'
        // will be displayed only if the permission mask contains information about them (at least one permission bit).
        for(int a= USER_ACCESS; a>=OTHER_ACCESS; a--) {

            if(a== USER_ACCESS || (availPerms & (7<<bitShift))!=0) {
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
     */
    public void deleteRecursively() throws IOException {
        deleteRecursively(this);
    }


    ///////////////////
    // Final methods //
    ///////////////////

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
     * An <code>IOException</code> may be thrown if the child file could not be instanciated but the returned file
     * instance should never be <code>null</code>.
     *
     * @param relativePath the child's path, relative to this file's path
     * @return an AbstractFile representing the requested child file, never null
     * @throws IOException if the child file could not be instanciated
     */
    public final AbstractFile getChild(String relativePath) throws IOException {
        FileURL childURL = (FileURL)getURL().clone();
        childURL.setPath(addTrailingSeparator(childURL.getPath())+ relativePath);

        return FileFactory.getFile(childURL, true);
    }

    /**
     * Returns a direct child of this file, whose path is the concatenation of this file's path and the given filename.
     * An <code>IOException</code> will be thrown in any of the following cases:
     * <ul>
     *  <li>if the filename contains one or several path separator (the file would not be a direct child)</li>
     *  <li>if the child file could not be instanciated</li>
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
     * Convience method that returns this file's parent, <code>null</code> if it doesn't have one or if it couldn't
     * be instanciated. This method is less granular than {@link #getParent} but is convenient in cases where no
     * distinction is made between having no parent and not being able to instanciate it.
     *
     * @return this file's parent, <code>null</code> if it doesn't have one or if it couldn't be instanciated
     */
    public final AbstractFile getParentSilently() {
        try {
            return getParent();
        }
        catch(IOException e) {
            return null;
        }
    }


    /**
     * Convenience method that creates a directory as a direct child of this directory.
     * This method will fail if this file is not a directory.
     *
     * @param name name of the directory to create
     * @throws IOException if the directory could not be created, either because the file already exists or for any
     * other reason.
     */
    public final void mkdir(String name) throws IOException {
        getChild(name).mkdir();
    }


    /**
     * Creates this file as a directory and any parent directory that does not already exist. This method will fail
     * (throw an <code>IOException</code>) if this file already exists. It may also fail because of an I/O error ;
     * in this case, this method will not remove the parent directories it has created (if any).
     *
     * @throws IOException if this file already exists or if an I/O error occurred.
     */
    public final void mkdirs() throws IOException {
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
     */
    public final void mkfile(String name) throws IOException {
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
     * Returns the first ancestor of this file that is an instance of the given Class or of a subclass of the given
     * Class, or <code>this</code> if this instance's class matches those criteria. Returns <code>null</code> if this
     * file has no such ancestor.
     * Note that the specified must correspond to an <code>AbstractFile</code> subclass. Specifying any other Class will
     * always yield to this method returning <code>null</code>. Also note that this method will always return
     * <code>this</code> if <code>AbstractFile.class</code> is specified.
     *
     * @param abstractFileClass a Class corresponding to an AbstractFile subclass
     * @return the first ancestor of this file that is an instance of the given Class or of a subclass of the given
     * Class, or <code>this</code> if this instance's class matches those criteria. Returns <code>null</code> if this
     * file has no such ancestor.
     */
    public final AbstractFile getAncestor(Class abstractFileClass) {
        AbstractFile ancestor = this;
        AbstractFile lastAncestor;

        do {
            if(abstractFileClass.isAssignableFrom(ancestor.getClass()))
                return ancestor;

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
    public final boolean hasAncestor(Class abstractFileClass) {
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
     * is an AbstractArchiveFile or an ancestor of AbstractArchiveFile, <code>this</code> is returned. If this file
     * is not contained by an archive or is not an archive, <code>null</code> is returned.
     *
     * @return the parent AbstractArchiveFile that contains this file
     */
    public final AbstractArchiveFile getParentArchive() {
        if(hasAncestor(AbstractArchiveFile.class))
            return (AbstractArchiveFile)getAncestor(AbstractArchiveFile.class);
        else if(hasAncestor(ArchiveEntryFile.class))
            return ((ArchiveEntryFile)getAncestor(ArchiveEntryFile.class)).getArchiveFile();

        return null;
    }


    /**
     * Returns an icon representing this file, using the default {@link com.mucommander.file.icon.FileIconProvider}
     * registered in {@link FileFactory}. The specified preferred resolution will be used as a hint, but the returned
     * icon may have different dimension; see {@link com.mucommander.file.icon.FileIconProvider#getFileIcon(AbstractFile, java.awt.Dimension)}
     * for full details.
     * This method may return <code>null</code> if the JVM is running on a headless environment.
     *
     * @param preferredResolution the preferred icon resolution
     * @return an icon representing this file, <code>null</code> if the JVM is running on a headless environment
     * @see com.mucommander.file.FileFactory#getDefaultFileIconProvider()
     * @see com.mucommander.file.icon.FileIconProvider#getFileIcon(AbstractFile, java.awt.Dimension)
     */
    public final Icon getIcon(Dimension preferredResolution) {
        return FileFactory.getDefaultFileIconProvider().getFileIcon(this, preferredResolution);
    }

    /**
     * Returns an icon representing this file, using the default {@link com.mucommander.file.icon.FileIconProvider}
     * registered in {@link FileFactory}. The default preferred resolution for the icon is 16x16 pixels.
     * This method may return <code>null</code> if the JVM is running on a headless environment.
     *
     * @return an icon representing this file, <code>null</code> if the JVM is running on a headless environment
     * @see com.mucommander.file.FileFactory#getDefaultFileIconProvider()
     * @see com.mucommander.file.icon.FileIconProvider#getFileIcon(AbstractFile, java.awt.Dimension)
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
     */
    public final String calculateChecksum(String algorithm) throws IOException, NoSuchAlgorithmException {
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
     */
    public final String calculateChecksum(MessageDigest messageDigest) throws IOException {
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
    protected final String addTrailingSeparator(String path) {
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

            AbstractFile child;
            AbstractFile destChild;
            for(int i=0; i<children.length; i++) {
                child = children[i];
                try {
                    destChild = destFile.getDirectChild(child.getName());
                }
                catch(IOException e) {
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
                destFile.copyStream(in, false);
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
     */
    protected final void deleteRecursively(AbstractFile file) throws IOException {
        if(file.isDirectory() && !file.isSymlink()) {
            AbstractFile children[] = file.ls();
            for(int i=0; i<children.length; i++)
                deleteRecursively(children[i]);
        }

        file.delete();
    }


    ////////////////////
    // Static methods //
    ////////////////////

    /**
     * Returns the given filename's extension, <code>null</code> if the filename doesn't have an extension.
     *
     * <p>A filename has an extension if and only if:<br/>
     * - it contains at least one <code>.</code> character<br/>
     * - the last <code>.</code> is not the last character of the filename<br/>
     * - the last <code>.</code> is not the first character of the filename</p>
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
        // Use BufferPool to reuse any available buffer of the same size
        byte buffer[] = BufferPool.getArrayBuffer(IO_BUFFER_SIZE);

        try {
            int nbRead;
            while(true) {
                try {
                    nbRead = in.read(buffer, 0, buffer.length);
                }
                catch(IOException e) {
                    throw new FileTransferException(FileTransferException.READING_SOURCE);
                }

                if(nbRead==-1)
                    break;

                messageDigest.update(buffer, 0, nbRead);
            }

            return ByteUtils.toHexString(messageDigest.digest());
        }
        finally {
            // Make the buffer available for further use
            BufferPool.releaseArrayBuffer(buffer);
        }
    }

    ////////////////////////
    // Overridden methods //
    ////////////////////////

    /**
     * Tests a file for equality: returns <code>true</code> if the given file has the same canonical path,
     * as returned by {@link #getCanonicalPath()}.
     *
     * <p>It is noteworthy that this method uses <code>java.lang.String#equals(Object)</code> to compare paths, which
     * in some rare cases may return <code>false</code> for non-ascii/Unicode paths that have the same written
     * representation but are not equal according to <code>java.lang.String#equals(Object)</code>. Handling such cases
     * would require a locale-aware String comparison which is not an option here.</p>
     *
     * <p>This method should be overriden for network-based filesystems for which a host can have multiple
     * path representations (hostname and IP address).</p>
     */
    public boolean equals(Object f) {
        if(f==null || !(f instanceof AbstractFile))
            return false;
		
        return getCanonicalPath(false).equals(((AbstractFile)f).getCanonicalPath(false));
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
     * Returns <code>true</code> if this file's date can be changed using {@link #changeDate(long)}. It's important
     * to note that a <code>true</code> return value doesn't mean that a call to {@link #changeDate(long)} will
     * necessarily succeed ; it could fail because of unsufficient permissions or simply because of a low-level I/O ;
     * but it should at least ensure that {@link #changeDate(long)} is implemented and has a chance of succeeding.   
     *
     * @return <code>true</code> if this file's date can be changed using {@link #changeDate(long)}
     */
    public abstract boolean canChangeDate();

    /**
     * Changes last modified date and returns <code>true</code> if date was changed successfully, <code>false</code>
     * if the operation could not be completed, either because this method is not implemented for this file type, or
     * because of insufficient permissions or a low-level I/O error.
     *
     * @param lastModified last modified date, in milliseconds since the epoch (00:00:00 GMT, January 1, 1970)
     * @return <code>true</code> if date was changed successfully.
     */
    public abstract boolean changeDate(long lastModified);

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
     * @throws IOException if the parent file could not be instanciated
     */
    public abstract AbstractFile getParent() throws IOException;
	
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
     * Returns <code>true</code> if this file has the specified permission enabled for the given access type.
     * If the permission flag for the access type is not supported (use {@link #getPermissionGetMask()} or
     * {@link #canGetPermission(int, int)} to determine that), the return value will be meaningless and therefore
     * should not be taken into account.
     *
     * @param access {@link #READ_PERMISSION}, {@link #WRITE_PERMISSION} or {@link #EXECUTE_PERMISSION}
     * @param permission {@link #USER_ACCESS}, {@link #GROUP_ACCESS} or {@link #OTHER_ACCESS}
     * @return true if the file has the specified permission flag enabled for the access type
     */
    public abstract boolean getPermission(int access, int permission);

    /**
     * Changes the specified permission flag for the given access type. If the permission bit in the access type is not
     * supported (use {@link #getPermissionSetMask()} or {@link #canSetPermission(int, int)} to determine that),
     * calling this method will have no effect.
     *
     * @param access {@link #READ_PERMISSION}, {@link #WRITE_PERMISSION} or {@link #EXECUTE_PERMISSION}
     * @param permission {@link #USER_ACCESS}, {@link #GROUP_ACCESS} or {@link #OTHER_ACCESS}
     * @param enabled true to enable the flag, false to disable it
     * @return true if the permission flag was successfully set for the access type
     */
    public abstract boolean setPermission(int access, int permission, boolean enabled);

    /**
     * Returns <code>true</code> if this file can retrieve the specified permission flag for the given access type.
     *
     * @param access {@link #READ_PERMISSION}, {@link #WRITE_PERMISSION} or {@link #EXECUTE_PERMISSION}
     * @param permission {@link #USER_ACCESS}, {@link #GROUP_ACCESS} or {@link #OTHER_ACCESS}
     * @return true if this file can retrieve the specified permission flag for the given access type
     */
    public abstract boolean canGetPermission(int access, int permission);

    /**
     * Returns <code>true</code> if this file can change the specified permission flag for the given access type.
     *
     * @param access {@link #READ_PERMISSION}, {@link #WRITE_PERMISSION} or {@link #EXECUTE_PERMISSION}
     * @param permission {@link #USER_ACCESS}, {@link #GROUP_ACCESS} or {@link #OTHER_ACCESS}
     * @return true if this file can change the specified permission flag for the given access type
     */
    public abstract boolean canSetPermission(int access, int permission);

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
     *  <li>this file is browsable (as reported by {@link #isBrowsable()} but not a directory</li>
     * </ul> 
     *
     * @return <code>true</code> if this file is a directory, <code>false</code> in any of the cases listed above
     */
    public abstract boolean isDirectory();

    /**
     * Returns <code>true</code> if this file is a symbolic link. Symbolic links need to be handled with special care,
     * especially when manipulating files recursively.
     *
     * @return <code>true</code> if this file is a symbolic link
     */
    public abstract boolean isSymlink();
	
    /**
     * Returns the children files that this file contains. For this operation to be successful, this file must be
     * 'browsable', i.e. {@link #isBrowsable()} must return <code>true</code>.
     * This method may return a zero-length array if it has no children but may never return <code>null</code>.
     *
     * @return the children files that this file contains
     * @throws IOException if this operation is not possible (file is not browsable) or if an error occurred.
     */
    public abstract AbstractFile[] ls() throws IOException;

    /**
     * Creates this file as a directory. This method will fail (throw an <code>IOException</code>) if this file
     * already exists.
     *
     * @throws IOException if the directory could not be created, either because this file already exists or for any
     * other reason.
     */
    public abstract void mkdir() throws IOException;

    /**
     * Returns an <code>InputStream</code> to read the contents of this file.
     * Throws an <code>IOException</code> in any of the following cases:
     * <ul>
     *  <li>this file does not exist</li>
     *  <li>this file is a directory</li>
     *  <li>this file cannot be read</li>
     *  <li>a {@link RandomAccessInputStream} cannot be provided because the underlying file protocol doesn't have
     * random access support (see {@link #hasRandomAccessInputStream()}</li>
     *  <li>an I/O error occurs</li>
     * </ul>
     * This method may never return <code>null</code>.
     *
     * @return an <code>InputStream</code> to read the contents of this file
     * @throws IOException in any of the cases listed above
     */
    public abstract InputStream getInputStream() throws IOException;

    /**
     * Returns an <code>OuputStream</code> to write the contents of this file, appending or overwriting the existing
     * contents. This file will be created as a zero-byte file if it does not yet exist.
     * Throws an <code>IOException</code> in any of the following cases:
     * <ul>
     *  <li>this file is a directory</li>
     *  <li>this file cannot be written</li>
     *  <li><code>append</code> is specified but not supported</li>
     *  <li>an I/O error occurs</li>
     * </ul>
     * This method may never return <code>null</code>.
     *
     * @param append if true, data written to the OutputStream will be appended to the end of this file. If false,
     * any existing data this file contains will be discarded and overwritten.
     * @return an <code>OuputStream</code> to write the contents of this file
     * @throws IOException in any of the cases listed above
     */
    public abstract OutputStream getOutputStream(boolean append) throws IOException;

    /**
     * Returns <code>true</code> if the underlying filesystem has support for random access input streams.
     * Note that of <code>true</code> is returned, this doesn't necessarily mean that
     * {@link #getRandomAccessInputStream()} will return a {@link RandomAccessInputStream}, it might still throw
     * an <code>IOException</code> if random access to the file cannot be provided.
     *
     * @return <code>true</code> if the underlying filesystem has support for random access input streams
     */
    public abstract boolean hasRandomAccessInputStream();

    /**
     * Returns a {@link RandomAccessInputStream} to read the contents of this file with random access.
     * Throws an <code>IOException</code> in any of the following cases:
     * <ul>
     *  <li>this file does not exist</li>
     *  <li>this file is a directory</li>
     *  <li>this file cannot be read</li>
     *  <li>a {@link RandomAccessInputStream} cannot be provided because the underlying file protocol doesn't have
     * random access support (see {@link #hasRandomAccessInputStream()}</li>
     *  <li>an I/O error occurs</li>
     * </ul>
     * This method may never return <code>null</code>.
     *
     * @return a <code>RandomAccessInputStream</code> to read the contents of this file with random access
     * @throws IOException in any of the cases listed above
     */
    public abstract RandomAccessInputStream getRandomAccessInputStream() throws IOException;

    /**
     * Returns <code>true</code> if the underlying filesystem has support for random access output streams.
     * Note that of <code>true</code> is returned, this doesn't necessarily mean that
     * {@link #getRandomAccessOutputStream()} will return a {@link RandomAccessOutputStream}, it might still throw
     * an <code>IOException</code> if random access to the file cannot be provided.
     *
     * @return <code>true</code> if the underlying filesystem has support for random access output streams
     */
    public abstract boolean hasRandomAccessOutputStream();

    /**
     * Returns a {@link RandomAccessOutputStream} to write the contents of this file with random access.
     * This file will be created as a zero-byte file if it does not yet exist.
     * Throws an <code>IOException</code> in any of the following cases:
     * <ul>
     *  <li>this file is a directory</li>
     *  <li>this file cannot be written</li>
     *  <li>a {@link RandomAccessOutputStream} cannot be provided because the underlying file protocol doesn't have
     * random access support (see {@link #hasRandomAccessOutputStream()}</li>
     *  <li>an I/O error occurs</li>
     * </ul>
     * This method may never return <code>null</code>.
     *
     * @return a <code>RandomAccessOutputStream</code> to write the contents of this file with random access
     * @throws IOException in any of the cases listed above
     */
    public abstract RandomAccessOutputStream getRandomAccessOutputStream() throws IOException;

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
     * @throws IOException if this file does not exist or could not be deleted
     */	
    public abstract void delete() throws IOException;
	
    /**
     * Returns the free space (in bytes) on the disk/volume where this file is, <code>-1</code> if this information is
     * not available.
     *
     * @return the free space (in bytes) on the disk/volume where this file is, <code>-1</code> if this information is
     * not available.
     */
    public abstract long getFreeSpace();

    /**
     * Returns the total space (in bytes) of the disk/volume where this file is, -1 if this information is not available. 
     *
     * @return the total space (in bytes) of the disk/volume where this file is, -1 if this information is not available
     */
    public abstract long getTotalSpace();

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


    /**
     * Returns <code>true</code> if it's possible to run processes on the underlying file system.
     * @return <code>true</code> if it's possible to run processes on the underlying file system, <code>false</code> otherwise.
     */
    public abstract boolean canRunProcess();

    /**
     * Creates a process executing the specified command tokens using this file as a working directory.
     * @param  tokens                        command and its arguments for the process to create.
     * @return                               a process executing the specified command tokens using this file as a working directory.
     * @throws IOException                   thrown if an error occured while creating the process, if the current file is not a directory or if the operation is not supported.
     */
    public abstract AbstractProcess runProcess(String[] tokens) throws IOException;
}
