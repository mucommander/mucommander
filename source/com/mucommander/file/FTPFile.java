
package com.mucommander.file;

import org.apache.commons.net.ftp.*;

import java.io.*;

/**
 * FTPFile represents a file on an FTP server.
 */
public class FTPFile extends AbstractFile implements RemoteFile {

	protected org.apache.commons.net.ftp.FTPFile file;
	protected FTPClient ftpClient;
	protected FileURL fileURL;

    protected String absPath;
	protected boolean isSymlink;

	/** File separator is '/' for urls */
	private final static String SEPARATOR = "/";

	private AbstractFile parent;
	private boolean parentValSet;
    

	private class FTPInputStream extends BufferedInputStream {
		
		private FTPInputStream(InputStream in) {
			super(in);
		}
		
		public void close() throws IOException {
			super.close();
			ftpClient.completePendingCommand();
		}
	}
	
	private class FTPOutputStream extends BufferedOutputStream {
		
		private FTPOutputStream(OutputStream out) {
			super(out);
		}
		
		public void close() throws IOException {
			super.close();
			ftpClient.completePendingCommand();
		}
	}

	
	public FTPFile(String fileURL) throws IOException {
		this(fileURL, true);
	}
	
	
	/**
	 * Creates a new instance of FTPFile and initializes the FTP connection to the server.
	 */
	private FTPFile(String url, boolean addAuthInfo) throws IOException {
//	 	if(!fileURL.endsWith("/"))
//			fileURL += '/';
		
		// At this point . and .. are not yet factored out, so authentication for paths which contain . or ..
		// will not behave properly  -> FileURL should factor out . and .. directly to fix the problem
		this.fileURL = new FileURL(url);
		this.absPath = this.fileURL.getPath();
		
		if(addAuthInfo)
			AuthManager.authenticate(this.fileURL);
		
		// Initialize connection
		initConnection(this.fileURL);

		this.file = getFTPFile(ftpClient, this.fileURL);
	}

	
	private FTPFile(FileURL fileURL, org.apache.commons.net.ftp.FTPFile file, FTPClient ftpClient) {
		this.fileURL = fileURL;
		this.absPath = this.fileURL.getPath();
		this.file = file;
		this.ftpClient = ftpClient;
	}

	
	private static org.apache.commons.net.ftp.FTPFile getFTPFile(FTPClient ftpClient, FileURL fileURL) throws IOException {
		FileURL parentURL = fileURL.getParent();
		if(parentURL==null) {
			org.apache.commons.net.ftp.FTPFile file = new org.apache.commons.net.ftp.FTPFile();
			file.setName("/");
			file.setSize(0);
			file.setTimestamp(java.util.Calendar.getInstance());
			file.setType(org.apache.commons.net.ftp.FTPFile.DIRECTORY_TYPE);
			return file;
		}
		else {
System.out.println("getFTPFile parent="+parentURL.getPath());
			org.apache.commons.net.ftp.FTPFile files[] = ftpClient.listFiles(parentURL.getPath());
			if(files==null || files.length==0)
				throw new IOException();
		
			int nbFiles = files.length;
			String wantedName = fileURL.getFilename();
System.out.println("getFTPFile wanted="+wantedName);
			for(int i=0; i<nbFiles; i++) {
System.out.println("getFTPFile candidate="+files[i].getName());
				if(files[i].getName().equalsIgnoreCase(wantedName))
					return files[i];
			}
			throw new IOException();
		}
	}
	
	

	private void initConnection(FileURL fileURL) throws IOException {
		this.ftpClient = new FTPClient();
		
		try {
			// Connect
			ftpClient.connect(fileURL.getHost());
System.out.print(ftpClient.getReplyString());

			// Check that connection went ok
			int replyCode = ftpClient.getReplyCode();
			// If not, throw an exception using the reply string
			if(!FTPReply.isPositiveCompletion(replyCode))
				throw new IOException(ftpClient.getReplyString());
		
			AuthInfo authInfo = AuthInfo.getAuthInfo(fileURL);
			if(authInfo!=null) {
				try { ftpClient.login(authInfo.getLogin(), authInfo.getPassword()); }
				catch(IOException e) {
					// Throw an AuthException so that we can ask the user to authentify
					throw new AuthException(fileURL, ftpClient.getReplyString());
				}
			}

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


	private void checkConnection() throws IOException {
		// Reconnect if disconnected
		if(!ftpClient.isConnected()) {
			// Let's be a good citizen and disconnect properly
			try { ftpClient.disconnect(); } catch(IOException e) {}
			// Connect again
			initConnection(this.fileURL);
		}
	}
	
	////////////////////////
	// RemoteFile methods //
	////////////////////////

	public String getProtocol() {
		return "FTP";
	}
	
	//////////////////////////
	// AbstractFile methods //
	//////////////////////////

	public String getName() {
		String name = file.getName();

		if(name.endsWith("/"))
			return name.substring(0, name.length()-1);
		return name;
	}

	
	public String getAbsolutePath() {
//		return absPath;
		return fileURL.getURL(false);
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
	
	public long getSize() {
		return file.getSize();
	}
	
	
	public AbstractFile getParent() {
		if(!parentValSet) {
			if(getAbsolutePath(false).lastIndexOf('/')==0)
				this.parent = null;
			else {
				FileURL parentFileURL = this.fileURL.getParent();
				if(parentFileURL!=null) {
					try { this.parent = new FTPFile(parentFileURL, getFTPFile(this.ftpClient, parentFileURL), this.ftpClient); }
					catch(IOException e) { this.parent = null;}
				}
			}
			
			this.parentValSet = true;
			return this.parent;
		}
		
		return this.parent;
    }
	
	
	protected void setParent(AbstractFile parent) {
		this.parent = parent;
		this.parentValSet = true;
	}
	
	
	public boolean exists() {
		return true;
	}
	
	public boolean canRead() {
		return file.hasPermission(org.apache.commons.net.ftp.FTPFile.USER_ACCESS, org.apache.commons.net.ftp.FTPFile.READ_PERMISSION);
	}
	
	public boolean canWrite() {
		return file.hasPermission(org.apache.commons.net.ftp.FTPFile.USER_ACCESS, org.apache.commons.net.ftp.FTPFile.WRITE_PERMISSION);
	}
	
	public boolean isHidden() {
		return false;
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
		InputStream in = ftpClient.retrieveFileStream(absPath);
		if(in==null)
			throw new IOException();
		return new FTPInputStream(in);
	}
	
	public OutputStream getOutputStream(boolean append) throws IOException {
		OutputStream out;
		
		if(append)
			out = ftpClient.appendFileStream(absPath);
		else
			out = ftpClient.storeUniqueFileStream(absPath);

		if(out==null)
			throw new IOException();
		
		return new FTPOutputStream(out);
	}
		
	public boolean moveTo(AbstractFile dest) {
		return false;
	}

	public void delete() throws IOException {
		ftpClient.deleteFile(absPath);
	}

	public AbstractFile[] ls() throws IOException {
        // Check connection and reconnect if connection timed out
		checkConnection();
		
		org.apache.commons.net.ftp.FTPFile files[] = ftpClient.listFiles(absPath);
		
        if(files==null)
			return new AbstractFile[] {};
        
        AbstractFile children[] = new AbstractFile[files.length];
        AbstractFile child;
		FileURL childURL;
		int nbFiles = files.length;
		String parentURL = fileURL.getURL(false);
		if(!parentURL.endsWith("/"))
			parentURL += "/";
		
		for(int i=0; i<nbFiles; i++) {
			childURL = new FileURL(parentURL+files[i].getName());

//			children[nbFiles] = AbstractFile.getAbstractFile(absPath+SEPARATOR+names[i], this);
			children[i] = new FTPFile(childURL, files[i], ftpClient);
			children[i].setParent(this);
        }
		
        return children;
	}

	public void mkdir(String name) throws IOException {
		ftpClient.makeDirectory(getAbsolutePath(true)+name);
	}
}