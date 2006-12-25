
package com.mucommander.file;

import com.mucommander.io.FileTransferException;
import com.mucommander.io.RandomAccessInputStream;
import com.mucommander.auth.AuthException;
import com.mucommander.auth.Credentials;
import com.mucommander.file.connection.ConnectionHandler;
import com.mucommander.file.connection.ConnectionPool;
import com.mucommander.file.connection.ConnectionFull;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;


/**
 * FTPFile represents a file on an FTP server.
 *
 * @author Maxence Bernard
 */
public class FTPFile extends AbstractFile implements ConnectionFull {

    private org.apache.commons.net.ftp.FTPFile file;

    private FTPConnectionHandler connHandler;

    protected String absPath;

    private AbstractFile parent;
    private boolean parentValSet;

    private boolean fileExists;

    private final static String SEPARATOR = DEFAULT_SEPARATOR;

    public final static String PASSIVE_MODE_PROPERTY_NAME = "passiveMode";


    public FTPFile(FileURL fileURL) throws IOException {
        this(fileURL, null);
    }

    public FTPFile(FileURL fileURL, org.apache.commons.net.ftp.FTPFile file) throws IOException {
        super(fileURL);

        this.absPath = fileURL.getPath();

        this.connHandler = (FTPConnectionHandler)ConnectionPool.getConnectionHandler(this, fileURL);
        connHandler.checkConnection();

        if(file==null)
            initFile(fileURL);
        else {
            this.file = file;
            this.fileExists = true;
        }
    }


    private org.apache.commons.net.ftp.FTPFile getFTPFile(FileURL fileURL) throws IOException {
        FileURL parentURL = fileURL.getParent();
        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("fileURL="+fileURL+" parent="+parentURL);

        // Parent is null, create '/' file
        if(parentURL==null) {
            return createFTPFile("/", true);
        }
        else {
            // Check connection and reconnect if connection timed out
            connHandler.checkConnection();

            org.apache.commons.net.ftp.FTPFile files[] = connHandler.ftpClient.listFiles(parentURL.getPath());
            // Throw an IOException if server replied with an error
            connHandler.checkServerReply();

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


    private void initFile(FileURL fileURL) throws IOException {
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



    ///////////////////////////////////
    // ConnectionFull implementation //
    ///////////////////////////////////

    public ConnectionHandler createConnectionHandler(FileURL location) {
        String passiveModeProperty = getURL().getProperty(PASSIVE_MODE_PROPERTY_NAME);
        return new FTPConnectionHandler(location, passiveModeProperty==null||!passiveModeProperty.equals("false"));
    }

    public ConnectionHandler getConnectionHandler() {
        return connHandler;
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
        // No way that I know of to date this with Commons-Net API, maybe there isn't even an FTP command to change a file's date 
        // Note: FTPFile.setTimeStamp only changes the instance's date, but doesn't change it on the server-side.
        return false;
    }

    public long getSize() {
        return file.getSize();
    }


    public AbstractFile getParent() {
        if(!parentValSet) {
            FileURL parentFileURL = this.fileURL.getParent();
            if(parentFileURL!=null) {
                parentFileURL.setProperty(PASSIVE_MODE_PROPERTY_NAME, ""+connHandler.passiveMode);
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
        // Spawn a new FTP connection
        FTPConnectionHandler connHandler = (FTPConnectionHandler)createConnectionHandler(getURL());
        connHandler.checkConnection();

        OutputStream out;
        if(append)
            out = connHandler.ftpClient.appendFileStream(absPath);
        else
            out = connHandler.ftpClient.storeUniqueFileStream(absPath);

        if(out==null)
            throw new IOException();

        return new FTPOutputStream(out, connHandler);
    }


    public void delete() throws IOException {
        connHandler.checkConnection();

        connHandler.ftpClient.deleteFile(absPath);

        // Throw an IOException if server replied with an error
        connHandler.checkServerReply();
    }


    public AbstractFile[] ls() throws IOException {
        connHandler.checkConnection();

        org.apache.commons.net.ftp.FTPFile files[];
        try { files = connHandler.ftpClient.listFiles(absPath); }
        // This exception is not an IOException and needs to be caught and rethrown
        catch(org.apache.commons.net.ftp.parser.ParserInitializationException e) {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("ParserInitializationException caught");
            throw new IOException();
        }

        // Throw an IOException if server replied with an error
        connHandler.checkServerReply();

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
            childURL.setProperty(PASSIVE_MODE_PROPERTY_NAME, ""+connHandler.passiveMode);

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
        // Check connection and reconnect if connection timed out
        connHandler.checkConnection();

        connHandler.ftpClient.makeDirectory(absPath+(absPath.endsWith(SEPARATOR)?"":SEPARATOR)+name);
        // Throw an IOException if server replied with an error
        connHandler.checkServerReply();
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
        // If destination file is an FTP file located on the same server, tells the server to rename the file.

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

        try {
            // Check connection and reconnect if connection timed out
            connHandler.checkConnection();

            if(!connHandler.ftpClient.rename(absPath, destFile.getURL().getPath()))
                throw new IOException();
        }
        catch(IOException e) {
            throw new FileTransferException(FileTransferException.UNKNOWN_REASON);    // Report that move failed
        }
    }


    public InputStream getInputStream(long skipBytes) throws IOException {
        // Spawn a new FTP connection
        FTPConnectionHandler connHandler = (FTPConnectionHandler)createConnectionHandler(getURL());
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


    public boolean equals(Object f) {
        if(!(f instanceof FTPFile))
            return super.equals(f);		// could be equal to a ZipArchiveFile

        return fileURL.equals(((FTPFile)f).fileURL);
    }


    private static class FTPInputStream extends FilterInputStream {

        private FTPConnectionHandler connHandler;
        private boolean closed;

        private FTPInputStream(InputStream in, FTPConnectionHandler connHandler) {
            super(in);
            this.connHandler = connHandler;
        }

        public void close() throws IOException {
            if(closed)
                return;

            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("closing", -1);
            super.close();
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("closed");

            try {
                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("complete pending commands");
                connHandler.ftpClient.completePendingCommand();
                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("commands completed");
            }
            catch(IOException e) {
                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("exception in complete pending commands, disconnecting");
            }
            finally {
                connHandler.closeConnection();
                closed = true;
            }
        }
    }

    private static class FTPOutputStream extends BufferedOutputStream {

        private FTPConnectionHandler connHandler;
        private boolean closed;

        private FTPOutputStream(OutputStream out, FTPConnectionHandler connHandler) {
            super(out);
            this.connHandler = connHandler;
        }

        public void close() throws IOException {
            if(closed)
                return;

            super.close();

            try {
                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("complete pending commands");
                connHandler.ftpClient.completePendingCommand();
                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("commands completed");
            }
            catch(IOException e) {
                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("exception in complete pending commands, disconnecting");
            }
            finally {
                connHandler.closeConnection();
                closed = true;
            }
        }
    }


    private static class FTPConnectionHandler extends ConnectionHandler {

        private FTPClient ftpClient;

        /** Sets whether passive mode should be used for data transfers (default is true) */
        private boolean passiveMode;

        private FTPConnectionHandler(FileURL location, boolean passiveMode) {
            super(location);
            this.passiveMode = passiveMode;
        }


        private void checkServerReply() throws IOException {
            // Check that connection went ok
            int replyCode = ftpClient.getReplyCode();
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("reply="+ftpClient.getReplyString());
            // If not, throw an exception using the reply string
            if(!FTPReply.isPositiveCompletion(replyCode)) {
                if(replyCode==FTPReply.CODE_503 || replyCode==FTPReply.NEED_PASSWORD || replyCode==FTPReply.NOT_LOGGED_IN)
                    throw new AuthException(realm, ftpClient.getReplyString());
                else
                    throw new IOException(ftpClient.getReplyString());
            }
        }


        public void startConnection() throws IOException {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("connecting to "+getRealm().getHost());

            this.ftpClient = new FTPClient();

            try {
                FileURL realm = getRealm();

                // Override default port (21) if a custom port was specified in the URL
                int port = realm.getPort();
                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("custom port="+port);
                if(port!=-1)
                    ftpClient.setDefaultPort(port);

                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("default timeout="+ftpClient.getDefaultTimeout());

                // Connect
                ftpClient.connect(realm.getHost());
                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace(ftpClient.getReplyString());

                // Throw an IOException if server replied with an error
                checkServerReply();

                Credentials credentials = realm.getCredentials();

                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("fileURL="+ realm.getStringRep(true)+" credentials="+ credentials);
                if(credentials ==null)
                    throw new AuthException(realm);

                ftpClient.login(credentials.getLogin(), credentials.getPassword());
                // Throw an IOException (possibly AuthException) if server replied with an error
                checkServerReply();

                // Enables/disables passive mode
                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("passive mode ="+passiveMode);
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
            return ftpClient!=null && ftpClient.isConnected();

//            if(ftpClient==null || !ftpClient.isConnected())
//                return false;
//
//            // Send NoOp to check connection
//            boolean noopSuccess = false;
//            try {
//                noopSuccess = ftpClient.sendNoOp();
//                checkServerReply();
//            }
//            catch(IOException e) {
//                // Something went wrong
//                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("exception in Noop "+e);
//            }
//
//            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("noop returned "+ noopSuccess);
//
//            return noopSuccess;
        }


        public void closeConnection() {
            if(isConnected()) {
                try {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
                catch(IOException e) {}
            }
        }
    }

}
