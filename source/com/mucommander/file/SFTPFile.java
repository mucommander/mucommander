
package com.mucommander.file;

import com.mucommander.io.FileTransferException;
import com.mucommander.io.RandomAccessInputStream;
import com.mucommander.auth.AuthException;
import com.mucommander.auth.Credentials;
import com.mucommander.Debug;
import com.mucommander.file.connection.ConnectionHandler;
import com.mucommander.file.connection.ConnectionFull;
import com.mucommander.file.connection.ConnectionPool;
import com.sshtools.j2ssh.SshClient;
import com.sshtools.j2ssh.SftpClient;
import com.sshtools.j2ssh.authentication.*;
import com.sshtools.j2ssh.io.UnsignedInteger32;
import com.sshtools.j2ssh.sftp.*;
import com.sshtools.j2ssh.transport.IgnoreHostKeyVerification;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Iterator;


/**
 * SFTPFile represents a file on an SSH/SFTP server.
 *
 * @author Maxence Bernard
 */
public class SFTPFile extends AbstractFile implements ConnectionFull {

    private SftpFile file;

    protected String absPath;

    private AbstractFile parent;
    private boolean parentValSet;

    private final static String SEPARATOR = DEFAULT_SEPARATOR;

    /** 'Password' SSH authentication method */
    private final static String PASSWORD_AUTH_METHOD = "password";

    /** 'Keyboard interactive' SSH authentication method */ 
    private final static String KEYBOARD_INTERACTIVE_AUTH_METHOD = "keyboard-interactive";

    /** 'Public key' SSH authentication method, not supported at the moment */
    private final static String PUBLIC_KEY_AUTH_METHOD = "publickey";


    static {
        // Disables J2SSH logging on standard output
        System.getProperties().setProperty(org.apache.commons.logging.Log.class.getName(), org.apache.commons.logging.impl.NoOpLog.class.getName());
    }
		

    /**
     * Creates a new instance of SFTPFile and initializes the SSH/SFTP connection to the server.
     */
    public SFTPFile(FileURL fileURL) throws IOException {
        this(fileURL, null);
    }

    
    private SFTPFile(FileURL fileURL, SftpFile sftpFile) throws IOException {
        super(fileURL);

        this.absPath = fileURL.getPath();

        if(sftpFile==null) {
            SFTPConnectionHandler connHandler = null;
            try {
                // Retrieve a ConnectionHandler and lock it
                connHandler = (SFTPConnectionHandler)ConnectionPool.getConnectionHandler(this, fileURL, true);
                // Makes sure the connection is started, if not starts it
                connHandler.checkConnection();

                try {
                    // Retrieve an SftpFile instance, will throw an IOException if the file does not exist
                    file = new SftpFile(absPath, connHandler.sftpChannel.getAttributes(absPath));
                }
                catch(IOException e) {
                    // File doesn't exist on the remote server), SftpFile will be null that's OK
                }
            }
            finally {
                // Release the lock on the ConnectionHandler
                if(connHandler!=null)
                    connHandler.releaseLock();
            }
        }
        else {
            file = sftpFile;
        }
    }


    ///////////////////////////////////
    // ConnectionFull implementation //
    ///////////////////////////////////

    public ConnectionHandler createConnectionHandler(FileURL location) {
        return new SFTPConnectionHandler(location);
    }


    /////////////////////////////////
    // AbstractFile implementation //
    /////////////////////////////////

    public boolean isSymlink() {
        return file!=null && file.isLink();
    }

    public long getDate() {
        return file==null?0:file.getAttributes().getModifiedTime().longValue()*1000;
    }

    public boolean changeDate(long lastModified) {
        SFTPConnectionHandler connHandler = null;
        SftpFile sftpFile = null;
        try {
            // Retrieve a ConnectionHandler and lock it
            connHandler = (SFTPConnectionHandler)ConnectionPool.getConnectionHandler(this, fileURL, true);
            // Makes sure the connection is started, if not starts it
            connHandler.checkConnection();

            // Retrieve an SftpFile instance for write, will throw an IOException if the file does not exist or cannot
            // be written.
            // /!\ SftpFile instance must be closed afterwards to release its file handle
            sftpFile = connHandler.sftpChannel.openFile(absPath, SftpSubsystemClient.OPEN_WRITE);
            FileAttributes attributes = sftpFile.getAttributes();
            attributes.setTimes(attributes.getAccessedTime(), new UnsignedInteger32(lastModified/1000));
            connHandler.sftpChannel.setAttributes(sftpFile, attributes);
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
            if(connHandler!=null)
                connHandler.releaseLock();
        }
    }
	
    public long getSize() {
        return file==null?0:file.getAttributes().getSize().longValue();
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
	
	
    public boolean exists() {
        return file!=null;
    }
	
    public boolean canRead() {
        return file!=null && file.canRead();
    }
	
    public boolean canWrite() {
        return file!=null && file.canWrite();
    }

    public boolean canExecute() {
        return file!=null && (file.getAttributes().getPermissions().intValue()&FileAttributes.S_IXUSR)!=0;
    }

    public boolean canSetPermissions() {
        return true;
    }

    public boolean setReadable(boolean readable) {
        int perms = setPermissionBit(getFilePermissions(), FileAttributes.S_IRUSR, readable);
        return changeFilePermissions(perms);
    }

    public boolean setWritable(boolean writable) {
        int perms = setPermissionBit(getFilePermissions(), FileAttributes.S_IWUSR, writable);
        return changeFilePermissions(perms);
    }

    public boolean setExecutable(boolean executable) {
        int perms = setPermissionBit(getFilePermissions(), FileAttributes.S_IXUSR, executable);
        return changeFilePermissions(perms);
    }

    public boolean setPermissions(int permissions) {
        int perms = setPermissionBit(getFilePermissions(), FileAttributes.S_IRUSR, (permissions&READ_MASK)!=0);
        perms = setPermissionBit(perms, FileAttributes.S_IWUSR, (permissions&WRITE_MASK)!=0);
        perms = setPermissionBit(perms, FileAttributes.S_IXUSR, (permissions&EXECUTE_MASK)!=0);

        return changeFilePermissions(perms);
    }

    /**
     * Returns the SFTP file permissions.
     */
    private int getFilePermissions() {
        if(file==null)
            return 0;

        return file.getAttributes().getPermissions().intValue();
    }

    /**
     * Changes the SFTP file permissions to the given permissions int.
     */
    private boolean changeFilePermissions(int permissions) {

        SFTPConnectionHandler connHandler = null;
        try {
            // Retrieve a ConnectionHandler and lock it
            connHandler = (SFTPConnectionHandler)ConnectionPool.getConnectionHandler(this, fileURL, true);
            // Makes sure the connection is started, if not starts it
            connHandler.checkConnection();

            connHandler.sftpChannel.changePermissions(absPath, permissions);

            return true;
        }
        catch(IOException e) {
            if(Debug.ON) Debug.trace("Failed to change permissions: "+e);
            return false;
        }
        finally {
            // Release the lock on the ConnectionHandler
            if(connHandler!=null)
                connHandler.releaseLock();
        }
    }

    public boolean isDirectory() {
        return file!=null && file.isDirectory();
    }
	
    public InputStream getInputStream() throws IOException {
        return getInputStream(0);
    }

    public RandomAccessInputStream getRandomAccessInputStream() throws IOException {
        // No random access for SFTP files unfortunately
        throw new IOException();
    }

    public OutputStream getOutputStream(boolean append) throws IOException {
        SFTPConnectionHandler connHandler = null;
        try {
            // Retrieve a ConnectionHandler and lock it
            connHandler = (SFTPConnectionHandler)ConnectionPool.getConnectionHandler(this, fileURL, true);
            // Makes sure the connection is started, if not starts it
            connHandler.checkConnection();

            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("using ConnectionHandler="+connHandler);

            boolean fileExists = exists();

            if(fileExists) {
                this.file = connHandler.sftpChannel.openFile(absPath,
                    append?SftpSubsystemClient.OPEN_WRITE|SftpSubsystemClient.OPEN_APPEND
                            :SftpSubsystemClient.OPEN_WRITE|SftpSubsystemClient.OPEN_TRUNCATE);
            }
            else {
                // Set new file permissions to 644 octal (420 dec): "rw-r--r--"
                // Note: by default, permissions for files freshly created is 0 (not readable/writable/executable by anyone)!
                FileAttributes atts = new FileAttributes();
                atts.setPermissions(new UnsignedInteger32(0644));
                this.file = connHandler.sftpChannel.openFile(absPath, SftpSubsystemClient.OPEN_WRITE|SftpSubsystemClient.OPEN_CREATE, atts);
            }

            final SFTPConnectionHandler connHandlerFinal = connHandler;

            // Custom made constructor, not part of the official J2SSH API
            return new SftpFileOutputStream(file, append?getSize():0) {

                public void close() throws IOException {
                    // SftpFileInputStream.close() closes the open SftpFile file handle
                    super.close();

                    // Release the lock on the ConnectionHandler
                    if(connHandlerFinal!=null)
                        connHandlerFinal.releaseLock();
                }
            };
        }
        catch(IOException e) {
            // Release the lock on the ConnectionHandler if the OutputStream could not be created 
            if(connHandler!=null)
                connHandler.releaseLock();

            // Re-throw IOException
            throw e;
        }
    }


    public void delete() throws IOException {
        SFTPConnectionHandler connHandler = null;
        try {
            // Retrieve a ConnectionHandler and lock it
            connHandler = (SFTPConnectionHandler)ConnectionPool.getConnectionHandler(this, fileURL, true);
            // Makes sure the connection is started, if not starts it
            connHandler.checkConnection();

            if(isDirectory())
                connHandler.sftpChannel.removeDirectory(absPath);
            else
                connHandler.sftpChannel.removeFile(absPath);
        }
        finally {
            // Release the lock on the ConnectionHandler if the OutputStream could not be created
            if(connHandler!=null)
                connHandler.releaseLock();
        }
    }


    public AbstractFile[] ls() throws IOException {
        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("starts, absPath="+absPath+" currentThread="+Thread.currentThread());

        SFTPConnectionHandler connHandler = null;
        List files;
        try {
            // Retrieve a ConnectionHandler and lock it
            connHandler = (SFTPConnectionHandler)ConnectionPool.getConnectionHandler(this, fileURL, true);
            // Makes sure the connection is started, if not starts it
            connHandler.checkConnection();

            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("using ConnectionHandler="+connHandler+" currentThread="+Thread.currentThread());

    //        connHandler.sftpChannel.listChildren(file, files);        // Modified J2SSH method to remove the 100 files limitation

            // Use SftpClient.ls() rather than SftpChannel.listChildren() as it seems to be working better
            files = connHandler.sftpClient.ls(absPath);
        }
        finally {
            // Release the lock on the ConnectionHandler
            if(connHandler!=null)
                connHandler.releaseLock();
        }

        int nbFiles = files.size();
        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("nbFiles="+nbFiles);

        // File doesn't exist, return an empty file array
        if(nbFiles==0)
            return new AbstractFile[] {};

        String parentURL = fileURL.getStringRep(true);
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
            childURL = new FileURL(parentURL+filename, fileURL);
            child = FileFactory.wrapArchive(new SFTPFile(childURL, sftpFile));
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

	
    public void mkdir(String name) throws IOException {
        SFTPConnectionHandler connHandler = null;
        try {
            // Retrieve a ConnectionHandler and lock it
            connHandler = (SFTPConnectionHandler)ConnectionPool.getConnectionHandler(this, fileURL, true);
            // Makes sure the connection is started, if not starts it
            connHandler.checkConnection();

            String dirPath = absPath+(absPath.endsWith(SEPARATOR)?"":SEPARATOR)+name;
            connHandler.sftpChannel.makeDirectory(dirPath);
            // Set new directory permissions to 755 octal (493 dec): "rwxr-xr-x"
            // Note: by default, permissions for files freshly created is 0 (not readable/writable/executable by anyone)!
            connHandler.sftpChannel.changePermissions(dirPath, 493);
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
     * uses SFTP and is located on the same host.
     */
    public void moveTo(AbstractFile destFile) throws FileTransferException {

        // Use the default moveTo() implementation if the destination file doesn't use the SFTP protocol
        // or is not on the same host
        if(!destFile.fileURL.getProtocol().equals(FileProtocols.SFTP) || !destFile.fileURL.getHost().equals(this.fileURL.getHost())) {
            super.moveTo(destFile);
            return;
        }

        // If file is an archive file, retrieve the enclosed file, which is likely to be an SFTPFile but not necessarily
        // (may be an ArchiveEntryFile)
        if(destFile instanceof AbstractArchiveFile)
            destFile = ((AbstractArchiveFile)destFile).getProxiedFile();

        // If destination file is not an SFTPFile (for instance an archive entry), server renaming won't work
        // so use default moveTo() implementation instead
        if(!(destFile instanceof SFTPFile)) {
            super.moveTo(destFile);
            return;
        }

        // If destination file is an SFTP file located on the same server, tell the server to rename the file.
        SFTPConnectionHandler connHandler = null;
        try {
            // Retrieve a ConnectionHandler and lock it
            connHandler = (SFTPConnectionHandler)ConnectionPool.getConnectionHandler(this, fileURL, true);
            // Makes sure the connection is started, if not starts it
            connHandler.checkConnection();

//            connHandler.sftpChannel.renameFile(absPath, destFile.getURL().getPath());
            connHandler.sftpClient.rename(absPath, destFile.getURL().getPath());
        }
        catch(IOException e) {
            if(Debug.ON) {
                Debug.trace("Failed to rename file: "+e);
                e.printStackTrace();
            }

            throw new FileTransferException(FileTransferException.UNKNOWN_REASON);    // Report that move failed
        }
        finally {
            // Release the lock on the ConnectionHandler
            if(connHandler!=null)
                connHandler.releaseLock();
        }
    }


    public InputStream getInputStream(long skipBytes) throws IOException {
        SFTPConnectionHandler connHandler = null;
        try {
            // Retrieve a ConnectionHandler and lock it
            connHandler = (SFTPConnectionHandler)ConnectionPool.getConnectionHandler(this, fileURL, true);
            // Makes sure the connection is started, if not starts it
            connHandler.checkConnection();

            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("using ConnectionHandler="+connHandler);

            SftpFile sftpFile = connHandler.sftpChannel.openFile(absPath, SftpSubsystemClient.OPEN_READ);

            final SFTPConnectionHandler connHandlerFinal = connHandler;

            // Custom made constructor, not part of the official J2SSH API
            return new SftpFileInputStream(sftpFile, skipBytes) {

                    public void close() throws IOException {
                        // SftpFileInputStream.close() closes the open SftpFile file handle
                        super.close();

                        // Release the lock on the ConnectionHandler
                        connHandlerFinal.releaseLock();
                }
            };
        }
        catch(IOException e) {
            // Release the lock on the ConnectionHandler if the InputStream could not be created
            if(connHandler!=null)
                connHandler.releaseLock();

            // Re-throw IOException
            throw e;
        }
    }

    public boolean equals(Object f) {
        if(!(f instanceof SFTPFile))
            return super.equals(f);		// could be equal to a ZipArchiveFile

        return fileURL.equals(((SFTPFile)f).fileURL);
    }


    /**
     * Handles connection to SFTP servers.
     */
    private static class SFTPConnectionHandler extends ConnectionHandler {

        private SshClient sshClient;
        private SftpClient sftpClient;
        private SftpSubsystemClient sftpChannel;

        private SFTPConnectionHandler(FileURL location) {
            super(location);

setKeepAlivePeriod(5);
setCloseOnInactivityPeriod(30);
        }


        public synchronized void startConnection() throws IOException {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("starting connection to "+realm);
            try {
                FileURL realm = getRealm();

                // Retrieve credentials for this URL
                final Credentials credentials = realm.getCredentials();

                // Throw an AuthException if no auth information, required for SSH
                if(credentials ==null)
                    throw new AuthException(realm, "Login and password required");  // Todo: localize this entry

                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("creating SshClient");

                // Init SSH client
                sshClient = new SshClient();

                // Override default port (22) if a custom port was specified in the URL
                int port = realm.getPort();
                if(port==-1)
                    port = 22;

                // Connect to server, no host key verification
                sshClient.connect(realm.getHost(), port, new IgnoreHostKeyVerification());

                // Retrieve a list of available authentication methods on the server.
                // Some SSH servers support the 'password' auth method (e.g. OpenSSH on Debian unstable), some don't
                // and only support the 'keyboard-interactive' method. 
                List authMethods = sshClient.getAvailableAuthMethods(credentials.getLogin());
                if(authMethods==null)   // this can happen
                    throw new IOException();

                if(Debug.ON) Debug.trace("getAvailableAuthMethods()="+sshClient.getAvailableAuthMethods(credentials.getLogin()));

                SshAuthenticationClient authClient;

                // Use 'keyboard-interactive' method only if 'password' auth method is not available and
                // 'keyboard-interactive' is supported by the server
                if(!authMethods.contains(PASSWORD_AUTH_METHOD) && authMethods.contains(KEYBOARD_INTERACTIVE_AUTH_METHOD)) {
                    if(Debug.ON) Debug.trace("Using "+KEYBOARD_INTERACTIVE_AUTH_METHOD+" authentication method");

                    KBIAuthenticationClient kbi = new KBIAuthenticationClient();
                    kbi.setUsername(credentials.getLogin());

                    // Fake keyboard password input
                    kbi.setKBIRequestHandler(new KBIRequestHandler() {
                        public void showPrompts(String name, String instruction, KBIPrompt[] prompts) {
                            // Workaround for what seems to be a bug in J2SSH: this method is called twice, first time
                            // with a valid KBIPrompt array, second time with null
                            if(prompts==null) {
                                if(Debug.ON) Debug.trace("prompts is null!");
                                return;
                            }

                            for(int i=0; i<prompts.length; i++) {
                                if(Debug.ON) Debug.trace("prompts["+i+"]="+prompts[i].getPrompt());
                                prompts[i].setResponse(credentials.getPassword());
                            }
                        }
                    });

                    authClient = kbi;
                }
                // Default to 'password' method, even if server didn't report as being supported
                else {
                    if(Debug.ON) Debug.trace("Using "+PASSWORD_AUTH_METHOD+" authentication method");

                    PasswordAuthenticationClient pwd = new PasswordAuthenticationClient();
                    pwd.setUsername(credentials.getLogin());
                    pwd.setPassword(credentials.getPassword());

                    authClient = pwd;
                }

                try {
                    int authResult = sshClient.authenticate(authClient);

                    // Throw an AuthException if authentication failed
                    if(authResult!=AuthenticationProtocolState.COMPLETE)
                        throw new AuthException(realm, "Login or password rejected");   // Todo: localize this entry

                    if(Debug.ON) Debug.trace("authentication complete, authResult="+authResult);
                }
                catch(IOException e) {
                    if(e instanceof AuthException)
                        throw e;

                    if(Debug.ON) {
                        Debug.trace("Caught exception while authenticating: "+e);
                        e.printStackTrace();
                        throw new AuthException(realm, e.getMessage());
                    }
                }
                

                // Init SFTP connections
                sftpClient = sshClient.openSftpClient();
                sftpChannel = sshClient.openSftpChannel();
            }
            catch(IOException e) {
                if(com.mucommander.Debug.ON)
                    com.mucommander.Debug.trace("IOException thrown while starting connection: "+e);

                // Disconnect if something went wrong
                if(sshClient!=null && sshClient.isConnected())
                    sshClient.disconnect();

                sshClient = null;
                sftpClient = null;
                sftpChannel = null;

                // Re-throw exception
                throw e;
            }
        }


        public synchronized boolean isConnected() {
            return sshClient!=null && sshClient.isConnected()
                && sftpClient!=null && !sftpClient.isClosed()
                && sftpChannel!=null && !sftpChannel.isClosed();
        }


        public synchronized void closeConnection() {
            if(sftpClient!=null) {
                try { sftpClient.quit(); }
                catch(IOException e) { if(Debug.ON) Debug.trace("IOException thrown while calling sftpClient.quit()"); }
            }

            if(sftpChannel!=null) {
                try { sftpChannel.close(); }
                catch(IOException e) { if(Debug.ON) Debug.trace("IOException thrown while calling sftpChannel.close ()"); }
            }

            if(sshClient!=null)
                sshClient.disconnect();
        }


        public void keepAlive() {
            // No-op, keep alive is not available and shouldn't really be necessary, SSH servers such as OpenSSH usually
            // maintain connections open without limit.
        }
    }
}
