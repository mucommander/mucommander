package com.mucommander.file;

import jcifs.smb.*;

import java.io.*;

/**
 * SMBFile represents a file shared through the SMB protocol.
 *
 * @author Maxence Bernard
 */
public class SMBFile extends AbstractFile implements RemoteFile {

	/** File separator is '/' for urls */
	private final static String SEPARATOR = "/";

	protected SmbFile file;
    protected String absPath;
	protected boolean isSymlink;

	private AbstractFile parent;
	private boolean parentValSet;

	
	/**
	 * Creates a new instance of SMBFile.
	 */
	public SMBFile(String fileURL) throws IOException {
		if(!fileURL.endsWith("/"))
			fileURL += '/';
		
		AuthInfo urlAuthInfo = SMBFile.getAuthInfo(fileURL);
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
			this.file = new SmbFile(fileURL);
			init(file);
		}
		catch(IOException e) {
			// Remove newly created AuthInfo entry from AuthManager
			if(urlAuthInfo!=null)
				AuthManager.remove(getPrivateURL(fileURL));
		
			throw e;
		}
	}


	/**
	 * Create a new SMBFile by using the given SmbFile instance and parent file.
	 */
	protected SMBFile(SmbFile file, SMBFile parent) {
		this.file = file;
		init(file);
		setParent(parent);
	}


	private void init(SmbFile smbFile) {
		this.absPath = file.getCanonicalPath();
		this.isSymlink = !file.getCanonicalPath().equals(this.absPath);
		
		// removes the ending separator character (if any)
		this.absPath = absPath.endsWith(SEPARATOR)?absPath.substring(0,absPath.length()-1):absPath;
		// removes login and password from canonical path
		absPath = getPrivateURL(absPath);
	}


	protected void setParent(AbstractFile parent) {
		this.parent = parent;	
		this.parentValSet = true;
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
		return this.isSymlink;
	}

	public long getDate() {
        try {
            return file.lastModified();
        }
        catch(SmbException e) {
            return 0;
        }
    }
	
	public long getSize() {
        try {
            return file.length();
        }
        catch(SmbException e) {
            return 0;
        }
	}
	
	
	public AbstractFile getParent() {
		if(!parentValSet) {
			String parentS = file.getParent();
			// SmbFile.getParent() never returns null
			if(parentS.equals("smb://"))
				this.parent = null;
			
			try { this.parent = new SMBFile(parentS); }
			catch(IOException e) { this.parent = null; }
			
			this.parentValSet = true;
			return this.parent;
		}
		
		return this.parent;
    }
	
	public boolean exists() {
		// Unlike java.io.File, SmbFile.exists() can throw an SmbException
		try {
//			return file==null?false:file.exists();
            return file.exists();
		}
		catch(IOException e) {
			return false;
		}
	}
	
	public boolean canRead() {
		// Unlike java.io.File, SmbFile.canRead() can throw an SmbException
		try {
//			return file==null?false:file.canRead();
            return file.canRead();
		}
		catch(SmbException e) {
			return false;
		}
	}
	
	public boolean canWrite() {
		// Unlike java.io.File, SmbFile.canWrite() can throw an SmbException
		try {
//			return file==null?false:file.canWrite();
            return file.canWrite();
		}
		catch(SmbException e) {
			return false;
		}
	}
	
	public boolean isHidden() {
        try {
            return file.isHidden();
        }
        catch(SmbException e) {
            return false;
        }			
	}

	public boolean isDirectory() {
        try {
            return file.isDirectory();
        }
        catch(SmbException e) {
            return false;
        }
	}
	
	
	public boolean equals(Object f) {
		if(!(f instanceof SMBFile))
			return super.equals(f);		// could be equal to a ZipArchiveFile
		
		// SmbFile's equals method is just perfect: compares canonical paths
		// and IP addresses
		return file.equals(((SMBFile)f).file);
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
		return new SmbFileInputStream(getPrivateURL());
	}
	
	public OutputStream getOutputStream(boolean append) throws IOException {
		return new SmbFileOutputStream(getPrivateURL(), append);
	}
		
	public boolean moveTo(AbstractFile dest) throws IOException  {
		if (dest instanceof SMBFile) 
			try{
				file.renameTo(new SmbFile(dest.getAbsolutePath()));
				return true;
			}
			catch(SmbException e) {
				return false;
			}
		return false;
	}

	public void delete() throws IOException {
		try{
			file.delete();
		}
		catch(SmbException e) {
			throw new IOException();
		}
	}

/*
	public AbstractFile[] ls() throws IOException {
        String names[] = file.list();
		
        if(names==null)
            throw new IOException();
        
        AbstractFile children[] = new AbstractFile[names.length];
        AbstractFile child;
        int nbFiles = 0;
		for(int i=0; i<names.length; i++) {
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

        // Rebuild array if one or more files are null
        if(nbFiles!=names.length) {
            AbstractFile newChildren[] = new AbstractFile[nbFiles];
            System.arraycopy(children, 0, newChildren, 0, nbFiles);
            return newChildren;
        }
		
        return children;
	}
*/

	public AbstractFile[] ls() throws IOException {
        SmbFile smbFiles[] = file.listFiles();
		
        if(smbFiles==null)
            throw new IOException();
        
		// Create SMBFile by recycling SmbFile instance and sharing parent instance
		// among children
        AbstractFile children[] = new AbstractFile[smbFiles.length];
		for(int i=0; i<smbFiles.length; i++)
			children[i] = new SMBFile(smbFiles[i], this);

        return children;
	}

	
	public void mkdir(String name) throws IOException {
		// Unlike java.io.File.mkdir(), SmbFile does not return a boolean value
		// to indicate if the folder could be created
		new SmbFile(getPrivateURL()+SEPARATOR+name).mkdir();
	}
}