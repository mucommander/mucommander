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


package com.mucommander.file.impl.sftp;

import com.mucommander.Debug;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.file.FileProtocols;
import com.mucommander.file.FileURL;
import com.mucommander.file.connection.ConnectionHandler;
import com.mucommander.file.connection.ConnectionPool;
import com.mucommander.io.*;
import com.mucommander.process.AbstractProcess;
import com.sshtools.j2ssh.io.UnsignedInteger32;
import com.sshtools.j2ssh.io.UnsignedInteger64;
import com.sshtools.j2ssh.session.SessionChannelClient;
import com.sshtools.j2ssh.sftp.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;


/**
 * SFTPFile provides access to files located on an SFTP server.
 *
 * <p>The associated {@link FileURL} protocol is {@link FileProtocols#SFTP}. The host part of the URL designates the
 * SFTP server. Credentials must be specified in the login and password parts as SFTP servers require a login and
 * password. The path separator is <code>'/'</code>.</p>
 *
 * <p>Here are a few examples of valid SFTP URLs:
 * <code>
 * sftp://server/pathto/somefile<br>
 * sftp://login:password@server/pathto/somefile<br>
 * </code>
 * </p>
 *
 * <p>Internally, SFTPFile uses {@link ConnectionPool} to create SFTP connections as needed and allows them to be
 * reused by SFTPFile instances located on the same server, dealing with concurrency issues. Connections are
 * thus managed transparently and need not be manually managed.</p>
 *
 * <p>Low-level SFTP implementation is provided by the <code>J2SSH</code> library distributed under the LGPL license.</p>
 *
 * @see ConnectionPool
 * @author Maxence Bernard
 */
public class SFTPFile extends AbstractFile {

    /** The absolute path to the file on the remote server, without the file protocol */
    protected String absPath;

    /** Contains the file attribute values */
    private SFTPFileAttributes fileAttributes;

    /** Cached parent file instance, null if not created yet or if this file has no parent */
    private AbstractFile parent;
    /** Has the parent file been determined yet? */
    private boolean parentValSet;

    /** Cached canonical path value, null if the canonical path hasn't been fetched yet */
    private String canonicalPath;
    /** Timestamp when the canonical path value was fetched */
    private long canonicalPathFetchedTime;


    /** Period of time during which file attributes are cached, before being fetched again from the server. */
    private static long attributeCachingPeriod = 60000;

    /** a SFTPConnectionHandlerFactory instance */
    private final static SFTPConnectionHandlerFactory connHandlerFactory = new SFTPConnectionHandlerFactory();

    private final static String SEPARATOR = DEFAULT_SEPARATOR;


    /**
     * Creates a new instance of SFTPFile and initializes the SSH/SFTP connection to the server.
     */
    public SFTPFile(FileURL fileURL) throws IOException {
        this(fileURL, null);
    }

    
    private SFTPFile(FileURL fileURL, SFTPFileAttributes fileAttributes) throws IOException {
        super(fileURL);

        this.absPath = fileURL.getPath();

        if(fileAttributes==null)
            this.fileAttributes = new SFTPFileAttributes(fileURL);
        else
            this.fileAttributes = fileAttributes;
    }

    /**
     * Sets the time period during which attributes values (e.g. isDirectory, last modified, ...) are cached.
     * The higher this value, the lower the number of network requests but also the longer it takes
     * before those attributes can be refreshed. A value of <code>0</code> disables attributes caching.
     *
     * <p>This class ensures that the attributes changed remotely by one of its methods are always updated locally, even
     * with attributes caching enabled. To illustrate, after a call to {@link #mkdir()}, {@link #isDirectory()} will
     * return <code>true</code>, even if the attributes haven't been refreshed. The attributes will however not be
     * consistent if they have been changed by another {@link SFTPFile} or by another process, and will remain
     * inconsistent for up to <code>period</code> milliseconds.
     *
     * @param period time period during which attributes values are cached, in milliseconds. 0 disables attributes caching.
     */
    public static void setAttributeCachingPeriod(long period) {
        attributeCachingPeriod = period;
    }


    /////////////////////////////////////////////
    // ConnectionHandlerFactory implementation //
    /////////////////////////////////////////////

    public ConnectionHandler createConnectionHandler(FileURL location) {
        return new SFTPConnectionHandler(location);
    }


    /////////////////////////////////
    // AbstractFile implementation //
    /////////////////////////////////

    /**
     * Implementation note: the value returned by this method will always be <code>false</code> if this file was
     * created by the public constructor. If this file was created by the private constructor (by {@link #ls()},
     * the value will be accurate (<code>true</code> if this file is a symlink) but will never get updated.
     * See {@link com.mucommander.file.impl.sftp.SFTPFile.SFTPFileAttributes} for more information.
     */
    public boolean isSymlink() {
        return fileAttributes.isSymlink();
    }

    /**
     * Implementation note: for symlinks, returns the date of the link's target.
     */
    public long getDate() {
        return ((SFTPFileAttributes)getCanonicalFile().getUnderlyingFileObject()).getDate();
    }

    public boolean canChangeDate() {
        return true;
    }

    public boolean changeDate(long lastModified) {
        // Retrieve a ConnectionHandler and lock it
        SFTPConnectionHandler connHandler = (SFTPConnectionHandler)ConnectionPool.getConnectionHandler(connHandlerFactory, fileURL, true);
        SftpFile sftpFile = null;
        try {
            // Makes sure the connection is started, if not starts it
            connHandler.checkConnection();

            // Retrieve an SftpFile instance for write, will throw an IOException if the file does not exist or cannot
            // be written.
            // /!\ SftpFile instance must be closed afterwards to release its file handle
            sftpFile = connHandler.sftpSubsystem.openFile(absPath, SftpSubsystemClient.OPEN_WRITE);
            FileAttributes attributes = sftpFile.getAttributes();
            attributes.setTimes(attributes.getAccessedTime(), new UnsignedInteger32(lastModified/1000));
            connHandler.sftpSubsystem.setAttributes(sftpFile, attributes);
            // Update local attribute copy
            fileAttributes.setDate(lastModified);

            return true;
        }
        catch(IOException e) {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Failed to change date: "+e);
            return false;
        }
        finally {
            // Close SftpFile instance to release its handle
            if(sftpFile!=null)
                try {sftpFile.close();}
                catch(IOException e) {}

            // Release the lock on the ConnectionHandler
            connHandler.releaseLock();
        }
    }

    /**
     * Implementation note: for symlinks, returns the size of the link's target.
     */
    public long getSize() {
        return ((SFTPFileAttributes)getCanonicalFile().getUnderlyingFileObject()).getSize();
    }
	
	
    public AbstractFile getParent() {
        if(!parentValSet) {
            FileURL parentFileURL = this.fileURL.getParent();
            if(parentFileURL!=null) {
                try { 
                    this.parent = new SFTPFile(parentFileURL);
                }
                catch(IOException e) {
                    // Parent will be null
                }
            }

            this.parentValSet = true;
            return this.parent;
        }
		
        return this.parent;
    }
	
	
    public void setParent(AbstractFile parent) {
        this.parent = parent;
        this.parentValSet = true;
    }
	
	
    /**
     * Implementation note: for symlinks, returns the value of the link's target.
     */
    public boolean exists() {
        return fileAttributes.exists();
    }


    public boolean getPermission(int access, int permission) {
        return (getPermissions() & (permission << (access*3))) != 0;
    }

    public boolean setPermission(int access, int permission, boolean enabled) {
        return setPermissions(setPermissionBit(getPermissions(), (permission << (access*3)), enabled));
    }

    public boolean canGetPermission(int access, int permission) {
        return true;    // Full permission support
    }

    public boolean canSetPermission(int access, int permission) {
        return true;    // Full permission support
    }


    /**
     * Changes the SFTP file permissions to the given permissions int.
     */
    private boolean changeFilePermissions(int permissions) {

        // Retrieve a ConnectionHandler and lock it
        SFTPConnectionHandler connHandler = (SFTPConnectionHandler)ConnectionPool.getConnectionHandler(connHandlerFactory, fileURL, true);
        try {
            // Makes sure the connection is started, if not starts it
            connHandler.checkConnection();

            connHandler.sftpSubsystem.changePermissions(absPath, permissions);
            // Update local attribute copy
            fileAttributes.setPermissions(permissions);

            return true;
        }
        catch(IOException e) {
            if(Debug.ON) Debug.trace("Failed to change permissions: "+e);
            return false;
        }
        finally {
            // Release the lock on the ConnectionHandler
            connHandler.releaseLock();
        }
    }

    /**
     * Implementation note: for symlinks, returns the value of the link's target.
     */
    public boolean isDirectory() {
        return ((SFTPFileAttributes)getCanonicalFile().getUnderlyingFileObject()).isDirectory();
    }
	
    public InputStream getInputStream() throws IOException {
        return getInputStream(0);
    }

    public OutputStream getOutputStream(boolean append) throws IOException {
        // Retrieve a ConnectionHandler and lock it
        final SFTPConnectionHandler connHandler = (SFTPConnectionHandler)ConnectionPool.getConnectionHandler(connHandlerFactory, fileURL, true);
        try {
            // Makes sure the connection is started, if not starts it
            connHandler.checkConnection();

            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("using ConnectionHandler="+connHandler);

            SftpFile sftpFile;
            if(exists()) {
                sftpFile = connHandler.sftpSubsystem.openFile(absPath,
                    append?SftpSubsystemClient.OPEN_WRITE|SftpSubsystemClient.OPEN_APPEND
                    :SftpSubsystemClient.OPEN_WRITE|SftpSubsystemClient.OPEN_TRUNCATE);

                // Update local attributes
                if(!append)
                    fileAttributes.setSize(0);
            }
            else {
                // Set new file permissions to 644 octal (420 dec): "rw-r--r--"
                // Note: by default, permissions for files freshly created is 0 (not readable/writable/executable by anyone)!
                FileAttributes atts = new FileAttributes();
                atts.setPermissions(new UnsignedInteger32(0644));
                sftpFile = connHandler.sftpSubsystem.openFile(absPath, SftpSubsystemClient.OPEN_WRITE|SftpSubsystemClient.OPEN_CREATE, atts);

                // Update local attributes
                fileAttributes.setExists(true);
                fileAttributes.setDate(System.currentTimeMillis());
                fileAttributes.setSize(0);
            }

            return new CounterOutputStream(
                // Custom SftpFileOutputStream constructor, not part of the official J2SSH API
                new SftpFileOutputStream(sftpFile, append?getSize():0) {
                    public void close() throws IOException {
                        // SftpFileOutputStream.close() closes the open SftpFile file handle
                        super.close();

                        // Release the lock on the ConnectionHandler
                        connHandler.releaseLock();
                    }
                }
                ,
                new ByteCounter() {
                    public synchronized void add(long nbBytes) {
                        fileAttributes.addToSize(nbBytes);
                        fileAttributes.setDate(System.currentTimeMillis());
                    }
                }
            );
        }
        catch(IOException e) {
            // Release the lock on the ConnectionHandler if the OutputStream could not be created 
            connHandler.releaseLock();

            // Re-throw IOException
            throw e;
        }
    }

    /**
     * Returns <code>true</code>: {@link #getRandomAccessInputStream()} is implemented.
     *
     * @return true
     */
    public boolean hasRandomAccessInputStream() {
        return true;
    }

    public RandomAccessInputStream getRandomAccessInputStream() throws IOException {
        return new SFTPRandomAccessInputStream();
    }

    /**
     * Returns <code>false</code>: {@link #getRandomAccessOutputStream()} is not implemented and throws an
     * <code>IOException</code>.
     *
     * @return false
     */
    public boolean hasRandomAccessOutputStream() {
        // No random access for SFTP files unfortunately
        return false;
    }

    public RandomAccessOutputStream getRandomAccessOutputStream() throws IOException {
        throw new IOException();
    }

    public void delete() throws IOException {
        // Retrieve a ConnectionHandler and lock it
        SFTPConnectionHandler connHandler = (SFTPConnectionHandler)ConnectionPool.getConnectionHandler(connHandlerFactory, fileURL, true);
        try {
            // Makes sure the connection is started, if not starts it
            connHandler.checkConnection();

            if(isDirectory())
                connHandler.sftpSubsystem.removeDirectory(absPath);
            else
                connHandler.sftpSubsystem.removeFile(absPath);

            // Update local attributes
            fileAttributes.setExists(false);
            fileAttributes.setDirectory(false);
            fileAttributes.setSymlink(false);
            fileAttributes.setSize(0);
        }
        finally {
            // Release the lock on the ConnectionHandler if the OutputStream could not be created
            connHandler.releaseLock();
        }
    }


    public AbstractFile[] ls() throws IOException {
        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("starts, absPath="+absPath+" currentThread="+Thread.currentThread());

        // Retrieve a ConnectionHandler and lock it
        SFTPConnectionHandler connHandler = (SFTPConnectionHandler)ConnectionPool.getConnectionHandler(connHandlerFactory, fileURL, true);
        List files;
        try {
            // Makes sure the connection is started, if not starts it
            connHandler.checkConnection();

            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("using ConnectionHandler="+connHandler+" currentThread="+Thread.currentThread());

    //        connHandler.sftpSubsystem.listChildren(file, files);        // Modified J2SSH method to remove the 100 files limitation

            // Use SftpClient.ls() rather than SftpChannel.listChildren() as it seems to be working better
            files = connHandler.sftpClient.ls(absPath);
        }
        finally {
            // Release the lock on the ConnectionHandler
            connHandler.releaseLock();
        }

        int nbFiles = files.size();
        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("nbFiles="+nbFiles);

        // File doesn't exist, return an empty file array
        if(nbFiles==0)
            return new AbstractFile[] {};

        String parentURL = fileURL.toString(true);
        if(!parentURL.endsWith(SEPARATOR))
            parentURL += SEPARATOR;

        AbstractFile children[] = new AbstractFile[nbFiles];
        AbstractFile child;
        FileURL childURL;
        SftpFile sftpFile;
        String filename;
        int fileCount = 0;

        // Fill AbstractFile array and discard '.' and '..' files
        Iterator iterator = files.iterator();
        while(iterator.hasNext()) {
            sftpFile = (SftpFile)iterator.next();
            filename = sftpFile.getFilename();
            // Discard '.' and '..' files, dunno why these are returned
            if(filename.equals(".") || filename.equals(".."))
                continue;
            childURL = new FileURL(parentURL+filename);
            child = FileFactory.wrapArchive(new SFTPFile(childURL, new SFTPFileAttributes(childURL, sftpFile.getAttributes())));
            child.setParent(this);

            children[fileCount++] = child;
        }

        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("ends, currentThread="+Thread.currentThread());
        
        // Create new array of the exact file count
        if(fileCount<nbFiles) {
            AbstractFile newChildren[] = new AbstractFile[fileCount];
            System.arraycopy(children, 0, newChildren, 0, fileCount);
            return newChildren;
        }

        return children;
    }

	
    public void mkdir() throws IOException {
        // Retrieve a ConnectionHandler and lock it
        SFTPConnectionHandler connHandler = (SFTPConnectionHandler)ConnectionPool.getConnectionHandler(connHandlerFactory, fileURL, true);
        try {
            // Makes sure the connection is started, if not starts it
            connHandler.checkConnection();

            connHandler.sftpSubsystem.makeDirectory(absPath);

            // Todo: patch j2ssh to create the directory directly with those permissions, would save one request
            // Set new directory permissions to 755 octal (493 dec): "rwxr-xr-x"
            // Note: by default, permissions for files freshly created is 0 (not readable/writable/executable by anyone)!
            connHandler.sftpSubsystem.changePermissions(absPath, 493);

            // Update local attributes
            fileAttributes.setExists(true);
            fileAttributes.setDirectory(true);
            fileAttributes.setDate(System.currentTimeMillis());
            fileAttributes.setSize(0);
        }
        finally {
            // Release the lock on the ConnectionHandler
            connHandler.releaseLock();
        }
    }


    public long getFreeSpace() {
        // No way to retrieve this information with J2SSH, return -1 (not available)
        return -1;
    }

    public long getTotalSpace() {
        // No way to retrieve this information with J2SSH, return -1 (not available)
        return -1;
    }

    /**
     * Returns a {@link com.mucommander.file.impl.sftp.SFTPFile.SFTPFileAttributes} instance corresponding to this file.
     */
    public Object getUnderlyingFileObject() {
        return fileAttributes;
    }


    public boolean canRunProcess() {
//        return true;
        return false;
    }

    public AbstractProcess runProcess(String[] tokens) throws IOException {
        return new SFTPProcess(tokens);
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////


    /**
     * Implementation note: for symlinks, returns the permissions of the link's target.
     */
    public int getPermissions() {
        return ((SFTPFileAttributes)getCanonicalFile().getUnderlyingFileObject()).getPermissions();
    }

    public boolean setPermissions(int permissions) {
        return changeFilePermissions(permissions);
    }

    public int getPermissionGetMask() {
        return 511;     // Full permission get support (777 octal)
    }

    public int getPermissionSetMask() {
        return 511;     // Full permission set support (777 octal)
    }

    /**
     * Overrides {@link AbstractFile#moveTo(AbstractFile)} to support server-to-server move if the destination file
     * uses SFTP and is located on the same host.
     */
    public boolean moveTo(AbstractFile destFile) throws FileTransferException {

        // Use the default moveTo() implementation if the destination file doesn't use the SFTP protocol
        // or is not on the same host
        if(!destFile.getURL().getProtocol().equals(FileProtocols.SFTP) || !destFile.getURL().getHost().equals(this.fileURL.getHost())) {
            return super.moveTo(destFile);
        }

        // If destination file is not an SFTPFile nor has an SFTPFile ancestor (for instance an archive entry),
        // server renaming won't work so use default moveTo() implementation instead
        if(!(destFile.getTopAncestor() instanceof SFTPFile)) {
            return super.moveTo(destFile);
        }

        // If destination file is an SFTP file located on the same server, tell the server to rename the file.

        // Retrieve a ConnectionHandler and lock it
        SFTPConnectionHandler connHandler = (SFTPConnectionHandler)ConnectionPool.getConnectionHandler(connHandlerFactory, fileURL, true);
        try {
            // Makes sure the connection is started, if not starts it
            connHandler.checkConnection();

            // Will throw an IOException if the operation failed
            connHandler.sftpClient.rename(absPath, destFile.getURL().getPath());

            // Update destination file attributes by fetching them from the server
            ((SFTPFileAttributes)destFile.getUnderlyingFileObject()).fetchAttributes();

            // Update this file's attributes locally
            fileAttributes.setExists(false);
            fileAttributes.setDirectory(false);
            fileAttributes.setSize(0);

            return true;
        }
        catch(IOException e) {
            if(Debug.ON) Debug.trace("Failed to rename file "+absPath+" : "+e);

            // Re-throw an exception
            throw new FileTransferException(FileTransferException.UNKNOWN_REASON);
        }
        finally {
            // Release the lock on the ConnectionHandler
            connHandler.releaseLock();
        }
    }


    public InputStream getInputStream(long offset) throws IOException {
        // Retrieve a ConnectionHandler and lock it
        final SFTPConnectionHandler connHandler = (SFTPConnectionHandler)ConnectionPool.getConnectionHandler(connHandlerFactory, fileURL, true);
        try {
            // Makes sure the connection is started, if not starts it
            connHandler.checkConnection();

            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("using ConnectionHandler="+connHandler);

            SftpFile sftpFile = connHandler.sftpSubsystem.openFile(absPath, SftpSubsystemClient.OPEN_READ);

            // Custom made constructor, not part of the official J2SSH API
            return new SftpFileInputStream(sftpFile, offset) {

                    public void close() throws IOException {
                        // SftpFileInputStream.close() closes the open SftpFile file handle
                        super.close();

                        // Release the lock on the ConnectionHandler
                        connHandler.releaseLock();
                }
            };
        }
        catch(IOException e) {
            // Release the lock on the ConnectionHandler if the InputStream could not be created
            connHandler.releaseLock();

            // Re-throw IOException
            throw e;
        }
    }

    public String getCanonicalPath() {
        if(isSymlink()) {
            // Check if there is a previous value that hasn't expired yet
            if(canonicalPath!=null && (System.currentTimeMillis()-canonicalPathFetchedTime<attributeCachingPeriod))
                return canonicalPath;

            // Retrieve a ConnectionHandler and lock it
            SFTPConnectionHandler connHandler = (SFTPConnectionHandler)ConnectionPool.getConnectionHandler(connHandlerFactory, fileURL, true);
            try {
                // Makes sure the connection is started, if not starts it
                connHandler.checkConnection();

                // getSymbolicLinkTarget returns the raw symlink target which can either be an absolute path or a
                // relative path. If the path is relative preprend the absolute path of the symlink's parent folder.
                String symlinkTargetPath = connHandler.sftpSubsystem.getSymbolicLinkTarget(fileURL.getPath());
                if(!symlinkTargetPath.startsWith("/")) {
                    String parentPath = fileURL.getParent().getPath();
                    if(!parentPath.endsWith("/"))
                        parentPath += "/";
                    symlinkTargetPath = parentPath + symlinkTargetPath;
                }

                FileURL canonicalURL = (FileURL)fileURL.clone();
                canonicalURL.setPath(symlinkTargetPath);

                // Cache the value and return it until it expires
                canonicalPath = canonicalURL.toString(false);
                canonicalPathFetchedTime = System.currentTimeMillis();
            }
            catch(IOException e) {
                // Simply continue and return the absolute path
            }
            finally {
                // Release the lock on the ConnectionHandler
                connHandler.releaseLock();
            }
        }

        // If this file is not a symlink, or the symlink target path could not be retrieved, return the absolute path
        return getAbsolutePath();
    }


    ///////////////////
    // Inner classes //
    ///////////////////


    /**
     * Provides getters and setters for the attributes of a file. The getters return a cached value if the
     * value has been fetched from the server less than {@link SFTPFile#attributeCachingPeriod} ago. If the value
     * has expired, a new value will be fetched from the server and cached. On the contrary, setters do not modify
     * the value on the server but simply update the cached value.
     */
    private static class SFTPFileAttributes {

        /** The URL pointing to the file whose attributes are cached by this class */
        private FileURL url;

        /** The J2SSH FileAttributes instance wrapped by this class */
        private FileAttributes attrs;

        /** True if the file exists */
        private boolean exists;

        /** Last time the attributes were fetched from the server */
        private long lastFetchedTime;

        /** True if the file is a symlink */
        private boolean isSymlink;


        private SFTPFileAttributes(FileURL url) {
            // this constructor is called by SFTPFile public constructor
            this.url = url;

            fetchAttributes();
            lastFetchedTime = System.currentTimeMillis();
        }

        private SFTPFileAttributes(FileURL url, FileAttributes attrs) {
            // this constructor is called by #ls()

            this.url = url;
            this.attrs = attrs;
            this.exists = true;

            // Some information about this value:
            // FileAttribute#isLink() returns a proper value only for FileAttributes instances that were returned by
            // SftpFile#ls(). FileAttributes that are returned by SftpSubsystemClient#getAttributes(String) always
            // return false for isLink().
            // That means the value of isSymlink is not updated by fetchAttributes(), because if it was, isSymlink
            // would be false after the first attributes update.
            this.isSymlink = attrs.isLink();

            lastFetchedTime = System.currentTimeMillis();
        }

        private void fetchAttributes() {
            // Retrieve a ConnectionHandler and lock it
            SFTPConnectionHandler connHandler = (SFTPConnectionHandler)ConnectionPool.getConnectionHandler(SFTPFile.connHandlerFactory, url, true);
            try {
                // Makes sure the connection is started, if not starts it
                connHandler.checkConnection();

                // Retrieve the file attributes from the server. This will throws an IOException if the file doesn't
                // exist on the server
                // Note for symlinks: the FileAttributes returned by SftpSubsystemClient#getAttributes(String)
                // returns the values of the symlink's target, not the symlink file itself. In other words: the size,
                // date, isDirectory, isLink values are those of the linked file. This is not a problem, except for
                // isLink because it makes impossible to detect changes in the isLink state. Changes should not happen
                // very often, but still.
                // Todo: try and fix for this in J2SSH
                attrs = connHandler.sftpSubsystem.getAttributes(url.getPath());
                exists = true;
            }
            catch(IOException e) {
                // File doesn't exist on the server, create FileAttributes instance with default values
                attrs = new FileAttributes();
                attrs.setPermissions(new UnsignedInteger32(0));     // need to prevent getPermissions() from returning null
                exists = false;
            }

            // Release the lock on the ConnectionHandler
            connHandler.releaseLock();

            lastFetchedTime = System.currentTimeMillis();
        }

        /**
         * Checks if the attribute values have expired, based on the value of {@link SFTPFile#attributeCachingPeriod}
         * and if they have, fetches them from the server.
         */
        private void checkForExpiration() {
            if(System.currentTimeMillis()-lastFetchedTime>=attributeCachingPeriod)
                fetchAttributes();
        }

        ///////////////////////////////
        // Attribute getters/setters //
        ///////////////////////////////

        private boolean exists() {
            checkForExpiration();

            return exists;
        }

        private void setExists(boolean exists) {
            this.exists = exists;
        }

        private boolean isDirectory() {
            checkForExpiration();

            return attrs.isDirectory();
        }

        private void setDirectory(boolean isDirectory) {
            int permissions = attrs.getPermissions().intValue();

            if(isDirectory)
                permissions |= FileAttributes.S_IFDIR;
            else
                permissions &= ~FileAttributes.S_IFDIR; 

            attrs.setPermissions(new UnsignedInteger32(permissions));
        }

        private long getDate() {
            checkForExpiration();

            return attrs.getModifiedTime().longValue()*1000;
        }

        private void setDate(long lastModified) {
            attrs.setTimes(attrs.getAccessedTime(), new UnsignedInteger32(lastModified/1000));
        }

        private long getSize() {
            checkForExpiration();

            return attrs.getSize().longValue();
        }

        private void setSize(long size) {
            attrs.setSize(new UnsignedInteger64(""+size));
        }

        private void addToSize(long size) {
            attrs.setSize(new UnsignedInteger64(""+(attrs.getSize().longValue()+size)));
        }

        private int getPermissions() {
            checkForExpiration();

            return attrs.getPermissions().intValue() & 511;
        }

        private void setPermissions(int permissions) {
            attrs.setPermissions(new UnsignedInteger32((attrs.getPermissions().intValue() & ~511) | (permissions & 511)));
        }

        private boolean isSymlink() {
//            checkForExpiration();
//            return attrs.isLink();

            return isSymlink;
        }

        private void setSymlink(boolean isSymlink) {
            this.isSymlink = isSymlink;
        }
    }


    /**
     * SFTPRandomAccessInputStream extends RandomAccessInputStream to provide random read access to an SFTPFile.
     */
    private class SFTPRandomAccessInputStream extends RandomAccessInputStream {

        private SftpFileInputStream in;

        private SFTPRandomAccessInputStream() throws IOException {
            this.in = (SftpFileInputStream)getInputStream();
        }

        public int read(byte b[], int off, int len) throws IOException {
            return in.read(b, off, len);
        }

        public int read() throws IOException {
            return in.read();
        }

        public long getOffset() throws IOException {
            // Custom method, not part of the official J2SSH API
            return in.getPosition();
        }

        public long getLength() throws IOException {
            return getSize();
        }

        public void seek(long offset) throws IOException {
            // Custom method, not part of the official J2SSH API
            in.setPosition(offset);
        }

        public void close() throws IOException {
            in.close();
        }
    }


    private class SFTPProcess extends AbstractProcess {

        private boolean success;
        private SessionChannelClient sessionClient;
        private SFTPConnectionHandler connHandler;

        private SFTPProcess(String tokens[]) throws IOException {

            try {
                // Retrieve a ConnectionHandler and lock it
                connHandler = (SFTPConnectionHandler)ConnectionPool.getConnectionHandler(connHandlerFactory, fileURL, true);
                // Makes sure the connection is started, if not starts it
                connHandler.checkConnection();

                sessionClient = connHandler.sshClient.openSessionChannel();
//                sessionClient.startShell();

//                success = sessionClient.executeCommand("cd "+(isDirectory()?fileURL.getPath():fileURL.getParent().getPath()));
//if(Debug.ON) Debug.trace("commmand="+("cd "+(isDirectory()?fileURL.getPath():fileURL.getParent().getPath()))+" returned "+success);

                // Environment variables are refused by most servers for security reasons  
//                sessionClient.setEnvironmentVariable("cd", isDirectory()?fileURL.getPath():fileURL.getParent().getPath());

                // No way to set the current working directory:
                // 1/ when executing a single command:
                //  + environment variables are ignored by most server, so can't use PWD for that.
                //  + could send 'cd dir ; command' but it's not platform independant and prevents the command from being
                //    executed under Windows
                // 2/ when starting a shell, no problem to change the current working directory (cd dir\n is sent before
                // the command), but there is no reliable way to detect the end of the command execution, as confirmed
                // by one of the J2SSH developers : http://sourceforge.net/forum/message.php?msg_id=1826569

                // Concatenates all tokens to create the command string
                String command = "";
                int nbTokens = tokens.length;
                for(int i=0; i<nbTokens; i++) {
                    command += tokens[i];
                    if(i!=nbTokens-1)
                        command += " ";
                }

                success = sessionClient.executeCommand(command);
                if(Debug.ON) Debug.trace("commmand="+command+" returned "+success);
            }
            catch(IOException e) {
                // Release the lock on the ConnectionHandler
                connHandler.releaseLock();

                sessionClient.close();

                // Re-throw exception
                throw e;
            }
        }

        public boolean usesMergedStreams() {
            return false;
        }

        public int waitFor() throws InterruptedException, IOException {
            return sessionClient.getExitCode().intValue();
        }

        protected void destroyProcess() throws IOException {
            // Release the lock on the ConnectionHandler
            connHandler.releaseLock();

            sessionClient.close();
        }

        public int exitValue() {
            return sessionClient.getExitCode().intValue();
        }

        public OutputStream getOutputStream() throws IOException {
            return sessionClient.getOutputStream();
        }

        public InputStream getInputStream() throws IOException {
            return sessionClient.getInputStream();
        }

        public InputStream getErrorStream() throws IOException {
            return sessionClient.getStderrInputStream();
        }
    }
}
