
package com.mucommander.file;

import org.apache.commons.net.ftp.*;

import java.io.*;

/**
 * FTPFile represents a file on an FTP server.
 */
//public class FTPFile extends AbstractFile implements RemoteFile {
public class FTPFile extends AbstractFile {

	private org.apache.commons.net.ftp.FTPFile file;
	private FTPClient ftpClient;

	/** Sets whether passive mode should be used for data transfers (default is true) */ 
	private boolean passiveMode = true;

//	private FileURL fileURL;

    protected String absPath;

	/** File separator is '/' for urls */
	private final static String SEPARATOR = "/";

	private AbstractFile parent;
	private boolean parentValSet;
    
	private boolean fileExists;
	

	private class FTPInputStream extends FilterInputStream {
		
		private FTPInputStream(InputStream in) {
			super(in);
		}
		
		public void close() throws IOException {
if(com.mucommander.Debug.ON) System.out.println("FTPInputStream.close: closing");
			super.close();
if(com.mucommander.Debug.ON) System.out.println("FTPInputStream.close: closed");

			try {
if(com.mucommander.Debug.ON) System.out.println("FTPInputStream.close: complete pending commands");
				ftpClient.completePendingCommand();
if(com.mucommander.Debug.ON) System.out.println("FTPInputStream.close: commands completed");
			}
			catch(IOException e) {
if(com.mucommander.Debug.ON) System.out.println("FTPInputStream.close: exception in complete pending commands, disconnecting");
				ftpClient.disconnect();
			}
		}
	}
	
	private class FTPOutputStream extends BufferedOutputStream {
		
		private FTPOutputStream(OutputStream out) {
			super(out);
		}
		
		public void close() throws IOException {
			super.close();

			try {
if(com.mucommander.Debug.ON) System.out.println("FTPOutputStream.close: complete pending commands");
				ftpClient.completePendingCommand();
if(com.mucommander.Debug.ON) System.out.println("FTPOutputStream.close: commands completed");
			}
			catch(IOException e) {
if(com.mucommander.Debug.ON) System.out.println("FTPOutputStream.close: exception in complete pending commands, disconnecting");
				ftpClient.disconnect();
			}
		}
	}

	
	public FTPFile(FileURL fileURL) throws IOException {
		this(fileURL, true, null);
	}

	
	/**
	 * Creates a new instance of FTPFile and initializes the FTP connection to the server.
	 */
	private FTPFile(FileURL fileURL, boolean addAuthInfo, FTPClient ftpClient) throws IOException {
		super(fileURL);
		
		this.absPath = this.fileURL.getPath();
				
		if(ftpClient==null)
			// Initialize connection
			initConnection(fileURL, addAuthInfo);
		else
			this.ftpClient = ftpClient;
	
		initFile(fileURL);
	}

	
	private FTPFile(FileURL fileURL, org.apache.commons.net.ftp.FTPFile file, FTPClient ftpClient) {
		super(fileURL);

		this.absPath = fileURL.getPath();
		this.file = file;
		this.ftpClient = ftpClient;
		this.fileExists = true;
	}

	
	private org.apache.commons.net.ftp.FTPFile getFTPFile(FTPClient ftpClient, FileURL fileURL) throws IOException {
		FileURL parentURL = fileURL.getParent();
if(com.mucommander.Debug.ON) System.out.println("getFTPFile "+fileURL+" parent="+parentURL);

		// Parent is null, create '/' file
		if(parentURL==null) {
			return createFTPFile("/", true);
		}
		else {
	        // Check connection and reconnect if connection timed out
			checkConnection();

			org.apache.commons.net.ftp.FTPFile files[] = ftpClient.listFiles(parentURL.getPath());
			// Throw an IOException if server replied with an error
			checkServerReply();

			// File doesn't exists
			if(files==null || files.length==0)
				return null;
		
			// Find file from parent folder
			int nbFiles = files.length;
			String wantedName = fileURL.getFilename();
//System.out.println("getFTPFile wanted="+wantedName+" nbcand="+nbFiles);
			for(int i=0; i<nbFiles; i++) {
//System.out.println("getFTPFile candidate"+i+"="+files[i].getName());
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
	

	private void initConnection(FileURL fileURL, boolean addAuthInfo) throws IOException {
if(com.mucommander.Debug.ON) System.out.print("initConnection: connecting to "+fileURL.getHost());

		this.ftpClient = new FTPClient();
		
		try {
			// Override default port (21) if a custom port was specified in the URL
			int port = fileURL.getPort();
if(com.mucommander.Debug.ON) System.out.println("initConnection: custom port="+port);
			if(port!=-1)
				ftpClient.setDefaultPort(port);

if(com.mucommander.Debug.ON) System.out.println("initConnection: default timeout="+ftpClient.getDefaultTimeout());
		
			// Connect
			ftpClient.connect(fileURL.getHost());
if(com.mucommander.Debug.ON) System.out.println("initConnection: "+ftpClient.getReplyString());

			// Throw an IOException if server replied with an error
			checkServerReply();

			AuthManager.authenticate(fileURL, addAuthInfo);
			AuthInfo authInfo = AuthInfo.getAuthInfo(fileURL);

if(com.mucommander.Debug.ON) System.out.println("initConnection: fileURL="+fileURL.getStringRep(true)+" authInfo="+authInfo);
			if(authInfo==null)
				throw new AuthException(fileURL);
			
			ftpClient.login(authInfo.getLogin(), authInfo.getPassword());
			// Throw an IOException (possibly AuthException) if server replied with an error
			checkServerReply();
			
			// Enables/disables passive mode
			String passiveModeProperty = fileURL.getProperty("passiveMode");
			this.passiveMode = passiveModeProperty==null||!passiveModeProperty.equals("false");
if(com.mucommander.Debug.ON) System.out.println("initConnection: passive mode ="+passiveMode);
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


	private void initFile(FileURL fileURL) throws IOException {
		this.file = getFTPFile(ftpClient, fileURL);
		// If file doesn't exist (could not be resolved), create it
		if(this.file==null) {
			this.file = createFTPFile(fileURL.getFilename(), false);
			this.fileExists = false;
		}
		else {
			this.fileExists = true;
		}
	}

	
	private void checkServerReply() throws IOException {
		// Check that connection went ok
		int replyCode = ftpClient.getReplyCode();
if(com.mucommander.Debug.ON) System.out.println("checkServerReply: "+ftpClient.getReplyString());
		// If not, throw an exception using the reply string
		if(!FTPReply.isPositiveCompletion(replyCode)) {
			if(replyCode==FTPReply.CODE_503 || replyCode==FTPReply.NEED_PASSWORD || replyCode==FTPReply.NOT_LOGGED_IN)
				throw new AuthException(fileURL, ftpClient.getReplyString());
			else
				throw new IOException(ftpClient.getReplyString());
		}
	}

	
	private void checkConnection() throws IOException {
if(com.mucommander.Debug.ON) System.out.println("checkConnection: isConnected= "+ftpClient.isConnected());
		// Reconnect if disconnected
		if(!ftpClient.isConnected()) {
			// Connect again
			initConnection(this.fileURL, false);
			return;
		}
		
		// Send Noop to check connection
		boolean noop = false;
		try {
			noop = ftpClient.sendNoOp();
			checkServerReply();
		}
		catch(IOException e) {
			// Something went wrong
if(com.mucommander.Debug.ON) System.out.println("checkConnection: exception in Noop "+e);
		}

if(com.mucommander.Debug.ON) System.out.println("checkConnection: noop returns "+noop);
		
		if(!noop) {
if(com.mucommander.Debug.ON) System.out.println("checkConnection: isConnected(2)= "+ftpClient.isConnected());
			if(ftpClient.isConnected()) {
				// Let's be a good citizen and disconnect properly
				try { ftpClient.disconnect(); } catch(IOException e) {}
			}
			// Connect again
			initConnection(this.fileURL, false);
		}
	}

	
//	/**
//	 * Enables / disables passive mode mode.
//	 * <p>Default is enabled, so no need to call this method to enable passive mode, this would result in 
//	 * issuing an unecessary command.</p>
//	 */
//	public void setPassiveMode(boolean enabled) throws IOException {
//		this.passiveMode = enabled;
//		
//		if(ftpClient!=null) {
//			if(enabled)
//				this.ftpClient.enterLocalPassiveMode();
//			else
//				this.ftpClient.enterLocalActiveMode();
//		}
//	}

	
	/////////////////////////////////////////
	// AbstractFile methods implementation //
	/////////////////////////////////////////

	public String getName() {
		String name = file.getName();
		
		if(name.endsWith(SEPARATOR))
			return name.substring(0, name.length()-1);
		return name;
	}

	
	public String getAbsolutePath() {
		return fileURL.getStringRep(false);
	}

	public String getSeparator() {
		return SEPARATOR;
	}
	
	public boolean isSymlink() {
		return file.isSymbolicLink();
	}

	public long getDate() {
		return file.getTimestamp().getTime().getTime();
    }
	
	public boolean changeDate(long lastModified) {
		// No way that I know of to change date in Commons-net API
		return false;
	}
	
	public long getSize() {
		return file.getSize();
	}
	
	
	public AbstractFile getParent() {
		if(!parentValSet) {
			FileURL parentFileURL = this.fileURL.getParent();
			if(parentFileURL!=null) {
if(com.mucommander.Debug.ON) System.out.println("getParent, parentURL="+parentFileURL.getStringRep(true)+" sig="+com.mucommander.Debug.getCallerSignature(1));
				parentFileURL.setProperty("passiveMode", ""+passiveMode);
				try { this.parent = new FTPFile(parentFileURL, false, this.ftpClient); }
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
		return this.fileExists;
	}
	
	public boolean canRead() {
		return file.hasPermission(org.apache.commons.net.ftp.FTPFile.USER_ACCESS, org.apache.commons.net.ftp.FTPFile.READ_PERMISSION);
	}
	
	public boolean canWrite() {
		return file.hasPermission(org.apache.commons.net.ftp.FTPFile.USER_ACCESS, org.apache.commons.net.ftp.FTPFile.WRITE_PERMISSION);
	}
	
	public boolean isDirectory() {
		return file.isDirectory();
	}
	
	public boolean equals(Object f) {
		if(!(f instanceof FTPFile))
			return super.equals(f);		// could be equal to a ZipArchiveFile
		
		return fileURL.equals(((FTPFile)f).fileURL);
	}
	
	
	public InputStream getInputStream() throws IOException {
		return getInputStream(0);
	}
	

	public InputStream getInputStream(long skipBytes) throws IOException {
        // Check connection and reconnect if connection timed out
		checkConnection();

		if(skipBytes>0) {
			// Resume transfer at the given offset
			this.ftpClient.setRestartOffset(skipBytes);
		}
		
		InputStream in = ftpClient.retrieveFileStream(absPath);
		if(in==null) {
			if(skipBytes>0) {
				// Reset offset
				this.ftpClient.setRestartOffset(0);
			}
			throw new IOException();
		}
		
		return new FTPInputStream(in);
	}

	
	public OutputStream getOutputStream(boolean append) throws IOException {
        // Check connection and reconnect if connection timed out
		checkConnection();

		OutputStream out;
		if(append)
			out = ftpClient.appendFileStream(absPath);
		else
			out = ftpClient.storeUniqueFileStream(absPath);

		if(out==null)
			throw new IOException();
		
		return new FTPOutputStream(out);
	}

		
	public boolean moveTo(AbstractFile destFile) throws IOException {
		// If destination file is an FTP file located on the same server,
		// have the server rename the file.
		if(destFile.fileURL.getProtocol().equals("ftp") && destFile.fileURL.getHost().equals(this.fileURL.getHost())) {
			// Check connection and reconnect if connection timed out
			checkConnection();
			
			try {
				return ftpClient.rename(absPath, destFile.getURL().getPath());
			}
			catch(IOException e) {
				return false;
			}
		}
		
		return false;
	}

	
	public void delete() throws IOException {
        // Check connection and reconnect if connection timed out
		checkConnection();

		ftpClient.deleteFile(absPath);

		// Throw an IOException if server replied with an error
		checkServerReply();
	}

	public AbstractFile[] ls() throws IOException {
        // Check connection and reconnect if connection timed out
		checkConnection();
		
		org.apache.commons.net.ftp.FTPFile files[];
		try { files = ftpClient.listFiles(absPath); }
		// This exception is not an IOException and needs to be caught and rethrown
		catch(org.apache.commons.net.ftp.parser.ParserInitializationException e) {
if(com.mucommander.Debug.ON) System.out.println("FTPFile.ls(): ParserInitializationException");
			throw new IOException();
		}
	
		// Throw an IOException if server replied with an error
		checkServerReply();
		
        if(files==null)
			return new AbstractFile[] {};
        
        AbstractFile children[] = new AbstractFile[files.length];
        AbstractFile child;
		FileURL childURL;
		String childName;
		int nbFiles = files.length;
		int fileCount = 0;
		String parentURL = fileURL.getStringRep(false);
		if(!parentURL.endsWith(SEPARATOR))
			parentURL += SEPARATOR;

		for(int i=0; i<nbFiles; i++) {
			childName = files[i].getName();
			if(childName.equals(".") || childName.equals(".."))
				continue;
				
			childURL = new FileURL(parentURL+childName);
			childURL.setProperty("passiveMode", ""+passiveMode);
			
			// Discard '.' and '..' files
			if(childName.equals(".") || childName.equals(".."))
				continue;

//			children[nbFiles] = AbstractFile.getAbstractFile(absPath+SEPARATOR+names[i], this);
			child = AbstractFile.wrapArchive(new FTPFile(childURL, files[i], ftpClient));
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
		checkConnection();

		ftpClient.makeDirectory(absPath+(absPath.endsWith(SEPARATOR)?"":SEPARATOR)+name);
		// Throw an IOException if server replied with an error
		checkServerReply();
	}
}