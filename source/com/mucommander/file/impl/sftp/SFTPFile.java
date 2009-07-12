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


package com.mucommander.file.impl.sftp;

import com.mucommander.Debug;
import com.mucommander.auth.AuthException;
import com.mucommander.file.*;
import com.mucommander.file.connection.ConnectionHandler;
import com.mucommander.file.connection.ConnectionPool;
import com.mucommander.io.*;
import com.sshtools.j2ssh.io.UnsignedInteger32;
import com.sshtools.j2ssh.sftp.FileAttributes;
import com.sshtools.j2ssh.sftp.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;


/**
 * SFTPFile provides access to files located on an SFTP server.
 *
 * <p>The associated {@link FileURL} scheme is {@link FileProtocols#SFTP}. The host part of the URL designates the
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

    /** The absolute path to the file on the remote server, not the full URL */
    private String absPath;

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

    /** Name of the property that holds the path to a private key. This property is optional; if it is set, private key
     * authentication is used. */
    public final static String PRIVATE_KEY_PATH_PROPERTY_NAME = "privateKeyPath";

    private final static String SEPARATOR = DEFAULT_SEPARATOR;


    /**
     * Creates a new instance of SFTPFile and initializes the SSH/SFTP connection to the server.
     */
    protected SFTPFile(FileURL fileURL) throws IOException {
        this(fileURL, null);
    }

    
    protected SFTPFile(FileURL fileURL, SFTPFileAttributes fileAttributes) throws IOException {
        super(fileURL);

//        // Throw an AuthException if the url doesn't contain any credentials
//        if(!fileURL.containsCredentials())
//            throw new AuthException(fileURL);

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
                    parent = new SFTPFile(parentFileURL);
                }
                catch(IOException e) {
                    // No parent, that's all
                }
            }

            parentValSet = true;
        }
		
        return parent;
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

    /**
     * Implementation note: for symlinks, returns the permissions of the link's target.
     */
    public FilePermissions getPermissions() {
        return ((SFTPFileAttributes)getCanonicalFile().getUnderlyingFileObject()).getPermissions();
    }

    public PermissionBits getChangeablePermissions() {
        return PermissionBits.FULL_PERMISSION_BITS;     // Full permission support (777 octal)
    }

    public boolean changePermission(int access, int permission, boolean enabled) {
        return changePermissions(ByteUtils.setBit(getPermissions().getIntValue(), (permission << (access*3)), enabled));
    }

    public String getOwner() {
        return fileAttributes.getOwner();
    }

    public boolean canGetOwner() {
        return true;
    }

    public String getGroup() {
        return fileAttributes.getGroup();
    }

    public boolean canGetGroup() {
        return true;
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

        AbstractFile children[] = new AbstractFile[nbFiles];
        AbstractFile child;
        FileURL childURL;
        SftpFile sftpFile;
        String filename;
        int fileCount = 0;
        String parentPath = fileURL.getPath();
        if(!parentPath .endsWith(SEPARATOR))
            parentPath  += SEPARATOR;

        // Fill AbstractFile array and discard '.' and '..' files
        Iterator iterator = files.iterator();
        while(iterator.hasNext()) {
            sftpFile = (SftpFile)iterator.next();
            filename = sftpFile.getFilename();
            // Discard '.' and '..' files, dunno why these are returned
            if(filename.equals(".") || filename.equals(".."))
                continue;

            childURL = (FileURL)fileURL.clone();
            childURL.setPath(parentPath+filename);

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

            // Note: this J2SSH method has been patched to set the permissions of the new directory to 0755 (rwxr-xr-x)
            // instead of 0. This patches allows to avoid a 'change permissions' request (cf comment code hereunder).
            connHandler.sftpSubsystem.makeDirectory(absPath);

//            // Set new directory permissions to 755 octal (493 dec): "rwxr-xr-x"
//            // Note: by default, permissions for files freshly created is 0 (not readable/writable/executable by anyone)!
//            connHandler.sftpSubsystem.changePermissions(absPath, 493);

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


    ////////////////////////
    // Overridden methods //
    ////////////////////////


    public boolean changePermissions(int permissions) {
        // Retrieve a ConnectionHandler and lock it
        SFTPConnectionHandler connHandler = (SFTPConnectionHandler)ConnectionPool.getConnectionHandler(connHandlerFactory, fileURL, true);
        try {
            // Makes sure the connection is started, if not starts it
            connHandler.checkConnection();

            connHandler.sftpSubsystem.changePermissions(absPath, permissions);
            // Update local attribute copy
            fileAttributes.setPermissions(new SimpleFilePermissions(permissions));

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
     * Overrides {@link AbstractFile#moveTo(AbstractFile)} to support server-to-server move if the destination file
     * uses SFTP and is located on the same host.
     */
    public boolean moveTo(AbstractFile destFile) throws FileTransferException {

        // Use the default moveTo() implementation if the destination file doesn't use the SFTP protocol
        // or is not on the same host
        if(!destFile.getURL().getScheme().equals(FileProtocols.SFTP) || !destFile.getURL().getHost().equals(this.fileURL.getHost())) {
            return super.moveTo(destFile);
        }

        // If destination file is not an SFTPFile nor has an SFTPFile ancestor (for instance an archive entry),
        // server renaming won't work so use default moveTo() implementation instead
        if(!(destFile.getTopAncestor() instanceof SFTPFile)) {
            return super.moveTo(destFile);
        }

        // Special tests to fail in situations where SmbFile#renameTo() does not, for instance:
        // - when the source and destination are the same
        checkCopyPrerequisites(destFile, true);

        // If destination file is an SFTP file located on the same server, tell the server to rename the file.

        // Retrieve a ConnectionHandler and lock it
        SFTPConnectionHandler connHandler = (SFTPConnectionHandler)ConnectionPool.getConnectionHandler(connHandlerFactory, fileURL, true);
        try {
            // Makes sure the connection is started, if not starts it
            connHandler.checkConnection();

            // SftpClient#rename() throws an IOException if the destination exists (instead of overwriting the file)
            if(destFile.exists())
                destFile.delete();

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
     * SFTPFileAttributes provides getters and setters for SFTP file attributes. By extending
     * <code>SyncedFileAttributes</code>, this class caches attributes for a  certain amount of time
     * ({@link SFTPFile#attributeCachingPeriod}) after which a fresh value is retrieved from the server.
     */
    private static class SFTPFileAttributes extends SyncedFileAttributes {

        /** The URL pointing to the file whose attributes are cached by this class */
        private FileURL url;

        /** True if the file is a symlink */
        private boolean isSymlink;

        // this constructor is called by SFTPFile public constructor
        private SFTPFileAttributes(FileURL url) throws AuthException {
            super(attributeCachingPeriod, false);       // no initial update

            this.url = url;
            setPermissions(FilePermissions.EMPTY_FILE_PERMISSIONS);

            fetchAttributes();      // throws AuthException if no or bad credentials

            updateExpirationDate(); // declare the attributes as 'fresh'
        }

        // this constructor is called by #ls()
        private SFTPFileAttributes(FileURL url, FileAttributes attrs) {
            super(attributeCachingPeriod, false);   // no initial update

            this.url = url;
            setPermissions(FilePermissions.EMPTY_FILE_PERMISSIONS);

            setAttributes(attrs);
            setExists(true);

            // Some information about this value:
            // FileAttribute#isLink() returns a proper value only for FileAttributes instances that were returned by
            // SftpFile#ls(). FileAttributes that are returned by SftpSubsystemClient#getAttributes(String) always
            // return false for isLink().
            // That means the value of isSymlink is not updated by fetchAttributes(), because if it was, isSymlink
            // would be false after the first attributes update.
            this.isSymlink = attrs.isLink();

            updateExpirationDate(); // declare the attributes as 'fresh'
        }

        private void fetchAttributes() throws AuthException {
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
                setAttributes(connHandler.sftpSubsystem.getAttributes(url.getPath()));
                setExists(true);
            }
            catch(IOException e) {
                // File doesn't exist on the server
                setExists(false);

                // Rethrow AuthException
                if(e instanceof AuthException)
                    throw (AuthException)e;
            }
            finally {
                // Release the lock on the ConnectionHandler
                connHandler.releaseLock();
            }
        }

        /**
         * Sets the file attributes using the values contained in the specified J2SSH FileAttributes instance.
         *
         * @param attrs J2SSH FileAttributes instance that contains the values to use
         */
        private void setAttributes(com.sshtools.j2ssh.sftp.FileAttributes attrs) {
            setDirectory(attrs.isDirectory());
            setDate(attrs.getModifiedTime().longValue()*1000);
            setSize(attrs.getSize().longValue());
            setPermissions(new SimpleFilePermissions(
               attrs.getPermissions().intValue() & PermissionBits.FULL_PERMISSION_INT
            ));
            setOwner(attrs.getUID().toString());
            setGroup(attrs.getGID().toString());
            setSymlink(isSymlink);
        }

        /**
         * Increments the size attribute's value by the given number of bytes.
         *
         * @param increment number of bytes to add to the current size attribute's value
         */
        private void addToSize(long increment) {
            setSize(getSize()+increment);
        }

        /**
         * Returns <code>true</code> if the file is a symlink.
         *
         * @return <code>true</code> if the file is a symlink
         */
        private boolean isSymlink() {
            checkForExpiration(false);

            return isSymlink;
        }

        /**
         * Sets whether the file is a symlink.
         *
         * @param isSymlink <code>true</code> if the file is a symlink
         */
        private void setSymlink(boolean isSymlink) {
            this.isSymlink = isSymlink;
        }


        ////////////////////////////////////////////
        // SyncedFileAttributes implementation //
        ////////////////////////////////////////////

        public void updateAttributes() {
            try {
                fetchAttributes();
            }
            catch(Exception e) {        // AuthException
                if(Debug.ON) Debug.trace("Failed to refresh attributes: "+e);
            }
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


//    private class SFTPProcess extends AbstractProcess {
//
//        private boolean success;
//        private SessionChannelClient sessionClient;
//        private SFTPConnectionHandler connHandler;
//
//        private SFTPProcess(String tokens[]) throws IOException {
//
//            try {
//                // Retrieve a ConnectionHandler and lock it
//                connHandler = (SFTPConnectionHandler)ConnectionPool.getConnectionHandler(connHandlerFactory, fileURL, true);
//                // Makes sure the connection is started, if not starts it
//                connHandler.checkConnection();
//
//                sessionClient = connHandler.sshClient.openSessionChannel();
////                sessionClient.startShell();
//
////                success = sessionClient.executeCommand("cd "+(isDirectory()?fileURL.getPath():fileURL.getParent().getPath()));
////if(Debug.ON) Debug.trace("commmand="+("cd "+(isDirectory()?fileURL.getPath():fileURL.getParent().getPath()))+" returned "+success);
//
//                // Environment variables are refused by most servers for security reasons
////                sessionClient.setEnvironmentVariable("cd", isDirectory()?fileURL.getPath():fileURL.getParent().getPath());
//
//                // No way to set the current working directory:
//                // 1/ when executing a single command:
//                //  + environment variables are ignored by most server, so can't use PWD for that.
//                //  + could send 'cd dir ; command' but it's not platform independant and prevents the command from being
//                //    executed under Windows
//                // 2/ when starting a shell, no problem to change the current working directory (cd dir\n is sent before
//                // the command), but there is no reliable way to detect the end of the command execution, as confirmed
//                // by one of the J2SSH developers : http://sourceforge.net/forum/message.php?msg_id=1826569
//
//                // Concatenates all tokens to create the command string
//                StringBuffer command = new StringBuffer();
//                int nbTokens = tokens.length;
//                for(int i=0; i<nbTokens; i++) {
//                    command.append(tokens[i]);
//                    if(i!=nbTokens-1)
//                        command.append(" ");
//                }
//
//                success = sessionClient.executeCommand(command.toString());
//                if(Debug.ON) Debug.trace("commmand="+command+" returned "+success);
//            }
//            catch(IOException e) {
//                // Release the lock on the ConnectionHandler
//                connHandler.releaseLock();
//
//                sessionClient.close();
//
//                // Re-throw exception
//                throw e;
//            }
//        }
//
//        public boolean usesMergedStreams() {
//            return false;
//        }
//
//        public int waitFor() throws InterruptedException, IOException {
//            return sessionClient.getExitCode().intValue();
//        }
//
//        protected void destroyProcess() throws IOException {
//            // Release the lock on the ConnectionHandler
//            connHandler.releaseLock();
//
//            sessionClient.close();
//        }
//
//        public int exitValue() {
//            return sessionClient.getExitCode().intValue();
//        }
//
//        public OutputStream getOutputStream() throws IOException {
//            return sessionClient.getOutputStream();
//        }
//
//        public InputStream getInputStream() throws IOException {
//            return sessionClient.getInputStream();
//        }
//
//        public InputStream getErrorStream() throws IOException {
//            return sessionClient.getStderrInputStream();
//        }
//    }
}
