/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2018 Maxence Bernard
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



package com.mucommander.commons.file.protocol.sftp;

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.AuthException;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.FileOperation;
import com.mucommander.commons.file.FilePermissions;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.PermissionAccess;
import com.mucommander.commons.file.PermissionBits;
import com.mucommander.commons.file.PermissionType;
import com.mucommander.commons.file.SimpleFilePermissions;
import com.mucommander.commons.file.SyncedFileAttributes;
import com.mucommander.commons.file.UnsupportedFileOperation;
import com.mucommander.commons.file.UnsupportedFileOperationException;
import com.mucommander.commons.file.connection.ConnectionHandler;
import com.mucommander.commons.file.connection.ConnectionPool;
import com.mucommander.commons.file.protocol.FileProtocols;
import com.mucommander.commons.file.protocol.ProtocolFile;
import com.mucommander.commons.io.ByteCounter;
import com.mucommander.commons.io.ByteUtils;
import com.mucommander.commons.io.CounterOutputStream;
import com.mucommander.commons.io.RandomAccessInputStream;
import com.mucommander.commons.io.RandomAccessOutputStream;


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
 * <p>Low-level SFTP implementation is provided by the <code>JSCH</code> library distributed under the BSD license.</p>
 *
 * @see ConnectionPool
 * @author Maxence Bernard, Arik Hadas
 */
public class SFTPFile extends ProtocolFile {
    private static final Logger LOGGER = LoggerFactory.getLogger(SFTPFile.class);

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

    private OutputStream getOutputStream(boolean append) throws IOException {
        // Retrieve a ConnectionHandler and lock it
        final SFTPConnectionHandler connHandler = (SFTPConnectionHandler)ConnectionPool.getConnectionHandler(connHandlerFactory, fileURL, true);
        try {
            // Makes sure the connection is started, if not starts it
            connHandler.checkConnection();

            OutputStream outputStream;
            if(exists()) {
                outputStream = connHandler.channelSftp.put(absPath,
                        append ? ChannelSftp.APPEND : ChannelSftp.OVERWRITE);

                // Update local attributes
                if(!append)
                    fileAttributes.setSize(0);
            }
            else {
                outputStream = connHandler.channelSftp.put(absPath);

                // Update local attributes
                fileAttributes.setExists(true);
                fileAttributes.setDate(System.currentTimeMillis());
                fileAttributes.setSize(0);
            }

            return new CounterOutputStream(
                    new SFTPOutputStream(outputStream, connHandler),
                    new ByteCounter() {
                        @Override
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
        } catch (SftpException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
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
     * See {@link com.mucommander.commons.file.protocol.sftp.SFTPFile.SFTPFileAttributes} for more information.
     */
    @Override
    public boolean isSymlink() {
        return fileAttributes.isSymlink();
    }

    @Override
    public boolean isSystem() {
        return false;
    }

    /**
     * Implementation note: for symlinks, returns the date of the link's target.
     */
    @Override
    public long getDate() {
        return ((SFTPFileAttributes)getCanonicalFile().getUnderlyingFileObject()).getDate();
    }

    @Override
    public void changeDate(long lastModified) throws IOException, UnsupportedFileOperationException {
        SFTPConnectionHandler connHandler = null;
        try {
            // Retrieve a ConnectionHandler and lock it
            connHandler = (SFTPConnectionHandler)ConnectionPool.getConnectionHandler(connHandlerFactory, fileURL, true);

            // Makes sure the connection is started, if not starts it
            connHandler.checkConnection();

            connHandler.channelSftp.setMtime(absPath, (int)(lastModified/1000));
            // Update local attribute copy
            fileAttributes.setDate(lastModified);
        } catch (SftpException e) {
            e.printStackTrace();
        }
        finally {
            // Release the lock on the ConnectionHandler
            if(connHandler!=null)
                connHandler.releaseLock();
        }
    }

    /**
     * Implementation note: for symlinks, returns the size of the link's target.
     */
    @Override
    public long getSize() {
        return ((SFTPFileAttributes)getCanonicalFile().getUnderlyingFileObject()).getSize();
    }


    @Override
    public AbstractFile getParent() {
        if(!parentValSet) {
            FileURL parentFileURL = this.fileURL.getParent();
            if(parentFileURL!=null) {
                parent = FileFactory.getFile(parentFileURL);
                // Note: parent may be null if it can't be resolved
            }

            parentValSet = true;
        }

        return parent;
    }


    @Override
    public void setParent(AbstractFile parent) {
        this.parent = parent;
        this.parentValSet = true;
    }


    /**
     * Implementation note: for symlinks, returns the value of the link's target.
     */
    @Override
    public boolean exists() {
        return fileAttributes.exists();
    }

    /**
     * Implementation note: for symlinks, returns the permissions of the link's target.
     */
    @Override
    public FilePermissions getPermissions() {
        return ((SFTPFileAttributes)getCanonicalFile().getUnderlyingFileObject()).getPermissions();
    }

    @Override
    public PermissionBits getChangeablePermissions() {
        return PermissionBits.FULL_PERMISSION_BITS;     // Full permission support (777 octal)
    }

    @Override
    public void changePermission(PermissionAccess access, PermissionType permission, boolean enabled) throws IOException {
        changePermissions(ByteUtils.setBit(getPermissions().getIntValue(), (permission.toInt() << (access.toInt()*3)), enabled));
    }

    @Override
    public String getOwner() {
        return fileAttributes.getOwner();
    }

    @Override
    public boolean canGetOwner() {
        return true;
    }

    @Override
    public String getGroup() {
        return fileAttributes.getGroup();
    }

    @Override
    public boolean canGetGroup() {
        return true;
    }

    /**
     * Implementation note: for symlinks, returns the value of the link's target.
     */
    @Override
    public boolean isDirectory() {
        return fileAttributes.isDirectory();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return getInputStream(0);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return getOutputStream(false);
    }

    @Override
    public OutputStream getAppendOutputStream() throws IOException {
        return getOutputStream(true);
    }

    @Override
    public RandomAccessInputStream getRandomAccessInputStream() throws IOException {
        return new SFTPRandomAccessInputStream();
    }

    @Override
    public void delete() throws IOException {
        // Retrieve a ConnectionHandler and lock it
        SFTPConnectionHandler connHandler = null;
        try {
            // Retrieve a ConnectionHandler and lock it
            connHandler = (SFTPConnectionHandler)ConnectionPool.getConnectionHandler(connHandlerFactory, fileURL, true);

            // Makes sure the connection is started, if not starts it
            connHandler.checkConnection();

            if(isDirectory())
                connHandler.channelSftp.rmdir(absPath);
            else
                connHandler.channelSftp.rm(absPath);

            // Update local attributes
            fileAttributes.setExists(false);
            fileAttributes.setDirectory(false);
            fileAttributes.setSymlink(false);
            fileAttributes.setSize(0);
        } catch (SftpException e) {
            e.printStackTrace();
        }
        finally {
            // Release the lock on the ConnectionHandler if the OutputStream could not be created
            if(connHandler!=null)
                connHandler.releaseLock();
        }
    }


    @SuppressWarnings("unchecked")
    @Override
    public AbstractFile[] ls() throws IOException {
        // Retrieve a ConnectionHandler and lock it
        SFTPConnectionHandler connHandler = (SFTPConnectionHandler)ConnectionPool.getConnectionHandler(connHandlerFactory, fileURL, true);
        List<LsEntry> files = new ArrayList<LsEntry>();
        try {
            // Makes sure the connection is started, if not starts it
            connHandler.checkConnection();

            files = connHandler.channelSftp.ls(absPath);
        } catch (SftpException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally {
            // Release the lock on the ConnectionHandler
            connHandler.releaseLock();
        }

        int nbFiles = files.size();

        // File doesn't exist, return an empty file array
        if(nbFiles==0)
            return new AbstractFile[] {};

        AbstractFile children[] = new AbstractFile[nbFiles];
        FileURL childURL;
        String filename;
        int fileCount = 0;
        String parentPath = fileURL.getPath();
        if(!parentPath .endsWith(SEPARATOR))
            parentPath  += SEPARATOR;

        // Fill AbstractFile array and discard '.' and '..' files
        for (LsEntry file : files) {
            filename = file.getFilename();
            // Discard '.' and '..' files, dunno why these are returned
            if (filename.equals(".") || filename.equals(".."))
                continue;

            childURL = (FileURL) fileURL.clone();
            childURL.setPath(parentPath + filename);

            children[fileCount++] = FileFactory.getFile(childURL, this, Collections.singletonMap("attributes", new SFTPFileAttributes(childURL, file.getAttrs())));
        }

        // Create new array of the exact file count
        if(fileCount<nbFiles) {
            AbstractFile newChildren[] = new AbstractFile[fileCount];
            System.arraycopy(children, 0, newChildren, 0, fileCount);
            return newChildren;
        }

        return children;
    }


    @Override
    public void mkdir() throws IOException {
        // Retrieve a ConnectionHandler and lock it
        SFTPConnectionHandler connHandler = (SFTPConnectionHandler)ConnectionPool.getConnectionHandler(connHandlerFactory, fileURL, true);
        try {
            // Makes sure the connection is started, if not starts it
            connHandler.checkConnection();

            connHandler.channelSftp.mkdir(absPath);

            // Update local attributes
            fileAttributes.setExists(true);
            fileAttributes.setDirectory(true);
            fileAttributes.setDate(System.currentTimeMillis());
            fileAttributes.setSize(0);
        } catch (SftpException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally {
            // Release the lock on the ConnectionHandler
            connHandler.releaseLock();
        }
    }

    /**
     * Implementation notes: server-to-server renaming will work if the destination file also uses the 'SFTP' scheme
     * and is located on the same host.
     */
    @Override
    public void renameTo(AbstractFile destFile) throws IOException {
        // Throw an exception if the file cannot be renamed to the specified destination.
        // Fail in situations where SFTPFile#renameTo() does not, for instance when the source and destination are the same.
        checkRenamePrerequisites(destFile, true, false);

        // Retrieve a ConnectionHandler and lock it
        SFTPConnectionHandler connHandler = null;
        try {
            connHandler = (SFTPConnectionHandler)ConnectionPool.getConnectionHandler(connHandlerFactory, fileURL, true);

            // Makes sure the connection is started, if not starts it
            connHandler.checkConnection();

            // SftpClient#rename() throws an IOException if the destination exists (instead of overwriting the file)
            if(destFile.exists())
                destFile.delete();

            // Will throw an IOException if the operation failed
            connHandler.channelSftp.rename(absPath, destFile.getURL().getPath());

            // Update destination file attributes by fetching them from the server
            ((SFTPFileAttributes)destFile.getUnderlyingFileObject()).fetchAttributes();

            // Update this file's attributes locally
            fileAttributes.setExists(false);
            fileAttributes.setDirectory(false);
            fileAttributes.setSize(0);
        } catch (SftpException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally {
            // Release the lock on the ConnectionHandler
            if(connHandler!=null)
                connHandler.releaseLock();
        }
    }

    /**
     * Returns a {@link com.mucommander.commons.file.protocol.sftp.SFTPFile.SFTPFileAttributes} instance corresponding to this file.
     */
    @Override
    public Object getUnderlyingFileObject() {
        return fileAttributes;
    }


    // Unsupported file operations

    /**
     * Always throws an {@link UnsupportedFileOperationException}: random write access is not supported.
     */
    @Override
    @UnsupportedFileOperation
    public RandomAccessOutputStream getRandomAccessOutputStream() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.RANDOM_WRITE_FILE);
    }

    /**
     * Always throws {@link UnsupportedFileOperationException} when called.
     *
     * @throws UnsupportedFileOperationException, always
     */
    @Override
    @UnsupportedFileOperation
    public void copyRemotelyTo(AbstractFile destFile) throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.COPY_REMOTELY);
    }

    /**
     * Always throws {@link UnsupportedFileOperationException} when called.
     *
     * @throws UnsupportedFileOperationException, always
     */
    @Override
    @UnsupportedFileOperation
    public long getFreeSpace() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.GET_FREE_SPACE);
    }

    /**
     * Always throws {@link UnsupportedFileOperationException} when called.
     *
     * @throws UnsupportedFileOperationException, always
     */
    @Override
    @UnsupportedFileOperation
    public long getTotalSpace() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.GET_TOTAL_SPACE);
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////


    @Override
    public void changePermissions(int permissions) throws IOException {
        // Retrieve a ConnectionHandler and lock it
        SFTPConnectionHandler connHandler = null;
        try {
            connHandler = (SFTPConnectionHandler)ConnectionPool.getConnectionHandler(connHandlerFactory, fileURL, true);

            // Makes sure the connection is started, if not starts it
            connHandler.checkConnection();

            connHandler.channelSftp.chmod(permissions, absPath);
            // Update local attribute copy
            fileAttributes.setPermissions(new SimpleFilePermissions(permissions));
        } catch (SftpException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally {
            // Release the lock on the ConnectionHandler
            if(connHandler!=null)
                connHandler.releaseLock();
        }
    }

    @Override
    public InputStream getInputStream(long offset) throws IOException {
        // Retrieve a ConnectionHandler and lock it
        final SFTPConnectionHandler connHandler = (SFTPConnectionHandler)ConnectionPool.getConnectionHandler(connHandlerFactory, fileURL, true);
        try {
            // Makes sure the connection is started, if not starts it
            connHandler.checkConnection();

            InputStream in = new SFTPInputStream(connHandler.channelSftp.get(absPath), connHandler);
            in.skip(offset);
            return in;
        }
        catch(IOException e) {
            // Release the lock on the ConnectionHandler if the InputStream could not be created
            connHandler.releaseLock();

            // Re-throw IOException
            throw e;
        } catch (SftpException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getCanonicalPath() {
        if(isSymlink()) {
            // Check if there is a previous value that hasn't expired yet
            if(canonicalPath!=null && (System.currentTimeMillis()-canonicalPathFetchedTime<attributeCachingPeriod))
                return canonicalPath;

            SFTPConnectionHandler connHandler = null;
            try {
                // Retrieve a ConnectionHandler and lock it
                connHandler = (SFTPConnectionHandler)ConnectionPool.getConnectionHandler(connHandlerFactory, fileURL, true);

                // Makes sure the connection is started, if not starts it
                connHandler.checkConnection();

                // getSymbolicLinkTarget returns the raw symlink target which can either be an absolute path or a
                // relative path. If the path is relative preprend the absolute path of the symlink's parent folder.
                String symlinkTargetPath = connHandler.channelSftp.readlink(fileURL.getPath());
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
                return canonicalPath;
            }
            catch(IOException e) {
                // Simply continue and return the absolute path
            } catch (SftpException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            finally {
                // Release the lock on the ConnectionHandler
                if(connHandler!=null)
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
    static class SFTPFileAttributes extends SyncedFileAttributes {

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
        private SFTPFileAttributes(FileURL url, SftpATTRS attrs) {
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
            SFTPConnectionHandler connHandler = null;
            try {
                // Retrieve a ConnectionHandler and lock it
                connHandler = (SFTPConnectionHandler)ConnectionPool.getConnectionHandler(SFTPFile.connHandlerFactory, url, true);

                // Makes sure the connection is started, if not starts it
                connHandler.checkConnection();

                // Retrieve the file attributes from the server. This will throws an IOException if the file doesn't
                // exist on the server
                // Note for symlinks: the FileAttributes returned by SftpSubsystemClient#getAttributes(String)
                // returns the values of the symlink's target, not the symlink file itself. In other words: the size,
                // date, isDirectory, isLink values are those of the linked file. This is not a problem, except for
                // isLink because it makes impossible to detect changes in the isLink state. Changes should not happen
                // very often, but still.
                setAttributes(connHandler.channelSftp.lstat(url.getPath()));
                setExists(true);
            } catch(SftpException e) {
                // File doesn't exist on the server
                if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE)
                    setExists(false);

            } catch(IOException e) {
                // Rethrow AuthException
                if(e instanceof AuthException)
                    throw (AuthException)e;

                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            finally {
                // Release the lock on the ConnectionHandler
                if(connHandler!=null)
                    connHandler.releaseLock();
            }
        }

        /**
         * Sets the file attributes using the values contained in the specified JSCH SftpATTRS instance.
         *
         * @param attrs  JSCH SftpATTRS instance that contains the values to use
         */
        private void setAttributes(SftpATTRS attrs) {
            setDirectory(attrs.isDir());
            setDate((long) attrs.getMTime() * 1000);
            setSize(attrs.getSize());
            setPermissions(new SimpleFilePermissions(
                    attrs.getPermissions() & PermissionBits.FULL_PERMISSION_INT
                    ));
            setOwner(String.valueOf(attrs.getUId()));
            setGroup(String.valueOf(attrs.getGId()));
            setSymlink(attrs.isLink());
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

        @Override
        public void updateAttributes() {
            try {
                fetchAttributes();
            }
            catch(Exception e) {        // AuthException
                LOGGER.info("Failed to refresh attributes", e);
            }
        }
    }

    /**
     * SFTPRandomAccessInputStream extends RandomAccessInputStream to provide random read access to an SFTPFile.
     */
    private class SFTPRandomAccessInputStream extends RandomAccessInputStream {

        private InputStream in;
        private long offset;

        private SFTPRandomAccessInputStream() throws IOException {
            this.in = getInputStream();
        }

        @Override
        public int read(byte b[], int off, int len) throws IOException {
            int nbRead = in.read(b, off, len);

            if(nbRead!=-1)
                offset += nbRead;

            return nbRead;
        }

        @Override
        public int read() throws IOException {
            int read = in.read();

            if(read!=-1)
                offset += 1;

            return read;
        }

        public long getOffset() throws IOException {
            return offset;
        }

        public long getLength() throws IOException {
            return getSize();
        }

        public void seek(long offset) throws IOException {
            try {
                in.close();
            }
            catch(IOException e) {}

            in = getInputStream(offset);
            this.offset = offset;
        }

        @Override
        public void close() throws IOException {
            in.close();
        }
    }

    private class SFTPInputStream extends FilterInputStream {

        private SFTPConnectionHandler connHandler;

        protected SFTPInputStream(InputStream in, SFTPConnectionHandler connHandler) {
            super(in);
            this.connHandler = connHandler;
        }

        @Override
        public void close() throws IOException {
            super.close();
            connHandler.releaseLock();
        }
    }

    private class SFTPOutputStream extends FilterOutputStream {

        private SFTPConnectionHandler connHandler;

        protected SFTPOutputStream(OutputStream out, SFTPConnectionHandler connHandler) {
            super(out);
            this.connHandler = connHandler;
        }

        @Override
        public void close() throws IOException {
            super.close();
            connHandler.releaseLock();
        }
    }
}
