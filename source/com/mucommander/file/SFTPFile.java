
package com.mucommander.file;

import com.sshtools.j2ssh.*;
import com.sshtools.j2ssh.sftp.*;
import com.sshtools.j2ssh.authentication.PasswordAuthenticationClient;
import com.sshtools.j2ssh.authentication.AuthenticationProtocolState;
import com.sshtools.j2ssh.io.UnsignedInteger32;
import com.sshtools.j2ssh.transport.IgnoreHostKeyVerification;

import java.io.*;

import java.util.List;
import java.util.Vector;
import java.util.Iterator;


/**
 * SFTPFile represents a file on an SSH/SFTP server.
 */
public class SFTPFile extends AbstractFile {

	private SftpFile file;
	private SshClient sshClient;
	SftpSubsystemClient sftpChannel;

//	private FileURL fileURL;

    protected String absPath;

	/** File separator is '/' for urls */
	private final static String SEPARATOR = "/";

	private AbstractFile parent;
	private boolean parentValSet;
    

	
	static {
		// Disables J2SSH logging on standard output
		System.getProperties().setProperty(org.apache.commons.logging.Log.class.getName(), org.apache.commons.logging.impl.NoOpLog.class.getName());
	}
		

	
	/**
	 * Creates a new instance of SFTPFile and initializes the SSH/SFTP connection to the server.
	 */
	public SFTPFile(FileURL fileURL) throws IOException {
		this(fileURL, true, null, null);
	}

	
	/**
	 * Creates a new instance of SFTPFile and reuses the given SSH/SFTP active connection.
	 */
	private SFTPFile(FileURL fileURL, boolean addAuthInfo, SshClient sshClient, SftpSubsystemClient sftpChannel) throws IOException {
		super(fileURL);

		this.absPath = fileURL.getPath();
		
		if(sshClient==null)
			// Initialize connection
			initConnection(this.fileURL, addAuthInfo);
		else {
			this.sshClient = sshClient;
			this.sftpChannel = sftpChannel;
		}
		
if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("fileURL="+fileURL+" sftpChannel="+this.sftpChannel);

		try {
if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Retrieving file "+fileURL.getPath());
			this.file = new SftpFile(fileURL.getPath(), this.sftpChannel.getAttributes(fileURL.getPath()));
		}
		catch(IOException e) {
if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Cannot retrieve file "+fileURL.getPath()+": "+e);
			// File is null (doesn't exist on the remote server), it's OK
		}
	}

	
	private SFTPFile(FileURL fileURL, SftpFile file, SshClient sshClient, SftpSubsystemClient sftpChannel) throws IOException {
		super(fileURL);
		this.absPath = this.fileURL.getPath();
		this.file = file;
		this.sshClient = sshClient;
		this.sftpChannel = sftpChannel;
//		this.fileExists = true;
	}
	
	
	private void initConnection(FileURL fileURL, boolean addAuthInfo) throws IOException {
if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("connecting to "+fileURL.getHost());
		try {
			// Init SSH client
			this.sshClient = new SshClient();

			// Override default port (22) if a custom port was specified in the URL
			int port = fileURL.getPort();
			if(port==-1)
				port = 22;

			// Connect
			sshClient.connect(fileURL.getHost(), port, new IgnoreHostKeyVerification());

			// Find auth info for this URL
			AuthManager.authenticate(fileURL, addAuthInfo);
			AuthInfo authInfo = AuthInfo.getAuthInfo(fileURL);

			// Throw an AuthException if no auth information
			if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("fileURL="+fileURL.getStringRep(true)+" authInfo="+authInfo);
			if(authInfo==null)
				throw new AuthException(fileURL, "Login and password required");
			
			// Authenticate
			PasswordAuthenticationClient pwd = new PasswordAuthenticationClient();
			pwd.setUsername(authInfo.getLogin());
			pwd.setPassword(authInfo.getPassword());
			int authResult = sshClient.authenticate(pwd);

			// Throw an AuthException if authentication failed
			if(authResult!=AuthenticationProtocolState.COMPLETE)
				throw new AuthException(fileURL, "Login or password rejected");
			
if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("creating sftpclient ");

			// Init SFTP connection
			this.sftpChannel = sshClient.openSftpChannel();

if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("sftpclient = "+this.sftpChannel);
		}
		catch(IOException e) {
if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("ioexception thrown = "+e);
			// Disconnect if something went wrong
			if(sshClient!=null && sshClient.isConnected())
				sshClient.disconnect();

			this.sshClient = null;
			this.sftpChannel = null;

			// Re-throw exception
			throw e;
		}
	}


	private void checkConnection() throws IOException {
if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("checking if connected...");
if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("isConnected= "+sshClient.isConnected());
		// Reconnect if disconnected
		if(sshClient==null || !sshClient.isConnected()) {
			// Connect again
			initConnection(this.fileURL, false);
			return;
		}
	}

	
	/////////////////////////////////////////
	// AbstractFile methods implementation //
	/////////////////////////////////////////

	public String getName() {
		return file==null?fileURL.getFilename():file.getFilename();
	}
	
	public String getAbsolutePath() {
		return fileURL.getStringRep(false);
	}

	public String getSeparator() {
		return SEPARATOR;
	}
	
	public boolean isSymlink() {
		return file==null?false:file.isLink();
	}

	public long getDate() {
		return file==null?0:file.getAttributes().getModifiedTime().longValue()*1000;
	}

	public boolean changeDate(long lastModified) {
		try {
			SftpFile sftpFile = sftpChannel.openFile(absPath, SftpSubsystemClient.OPEN_WRITE);
			FileAttributes attributes = sftpFile.getAttributes();
			attributes.setTimes(attributes.getAccessedTime(), new UnsignedInteger32(lastModified/1000));
			sftpChannel.setAttributes(sftpFile, attributes);
if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("return true");
			return true;
		}
		catch(IOException e) {
if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("return false "+e);
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
if(com.mucommander.Debug.ON) System.out.println("getParent, parentURL="+parentFileURL.getStringRep(true)+" sig="+com.mucommander.Debug.getCallerSignature(1));
				try { this.parent = new SFTPFile(parentFileURL, false, this.sshClient, this.sftpChannel); }
				catch(IOException e) {}
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
		return file==null?false:file.canRead();
	}
	
	public boolean canWrite() {
		return file==null?false:file.canWrite();
	}
	
	public boolean isDirectory() {
		return file==null?false:file.isDirectory();
	}
	
	public boolean equals(Object f) {
		if(!(f instanceof SFTPFile))
			return super.equals(f);		// could be equal to a ZipArchiveFile
		
		return fileURL.equals(((SFTPFile)f).fileURL);
	}
	
	
	public InputStream getInputStream() throws IOException {
		return getInputStream(0);
	}


	public InputStream getInputStream(long skipBytes) throws IOException {
        // Check connection and reconnect if connection timed out
		checkConnection();

if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("skipBytes="+skipBytes, -1);

		SftpFile sftpFile = sftpChannel.openFile(absPath, SftpSubsystemClient.OPEN_READ);
		// Custom made constructor, not part of the official J2SSH API
		return new SftpFileInputStream(sftpFile, skipBytes);
	}


	public OutputStream getOutputStream(boolean append) throws IOException {
        // Check connection and reconnect if connection timed out
		checkConnection();

if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("file="+getAbsolutePath()+" append="+append+" exists="+exists());

		boolean fileExists = exists();
//		FileAttributes attributes = new FileAttributes();
//		attributes.setPermissions("rw-------");
//		SftpFile sftpFile = sftpChannel.openFile(absPath, fileExists?(append?SftpSubsystemClient.OPEN_WRITE|SftpSubsystemClient.OPEN_APPEND:SftpSubsystemClient.OPEN_WRITE):SftpSubsystemClient.OPEN_WRITE|SftpSubsystemClient.OPEN_CREATE, attributes);
		SftpFile sftpFile = sftpChannel.openFile(absPath, 
			fileExists?(append?SftpSubsystemClient.OPEN_WRITE|SftpSubsystemClient.OPEN_APPEND:SftpSubsystemClient.OPEN_WRITE|SftpSubsystemClient.OPEN_TRUNCATE)
			:SftpSubsystemClient.OPEN_WRITE|SftpSubsystemClient.OPEN_CREATE);
		
		// If file was just created, change permissions to 600: read+write for owner only (default is 0)
		if(!fileExists)
			sftpChannel.changePermissions(sftpFile, "rw-------");

		// Custom made constructor, not part of the official J2SSH API
		return new SftpFileOutputStream(sftpFile, append?getSize():0);
	}
	

	public boolean moveTo(AbstractFile destFile) throws IOException {

		// If destination file is an SFTP file located on the same server,
		// have the server rename the file.
		if(destFile.fileURL.getProtocol().equals("sftp") && destFile.fileURL.getHost().equals(this.fileURL.getHost())) {
if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("host equality= "+(destFile.fileURL.getHost().equals(this.fileURL.getHost())));
			
			// Check connection and reconnect if connection timed out
			checkConnection();
			
			try { 
if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("renameTo "+absPath+" -> "+destFile.getURL().getPath());
				sftpChannel.renameFile(absPath, destFile.getURL().getPath());
if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("returns true");
				return true;
			}
			catch(IOException e) {
if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("returns false: "+e);
				return false;
			}
		}
		
		return false;
	}

	
	public void delete() throws IOException {
        // Check connection and reconnect if connection timed out
		checkConnection();

		if(isDirectory())
			sftpChannel.removeDirectory(absPath);
		else
			sftpChannel.removeFile(absPath);
	}


	public AbstractFile[] ls() throws IOException {
if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("starts");

        // Check connection and reconnect if connection timed out
		checkConnection();

		List files = new Vector();

		// Modified J2SSH method
		sftpChannel.listChildren(file, files);

		// File doesn't exists
		int nbFiles = files.size();
if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("nbFiles="+nbFiles);
		if(nbFiles==0)
			return new AbstractFile[] {};
	
		String parentURL = fileURL.getStringRep(false);
		if(!parentURL.endsWith(SEPARATOR))
			parentURL += SEPARATOR;

		Iterator iterator = files.iterator();
        AbstractFile children[] = new AbstractFile[nbFiles];
        AbstractFile child;
		FileURL childURL;
		SftpFile sftpFile;
		String filename;
		int fileCount = 0;
		// Fill AbstractFile array and discard '.' and '..' files
		while(iterator.hasNext()) {
			sftpFile = (SftpFile)iterator.next();
			filename = sftpFile.getFilename();
			// Discard '.' and '..' files
			if(filename.equals(".") || filename.equals(".."))
				continue;
			childURL = new FileURL(parentURL+filename, fileURL);
// if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("sftpFile="+sftpFile);
			child = AbstractFile.wrapArchive(new SFTPFile(childURL, sftpFile, sshClient, sftpChannel));
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
		String dirPath = absPath+(absPath.endsWith(SEPARATOR)?"":SEPARATOR)+name;
		sftpChannel.makeDirectory(dirPath);
		// Set new directory permissions to 700: read+write+execute for owner only
//		sftpChannel.changePermissions(dirPath, 700);
		sftpChannel.changePermissions(dirPath, "rwx------");
	}
}