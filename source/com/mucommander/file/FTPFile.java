package com.mucommander.file;

import org.apache.commons.net.*;

import java.io.*;

/**
 * FTPFile represents a file on an FTP server.
 */
public class FTPFile extends AbstractFile implements RemoteFile {

	protected FTPFile file;
    protected String absPath;
	protected boolean isSymlink;

	/** File separator is '/' for urls */
	private final static String SEPARATOR = "/";

	private AbstractFile parent;
	private boolean parentValRetrieved;
	
	/**
	 * Creates a new instance of FTPFile.
	 */
	 public FTPFile(String fileURL) throws IOException {
	 	if(!fileURL.endsWith("/"))
			fileURL += '/';
		
		AuthInfo urlAuthInfo = FTPFile.getAuthInfo(fileURL);
	 	// if the URL specifies a login and password (typed in by the user)
	 	// add it to AuthManager and use it
	 	if (urlAuthInfo!=null) {
	 		AuthManager.put(getPrivateURL(fileURL), urlAuthInfo);
	 	}
	 	// if not, checks if AuthManager has a login/password matching this url
	 	else {
	 		AuthInfo authInfo = AuthManager.get(fileURL);
	 		
	 		if (authInfo!=null) {
	 			// Adds login and password to the URL
	 			fileURL = getPrivateURL(fileURL, authInfo);
	 		}
	 	}
	 	
	 	// Unlike java.io.File, SmbFile throws an SmbException
	 	// when file doesn't exist
	 	try {
	 		file = new SmbFile(fileURL);

	 		this.absPath = file.getCanonicalPath();
			this.isSymlink = !file.getCanonicalPath().equals(this.absPath);
			
	 		// removes the ending separator character (if any)
	 		this.absPath = absPath.endsWith(SEPARATOR)?absPath.substring(0,absPath.length()-1):absPath;
	 		// removes login and password from canonical path
	 		absPath = getPrivateURL(absPath);
	 	}
	 	catch(IOException e) {
	 		// Remove newly created AuthInfo entry from AuthManager
	 		if(urlAuthInfo!=null)
	 			AuthManager.remove(getPrivateURL(fileURL));

            throw e;
		}
	}

	
	protected FTPFile(org.apache.commons.net.ftp.FTPFile file, AbstractFile parent) {
		this.file = file;
		setParent(parent);
	}
	
	
	protected void setParent(AbstractFile parent) {
		this.parent = parent;
		this.parentValRetrieved;
	}
	
	
	
	
	/**
	 * Removes login and password information (if any) from the URL.
	 */
	private static String getPrivateURL(String url) {
		String shortURL = "smb://";

		int pos = url.indexOf('@', 6);
		if(pos==-1)
			return url;
		
		shortURL += url.substring(pos+1, url.length());
		return shortURL;			
	}

	/** 
	 * Adds login and password to the URL.
	 */
	private static String getPrivateURL(String url, AuthInfo authInfo) {
		return getPrivateURL(url, authInfo.getLogin(), authInfo.getPassword());
	}
	
	/** 
	 * Adds login and password to the URL.
	 */
	public static String getPrivateURL(String url, String login, String password) {
		String fullURL = "smb://";
		
		if (!login.trim().equals(""))
			fullURL += login+":"+password+"@";

		if(url.length()>6)
			fullURL += url.substring(6, url.length());

		return fullURL;
	}


	/** 
	 * Returns the login and password information contained in this url, <code>null</code> if
	 * there is none.
	 */
	private static AuthInfo getAuthInfo(String url) {
		String login = "";
		String password = "";

		int pos = url.indexOf('@', 6);
		if (pos==-1) {
			return null;
		}

		int pos2 = url.indexOf(':', 6);
		if (pos2!=-1 && pos2<pos) {
			login = url.substring(6, pos2);
			password = url.substring(pos2+1, pos);
		}
		else {
			login = url.substring(6, pos);
		}

		return new AuthInfo(login, password);
	} 


	public String getName() {
		String name = file.getName();

		if(name.endsWith("/"))
			return name.substring(0, name.length()-1);
		return name;
	}

	/**
	 * Returns a String representation of this AbstractFile which is the name as returned by getName().
	 */
	public String toString() {
		return getName();
	}
	
	public String getAbsolutePath() {
		return absPath;
	}

	public String getSeparator() {
		return SEPARATOR;
	}
	
	public boolean isSymlink() {
		return file.isSymbolicLink();
	}

	public long getDate() {
		return file.getTimeStamp().getTime().getTime();
    }
	
	public long getSize() {
		return file.getSize();
	}
	
	
	public AbstractFile getParent() {
		if(!parentValRetrieved) {
			String parentS = file.getParent();
			// SmbFile.getParent() never returns null
			if(parentS.equals("smb://"))
				this.parent = null;
			
			try {
				this.parent = new FTPFile(parentS);
			}
			catch(IOException e) {
				this.parent = null;
			}
			
			this.parentValRetrieved = true;
			return this.parent;
		}
		
		return this.parent;
    }
	
	public boolean exists() {
		return true;
	}
	
	public boolean canRead() {
		file.hasPermission(USER_ACCESS, READ_PERMISSION);
	}
	
	public boolean canWrite() {
		file.hasPermission(USER_ACCESS, WRITE_PERMISSION);
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
		
		return absPath.equals(((FTPFile)f).absPath);
	}
	
	
	private String getPrivateURL() {
		String fileURL = absPath;

		AuthInfo authInfo = AuthManager.get(fileURL);
		if (authInfo!=null) {
			// Adds login and password to the URL
			fileURL = getPrivateURL(fileURL, authInfo);
		}
	
		return fileURL;
	}

	public InputStream getInputStream() throws IOException {
		ftpClient.retrieveFileStream(absPath);
	}
	
	public OutputStream getOutputStream(boolean append) throws IOException {
		ftpClient.storeUniqueFileStream(absPath);
	}
		
	public boolean moveTo(AbstractFile dest) {
		return false;
	}

	public void delete() throws IOException {
		file.deleteFile(absPath);
	}

	public AbstractFile[] ls() throws IOException {
        FTPFile files[] = ftpClient.listFiles(absPath);
		
        if(names==null)
            throw new IOException();
        
        AbstractFile children[] = new AbstractFile[files.length];
        AbstractFile child;
		int nbFiles = files.length;
		for(int i=0; i<nbFiles; i++) {
			children[nbFiles] = AbstractFile.getAbstractFile(absPath+SEPARATOR+names[i], this);
			child = children[nbFiles];
		
            // It can happen that the SmbFile constructor throws an SmbException (for example
            // when a filename contains an '@' symbol in jCIFS v0.6.7), in which
            // case getAbstractFile() will return null, so we have to handle this case; 
            if(child!=null) {
                nbFiles++;
				child.setParent(this);
			}
        }
		
        return children;
	}

	public void mkdir(String name) throws IOException {
		// Unlike java.io.File.mkdir(), SmbFile does not return a boolean value
		// to indicate if the folder could be created
		new SmbFile(getPrivateURL()+SEPARATOR+name).mkdir();
	}
}