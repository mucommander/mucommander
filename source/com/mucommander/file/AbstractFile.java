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

import com.mucommander.PlatformManager;
import com.mucommander.file.filter.FileFilter;
import com.mucommander.file.filter.FilenameFilter;
import com.mucommander.file.impl.ProxyFile;
import com.mucommander.io.BufferPool;
import com.mucommander.io.FileTransferException;
import com.mucommander.io.RandomAccessInputStream;
import com.mucommander.io.RandomAccessOutputStream;
import com.mucommander.process.AbstractProcess;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
     * <p>The returned path will always be free of any login and password and thus can be safely displayed or stored.
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
     * Returns <code>true</code> if this file is a parent folder of the given file, or if the 2 files are equal.
     *
     * @param file the AbstractFile to test
     * @return true if this file is a parent folder of the given file, or if the 2 files are equal
     */
    public boolean isParentOf(AbstractFile file) {
        return isBrowsable() && file.getCanonicalPath(true).startsWith(getCanonicalPath(true));
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
     */
    public AbstractFile getRoot() {
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
     *
     * @return <code>true</code> if this file is a root folder
     */
    public boolean isRoot() {
        String path = fileURL.getPath();

        if(fileURL.getProtocol().equals(FileProtocols.FILE))
            return PlatformManager.isWindowsFamily()?windowsDriveRootPattern.matcher(path).matches():path.equals("/");
        else
            return path.equals("/");
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
     * Returns an <code>InputStream</code> to read this file's contents, starting at the specified offset (in bytes).
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
     * implementation, but that can take an <code>InputStream</code> and use it to write the file.
     *
     * <p>Read and write operations are buffered, with a buffer of {@link #IO_BUFFER_SIZE} bytes. For performance
     * reasons, this buffer is provided by {@link BufferPool}. There is no need to provide a BufferedInputStream.
     *
     * <p>Copy progress can optionally be monitored by supplying a {@link com.mucommander.io.CounterInputStream}.
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
            copyStream(in, out);
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
     * Copies this file to another specified one, overwriting the contents of the destination file (if any).
     * Returns <code>true</code> if the operation could be successfully be completed, <code>false</code> if the
     * operation could not be performed because of unsatisfied conditions (not an error), or throws an
     * {@link FileTransferException} if the operation was attempted but failed.
     *
     * <p>This generic implementation copies this file to the destination one, overwriting any data it contains.
     * The operation will always be attempted, thus will either return <code>true</code> or throw an exception, but
     * will never return <code>false</code>.</p>
     *
     * <p>This method should be overridden by file protocols which are able to perform a server-to-server copy.</p>
     *
     * @param destFile the destination file this file should be copied to
     * @return true if the operation could be successfully be completed, false if the operation could not be performed
     * because of unsatisfied conditions (not an error)
     * @throws FileTransferException if this file or the destination could not be written or if the operation failed
     * for any other reason (use {@link FileTransferException#getReason()} to get the reason of the failure).
     */
    public boolean copyTo(AbstractFile destFile) throws FileTransferException {
        // Throw a specific FileTransferException if source and destination files are identical
        if(this.equals(destFile))
            throw new FileTransferException(FileTransferException.SOURCE_AND_DESTINATION_IDENTICAL);
        
        InputStream in;

        try {
            in = getInputStream();
        }
        catch(IOException e) {
            throw new FileTransferException(FileTransferException.OPENING_SOURCE);
        }

        try {
            destFile.copyStream(in, false);
            return true;
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

    
    /**
     * Returns a hint that indicates whether the {@link #copyTo(AbstractFile)} method should be used to
     * copy this file to the specified destination file, rather than copying the file using
     * {@link #copyStream(InputStream, boolean)} or {@link #getInputStream()} and {@link #getOutputStream(boolean)}.
     *
     * <p>Potential returned values are:
     * <ul>
     * <li>{@link #SHOULD_HINT} if copyTo() should be preferred (more efficient)
     * <li>{@link #SHOULD_NOT_HINT} if the file should rather be copied using copyStream()
     * <li>{@link #MUST_HINT} if the file can only be copied using copyTo(), that's the case when getOutputStream() or copyStream() is not implemented
     * <li>{@link #MUST_NOT_HINT} if the file can only be copied using copyStream()
     * </ul>
     *
     * <p>This default implementation returns {@link #SHOULD_NOT_HINT} as some granularity is lost when using
     *  <code>copyTo()</code> making it impossible to monitor progress when copying a file.
     * This method should be overridden when <code>copyTo()</code> should be favored over <code>copyStream()</code>.
     *
     * @param destFile the destination file that is considered being copied
     * @return the hint int indicating whether the {@link #copyTo(AbstractFile)} method should be used
     */
    public int getCopyToHint(AbstractFile destFile) {
        return SHOULD_NOT_HINT;
    }


    /**
     * Moves this file to another specified one. Returns <code>true</code> if the operation was successfully
     * completed, <code>false</code> if the operation could not be performed because of unsatisfied conditions
     * (not an error), or throws an {@link FileTransferException} if the operation was attempted but failed.
     *
     * <p>This generic implementation copies this file to the destination one, overwriting any data it contains,
     * and if (and only if) the copy was successful, deletes the original file (this file). The operation will always
     * be attempted, thus will either return <code>true</code> or throw an exception, but will never return
     * <code>false</code>.
     *
     * <p>This method should be overridden by file protocols which are able to rename files.</p>
     *
     * @param destFile the destination file this file should be moved to
     * @return true if the operation could be successfully be completed, false if the operation could not be performed
     * because of unsatisfied conditions (not an error)
     * @throws FileTransferException if this file or the destination could be written or if the operation failed
     * for any other reason (use {@link FileTransferException#getReason()} to get the reason of the failure).
     */
    public boolean moveTo(AbstractFile destFile) throws FileTransferException {
        // Throw a specific FileTransferException if source and destination files are identical
        if(this.equals(destFile))
            throw new FileTransferException(FileTransferException.SOURCE_AND_DESTINATION_IDENTICAL);

        copyTo(destFile);

        // The file won't be deleted if copyTo() failed (threw an IOException)
        try {
            delete();
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
     *
     * <p>This default implementation returns {@link #SHOULD_HINT} if both this file and the specified destination file
     * use the same protocol and are located on the same host, {@link #SHOULD_NOT_HINT} otherwise.
     * This method should be overridden to return {@link #SHOULD_NOT_HINT} if the underlying file protocol doesn't not
     * allow direct move/renaming without copying the contents of the source (this) file.
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
     * Creates this file as a normal file. This method will fail if this file already exists.
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
     * should be overridden if a more efficient implementation can be provided by subclasses.
     *
     * @param filter the FilenameFilter to be used to filter out files from the list, may be <code>null</code>
     * @return the children files that this file contains
     * @throws IOException if this operation is not possible (file is not browsable) or if an error occurred.
     */
    public AbstractFile[] ls(FilenameFilter filter) throws IOException {
        return filter==null?ls():filter.filter(ls());
    }


    /**
     * Convenience method that sets/unsets a bit in the given permission int.
     *
     * @param permissions the permission int
     * @param bit the bit to set
     * @param enabled true to enable the bit, false to disable it
     * @return the modified permission int
     */
    protected static int setPermissionBit(int permissions, int bit, boolean enabled) {
        if(enabled)
            permissions |= bit;
        else
            permissions &= ~bit;

        return permissions;
    }


    /**
     * Returns this file's permissions as an int, UNIX octal style.
     * The permissions can be compared against {@link #READ_PERMISSION}, {@link #WRITE_PERMISSION}, {@link #EXECUTE_PERMISSION}
     * and {@link #USER_ACCESS}, {@link #GROUP_ACCESS} and {@link #OTHER_ACCESS} masks.
     *
     * <p>Implementation note: the default implementation of this method calls sequentially {@link #getPermission(int, int)} for
     * each permission and access (that's a total of 9 calls). This may affect performance on filesystems which need to perform
     * an I/O request to retrieve each permission individually. In that case, and if the fileystem allows to retrieve all
     * permissions at once, this method should be overridden.
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
     * to change all permissions at once, this method should be overridden.
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
     * that is the case, this method should be overridden to return a static permission mask.
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
     * that is the case, this method should be overridden to return a static permission mask.
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
     * @param relativePath the child's path, relatively to this file's path
     * @return an AbstractFile representing the requested child file, never null
     * @throws IOException if the child file could not be instanciated
     */
    public final AbstractFile getChild(String relativePath) throws IOException {
        FileURL childURL = (FileURL)getURL().clone();
        childURL.setPath(addTrailingSeparator(childURL.getPath())+ relativePath);

        return FileFactory.getFile(childURL, true);
    }

    /**
     * Returns a child of this file, whose path is the concatenation of this file's path and the given filename.
     * Although this method does not enforce it, the specified filename should not contain any separator character,
     * except for a trailing one.<br/>
     * An <code>IOException</code> may be thrown if the child file could not be instanciated but the returned file 
     * instance should never be <code>null</code>.
     *
     * <p>Although {@link #getChild} can be used to retrieve a direct child file, this method should be favored because
     * it allows to use this file instance as the parent of the returned child file.</p>
     *
     * @param filename the child's filename
     * @return an AbstractFile representing the requested direct child file, never null
     * @throws IOException if the child file could not be instanciated
     */
    public final AbstractFile getDirectChild(String filename) throws IOException {
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
     */
    public final void mkdir(String name) throws IOException {
        getChild(name).mkdir();
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
     * Convenience method that returns the parent {@link AbstractArchiveFile} that contains this file. If this file
     * is an AbstractArchiveFile or an ancestor of AbstractArchiveFile, <code>this</code> is returned. If this file
     * is not contained by an archive, <code>null</code> is returned.
     *
     * @return the parent AbstractArchiveFile that contains this file
     */
    public final AbstractArchiveFile getParentArchive() {
        AbstractArchiveFile archiveFile = null;
        if(hasAncestor(AbstractArchiveFile.class))
            return (AbstractArchiveFile)getAncestor(AbstractArchiveFile.class);
        else if(hasAncestor(ArchiveEntryFile.class))
            return ((ArchiveEntryFile)getAncestor(ArchiveEntryFile.class)).getArchiveFile();

        return archiveFile;
    }


    /**
     * Returns an icon representing this file, using the default {@link com.mucommander.file.icon.FileIconProvider}
     * registered in {@link FileFactory}. The specified preferred resolution will be used as a hint, but the returned
     * icon may have different dimension; see {@link com.mucommander.file.icon.FileIconProvider#getFileIcon(AbstractFile, java.awt.Dimension)}
     * for full details.
     * @param preferredResolution the preferred icon resolution
     * @return an icon representing this file
     * @see com.mucommander.file.FileFactory#getDefaultFileIconProvider()
     * @see com.mucommander.file.icon.FileIconProvider#getFileIcon(AbstractFile, java.awt.Dimension)
     */
    public final Icon getIcon(Dimension preferredResolution) {
        return FileFactory.getDefaultFileIconProvider().getFileIcon(this, preferredResolution);
    }

    /**
     * Returns an icon representing this file, using the default {@link com.mucommander.file.icon.FileIconProvider}
     * registered in {@link FileFactory}. The default preferred resolution for the icon is 16x16 pixels.
     *
     * @return an icon representing this file
     * @see com.mucommander.file.FileFactory#getDefaultFileIconProvider()
     * @see com.mucommander.file.icon.FileIconProvider#getFileIcon(AbstractFile, java.awt.Dimension)
     */
    public final Icon getIcon() {
        // Note: the Dimension object is created here instead of returning a final static field, because creating
        // a Dimension object triggers the AWT and Swing classes loading. Since these classes are not
        // needed in a headless environment, we want them to be loaded only if strictly necessary.
        return getIcon(new java.awt.Dimension(16, 16));
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
     * Copies the contents of the given <code>InputStream</code> to the specified </code>OutputStream</code>
     * and throws an IOException if something went wrong. The streams will *NOT* be closed by this method.
     *
     * <p>Read and write operations are buffered, with a buffer of {@link #IO_BUFFER_SIZE} bytes. For performance
     * reasons, this buffer is provided by {@link BufferPool}. There is no need to provide a BufferedInputStream.
     * A BufferedOutputStream also isn't necessary, unless this method is called repeatedly with the same OutputStream
     * and with potentially small InputStream (smaller than {@link #IO_BUFFER_SIZE}: in this case, providing a
     * BufferedOutputStream will further improve performance by grouping calls to the underlying OutputStream write method.
     *
     * <p>Copy progress can optionally be monitored by supplying a {@link com.mucommander.io.CounterInputStream}
     * and/or {@link com.mucommander.io.CounterOutputStream}.
     *
     * @param in the InputStream to read from
     * @param out the OutputStream to write to
     * @throws FileTransferException if something went wrong while reading from or writing to one of the provided streams
     */
    public static void copyStream(InputStream in, OutputStream out) throws FileTransferException {
        // Use BufferPool to reuse any available buffer of the same size
        byte buffer[] = BufferPool.getBuffer(IO_BUFFER_SIZE);
        try {
            // Copies the InputStream's content to the OutputStream chunks by chunks
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

                try {
                    out.write(buffer, 0, nbRead);
                }
                catch(IOException e) {
                    throw new FileTransferException(FileTransferException.WRITING_DESTINATION);
                }
            }
        }
        finally {
            // Make the buffer available for further use
            BufferPool.releaseBuffer(buffer);
        }
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    /**
     * Tests a file for equality: returns <code>true</code> if the given file has the same canonical path,
     * as returned by {@link #getCanonicalPath()}.
     *
     * <p>This method should be overriden for network-based filesystems for which a host can have multiple
     * path representations (hostname and IP address).
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
     * Returns this file's size in bytes, <code>-1</code> if unknown.
     *
     * @return this file's size in bytes, <code>-1</code> if unknown
     */
    public abstract long getSize();
	
    /**
     * Returns this file's parent, <code>null</code> if it doesn't have any parent.
     *
     * @return this file's parent, <code>null</code> if it doesn't have any parent
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
     * Returns <code>true</code> if this file is a regular directory, and not just a 'browsable' file.
     *
     * @return <code>true</code> if this file is a directory
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
     *
     * @return the children files that this file contains
     * @throws IOException if this operation is not possible (file is not browsable) or if an error occurred.
     */
    public abstract AbstractFile[] ls() throws IOException;

    /**
     * Creates this file as a directory. This method will fail if this file already exists.
     *
     * @throws IOException if the directory could not be created, either because this file already exists or for any
     * other reason.
     */
    public abstract void mkdir() throws IOException;

    /**
     * Returns an <code>InputStream</code> to read this file's contents.
     *
     * @return an <code>InputStream</code> to read this file's contents
     * @throws IOException if this file could not be read or if an <code>InputStream</code> could not be
     * provided for any other reason (e.g. file is a directory).
     */
    public abstract InputStream getInputStream() throws IOException;

    /**
     * Returns an <code>OuputStream</code> to write this file's contents, appending or overwriting the existing
     * contents.
     *
     * @param append if true, data written to the OutputStream will be appended to the end of this file. If false,
     * any existing data this file contains will be discarded and overwritten.
     * @return an <code>OuputStream</code> to write this file's contents
     * @throws IOException if this operation is not permitted or if this file is a folder
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
     * Returns a {@link RandomAccessInputStream} to read this file's contents with random access.
     *
     * @return a <code>RandomAccessInputStream</code> to read this file's contents with random access
     * @throws IOException if this file cannot be read or if a {@link RandomAccessInputStream} cannot
     * be provided because the underlying file protocol doesn't have random access support or for any other reason
     * (e.g. file is a directory).
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
     * Returns a {@link RandomAccessOutputStream} to write this file's contents with random access.
     *
     * @return a <code>RandomAccessOutputStream</code> to write this file's contents with random access
     * @throws IOException if this file cannot be written or if a {@link RandomAccessOutputStream} cannot
     * be provided because the underlying file protocol doesn't have random access support or for any other reason
     * (e.g. file is a directory).
     */
    public abstract RandomAccessOutputStream getRandomAccessOutputStream() throws IOException;

    /**
     * Deletes this file and this file only (does not recurse). Directories must be empty before they can be deleted.
     *
     * @throws IOException if this file could not be deleted
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



    //////////////////
    // Test methods //
    //////////////////

    /**
     * Simple bench method.
     */
    public static void main(String args[]) throws IOException {
        AbstractFile folder = FileFactory.getFile(args[0]);
        folder.ls();

        int nbIter = 100;

        long totalTime = 0;
        long now;
        for(int i=0; i<nbIter; i++) {
            now = System.currentTimeMillis();
            folder.ls();
            totalTime += System.currentTimeMillis()-now;
        }
	
        System.out.println("Average AbstractFile#ls() time = "+totalTime/nbIter);

        totalTime = 0;
        java.io.File ioFolder = new java.io.File(args[0]);
        for(int i=0; i<nbIter; i++) {
            now = System.currentTimeMillis();
            ioFolder.listFiles();
            totalTime += System.currentTimeMillis()-now;
        }

        System.out.println("Average java.io.File#listFiles() time = "+totalTime/nbIter);

        totalTime = 0;
        ioFolder = new java.io.File(args[0]);
        for(int i=0; i<nbIter; i++) {
            now = System.currentTimeMillis();
            String names[] = ioFolder.list();
            for(int j=0; j<names.length; j++)
                new java.io.File(names[j]);
            totalTime += System.currentTimeMillis()-now;
        }

        System.out.println("Average java.io.File#list() time = "+totalTime/nbIter);
    }
}
