
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
import com.sshtools.j2ssh.authentication.*;
import com.sshtools.j2ssh.io.UnsignedInteger32;
import com.sshtools.j2ssh.sftp.*;
import com.sshtools.j2ssh.transport.IgnoreHostKeyVerification;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.util.Vector;
import java.util.List;


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

    private SFTPConnectionHandler connHandler;
    
    private final static String SEPARATOR = DEFAULT_SEPARATOR;

    private final static String PASSWORD_AUTH_METHOD = "password";
    private final static String KEYBOARD_INTERACTIVE_AUTH_METHOD = "keyboard-interactive";
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
        this.connHandler = (SFTPConnectionHandler)ConnectionPool.getConnectionHandler(this, fileURL);
        connHandler.checkConnection();

        if(sftpFile==null) {
            try {
                file = new SftpFile(fileURL.getPath(), connHandler.sftpClient.getAttributes(fileURL.getPath()));
            }
            catch(IOException e) {
                // File doesn't exist on the remote server), SftpFile will be null that's OK
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

    public ConnectionHandler getConnectionHandler() {
        return connHandler;
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
        try {
            connHandler.checkConnection();

            SftpFile sftpFile = connHandler.sftpClient.openFile(absPath, SftpSubsystemClient.OPEN_WRITE);
            FileAttributes attributes = sftpFile.getAttributes();
            attributes.setTimes(attributes.getAccessedTime(), new UnsignedInteger32(lastModified/1000));
            connHandler.sftpClient.setAttributes(sftpFile, attributes);
            return true;
        }
        catch(IOException e) {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Failed to change date: "+e);
            return false;
        }
    }
	
    public long getSize() {
        return file==null?0:file.getAttributes().getSize().longValue();
    }
	
	
    public AbstractFile getParent() {
        if(!parentValSet) {
            FileURL parentFileURL = this.fileURL.getParent();
            if(parentFileURL!=null) {
                try { this.parent = new SFTPFile(parentFileURL); }
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
        try {
            connHandler.checkConnection();

            connHandler.sftpClient.changePermissions(absPath, permissions);
            return true;
        }
        catch(IOException e) {
            if(Debug.ON) Debug.trace("Exception thrown: "+e);
            return false;
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
//SFTPConnectionHandler connHandler = (SFTPConnectionHandler)createConnectionHandler(getURL());
        connHandler.checkConnection();

        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("file="+getAbsolutePath()+" append="+append+" exists="+exists());

        boolean fileExists = exists();
//        SftpFile sftpFile = sftpSubsystem.openFile(absPath,
//                                                 fileExists?(append?SftpSubsystemClient.OPEN_WRITE|SftpSubsystemClient.OPEN_APPEND:SftpSubsystemClient.OPEN_WRITE|SftpSubsystemClient.OPEN_TRUNCATE)
//                                                 :SftpSubsystemClient.OPEN_WRITE|SftpSubsystemClient.OPEN_CREATE);

        this.file = connHandler.sftpClient.openFile(absPath,
             fileExists?(append?SftpSubsystemClient.OPEN_WRITE|SftpSubsystemClient.OPEN_APPEND:SftpSubsystemClient.OPEN_WRITE|SftpSubsystemClient.OPEN_TRUNCATE)
             :SftpSubsystemClient.OPEN_WRITE|SftpSubsystemClient.OPEN_CREATE);

        // If file was just created, change permissions to 644 octal (420 dec): "rw-r--r--"
        // Note: by default, permissions for created files is 0 !
        if(!fileExists)
            connHandler.sftpClient.changePermissions(file, 420);

        // Custom made constructor, not part of the official J2SSH API
        return new SftpFileOutputStream(file, append?getSize():0);
//return new SFTPOutputStream(new SftpFileOutputStream(file, append?getSize():0), connHandler);
    }
	

    public void delete() throws IOException {
        connHandler.checkConnection();

        if(isDirectory())
            connHandler.sftpClient.removeDirectory(absPath);
        else
            connHandler.sftpClient.removeFile(absPath);
    }


    public AbstractFile[] ls() throws IOException {
        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("starts");

        connHandler.checkConnection();

        Vector files = new Vector();

        // Modified J2SSH method to remove the 100 files limitation
        connHandler.sftpClient.listChildren(file, files);

        // File doesn't exists
        int nbFiles = files.size();
        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("nbFiles="+nbFiles);
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
        for(int i=0; i<nbFiles; i++) {
            sftpFile = (SftpFile)files.elementAt(i);
            filename = sftpFile.getFilename();
            // Discard '.' and '..' files
            if(filename.equals(".") || filename.equals(".."))
                continue;
            childURL = new FileURL(parentURL+filename, fileURL);
            child = FileFactory.wrapArchive(new SFTPFile(childURL, sftpFile));
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
        connHandler.checkConnection();

        String dirPath = absPath+(absPath.endsWith(SEPARATOR)?"":SEPARATOR)+name;
        connHandler.sftpClient.makeDirectory(dirPath);
        // Set new directory permissions to 755 octal (493 dec): "rwxr-xr-x"
        // Note: by default, permissions for created files is 0 !
        connHandler.sftpClient.changePermissions(dirPath, 493);
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
        // If destination file is an SFTP file located on the same server, tells the server to rename the file.

        // Use the default moveTo() implementation if the destination file doesn't use FTP
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

        try {
            connHandler.checkConnection();

            connHandler.sftpClient.renameFile(absPath, destFile.getURL().getPath());
        }
        catch(IOException e) {
            throw new FileTransferException(FileTransferException.UNKNOWN_REASON);    // Report that move failed
        }
    }


    public InputStream getInputStream(long skipBytes) throws IOException {
        connHandler.checkConnection();

        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("skipBytes="+skipBytes, -1);

        SftpFile sftpFile = connHandler.sftpClient.openFile(absPath, SftpSubsystemClient.OPEN_READ);
        // Custom made constructor, not part of the official J2SSH API
        return new SftpFileInputStream(sftpFile, skipBytes);
    }


    public boolean equals(Object f) {
        if(!(f instanceof SFTPFile))
            return super.equals(f);		// could be equal to a ZipArchiveFile

        return fileURL.equals(((SFTPFile)f).fileURL);
    }


//    private static class SFTPOutputStream extends BufferedOutputStream {
//
//        SFTPConnectionHandler connHandler;
//
//        private SFTPOutputStream(OutputStream out, SFTPConnectionHandler connHandler) {
//            super(out);
//            this.connHandler = connHandler;
//        }
//
//
//        public void close() throws IOException {
//            super.close();
//
//            connHandler.closeConnection();
//        }
//    }


    private static class SFTPConnectionHandler extends ConnectionHandler {

        private SshClient sshClient;
        private SftpSubsystemClient sftpClient;

        private SFTPConnectionHandler(FileURL location) {
            super(location);
        }


        public void startConnection() throws IOException {
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
                if(Debug.ON) Debug.trace("getAvailableAuthMethods()="+sshClient.getAvailableAuthMethods(credentials.getLogin()));

                // Authenticate using the 'password' method if available, if not using 'keyboard-interactive'
                SshAuthenticationClient authClient;
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
                else {
                    if(Debug.ON) Debug.trace("Using "+PASSWORD_AUTH_METHOD+" authentication method");

                    PasswordAuthenticationClient pwd = new PasswordAuthenticationClient();
                    pwd.setUsername(credentials.getLogin());
                    pwd.setPassword(credentials.getPassword());

                    authClient = pwd;
                }

                try {
                    if(Debug.ON) Debug.trace("authenticating sshClient, authClient="+authClient);
                    int authResult = sshClient.authenticate(authClient);
                    if(Debug.ON) Debug.trace("authentication complete, authResult="+authResult);

                    // Throw an AuthException if authentication failed
                    if(authResult!=AuthenticationProtocolState.COMPLETE)
                        throw new AuthException(realm, "Login or password rejected");
                }
                catch(IOException e) {
                    if(e instanceof AuthException)
                        throw e;

                    if(Debug.ON) {
                        Debug.trace("Caught exception in SshClient.authenticate: "+e);
                        e.printStackTrace();
                        throw new AuthException(realm, e.getMessage());
                    }
                }
                

                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("creating SftpSubsystemClient");

                // Init SFTP connection
                sftpClient = sshClient.openSftpChannel();
            }
            catch(IOException e) {
                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("IOException thrown: "+e);

                // Disconnect if something went wrong
                if(sshClient!=null && sshClient.isConnected())
                    sshClient.disconnect();

                sshClient = null;
                sftpClient = null;

                // Re-throw exception
                throw e;
            }
        }

        public boolean isConnected() {
if(Debug.ON && sshClient!=null) Debug.trace("isClosed="+sftpClient.isClosed()+"isConnected="+sshClient.isConnected()+" hasError="+sshClient.getConnectionState().hasError()+" getLastError="+sshClient.getConnectionState().getLastError()+" isValidState()="+sshClient.getConnectionState().isValidState(sshClient.getConnectionState().getValue()));
            return sshClient!=null && sshClient.isConnected();
        }

        public void closeConnection() {
            if(sshClient!=null)
                sshClient.disconnect();
        }
    }
}
