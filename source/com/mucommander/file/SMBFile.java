package com.mucommander.file;

import jcifs.smb.*;

import java.io.*;
import java.net.MalformedURLException;

/**
 * SMBFile represents a file shared through the SMB protocol.
 *
 * @author Maxence Bernard
 */
public class SMBFile extends AbstractFile implements RemoteFile {

	/** File separator is '/' for urls */
	private final static String SEPARATOR = "/";

	private SmbFile file;
	private String publicURL;
	private String privateURL;
//	private boolean isSymlink;

	private AbstractFile parent;
	private boolean parentValSet;


	public SMBFile(String url) throws IOException {	
		this(url, true);
	}
	

	private SMBFile(String url, boolean addAuthInfo) throws IOException {	
//System.out.println("SMBFile() "+url);
		// all SMB URLs that represent  workgroups, servers, shares, or directories require a trailing slash '/'. 
		if(!url.endsWith("/"))
			url += '/';

		// At this point . and .. are not yet factored out, so authentication for paths which contain . or ..
		// will not behave properly  -> FileURL should factor out . and .. directly to fix the problem
		FileURL fileURL = new FileURL(url);
		AuthInfo prevAuthInfo = null;
		
		if(addAuthInfo)
			prevAuthInfo = AuthManager.authenticate(fileURL);
		
		// Unlike java.io.File, SmbFile throws an SmbException
		// when file doesn't exist
		try {
			this.file = new SmbFile(fileURL.getURL(true));
			init(file);
		}
		catch(Exception e) {
// /!\ /!\ /!\  Recovery mechanism hereunder is pretty much useless since
// user permissions are not checked in SmbFile's constructor, i.e. exception
// won't be thrown here but later
			if(addAuthInfo) {
				// Remove newly created AuthInfo entry from AuthManager
				if(prevAuthInfo==null)
					AuthManager.remove(fileURL.getURL(false));
				else
					AuthManager.put(fileURL.getURL(false), prevAuthInfo);
			}
				
			if(e instanceof IOException)
				throw (IOException)e;
			throw new IOException();
		}	
	}
	
	/**
	 * Create a new SMBFile by using the given SmbFile instance and parent file.
	 */
	protected SMBFile(SmbFile file, SMBFile parent) throws MalformedURLException {
		this.file = file;
		init(file);
		setParent(parent);
	}


	private void init(SmbFile smbFile) throws MalformedURLException {
		String url = file.getCanonicalPath();
		FileURL fileURL = new FileURL(url);
		
		this.privateURL = fileURL.getURL(true);
		this.publicURL = fileURL.getURL(false);

//System.out.println("SMBFile.init() private="+privateURL);
//System.out.println("SMBFile.init() public="+publicURL);
	}


	protected void setParent(AbstractFile parent) {
		this.parent = parent;	
		this.parentValSet = true;
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
		return publicURL;
	}

	public String getSeparator() {
		return SEPARATOR;
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
			// SmbFile.getParent() never returns null
			if(this.publicURL.equals("smb://"))
				this.parent = null;
			else {
				String parentS = file.getParent();
//System.out.println("SMBFile.getParent, parentS= "+parentS);				
				try { this.parent = new SMBFile(parentS, false); }
				catch(IOException e) { this.parent = null; }
			}
				
			this.parentValSet = true;
			return this.parent;
		}
		
		return this.parent;
    }
	
	public boolean exists() {
		// Unlike java.io.File, SmbFile.exists() can throw an SmbException
		try {
            return file.exists();
		}
		catch(IOException e) {
			if(e instanceof SmbAuthException)
				return true;
			return false;
		}

	}
	
	public boolean canRead() {
		// Unlike java.io.File, SmbFile.canRead() can throw an SmbException
		try {
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
	

	public InputStream getInputStream() throws IOException {
		return new SmbFileInputStream(privateURL);
	}
	
	public OutputStream getOutputStream(boolean append) throws IOException {
		return new SmbFileOutputStream(privateURL, append);
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
		new SmbFile(privateURL+SEPARATOR+name).mkdir();
	}
}