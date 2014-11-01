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



package com.mucommander.commons.file.impl.ftp;

import com.mucommander.commons.file.*;
import com.mucommander.commons.file.connection.ConnectionHandler;
import com.mucommander.commons.file.connection.ConnectionHandlerFactory;
import com.mucommander.commons.file.connection.ConnectionPool;
import com.mucommander.commons.io.ByteUtils;
import com.mucommander.commons.io.FilteredOutputStream;
import com.mucommander.commons.io.RandomAccessInputStream;
import com.mucommander.commons.io.RandomAccessOutputStream;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * FTPFile provides access to files located on an FTP server.
 *
 * <p>The associated {@link FileURL} scheme is {@link FileProtocols#FTP}. The host part of the URL designates the
 * FTP server. Credentials must be specified in the login and password parts as FTP servers require a login and
 * password. The path separator is '/'.
 *
 * <p>Here are a few examples of valid FTP URLs:
 * <code>
 * ftp://garfield/stuff/somefile<br>
 * ftp://john:p4sswd@garfield/stuff/somefile<br>
 * ftp://anonymous:john@somewhere.net@garfield/stuff/somefile<br>
 * </code>
 *
 * <p>Internally, FTPFile uses {@link ConnectionPool} to create FTP connections as needed and allows them to be reused
 * by FTPFile instances located on the same server, dealing with concurrency issues. Connections are thus managed
 * transparently and need not be manually managed.
 *
 * <p>Some FileURL properties control certain FTP connection settings:
 * <ul>
 *  <li>{@link #PASSIVE_MODE_PROPERTY_NAME}: controls whether passive or active transfer mode, <code>"true"</code> for
 *  passive mode, <code>"false"</code> for activemode. If the property is not specified when the connection is created,
 *  passive mode is assumed.
 *  <li>{@link #ENCODING_PROPERTY_NAME}: specifies the character encoding used by the server. If the property is not 
 *  specified when the connection is created, {@link #DEFAULT_ENCODING} is assumed.
 * </ul>
 * These properties are only used when the FTP connection is created. Setting them after the connection is created
 * will not have any immediate effect, their values will only be used if the connection needs to be re-established.
 *
 * <p>Access to FTP files is provided by the <code>Commons-net</code> library distributed under the Apache Software License.
 * The {@link #getUnderlyingFileObject()} method allows to retrieve a <code>org.apache.commons.net.ftp.FTPFile</code>
 * instance corresponding to this FTPFile.
 *
 * @see ConnectionPool
 * @author Maxence Bernard
 */
public class FTPFile extends ProtocolFile implements ConnectionHandlerFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(FTPFile.class);

    private org.apache.commons.net.ftp.FTPFile file;

    private String absPath;

    private AbstractFile parent;
    private boolean parentValSet;
    private FilePermissions permissions;

    private boolean fileExists;

    private AbstractFile canonicalFile;

    private final static String SEPARATOR = "/";

    /** Name of the FTP passive mode property */
    public final static String PASSIVE_MODE_PROPERTY_NAME = "passiveMode";

    /** Name of the FTP encoding property */
    public final static String ENCODING_PROPERTY_NAME = "encoding";

    /** Default FTP encoding if {@link #ENCODING_PROPERTY_NAME} is not set */
    public final static String DEFAULT_ENCODING = "UTF-8";

    /** Name of the property that holds the number of retries after a recoverable connection failure (connection error
     * or temporary server error in the 4xx range) */
    public final static String NB_CONNECTION_RETRIES_PROPERTY_NAME = "nbConnectionRetries";

    /** Default value if {@link #NB_CONNECTION_RETRIES_PROPERTY_NAME} is not set */
    public final static int DEFAULT_NB_CONNECTION_RETRIES = 0;

    /** Name of the property that holds the amount of time (in seconds) to wait before retrying to connect after a
     *  temporary connection failure. */
    public final static String CONNECTION_RETRY_DELAY_PROPERTY_NAME = "connectionRetryDelay";

    /** Default value if {@link #CONNECTION_RETRY_DELAY_PROPERTY_NAME} is not set */
    public final static int DEFAULT_CONNECTION_RETRY_DELAY = 15;

    /** Date format used by the SITE UTIME command */
    private final static SimpleDateFormat SITE_UTIME_DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmm");


    protected FTPFile(FileURL fileURL) throws IOException {
        this(fileURL, null);
    }

    protected FTPFile(FileURL fileURL, org.apache.commons.net.ftp.FTPFile file) throws IOException {
        super(fileURL);

        this.absPath = fileURL.getPath();

        if(file==null) {
            this.file = getFTPFile(fileURL);
            // If file doesn't exist (could not be resolved), create it
            if(this.file==null) {
                String name = fileURL.getFilename();    // Filename could potentially be null
                this.file = createFTPFile(name==null?"":name, false);
                this.fileExists = false;
            }
            else {
                this.fileExists = true;
            }
        }
        else {
            this.file = file;
            this.fileExists = true;
        }

        this.permissions = new FTPFilePermissions(this.file);
    }


    private org.apache.commons.net.ftp.FTPFile getFTPFile(FileURL fileURL) throws IOException {
        // Todo: this method is very ineffective as it lists the parent directory to retrieve the information about the
        // requested file to workaround the fact that FTPClient#listFiles follows directories.
        // => Use the MLST command if supported by the server (use FEAT command to find out if it is supported).
        // See http://tools.ietf.org/html/draft-ietf-ftpext-mlst-16
        FileURL parentURL = fileURL.getParent();
        LOGGER.trace("fileURL={} parent={}", fileURL, parentURL);

        // Parent is null, create '/' file
        if(parentURL==null) {
            return createFTPFile("/", true);
        }
        else {
            FTPConnectionHandler connHandler = (FTPConnectionHandler)ConnectionPool.getConnectionHandler(this, fileURL, true);
            org.apache.commons.net.ftp.FTPFile files[];
            try {
                // Makes sure the connection is started, if not starts it
                connHandler.checkConnection();

                // List files contained by this file's parent in order to retrieve the FTPFile instance corresponding
                // to this file
                files = listFiles(connHandler, parentURL.getPath());
            }
            finally {
                // Release the lock on the ConnectionHandler
                connHandler.releaseLock();
            }

            // File doesn't exist
            if(files==null || files.length==0)
                return null;

            // Find the file in the parent folder's contents
            int nbFiles = files.length;
            String wantedName = fileURL.getFilename();
            for(int i=0; i<nbFiles; i++) {
                if(files[i].getName().equalsIgnoreCase(wantedName))
                    return files[i];
            }

            // File doesn't exists
            return null;
        }
    }


    private org.apache.commons.net.ftp.FTPFile createFTPFile(String name, boolean isDirectory) {
        org.apache.commons.net.ftp.FTPFile file = new org.apache.commons.net.ftp.FTPFile();
        file.setName(name);
        file.setSize(0);
        file.setTimestamp(java.util.Calendar.getInstance());
        file.setType(isDirectory?org.apache.commons.net.ftp.FTPFile.DIRECTORY_TYPE:org.apache.commons.net.ftp.FTPFile.FILE_TYPE);
        return file;
    }


    /**
     * Lists and returns the contents of the given path on the server using the given connection handler.
     * The directory contents is listed by issuing a CWD followed by a LIST so after this method is called, the current
     * working directory is left to the specified path.
     *
     * @param connHandler the connection handler to use for communicating with the server
     * @param absPath absolute path to the directory to list
     * @return the directory's contents. The returned array may be empty but never null. The array may contain null
     * individual entries as FTPClient#listFiles's Javadoc mentions.
     * @throws IOException if an error occurred while communicating with the server
     * @throws AuthException if the user is not allowed to access this directory
     */
    private static org.apache.commons.net.ftp.FTPFile[] listFiles(FTPConnectionHandler connHandler, String absPath) throws IOException, AuthException {
        org.apache.commons.net.ftp.FTPFile files[];
        try {
            // Important: the folder is listed by changing the current working directory using the CWD command and then
            // issuing a LIST to list the current directory, instead of issuing a LIST with the path as an argument.
            // So we're sending:
            //
            //   CWD path
            //   LIST
            //
            // Instead of:
            //
            //   LIST path
            //
            // The reason for that is that on some servers 'LIST path with spaces' fails whereas 'CWD path with spaces'
            // succeeds. Most FTP clients seem to be doing this (CWD/LIST instead of LIST), there must be a reason.
            //
            // See:
            // http://www.mucommander.com/forums/viewtopic.php?f=4&t=714
            // http://issues.apache.org/jira/browse/NET-10

            connHandler.ftpClient.changeWorkingDirectory(absPath);
            files = connHandler.ftpClient.listFiles();

            // Throw an IOException if server replied with an error
            connHandler.checkServerReply();

            if(files==null)     // In some rare conditions (bug) this method can return null
                return new org.apache.commons.net.ftp.FTPFile[0];

            return files;
        }
        // This exception is not an IOException and needs to be caught and thrown back as an IOException
        catch(org.apache.commons.net.ftp.parser.ParserInitializationException e) {
            LOGGER.info("ParserInitializationException caught", e);
            throw new IOException();
        }
        catch(IOException e) {
            // Checks if the IOException corresponds to a socket error and in that case, closes the connection
            connHandler.checkSocketException(e);

            // Throw back the IOException
            throw e;
        }
    }


    /////////////////////////////////////////////
    // ConnectionHandlerFactory implementation //
    /////////////////////////////////////////////

    public ConnectionHandler createConnectionHandler(FileURL location) {
        return new FTPConnectionHandler(location);
    }


    /////////////////////////////////////////
    // AbstractFile methods implementation //
    /////////////////////////////////////////

    @Override
    public boolean isSymlink() {
        return file.isSymbolicLink();
    }

    @Override
    public boolean isSystem() {
        return false;
    }

    @Override
    public long getDate() {
        if(isSymlink())
            return ((org.apache.commons.net.ftp.FTPFile)getCanonicalFile().getUnderlyingFileObject()).getTimestamp().getTimeInMillis();

        return file.getTimestamp().getTimeInMillis();
    }

    /**
     * Attempts to change this file's date using the <i>'SITE UTIME'</i> FTP command.
     * This command seems to be implemeted by modern FTP servers such as ProFTPd or PureFTP Server but since it is not
     * part of the basic FTP command set, it may as well not be supported by the remote server.
     */
    @Override
    public void changeDate(long lastModified) throws IOException, UnsupportedFileOperationException {
        // Note: FTPFile.setTimeStamp only changes the instance's date, but doesn't change it on the server-side.
        FTPConnectionHandler connHandler = null;
        try {
            // Retrieve a ConnectionHandler and lock it
            connHandler = (FTPConnectionHandler)ConnectionPool.getConnectionHandler(this, fileURL, true);

            // Throw UnsupportedFileOperationException if we know the 'SITE UTIME' command is not supported by the server
            if(!connHandler.utimeCommandSupported)
                throw new UnsupportedFileOperationException(FileOperation.CHANGE_DATE);

            // Makes sure the connection is started, if not starts it
            connHandler.checkConnection();

            String sdate;
            // SimpleDateFormat instance must be synchronized externally if it is accessed concurrently
            synchronized(SITE_UTIME_DATE_FORMAT) {
                sdate = SITE_UTIME_DATE_FORMAT.format(new Date(lastModified));
            }

            LOGGER.info("sending SITE UTIME {} {}", sdate, absPath);
            boolean success = connHandler.ftpClient.sendSiteCommand("UTIME "+sdate+" "+absPath);
            LOGGER.info("server reply: {}", connHandler.ftpClient.getReplyString());

            if(!success) {
                int replyCode = connHandler.ftpClient.getReplyCode();

                // If server reported that the command is not supported, mark it in the ConnectionHandler so that
                // we don't try it anymore
                if(replyCode==FTPReply.UNRECOGNIZED_COMMAND
                        || replyCode==FTPReply.COMMAND_NOT_IMPLEMENTED 
                        || replyCode==FTPReply.COMMAND_NOT_IMPLEMENTED_FOR_PARAMETER) {

                    LOGGER.info("marking UTIME command as unsupported");
                    connHandler.utimeCommandSupported = false;
                }

                throw new IOException();
            }
        }
        catch(IOException e) {
            // Checks if the IOException corresponds to a socket error and in that case, closes the connection
            if(connHandler!=null)
                connHandler.checkSocketException(e);

            throw e;
        }
        finally {
            // Release the lock on the ConnectionHandler
            if(connHandler!=null)
                connHandler.releaseLock();
        }
    }

    @Override
    public long getSize() {
        if(isSymlink())
            return ((org.apache.commons.net.ftp.FTPFile)getCanonicalFile().getUnderlyingFileObject()).getSize();

        return file.getSize();
    }


    @Override
    public AbstractFile getParent() {
        if(!parentValSet) {
            FileURL parentFileURL = this.fileURL.getParent();
            if(parentFileURL!=null) {
                try {
                    parent = FileFactory.getFile(parentFileURL, null, createFTPFile(parentFileURL.getFilename(), true));
                }
                catch(IOException e) {
                    // No parent, that's all
                }
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


    @Override
    public boolean exists() {
        return this.fileExists;
    }

    @Override
    public FilePermissions getPermissions() {
        if(isSymlink())
            return getCanonicalFile().getAncestor(FTPFile.class).permissions;

        return permissions;
    }

    @Override
    public void changePermission(int access, int permission, boolean enabled) throws IOException, UnsupportedFileOperationException {
        changePermissions(ByteUtils.setBit(permissions.getIntValue(), (permission << (access*3)), enabled));
    }

    /**
     * Returns {@link PermissionBits#FULL_PERMISSION_BITS} if the server supports the 'site chmod' command (not all
     * servers do), {@link PermissionBits#EMPTY_PERMISSION_BITS} otherwise.
     *
     * @return {@link PermissionBits#FULL_PERMISSION_BITS} if the server supports the 'site chmod' command (not all
     * servers do), {@link PermissionBits#EMPTY_PERMISSION_BITS} otherwise
     */
    @Override
    public PermissionBits getChangeablePermissions() {
        try {
            // Do not lock the connection handler, not needed.
            return ((FTPConnectionHandler)ConnectionPool.getConnectionHandler(this, fileURL, false)).chmodCommandSupported
                    ?PermissionBits.FULL_PERMISSION_BITS    // Full permission support (777 octal)
                    :PermissionBits.EMPTY_PERMISSION_BITS;  // Permissions can't be changed
        }
        catch(InterruptedIOException e) {
            // Should not happen in practice
            return PermissionBits.EMPTY_PERMISSION_BITS;  // Permissions can't be changed
        }
    }

    @Override
    public String getOwner() {
        return file.getUser();
    }

    @Override
    public boolean canGetOwner() {
        return true;
    }

    @Override
    public String getGroup() {
        return file.getGroup();
    }

    @Override
    public boolean canGetGroup() {
        return true;
    }

    @Override
    public boolean isDirectory() {
        // org.apache.commons.net.ftp.FTPFile#isDirectory() returns false if the file is a symlink pointing to a
        // directory, this is a limitation of the Commons-net library.
        // Todo: fix this by either:
        // a) find a combination of 'LIST' switches which allows the output to contain both the 'is symlink' and the
        // 'is the symlink target a directory' information. At a first glance, there doesn't seem to be one: either
        // symlinks are followed or there aren't.
        // b) Patch #ls() to issue an extra 'LIST -ldH *' to retrieve all symlinks' information when the directory has
        // at least one symlink.
        // c) if this file is a symlink, retrieve the symlink's target using #getFTPFile(FileURL) with '-ldH' switches
        // and return the value of isDirectory(). This clearly is the least effective solution at it requires issuing
        // one 'ls' command per symlink.

        if(isSymlink())
            return ((org.apache.commons.net.ftp.FTPFile)getCanonicalFile().getUnderlyingFileObject()).isDirectory();

        return file.isDirectory();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return getInputStream(0);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return new FTPOutputStream(false);
    }

    @Override
    public OutputStream getAppendOutputStream() throws IOException {
        return new FTPOutputStream(true);
    }

    /**
     * Always throws an {@link UnsupportedFileOperationException}: random read access is not available.
     *
     * @throws UnsupportedFileOperationException always
     */
    @Override
    @UnsupportedFileOperation
    public RandomAccessInputStream getRandomAccessInputStream() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.RANDOM_READ_FILE);
    }

//    public RandomAccessInputStream getRandomAccessInputStream() throws IOException {
//        return new FTPRandomAccessInputStream();
//    }

    /**
     * Always throws an {@link UnsupportedFileOperationException}: random write access is not available.
     *
     * @throws UnsupportedFileOperationException always
     */
    @Override
    @UnsupportedFileOperation
    public RandomAccessOutputStream getRandomAccessOutputStream() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.RANDOM_WRITE_FILE);
    }

    @Override
    public void delete() throws IOException {
        // Retrieve a ConnectionHandler and lock it
        FTPConnectionHandler connHandler = (FTPConnectionHandler)ConnectionPool.getConnectionHandler(this, fileURL, true);
        try {
            // Makes sure the connection is started, if not starts it
            connHandler.checkConnection();

            if(isDirectory())
                connHandler.ftpClient.removeDirectory(absPath);
            else
                connHandler.ftpClient.deleteFile(absPath);

            // Throw an IOException if server replied with an error
            connHandler.checkServerReply();
        }
        catch(IOException e) {
            // Checks if the IOException corresponds to a socket error and in that case, closes the connection
            connHandler.checkSocketException(e);

            // Re-throw IOException
            throw e;
        }
        finally {
            // Release the lock on the ConnectionHandler
            connHandler.releaseLock();
        }
    }


    @Override
    public AbstractFile[] ls() throws IOException {
        // Retrieve a ConnectionHandler and lock it
        FTPConnectionHandler connHandler = (FTPConnectionHandler)ConnectionPool.getConnectionHandler(this, fileURL, true);
        org.apache.commons.net.ftp.FTPFile files[];
        try {
            // Makes sure the connection is started, if not starts it
            connHandler.checkConnection();

            files = listFiles(connHandler, absPath);
        }
        finally {
            // Release the lock on the ConnectionHandler
            connHandler.releaseLock();
        }

        if(files==null || files.length==0)
            return new AbstractFile[] {};

        AbstractFile children[] = new AbstractFile[files.length];
        AbstractFile child;
        FileURL childURL;
        String childName;
        int nbFiles = files.length;
        int fileCount = 0;
        String parentPath = fileURL.getPath();
        if(!parentPath.endsWith(SEPARATOR))
            parentPath += SEPARATOR;

        for(int i=0; i<nbFiles; i++) {
            if(files[i]==null)
                continue;

            childName = files[i].getName();
            if(childName.equals(".") || childName.equals(".."))
                continue;

            // Note: properties and credentials are cloned for every children's url
            childURL = (FileURL)fileURL.clone();
            childURL.setPath(parentPath+childName);

            // Discard '.' and '..' files
            if(childName.equals(".") || childName.equals(".."))
                continue;

            child = FileFactory.getFile(childURL, this, files[i]);
            children[fileCount++] = child;
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
        FTPConnectionHandler connHandler = (FTPConnectionHandler)ConnectionPool.getConnectionHandler(this, fileURL, true);
        try {
            // Makes sure the connection is started, if not starts it
            connHandler.checkConnection();

            connHandler.ftpClient.makeDirectory(absPath);
            // Throw an IOException if server replied with an error
            connHandler.checkServerReply();

            file.setType(org.apache.commons.net.ftp.FTPFile.DIRECTORY_TYPE);
            fileExists = true;
        }
        catch(IOException e) {
            // Checks if the IOException corresponds to a socket error and in that case, closes the connection
            connHandler.checkSocketException(e);

            // Re-throw IOException
            throw e;
        }
        finally {
            // Release the lock on the ConnectionHandler
            connHandler.releaseLock();
        }
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

    /**
     * Returns an <code>org.apache.commons.net.FTPFile</code> instance corresponding to this file.
     */
    @Override
    public Object getUnderlyingFileObject() {
        return file;
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    /**
     * Changes permissions using the SITE CHMOD FTP command.
     *
     * This command is optional but seems to be supported by modern FTP servers such as ProFTPd or PureFTP Server.
     * But it may as well not be supported by the remote FTP server as it is not part of the basic FTP command set.
     *
     * Implementation note: FTPFile.setPermission only changes the instance's permissions, but doesn't change it on the
     * server-side.
     */
    @Override
    public void changePermissions(int permissions) throws IOException, UnsupportedFileOperationException {
        FTPConnectionHandler connHandler = null;
        try {
            // Retrieve a ConnectionHandler and lock it
            connHandler = (FTPConnectionHandler)ConnectionPool.getConnectionHandler(this, fileURL, true);

            // Return if we know the CHMOD command is not supported by the server
            if(!connHandler.chmodCommandSupported)
                throw new UnsupportedFileOperationException(FileOperation.CHANGE_PERMISSION);

            // Makes sure the connection is started, if not starts it
            connHandler.checkConnection();

            LOGGER.info("sending SITE CHMOD {} {}", Integer.toOctalString(permissions), absPath);
            boolean success = connHandler.ftpClient.sendSiteCommand("CHMOD "+Integer.toOctalString(permissions)+" "+absPath);
            LOGGER.info("server reply: {}", connHandler.ftpClient.getReplyString());

            if(!success) {
                int replyCode = connHandler.ftpClient.getReplyCode();

                // If server reported that the command is not supported, mark it in the ConnectionHandler so that
                // we don't try it anymore
                if(replyCode==FTPReply.UNRECOGNIZED_COMMAND
                        || replyCode==FTPReply.COMMAND_NOT_IMPLEMENTED
                        || replyCode==FTPReply.COMMAND_NOT_IMPLEMENTED_FOR_PARAMETER) {

                    LOGGER.info("marking CHMOD command as unsupported");
                    connHandler.chmodCommandSupported = false;
                }

                throw new IOException();
            }
        }
        catch(IOException e) {
            // Checks if the IOException corresponds to a socket error and in that case, closes the connection
            if(connHandler!=null)
                connHandler.checkSocketException(e);

            throw e;
        }
        finally {
            // Release the lock on the ConnectionHandler
            if(connHandler!=null)
                connHandler.releaseLock();
        }
    }

    /**
     * Implementation notes: always throws an {@link UnsupportedFileOperationException}.
     *
     * @throws UnsupportedFileOperationException always
     */
    @Override
    @UnsupportedFileOperation
    public void copyRemotelyTo(AbstractFile destFile) throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.COPY_REMOTELY);
    }
    
    /**
     * Implementation notes: server-to-server renaming will work if the destination file also uses the 'FTP' scheme
     * and is located on the same host.
     */
    @Override
    public void renameTo(AbstractFile destFile) throws IOException {
        // Throw an exception if the file cannot be renamed to the specified destination
        checkRenamePrerequisites(destFile, false, false);

        FTPConnectionHandler connHandler = null;
        try {
            // Retrieve a ConnectionHandler and lock it
            connHandler = (FTPConnectionHandler)ConnectionPool.getConnectionHandler(this, fileURL, true);
            // Makes sure the connection is started, if not starts it
            connHandler.checkConnection();

            if(!connHandler.ftpClient.rename(absPath, destFile.getURL().getPath()))
                throw new IOException();
        }
        catch(IOException e) {
            // Checks if the IOException corresponds to a socket error and in that case, closes the connection
            if(connHandler!=null)
                connHandler.checkSocketException(e);

            throw e;
        }
        finally {
            // Release the lock on the ConnectionHandler
            if(connHandler!=null)
                connHandler.releaseLock();
        }
    }


    @Override
    public InputStream getInputStream(long offset) throws IOException {
        return new FTPInputStream(offset);
    }

    @Override
    public AbstractFile getCanonicalFile() {
        if(!isSymlink())
            return this;

        // Create the canonical file instance and cache it
        if(canonicalFile==null) {
            // getLink() returns the raw symlink target which can either be an absolute or a relative path. If the path is
            // relative, preprend the absolute path of the symlink's parent folder.
            String symlinkTargetPath = file.getLink();
            if(!symlinkTargetPath.startsWith("/")) {
                String parentPath = fileURL.getParent().getPath();
                if(!parentPath.endsWith("/"))
                    parentPath += "/";
                symlinkTargetPath = parentPath + symlinkTargetPath;
            }

            FileURL canonicalURL = (FileURL)fileURL.clone();
            canonicalURL.setPath(symlinkTargetPath);

            canonicalFile = FileFactory.getFile(canonicalURL);
        }

        return canonicalFile;
    }


    ///////////////////
    // Inner classes //
    ///////////////////

//    private class FTPProcess extends AbstractProcess {
//
//        /** True if the command returned a positive FTP reply code */
//        private boolean success;
//
//        /** Allows to read the command's output */
//        private ByteArrayInputStream bais;
//
//
//        public FTPProcess(String tokens[]) throws IOException {
//
//            // Concatenates all tokens to create the command string
//            String command = "";
//            int nbTokens = tokens.length;
//            for(int i=0; i<nbTokens; i++) {
//                command += tokens[i];
//                if(i!=nbTokens-1)
//                    command += " ";
//            }
//
//            FTPConnectionHandler connHandler = null;
//            try {
//                // Retrieve a ConnectionHandler and lock it
//                connHandler = (FTPConnectionHandler)ConnectionPool.getConnectionHandler(FTPFile.this, fileURL, true);
//                // Makes sure the connection is started, if not starts it
//                connHandler.checkConnection();
//
//                // Change the current directory on the remote server to :
//                //  - this file's path if this file is a directory
//                //  - to the parent folder's path otherwise
//                if(!connHandler.ftpClient.changeWorkingDirectory(isDirectory()?fileURL.getPath():fileURL.getParent().getPath()))
//                    throw new IOException();
//
//                // Has the command been successfully completed by the server ?
//                success = FTPReply.isPositiveCompletion(connHandler.ftpClient.sendCommand(command));
//
//                // Retrieves the command's output and create an InputStream for getInputStream()
//                ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                PrintWriter pw = new PrintWriter(baos, true);
//                String replyStrings[] = connHandler.ftpClient.getReplyStrings();
//                for(int i=0; i<replyStrings.length; i++)
//                    pw.println(replyStrings[i]);
//                pw.close();
//
//                bais = new ByteArrayInputStream(baos.toByteArray());
//                // No need to close the ByteArrayOutputStream
//            }
//            catch(IOException e) {
//               // Checks if the IOException corresponds to a socket error and in that case, closes the connection
//                connHandler.checkSocketException(e);
//
//                // Re-throw IOException
//                throw e;
//            }
//            finally {
//                // Release the lock on the ConnectionHandler
//                if(connHandler!=null)
//                    connHandler.releaseLock();
//            }
//        }
//
//        public boolean usesMergedStreams() {
//            // No specific stream for errors
//            return true;
//        }
//
//        public int waitFor() throws InterruptedException, IOException {
//            return success?0:1;
//        }
//
//        protected void destroyProcess() throws IOException {
//            // No-op, command has already been executed
//        }
//
//        public int exitValue() {
//            return success?0:1;
//        }
//
//        public OutputStream getOutputStream() throws IOException {
//            // FTP commands are not interactive, the returned OutputStream simply ignores data that's fed to it
//            return new SinkOutputStream();
//        }
//
//        public InputStream getInputStream() throws IOException {
//            if(bais==null)
//                throw new IOException();
//
//            return bais;
//        }
//
//        public InputStream getErrorStream() throws IOException {
//            return getInputStream();
//        }
//    }

    private class FTPInputStream extends FilterInputStream {

        private FTPConnectionHandler connHandler;
        private boolean isClosed;

        private FTPInputStream(long skipBytes) throws IOException {
            super(null);

            try {
                // Retrieve a ConnectionHandler and lock it
                connHandler = (FTPConnectionHandler)ConnectionPool.getConnectionHandler(FTPFile.this, FTPFile.this.fileURL, true);
                // Makes sure the connection is started, if not starts it
                connHandler.checkConnection();

                if(skipBytes>0) {
                    // Resume transfer at the given offset
                    connHandler.ftpClient.setRestartOffset(skipBytes);
                }

                in = connHandler.ftpClient.retrieveFileStream(absPath);
                if(in==null) {
                    if(skipBytes>0) {
                        // Reset offset
                        connHandler.ftpClient.setRestartOffset(0);
                    }
                    throw new IOException();
                }
            }
            catch(IOException e) {
                if(connHandler!=null) {
                    // Checks if the IOException corresponds to a socket error and in that case, closes the connection
                    connHandler.checkSocketException(e);

                    // Release the lock on the ConnectionHandler if the InputStream could not be created
                    connHandler.releaseLock();
                }

                // Re-throw IOException
                throw e;
            }
        }

        @Override
        public void close() throws IOException {
            // Make sure this method is only executed once, otherwise FTPClient#completePendingCommand() would lock
            if(isClosed)
                return;

            isClosed = true;

            try {
                super.close();

                LOGGER.info("complete pending commands");
                connHandler.ftpClient.completePendingCommand();
                LOGGER.info("commands completed");

                // Todo: An IOException will be thrown by completePendingCommand if the transfer has not finished before calling close.
                // An 'abort' command should be issued to the server before closing if the transfer is not finished yet.
                // Currently in that case (transfer not finished) the whole connection has to be re-established (bad!).
                // FTPClient#abort() is difficult to use to say the least. This post gives some insight: http://mail-archives.apache.org/mod_mbox/commons-user/200604.mbox/%3c78A73ABD8DB470439179DB682EA990B3025B87DF@mtlex02.NEXXLINK.INT%3e
            }
            catch(IOException e) {
                LOGGER.info("exception in completePendingCommands()", e);

                // Checks if the IOException corresponds to a socket error and in that case, closes the connection
                connHandler.checkSocketException(e);

                // Do not re-throw the exception because an IOException will be thrown if close is called before
                // the transfer is finished (see above) which is pseudo-normal behavior (though sub-optimal).
//                // Re-throw IOException
//                throw e;
            }
            finally {
                // Release the lock on the ConnectionHandler
                connHandler.releaseLock();
            }
        }
    }


    // This class works but because of the bug in FTPInputStream#close() which fails to interrupt an ongoing transfer
    // gracefully, seek() will re-establish the FTP connection each time it is called, which is definitely not acceptable.
    // Therefore, this class cannot be used at the moment.
    private class FTPRandomAccessInputStream extends RandomAccessInputStream {

        private FTPInputStream in;
        private long offset;

        private FTPRandomAccessInputStream() throws IOException {
            this.in = new FTPInputStream(0);
        }

        @Override
        public int read() throws IOException {
            int read = in.read();

            if(read!=-1)
                offset += 1;

            return read;
        }

        @Override
        public int read(byte b[], int off, int len) throws IOException {
            int nbRead = in.read(b, off, len);

            if(nbRead!=-1)
                offset += nbRead;

            return nbRead;
        }

        public long getOffset() throws IOException {
            return offset;
        }

        public long getLength() throws IOException {
            return FTPFile.this.getSize();
        }

        public void seek(final long offset) throws IOException {
            try {
                in.close();
            }
            catch(IOException e) {}

            in = new FTPInputStream(offset);
            this.offset = offset;
        }

        @Override
        public void close() throws IOException {
            in.close();
        }
    }


    private class FTPOutputStream extends FilteredOutputStream {

        private FTPConnectionHandler connHandler;
        private boolean isClosed;

        private FTPOutputStream(boolean append) throws IOException {
            super(null);

            try {
                // Retrieve a ConnectionHandler and lock it
                connHandler = (FTPConnectionHandler)ConnectionPool.getConnectionHandler(FTPFile.this, fileURL, true);
                // Makes sure the connection is started, if not starts it
                connHandler.checkConnection();

                if(append)
                    out = connHandler.ftpClient.appendFileStream(absPath);
                else
                    out = connHandler.ftpClient.storeFileStream(absPath);   // Note: do NOT use storeUniqueFileStream which appends .1 if the file already exists and fails with proftpd

                if(out==null)
                    throw new IOException();
            }
            catch(IOException e) {
                if(connHandler!=null) {
                    // Checks if the IOException corresponds to a socket error and in that case, closes the connection
                    connHandler.checkSocketException(e);

                    // Release the lock on the ConnectionHandler if the OutputStream could not be created
                    connHandler.releaseLock();
                }

                // Re-throw IOException
                throw e;
            }
        }

        @Override
        public void close() throws IOException {
            // Make sure this method is only executed once, otherwise FTPClient#completePendingCommand() would lock
            if(isClosed)
                return;

            isClosed = true;

            try {
                super.close();

                LOGGER.trace("complete pending commands");
                connHandler.ftpClient.completePendingCommand();
                LOGGER.trace("commands completed");
            }
            catch(IOException e) {
                LOGGER.info("exception in completePendingCommands()", e);

                // Checks if the IOException corresponds to a socket error and in that case, closes the connection
                connHandler.checkSocketException(e);

                // Re-throw IOException
                throw e;
            }
            finally {
                // Release the lock on the ConnectionHandler
                connHandler.releaseLock();
            }
        }
    }


    /**
     * Handles connection to an FTP server.
     */
    private static class FTPConnectionHandler extends ConnectionHandler {

        private FTPClient ftpClient;
//        private CustomFTPClient ftpClient;

        /** Controls whether passive mode should be used for data transfers (default is true) */
        private boolean passiveMode;

        /** Encoding used by the FTP control connection */
        private String encoding;

        /** Number of connection retry attempts after a recoverable connection failure */
        private int nbConnectionRetries;

        /** Amount of time (in seconds) to wait before retrying to connect after a recoverable connection failure */
        private int connectionRetryDelay;

        /** False if SITE UTIME command is not supported by the remote server (once tried and failed) */
        private boolean utimeCommandSupported = true;

        /** False if SITE CHMOD command is not supported by the remote server (once tried and failed) */
        private boolean chmodCommandSupported = true;

        /** Controls how ofter should keepAlive() be called by ConnectionPool */
        private final static long KEEP_ALIVE_PERIOD = 60;

//        /** Connection timeout to the FTP server in seconds */
//        private final static int CONNECTION_TIMEOUT = 30;

//        private class CustomFTPClient extends FTPClient {
//
//            private Socket getSocket() {
//                return _socket_;
//            }
//        }


        private FTPConnectionHandler(FileURL location) {
            super(location);

            // Use the passive mode property if it is set
            String passiveModeProperty = location.getProperty(PASSIVE_MODE_PROPERTY_NAME);
            // Passive mode is enabled by default if property isn't specified
            this.passiveMode = passiveModeProperty==null || !passiveModeProperty.equals("false");

            // Use the encoding property if it is set
            this.encoding = location.getProperty(ENCODING_PROPERTY_NAME);
            if(encoding==null || encoding.equals(""))
                encoding = DEFAULT_ENCODING;

            // Use the property that controls the number of connection retries after a recoverable connection failure,
            // if the property is set
            String prop = location.getProperty(NB_CONNECTION_RETRIES_PROPERTY_NAME);
            if(prop==null) {
                nbConnectionRetries = DEFAULT_NB_CONNECTION_RETRIES;
            }
            else {
                try { nbConnectionRetries = Integer.parseInt(prop); }
                catch(NumberFormatException e) { nbConnectionRetries = DEFAULT_NB_CONNECTION_RETRIES; }
            }

            // Use the property that controls the connection retry delay after a recoverable connection failure,
            // if the property is set
            prop = location.getProperty(CONNECTION_RETRY_DELAY_PROPERTY_NAME);
            if(prop==null) {
                connectionRetryDelay = DEFAULT_CONNECTION_RETRY_DELAY;
            }
            else {
                try { connectionRetryDelay = Integer.parseInt(prop); }
                catch(NumberFormatException e) { connectionRetryDelay = DEFAULT_CONNECTION_RETRY_DELAY; }
            }

            setKeepAlivePeriod(KEEP_ALIVE_PERIOD);
        }


        /**
         * Checks the last server reply code and throws an IOException if the code doesn't correspond to a positive
         * FTP reply:
         *
         * <ul>
         * <li>If the reply is a credentials error (lack of permissions or not logged in), an {@link AuthException}
         * is thrown. For all other error codes, an IOException is thrown with the server reply message.
         * <li>If the reply code is FTPReply.SERVICE_NOT_AVAILABLE (connection dropped prematurely), the connection
         * will be closed before an IOException with the server reply message is thrown.
         * </ul>
         *
         * <p>If the reply is a positive one (not an error error), this method does nothing.
         */
        private void checkServerReply() throws IOException, AuthException {
            // Check that connection went ok
            int replyCode = ftpClient.getReplyCode();
            LOGGER.trace("server reply="+ftpClient.getReplyString());

            // Close connection if the connection dropped prematurely so that isConnected() returns false
            if(replyCode==FTPReply.SERVICE_NOT_AVAILABLE)
                closeConnection();

            // If not, throw an exception using the reply string
            if(!FTPReply.isPositiveCompletion(replyCode)) {
                if(replyCode==FTPReply.CODE_503 || replyCode==FTPReply.NEED_PASSWORD || replyCode==FTPReply.NOT_LOGGED_IN)
                    throwAuthException(ftpClient.getReplyString());
                else
                    throw new IOException(ftpClient.getReplyString());
            }
        }


        /**
         * Checks if the given IOException corresponds to a low-level socket exception, and if that is the case,
         * closes the connection so that {@link #isConnected()} returns false.
         * All IOException raised by FTPClient should be checked by this method so that socket errors are properly detected.
         */
        private void checkSocketException(IOException e) {
            if(((e instanceof FTPConnectionClosedException) || (e instanceof SocketException) || (e instanceof SocketTimeoutException)) && isConnected()) {
                LOGGER.info("socket exception detected, closing connection", e);
                closeConnection();
            }
        }


        //////////////////////////////////////
        // ConnectionHandler implementation //
        //////////////////////////////////////

        @Override
        public void startConnection() throws IOException {
            LOGGER.info("connecting to {}", getRealm().getHost());

//            this.ftpClient = new CustomFTPClient();
            this.ftpClient = new FTPClient();

            int retriesLeft = nbConnectionRetries;
            int retryDelay = connectionRetryDelay *1000;
            do{
	            try {
	                FileURL realm = getRealm();
	
	                // Override default port (21) if a custom port was specified in the URL
	                int port = realm.getPort();
	                LOGGER.info("custom port={}", port);
	                if(port!=-1)
	                    ftpClient.setDefaultPort(port);
	
	                // Sets the control encoding
	                // - most modern FTP servers seem to default to UTF-8, but not all of them do.
	                // - commons-ftp defaults to ISO-8859-1 which is not good
	                // Note: this has to be done before the connection is established otherwise it won't be taken into account
	                LOGGER.info("encoding={}", encoding);
	                ftpClient.setControlEncoding(encoding);
	
	                // Connect to the FTP server
	                ftpClient.connect(realm.getHost());
	
	//                // Set a socket timeout: default value is 0 (no timeout)
	//                ftpClient.setSoTimeout(CONNECTION_TIMEOUT*1000);
	//                FileLogger.finer("soTimeout="+ftpClient.getSoTimeout());
	
	                // Throw an IOException if server replied with an error
	                checkServerReply();

	                Credentials credentials = getCredentials();
	
	                // Throw an AuthException if there are no credentials
	                LOGGER.info("fileURL={} credentials={}", realm.toString(true), credentials);
	                if(credentials ==null)
	                    throwAuthException(null);
	
	                // Login
	                ftpClient.login(credentials.getLogin(), credentials.getPassword());
	                // Throw an IOException (potentially an AuthException) if the server replied with an error
	                checkServerReply();
	
	                // Enables/disables passive mode
	                LOGGER.info("passiveMode={}", passiveMode);
	                if(passiveMode)
	                    this.ftpClient.enterLocalPassiveMode();
	                else
	                    this.ftpClient.enterLocalActiveMode();
	
	                // Set file type to 'binary'
	                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
	
	                // Issue 'LIST -al' command to list hidden files (instead of LIST -l), only if the corresponding
	                // configuration option has been manually enabled in the preferences.
	                // The reason for not doing so by default is that the commons-net library will fail to properly parse
	                // directory listings on some servers when 'LIST -al' is used (bug).
	                // Note that by default, if 'LIST -l' is used, the decision to list hidden files is left to the
	                // FTP server: some servers will choose to show them, some will not. This behavior is usually a
	                // configuration setting of the FTP server.
	                ftpClient.setListHiddenFiles(FTPProtocolProvider.getForceHiddenFilesListing());
	
	                if(encoding.equalsIgnoreCase("UTF-8")) {
	                    // This command enables UTF8 on the remote server... but only a few FTP servers currently support this command
	                    ftpClient.sendCommand("OPTS UTF8 ON");
	                }

	                break;
	            }
	            catch(IOException e) {
                    // Attempt to retry if the connection failed, or if the server reply corresponds to a temporary error.
                    // Unlike 5xx errors which are permanent, 4xx errors are temporary and may be retried, quote from
                    // RFC 959: "The command was not accepted and the requested action did not take place, but the error
                    // condition is temporary and the action may be requested again."
	                int replyCode = ftpClient.getReplyCode();
                    if(!ftpClient.isConnected() || FTPReply.isNegativeTransient(replyCode)) {
                        LOGGER.info((!ftpClient.isConnected()?"Connection error":"Temporary server error ("+replyCode+")")+", retries left="+retriesLeft, e);

                        // Retry to connect, if we have at least an attempt left
                        if(retriesLeft>0) {
                            retriesLeft--;

                            // Wait before retrying
                            if(retryDelay>0) {
                                LOGGER.info("waiting {} ms before retrying to connect", retryDelay);

                                try { Thread.sleep(retryDelay); }
                                catch(InterruptedException e2) {}
                            }

                            continue;
                        }
                    }

                    // Disconnect if the connection could not be established
                    if(ftpClient.isConnected())
                        try { ftpClient.disconnect(); } catch(IOException e2) {}

                    // Re-throw the exception
                    throw e;
	            }
            }
            while(true);
        }


        @Override
        public boolean isConnected() {
            // FTPClient#isConnected() will always return true once it is connected and does not detect socket
            // disconnections. Furthermore, retrieving the underlying Socket instance does not help any more:
            // Socket#isConnected() and Socket#isClosed() do not reflect socket errors that happen after the socket is
            // connected.
            // Thus, the only way (AFAIK) to know if the socket is still connected is to intercept all IOException
            // thrown by FTPClient and check if they correspond to a socket exception.

            return ftpClient!=null && ftpClient.isConnected();

//            if(ftpClient==null || !ftpClient.isConnected())
//                return false;
//
//            Socket socket = ftpClient.getSocket();
//            FileLogger.finest("socket="+socket+" socket.isConnected()"+socket.isConnected()+" socket.isClosed()="+socket.isClosed());
//
//            return socket!=null && socket.isConnected() && !socket.isClosed();
        }


        @Override
        public void closeConnection() {
            if(ftpClient!=null) {
                // Try to logout, this may fail if the connection is broken
                try { ftpClient.logout(); }
                catch(IOException e) {}

                // Close the socket connection
                try { ftpClient.disconnect(); }
                catch(IOException e) {}

                ftpClient = null;
            }
        }


        @Override
        public void keepAlive() {
            // Send a NOOP command to the server to keep the connection alive.
            // Note: not all FTP servers support the NOOP command.
            if(ftpClient!=null) {
                try {
                    ftpClient.sendNoOp();
                }
                catch(IOException e) {
                    // Checks if the IOException corresponds to a socket error and in that case, closes the connection
                    checkSocketException(e);
                }
            }
        }
    }

    /**
     * A Permissions implementation for FTPFile.
     */
    private static class FTPFilePermissions extends IndividualPermissionBits implements FilePermissions {

        private org.apache.commons.net.ftp.FTPFile file;

        public FTPFilePermissions(org.apache.commons.net.ftp.FTPFile file) {
            this.file = file;
        }

        public boolean getBitValue(int access, int type) {
            int fAccess;
            int fPermission;

            if(access==USER_ACCESS)
                fAccess = org.apache.commons.net.ftp.FTPFile.USER_ACCESS;
            else if(access==GROUP_ACCESS)
                fAccess = org.apache.commons.net.ftp.FTPFile.GROUP_ACCESS;
            else if(access==OTHER_ACCESS)
                fAccess = org.apache.commons.net.ftp.FTPFile.WORLD_ACCESS;
            else
                return false;

            if(type==READ_PERMISSION)
                fPermission = org.apache.commons.net.ftp.FTPFile.READ_PERMISSION;
            else if(type==WRITE_PERMISSION)
                fPermission = org.apache.commons.net.ftp.FTPFile.WRITE_PERMISSION;
            else if(type==EXECUTE_PERMISSION)
                fPermission = org.apache.commons.net.ftp.FTPFile.EXECUTE_PERMISSION;
            else
                return false;

            return file.hasPermission(fAccess, fPermission);
        }

        public PermissionBits getMask() {
            return FULL_PERMISSION_BITS;        
        }
    }
}
