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


package com.mucommander.file.impl.ftp;

import com.mucommander.Debug;
import com.mucommander.auth.AuthException;
import com.mucommander.auth.Credentials;
import com.mucommander.conf.impl.MuConfiguration;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.file.FileProtocols;
import com.mucommander.file.FileURL;
import com.mucommander.file.connection.ConnectionHandler;
import com.mucommander.file.connection.ConnectionHandlerFactory;
import com.mucommander.file.connection.ConnectionPool;
import com.mucommander.io.FileTransferException;
import com.mucommander.io.RandomAccessInputStream;
import com.mucommander.io.RandomAccessOutputStream;
import com.mucommander.io.SinkOutputStream;
import com.mucommander.process.AbstractProcess;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * FTPFile provides access to files located on an FTP server.
 *
 * <p>The associated {@link FileURL} protocol is {@link FileProtocols#FTP}. The host part of the URL designates the
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
public class FTPFile extends AbstractFile implements ConnectionHandlerFactory {

    private org.apache.commons.net.ftp.FTPFile file;

    protected String absPath;

    private AbstractFile parent;
    private boolean parentValSet;

    private boolean fileExists;

    private final static String SEPARATOR = "/";

    /** Name of the FTP passive mode property */
    public final static String PASSIVE_MODE_PROPERTY_NAME = "passiveMode";

    /** Name of the FTP encoding property */
    public final static String ENCODING_PROPERTY_NAME = "encoding";

    /** Default FTP encoding */
    public final static String DEFAULT_ENCODING = "UTF-8";

    /** Date format used by the SITE UTIME command */
    private final static SimpleDateFormat SITE_UTIME_DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmm");


    public FTPFile(FileURL fileURL) throws IOException {
        this(fileURL, null);
    }

    public FTPFile(FileURL fileURL, org.apache.commons.net.ftp.FTPFile file) throws IOException {
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
    }


    private org.apache.commons.net.ftp.FTPFile getFTPFile(FileURL fileURL) throws IOException {
        FileURL parentURL = fileURL.getParent();
        if(Debug.ON) Debug.trace("fileURL="+fileURL+" parent="+parentURL);

        // Parent is null, create '/' file
        if(parentURL==null) {
            return createFTPFile("/", true);
        }
        else {
            FTPConnectionHandler connHandler = null;
            org.apache.commons.net.ftp.FTPFile files[];
            try {
                // Retrieve a ConnectionHandler and lock it
                connHandler = (FTPConnectionHandler)ConnectionPool.getConnectionHandler(this, fileURL, true);
                // Makes sure the connection is started, if not starts it
                connHandler.checkConnection();

                // List files contained by this file's parent in order to retrieve the FTPFile instance corresponding
                // to this file
                files = connHandler.ftpClient.listFiles(parentURL.getPath());

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
                if(connHandler!=null)
                    connHandler.releaseLock();
            }

            // File doesn't exist
            if(files==null || files.length==0)
                return null;

            // Find file from parent folder
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


    /////////////////////////////////////////////
    // ConnectionHandlerFactory implementation //
    /////////////////////////////////////////////

    public ConnectionHandler createConnectionHandler(FileURL location) {
        return new FTPConnectionHandler(location);
    }


    /////////////////////////////////////////
    // AbstractFile methods implementation //
    /////////////////////////////////////////

    public boolean isSymlink() {
        return file.isSymbolicLink();
    }

    public long getDate() {
        return file.getTimestamp().getTime().getTime();
    }

    public boolean changeDate(long lastModified) {
        // Changes the date using the SITE UTIME FTP command.
        // This command is optional but seems to be supported by modern FTP servers such as ProFTPd or PureFTP Server.
        // But it may as well not be supported by the remote FTP server as it is not part of the basic FTP command set.

        // Implementation note: FTPFile.setTimeStamp only changes the instance's date, but doesn't change it on the server-side.
        FTPConnectionHandler connHandler = null;
        try {
            // Retrieve a ConnectionHandler and lock it
            connHandler = (FTPConnectionHandler)ConnectionPool.getConnectionHandler(this, fileURL, true);

            // Return if we know the UTIME command is not supported by the server
            if(!connHandler.utimeCommandSupported)
                return false;

            // Makes sure the connection is started, if not starts it
            connHandler.checkConnection();

            String sdate;
            // SimpleDateFormat instance must be synchronized externally if it is accessed concurrently
            synchronized(SITE_UTIME_DATE_FORMAT) {
                sdate = SITE_UTIME_DATE_FORMAT.format(new Date(lastModified));
            }

            if(Debug.ON) Debug.trace("sending SITE UTIME "+sdate+" "+absPath);
            boolean success = connHandler.ftpClient.sendSiteCommand("UTIME "+sdate+" "+absPath);
            if(Debug.ON) Debug.trace("server reply: "+connHandler.ftpClient.getReplyString());

            if(!success) {
                int replyCode = connHandler.ftpClient.getReplyCode();

                // If server reported that the command is not supported, mark it in the ConnectionHandler so that
                // we don't try it anymore
                if(replyCode==FTPReply.UNRECOGNIZED_COMMAND
                        || replyCode==FTPReply.COMMAND_NOT_IMPLEMENTED 
                        || replyCode==FTPReply.COMMAND_NOT_IMPLEMENTED_FOR_PARAMETER) {

                    if(Debug.ON) Debug.trace("marking UTIME command as unsupported");
                    connHandler.utimeCommandSupported = false;
                }
            }

            return success;
        }
        catch(IOException e) {
            // Checks if the IOException corresponds to a socket error and in that case, closes the connection
            connHandler.checkSocketException(e);

            return false;
        }
        finally {
            // Release the lock on the ConnectionHandler
            if(connHandler!=null)
                connHandler.releaseLock();
        }
    }

    public long getSize() {
        return file.getSize();
    }


    public AbstractFile getParent() {
        if(!parentValSet) {
            FileURL parentFileURL = this.fileURL.getParent();
            if(parentFileURL!=null) {
                try {
                    this.parent = new FTPFile(parentFileURL, createFTPFile(parentFileURL.getFilename(), true));
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


    public boolean exists() {
        return this.fileExists;
    }


    public boolean getPermission(int access, int permission) {
        int fAccess;
        int fPermission;

        if(access== USER_ACCESS)
            fAccess = org.apache.commons.net.ftp.FTPFile.USER_ACCESS;
        else if(access==GROUP_ACCESS)
            fAccess = org.apache.commons.net.ftp.FTPFile.GROUP_ACCESS;
        else if(access==OTHER_ACCESS)
            fAccess = org.apache.commons.net.ftp.FTPFile.WORLD_ACCESS;
        else
            return false;

        if(permission==READ_PERMISSION)
            fPermission = org.apache.commons.net.ftp.FTPFile.READ_PERMISSION;
        else if(permission==WRITE_PERMISSION)
            fPermission = org.apache.commons.net.ftp.FTPFile.WRITE_PERMISSION;
        else if(permission==EXECUTE_PERMISSION)
            fPermission = org.apache.commons.net.ftp.FTPFile.EXECUTE_PERMISSION;
        else
            return false;

        return file.hasPermission(fAccess, fPermission);
    }


    public boolean setPermission(int access, int permission, boolean enabled) {
        return setPermissions(setPermissionBit(getPermissions(), (permission << (access*3)), enabled));
    }

    public boolean canGetPermission(int access, int permission) {
        return true;    // Full permission support
    }

    public boolean canSetPermission(int access, int permission) {
        // Return true if the server supports the 'site chmod' command, not all servers do.
        // Do not lock the connection handler, not needed.
        return ((FTPConnectionHandler)ConnectionPool.getConnectionHandler(this, fileURL, false)).chmodCommandSupported;
    }


    public boolean isDirectory() {
        return file.isDirectory();
    }

    public InputStream getInputStream() throws IOException {
        return getInputStream(0);
    }

    public OutputStream getOutputStream(boolean append) throws IOException {
        return new FTPOutputStream(append);
    }

    public boolean hasRandomAccessInputStream() {
        // No random access for FTP files unfortunately
        return false;
    }

    public RandomAccessInputStream getRandomAccessInputStream() throws IOException {
        throw new IOException();
    }

//    public boolean hasRandomAccessInputStream() {
//        return true;
//    }
//
//    public RandomAccessInputStream getRandomAccessInputStream() throws IOException {
//        return new FTPRandomAccessInputStream();
//    }

    public boolean hasRandomAccessOutputStream() {
        // No random access for FTP files unfortunately
        return false;
    }

    public RandomAccessOutputStream getRandomAccessOutputStream() throws IOException {
        throw new IOException();
    }

    public void delete() throws IOException {
        FTPConnectionHandler connHandler = null;
        try {
            // Retrieve a ConnectionHandler and lock it
            connHandler = (FTPConnectionHandler)ConnectionPool.getConnectionHandler(this, fileURL, true);
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
            if(connHandler!=null)
                connHandler.releaseLock();
        }
    }


    public AbstractFile[] ls() throws IOException {
        org.apache.commons.net.ftp.FTPFile files[];
        FTPConnectionHandler connHandler = null;
        try {
            // Retrieve a ConnectionHandler and lock it
            connHandler = (FTPConnectionHandler)ConnectionPool.getConnectionHandler(this, fileURL, true);
            // Makes sure the connection is started, if not starts it
            connHandler.checkConnection();

            try {
                files = connHandler.ftpClient.listFiles(absPath);
            }
            // This exception is not an IOException and needs to be caught and rethrown
            catch(org.apache.commons.net.ftp.parser.ParserInitializationException e) {
                if(Debug.ON) Debug.trace("ParserInitializationException caught");
                throw new IOException();
            }

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
            if(connHandler!=null)
                connHandler.releaseLock();
        }

        if(files==null)
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

            child = FileFactory.wrapArchive(new FTPFile(childURL, files[i]));
            child.setParent(this);
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


    public void mkdir() throws IOException {
        FTPConnectionHandler connHandler = null;
        try {
            // Retrieve a ConnectionHandler and lock it
            connHandler = (FTPConnectionHandler)ConnectionPool.getConnectionHandler(this, fileURL, true);
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
            if(connHandler!=null)
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
     * Returns an <code>org.apache.commons.net.FTPFile</code> instance corresponding to this file.
     */
    public Object getUnderlyingFileObject() {
        return file;
    }

    public boolean canRunProcess() {
        return true;
    }

    public AbstractProcess runProcess(String[] tokens) throws IOException {
        return new FTPProcess(tokens);
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    public boolean setPermissions(int permissions) {
        // Changes permissions using the SITE CHMOD FTP command.

        // This command is optional but seems to be supported by modern FTP servers such as ProFTPd or PureFTP Server.
        // But it may as well not be supported by the remote FTP server as it is not part of the basic FTP command set.

        // Implementation note: FTPFile.setPermission only changes the instance's permissions, but doesn't change it on the server-side.
        FTPConnectionHandler connHandler = null;
        try {
            // Retrieve a ConnectionHandler and lock it
            connHandler = (FTPConnectionHandler)ConnectionPool.getConnectionHandler(this, fileURL, true);

            // Return if we know the CHMOD command is not supported by the server
            if(!connHandler.chmodCommandSupported)
                return false;

            // Makes sure the connection is started, if not starts it
            connHandler.checkConnection();

            if(Debug.ON) Debug.trace("sending SITE CHMOD "+Integer.toOctalString(permissions)+" "+absPath);
            boolean success = connHandler.ftpClient.sendSiteCommand("CHMOD "+Integer.toOctalString(permissions)+" "+absPath);
            if(Debug.ON) Debug.trace("server reply: "+connHandler.ftpClient.getReplyString());

            if(!success) {
                int replyCode = connHandler.ftpClient.getReplyCode();

                // If server reported that the command is not supported, mark it in the ConnectionHandler so that
                // we don't try it anymore
                if(replyCode==FTPReply.UNRECOGNIZED_COMMAND
                        || replyCode==FTPReply.COMMAND_NOT_IMPLEMENTED
                        || replyCode==FTPReply.COMMAND_NOT_IMPLEMENTED_FOR_PARAMETER) {

                    if(Debug.ON) Debug.trace("marking CHMOD command as unsupported");
                    connHandler.chmodCommandSupported = false;
                }
            }

            return success;
        }
        catch(IOException e) {
            // Checks if the IOException corresponds to a socket error and in that case, closes the connection
            connHandler.checkSocketException(e);

            return false;
        }
        finally {
            // Release the lock on the ConnectionHandler
            if(connHandler!=null)
                connHandler.releaseLock();
        }
    }

    public int getPermissionGetMask() {
        return 511;     // Full get permission support (777 octal)
    }

    public int getPermissionSetMask() {
        // Return true if the server supports the 'site chmod' command, not all servers do.
        // Do not lock the connection handler, not needed.
        return ((FTPConnectionHandler)ConnectionPool.getConnectionHandler(this, fileURL, false)).chmodCommandSupported
                ?511    // Full permission support (777 octal)
                :0;     // No set permission support
    }


    /**
     * Overrides {@link AbstractFile#moveTo(AbstractFile)} to support server-to-server move if the destination file
     * uses FTP and is located on the same host.
     */
    public boolean moveTo(AbstractFile destFile) throws FileTransferException {
        // If destination file is an FTP file located on the same server, tell the server to rename the file.

        // Use the default moveTo() implementation if the destination file doesn't use FTP
        // or is not on the same host
        if(!destFile.getURL().getProtocol().equals(FileProtocols.FTP) || !destFile.getURL().getHost().equals(this.fileURL.getHost())) {
            return super.moveTo(destFile);
        }

        // If destination file is not an FTPFile nor has a FTPFile ancestor (for instance an archive entry),
        // server renaming won't work so use default moveTo() implementation instead
        if(!(destFile.getTopAncestor() instanceof FTPFile)) {
            return super.moveTo(destFile);
        }

        FTPConnectionHandler connHandler = null;
        try {
            // Retrieve a ConnectionHandler and lock it
            connHandler = (FTPConnectionHandler)ConnectionPool.getConnectionHandler(this, fileURL, true);
            // Makes sure the connection is started, if not starts it
            connHandler.checkConnection();

            return connHandler.ftpClient.rename(absPath, destFile.getURL().getPath());
        }
        catch(IOException e) {
            // Checks if the IOException corresponds to a socket error and in that case, closes the connection
            connHandler.checkSocketException(e);

            throw new FileTransferException(FileTransferException.UNKNOWN_REASON);    // Report that move failed
        }
        finally {
            // Release the lock on the ConnectionHandler
            if(connHandler!=null)
                connHandler.releaseLock();
        }
    }


    public InputStream getInputStream(long offset) throws IOException {
        return new FTPInputStream(offset);
    }


    ///////////////////
    // Inner classes //
    ///////////////////

    private class FTPProcess extends AbstractProcess {

        /** True if the command returned a positive FTP reply code */
        private boolean success;

        /** Allows to read the command's output */
        private ByteArrayInputStream bais;


        public FTPProcess(String tokens[]) throws IOException {

            // Concatenates all tokens to create the command string
            String command = "";
            int nbTokens = tokens.length;
            for(int i=0; i<nbTokens; i++) {
                command += tokens[i];
                if(i!=nbTokens-1)
                    command += " ";
            }

            FTPConnectionHandler connHandler = null;
            try {
                // Retrieve a ConnectionHandler and lock it
                connHandler = (FTPConnectionHandler)ConnectionPool.getConnectionHandler(FTPFile.this, fileURL, true);
                // Makes sure the connection is started, if not starts it
                connHandler.checkConnection();

                // Change the current directory on the remote server to :
                //  - this file's path if this file is a directory
                //  - to the parent folder's path otherwise
                if(!connHandler.ftpClient.changeWorkingDirectory(isDirectory()?fileURL.getPath():fileURL.getParent().getPath()))
                    throw new IOException();

                // Has the command been successfully completed by the server ?
                success = FTPReply.isPositiveCompletion(connHandler.ftpClient.sendCommand(command));

                // Retrieves the command's output and create an InputStream for getInputStream()
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintWriter pw = new PrintWriter(baos, true);
                String replyStrings[] = connHandler.ftpClient.getReplyStrings();
                for(int i=0; i<replyStrings.length; i++)
                    pw.println(replyStrings[i]);
                pw.close();

                bais = new ByteArrayInputStream(baos.toByteArray());
                // No need to close the ByteArrayOutputStream
            }
            catch(IOException e) {
               // Checks if the IOException corresponds to a socket error and in that case, closes the connection
                connHandler.checkSocketException(e);

                // Re-throw IOException
                throw e;
            }
            finally {
                // Release the lock on the ConnectionHandler
                if(connHandler!=null)
                    connHandler.releaseLock();
            }
        }

        public boolean usesMergedStreams() {
            // No specific stream for errors
            return true;
        }

        public int waitFor() throws InterruptedException, IOException {
            return success?0:1;
        }

        protected void destroyProcess() throws IOException {
            // No-op, command has already been executed
        }

        public int exitValue() {
            return success?0:1;
        }

        public OutputStream getOutputStream() throws IOException {
            // FTP commands are not interactive, the returned OutputStream simply ignores data that's fed to it
            return new SinkOutputStream();
        }

        public InputStream getInputStream() throws IOException {
            if(bais==null)
                throw new IOException();

            return bais;
        }

        public InputStream getErrorStream() throws IOException {
            return getInputStream();
        }
    }


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
                // Checks if the IOException corresponds to a socket error and in that case, closes the connection
                connHandler.checkSocketException(e);

                // Release the lock on the ConnectionHandler if the InputStream could not be created
                connHandler.releaseLock();

                // Re-throw IOException
                throw e;
            }
        }

        public void close() throws IOException {
            // Make sure this method is only executed once, otherwise FTPClient#completePendingCommand() would lock
            if(isClosed)
                return;

            isClosed = true;

            try {
                super.close();

                if(Debug.ON) Debug.trace("complete pending commands");
                connHandler.ftpClient.completePendingCommand();
                if(Debug.ON) Debug.trace("commands completed");

                // Todo: An IOException will be thrown by completePendingCommand if the transfer has not finished before calling close.
                // An 'abort' command should be issued to the server before closing if the transfer is not finished yet.
                // Currently in that case (transfer not finished) the whole connection has to be re-established (bad!).
                // FTPClient#abort() is difficult to use to say the least. This post gives some insight: http://mail-archives.apache.org/mod_mbox/commons-user/200604.mbox/%3c78A73ABD8DB470439179DB682EA990B3025B87DF@mtlex02.NEXXLINK.INT%3e
            }
            catch(IOException e) {
                if(Debug.ON) Debug.trace("exception in completePendingCommands(): "+e);

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

        public int read() throws IOException {
            int read = in.read();

            if(read!=-1)
                offset += 1;

            return read;
        }

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

        public void close() throws IOException {
            in.close();
        }
    }


    private class FTPOutputStream extends FilterOutputStream {

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
                // Checks if the IOException corresponds to a socket error and in that case, closes the connection
                connHandler.checkSocketException(e);

                // Release the lock on the ConnectionHandler if the OutputStream could not be created
                connHandler.releaseLock();

                // Re-throw IOException
                throw e;
            }
        }

        public void close() throws IOException {
            // Make sure this method is only executed once, otherwise FTPClient#completePendingCommand() would lock
            if(isClosed)
                return;

            isClosed = true;

            try {
                super.close();

                if(Debug.ON) Debug.trace("complete pending commands");
                connHandler.ftpClient.completePendingCommand();
                if(Debug.ON) Debug.trace("commands completed");
            }
            catch(IOException e) {
                if(Debug.ON) Debug.trace("exception in completePendingCommands(): "+e);

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

            // Determine if passive or active mode is to used
            String passiveModeProperty = location.getProperty(PASSIVE_MODE_PROPERTY_NAME);
            // Passive mode is enabled by default if property isn't specified
            this.passiveMode = passiveModeProperty==null || !passiveModeProperty.equals("false");

            // Determine encoding to use based on the encoding URL property, UTF-8 if property is not set
            this.encoding = location.getProperty(ENCODING_PROPERTY_NAME);
            if(encoding==null || encoding.equals(""))
                encoding = DEFAULT_ENCODING;

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
            if(Debug.ON) Debug.trace("server reply="+ftpClient.getReplyString());

            // Close connection if the connection dropped prematurely so that isConnected() returns false
            if(replyCode==FTPReply.SERVICE_NOT_AVAILABLE)
                closeConnection();

            // If not, throw an exception using the reply string
            if(!FTPReply.isPositiveCompletion(replyCode)) {
                if(replyCode==FTPReply.CODE_503 || replyCode==FTPReply.NEED_PASSWORD || replyCode==FTPReply.NOT_LOGGED_IN)
                    throw new AuthException(realm, ftpClient.getReplyString());
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
                if(Debug.ON) Debug.trace("socket exception detected, closing connection: "+e);
                closeConnection();
            }
        }


        //////////////////////////////////////
        // ConnectionHandler implementation //
        //////////////////////////////////////

        public void startConnection() throws IOException {
            if(Debug.ON) Debug.trace("connecting to "+getRealm().getHost());

//            this.ftpClient = new CustomFTPClient();
            this.ftpClient = new FTPClient();

            try {
                FileURL realm = getRealm();

                // Override default port (21) if a custom port was specified in the URL
                int port = realm.getPort();
                if(Debug.ON) Debug.trace("custom port="+port);
                if(port!=-1)
                    ftpClient.setDefaultPort(port);

                // Sets the control encoding
                // - most modern FTP servers seem to default to UTF-8, but not all of them do.
                // - commons-ftp defaults to ISO-8859-1 which is not good
                // Note: this has to be done before the connection is established otherwise it won't be taken into account
                if(Debug.ON) Debug.trace("encoding="+encoding);
                ftpClient.setControlEncoding(encoding);

                // Connect to the FTP server
                ftpClient.connect(realm.getHost());

//                // Set a socket timeout: default value is 0 (no timeout)
//                ftpClient.setSoTimeout(CONNECTION_TIMEOUT*1000);
//                if(Debug.ON) Debug.trace("soTimeout="+ftpClient.getSoTimeout());

                // Throw an IOException if server replied with an error
                checkServerReply();

                Credentials credentials = getCredentials();

                // Throw an AuthException if there are no credentials
                if(Debug.ON) Debug.trace("fileURL="+ realm.toString(true)+" credentials="+ credentials);
                if(credentials ==null)
                    throw new AuthException(realm);

                // Login
                ftpClient.login(credentials.getLogin(), credentials.getPassword());
                // Throw an IOException (potentially an AuthException) if the server replied with an error
                checkServerReply();

                // Enables/disables passive mode
                if(Debug.ON) Debug.trace("passiveMode="+passiveMode);
                if(passiveMode)
                    this.ftpClient.enterLocalPassiveMode();
                else
                    this.ftpClient.enterLocalActiveMode();

                // Set file type to 'binary'
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);

                // Issue 'LIST -al' command to list hidden files (instead of LIST -l), only if the corresponding
                // configuration variable has been manually enabled in the preferences.
                // The reason for not doing so by default is that the commons-net library will fail to properly parse
                // directory listings on some servers when 'LIST -al' is used (bug).
                // Note that by default, if 'LIST -l' is used, the decision to list hidden files is left to the
                // FTP server: some servers will choose to show them, some other will not. This behavior usually is a
                // configuration setting of the FTP server.
                ftpClient.setListHiddenFiles(MuConfiguration.getVariable(MuConfiguration.LIST_HIDDEN_FILES, MuConfiguration.DEFAULT_LIST_HIDDEN_FILES));

                if(encoding.equalsIgnoreCase("UTF-8")) {
                    // This command enables UTF8 on the remote server... but only a few FTP servers currently support this command
                    ftpClient.sendCommand("OPTS UTF8 ON");
                }
            }
            catch(IOException e) {
                // Disconnect if something went wrong
                if(ftpClient.isConnected())
                    try { ftpClient.disconnect(); } catch(IOException e2) {}

                // Re-throw exception
                throw e;
            }
        }


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
//            if(Debug.ON) Debug.trace("socket="+socket+" socket.isConnected()"+socket.isConnected()+" socket.isClosed()="+socket.isClosed());
//
//            return socket!=null && socket.isConnected() && !socket.isClosed();
        }


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
}
