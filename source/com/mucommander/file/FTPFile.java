
package com.mucommander.file;

import com.mucommander.io.FileTransferException;
import com.mucommander.io.RandomAccessInputStream;
import com.mucommander.auth.AuthException;
import com.mucommander.auth.Credentials;
import com.mucommander.file.connection.ConnectionHandler;
import com.mucommander.file.connection.ConnectionPool;
import com.mucommander.file.connection.ConnectionFull;
import com.mucommander.Debug;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPConnectionClosedException;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.net.SocketException;
import java.net.SocketTimeoutException;


/**
 * FTPFile represents a file located on an FTP server.
 *
 * @author Maxence Bernard
 */
public class FTPFile extends AbstractFile implements ConnectionFull {

    private org.apache.commons.net.ftp.FTPFile file;

    protected String absPath;

    private AbstractFile parent;
    private boolean parentValSet;

    private boolean fileExists;

    private final static String SEPARATOR = DEFAULT_SEPARATOR;

    /** Name of the FTP passive mode property */
    public final static String PASSIVE_MODE_PROPERTY_NAME = "passiveMode";

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


    private boolean isPassiveModeEnabled() {
        String passiveModeProperty = getURL().getProperty(PASSIVE_MODE_PROPERTY_NAME);
        // Passive mode is enabled by default if property isn't specified
        return passiveModeProperty==null || !passiveModeProperty.equals("false");
    }


    ///////////////////////////////////
    // ConnectionFull implementation //
    ///////////////////////////////////

    public ConnectionHandler createConnectionHandler(FileURL location) {
        return new FTPConnectionHandler(location, isPassiveModeEnabled());
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
        // Changes the date using the SITE UTIME FTP command. This command seems to be supported by modern FTP servers
        // such as ProFTPd or PureFTP Server, but may as well not be supported by the remote FTP server as it is not
        // part of the basic command set.

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
                parentFileURL.setProperty(PASSIVE_MODE_PROPERTY_NAME, ""+isPassiveModeEnabled());
                parentFileURL.setCredentials(fileURL.getCredentials());
                try {
                    this.parent = new FTPFile(parentFileURL);
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

    public boolean canRead() {
        return file.hasPermission(org.apache.commons.net.ftp.FTPFile.USER_ACCESS, org.apache.commons.net.ftp.FTPFile.READ_PERMISSION);
    }

    public boolean canWrite() {
        return file.hasPermission(org.apache.commons.net.ftp.FTPFile.USER_ACCESS, org.apache.commons.net.ftp.FTPFile.WRITE_PERMISSION);
    }

    public boolean canExecute() {
        return file.hasPermission(org.apache.commons.net.ftp.FTPFile.USER_ACCESS, org.apache.commons.net.ftp.FTPFile.EXECUTE_PERMISSION);
    }

    public boolean setReadable(boolean readable) {
        return false;
    }

    public boolean setWritable(boolean writable) {
        return false;
    }

    public boolean setExecutable(boolean executable) {
        return false;
    }

    public boolean canSetPermissions() {
        // Unfortuntely there is no way to change file permissions in commons-net FTP library
        return false;
    }

    public boolean isDirectory() {
        return file.isDirectory();
    }

    public InputStream getInputStream() throws IOException {
        return getInputStream(0);
    }

    public RandomAccessInputStream getRandomAccessInputStream() throws IOException {
        // No random access for FTP files unfortunately
        throw new IOException();
    }

    public OutputStream getOutputStream(boolean append) throws IOException {
        FTPConnectionHandler connHandler = null;
        try {
            // Retrieve a ConnectionHandler and lock it
            connHandler = (FTPConnectionHandler)ConnectionPool.getConnectionHandler(this, fileURL, true);
            // Makes sure the connection is started, if not starts it
            connHandler.checkConnection();

            OutputStream out;
            if(append)
                out = connHandler.ftpClient.appendFileStream(absPath);
            else
                out = connHandler.ftpClient.storeFileStream(absPath);   // Note: do NOT use storeUniqueFileStream which appends .1 if the file already exists and fails with proftpd

            if(out==null)
                throw new IOException();

            return new FTPOutputStream(out, connHandler);
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

            try { files = connHandler.ftpClient.listFiles(absPath); }
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
        String parentURL = fileURL.getStringRep(true);
        if(!parentURL.endsWith(SEPARATOR))
            parentURL += SEPARATOR;

        for(int i=0; i<nbFiles; i++) {
            childName = files[i].getName();
            if(childName.equals(".") || childName.equals(".."))
                continue;

            childURL = new FileURL(parentURL+childName, fileURL);
            childURL.setProperty(PASSIVE_MODE_PROPERTY_NAME, ""+isPassiveModeEnabled());

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


    public void mkdir(String name) throws IOException {
        FTPConnectionHandler connHandler = null;
        try {
            // Retrieve a ConnectionHandler and lock it
            connHandler = (FTPConnectionHandler)ConnectionPool.getConnectionHandler(this, fileURL, true);
            // Makes sure the connection is started, if not starts it
            connHandler.checkConnection();

            connHandler.ftpClient.makeDirectory(absPath+(absPath.endsWith(SEPARATOR)?"":SEPARATOR)+name);
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


    public long getFreeSpace() {
        // No way to retrieve this information with J2SSH, return -1 (not available)
        return -1;
    }

    public long getTotalSpace() {
        // No way to retrieve this information with J2SSH, return -1 (not available)
        return -1;
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    /**
     * Overrides {@link AbstractFile#moveTo(AbstractFile)} to support server-to-server move if the destination file
     * uses FTP and is located on the same host.
     */
    public void moveTo(AbstractFile destFile) throws FileTransferException {
        // If destination file is an FTP file located on the same server, tell the server to rename the file.

        // Use the default moveTo() implementation if the destination file doesn't use FTP
        // or is not on the same host
        if(!destFile.fileURL.getProtocol().equals(FileProtocols.FTP) || !destFile.fileURL.getHost().equals(this.fileURL.getHost())) {
            super.moveTo(destFile);
            return;
        }

        // If file is an archive file, retrieve the enclosed file, which is likely to be an FTPFile but not necessarily
        // (may be an ArchiveEntryFile)
        if(destFile instanceof AbstractArchiveFile)
            destFile = ((AbstractArchiveFile)destFile).getProxiedFile();

        // If destination file is not an FTPFile (for instance an archive entry), server renaming won't work
        // so use default moveTo() implementation instead
        if(!(destFile instanceof FTPFile)) {
            super.moveTo(destFile);
            return;
        }

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
            connHandler.checkSocketException(e);

            throw new FileTransferException(FileTransferException.UNKNOWN_REASON);    // Report that move failed
        }
        finally {
            // Release the lock on the ConnectionHandler
            if(connHandler!=null)
                connHandler.releaseLock();
        }
    }


    public InputStream getInputStream(long skipBytes) throws IOException {
        FTPConnectionHandler connHandler = null;
        try {
            // Retrieve a ConnectionHandler and lock it
            connHandler = (FTPConnectionHandler)ConnectionPool.getConnectionHandler(this, fileURL, true);
            // Makes sure the connection is started, if not starts it
            connHandler.checkConnection();

            if(skipBytes>0) {
                // Resume transfer at the given offset
                connHandler.ftpClient.setRestartOffset(skipBytes);
            }

            InputStream in = connHandler.ftpClient.retrieveFileStream(absPath);
            if(in==null) {
                if(skipBytes>0) {
                    // Reset offset
                    connHandler.ftpClient.setRestartOffset(0);
                }
                throw new IOException();
            }

            return new FTPInputStream(in, connHandler);
        }
        catch(IOException e) {
            // Checks if the IOException corresponds to a socket error and in that case, closes the connection
            connHandler.checkSocketException(e);

            // Release the lock on the ConnectionHandler if the InputStream could not be created
            if(connHandler!=null)
                connHandler.releaseLock();

            // Re-throw IOException
            throw e;
        }
    }


    public boolean equals(Object f) {
        if(!(f instanceof FTPFile))
            return super.equals(f);		// could be equal to a ZipArchiveFile

        return fileURL.equals(((FTPFile)f).fileURL);
    }


    private static class FTPInputStream extends FilterInputStream {

        private FTPConnectionHandler connHandler;
        private boolean isClosed;

        private FTPInputStream(InputStream in, FTPConnectionHandler connHandler) {
            super(in);
            this.connHandler = connHandler;
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

    private static class FTPOutputStream extends BufferedOutputStream {

        private FTPConnectionHandler connHandler;
        private boolean isClosed;

        private FTPOutputStream(OutputStream out, FTPConnectionHandler connHandler) {
            super(out);
            this.connHandler = connHandler;
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
     * Handles connection to FTP servers.
     */
    private static class FTPConnectionHandler extends ConnectionHandler {

        private FTPClient ftpClient;
//        private CustomFTPClient ftpClient;

        /** Sets whether passive mode should be used for data transfers (default is true) */
        private boolean passiveMode;

        /** False if SITE UTIME command is not supported by the remote server (once tried and failed) */
        private boolean utimeCommandSupported = true;
        
        /** Controls how ofter should keepAlive() be called by ConnectionPool */
        private final static long KEEP_ALIVE_PERIOD = 60;

        /** Socket timeout in seconds */
        private final static int SO_TIMEOUT = 30;


//        private class CustomFTPClient extends FTPClient {
//
//            private Socket getSocket() {
//                return _socket_;
//            }
//        }


        private FTPConnectionHandler(FileURL location, boolean passiveMode) {
            super(location);
            this.passiveMode = passiveMode;

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

                // Connect
                ftpClient.connect(realm.getHost());

                // Set a socket timeout: default value is 0 (no timeout)
                ftpClient.setSoTimeout(SO_TIMEOUT*1000);

                if(Debug.ON) Debug.trace("soTimeout="+ftpClient.getSoTimeout());

                // Throw an IOException if server replied with an error
                checkServerReply();

                Credentials credentials = getCredentials();

                if(Debug.ON) Debug.trace("fileURL="+ realm.getStringRep(true)+" credentials="+ credentials);
                if(credentials ==null)
                    throw new AuthException(realm);

                ftpClient.login(credentials.getLogin(), credentials.getPassword());
                // Throw an IOException (potentially an AuthException) if the server replied with an error
                checkServerReply();

                // Enables/disables passive mode
                if(Debug.ON) Debug.trace("passive mode ="+passiveMode);
                if(passiveMode)
                    this.ftpClient.enterLocalPassiveMode();
                else
                    this.ftpClient.enterLocalActiveMode();

                // Set file type to 'binary'
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
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
