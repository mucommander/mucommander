package com.mucommander.file;

import jcifs.smb.*;

import java.io.*;
import java.net.MalformedURLException;

/**
 * SMBFile represents a file shared through the SMB protocol.
 *
 * @author Maxence Bernard
 */
//public class SMBFile extends AbstractFile implements RemoteFile {
public class SMBFile extends AbstractFile {

	/** File separator is '/' for urls */
	private final static String SEPARATOR = "/";

	private SmbFile file;
	private FileURL fileURL;
	private String publicURL;
	private String privateURL;

	private AbstractFile parent;
	private boolean parentValSet;


	public SMBFile(String url) throws IOException {	
		this(url, true);
	}
	
	
	private SMBFile(String url, boolean addAuthInfo) throws IOException {	
		// all SMB URLs that represent  workgroups, servers, shares, or directories require a trailing slash '/'. 
		if(!url.endsWith("/"))
			url += '/';

		// At this point . and .. are not yet factored out, so authentication for paths which contain . or ..
		// will not behave properly  -> FileURL should factor out . and .. directly to fix the problem
		FileURL fileURL = new FileURL(url);
		
		AuthManager.authenticate(fileURL, addAuthInfo);
		
		// Unlike java.io.File, SmbFile throws an SmbException
		// when file doesn't exist
		this.file = new SmbFile(fileURL.getURL(true));
		url = file.getCanonicalPath();
		this.fileURL = new FileURL(url);
		
		this.privateURL = fileURL.getURL(true);
		this.publicURL = fileURL.getURL(false);
	}
	
	
	private void init(SmbFile smbFile) throws MalformedURLException {
	}


	/////////////////////////////////////////
	// AbstractFile methods implementation //
	/////////////////////////////////////////
	
	public String getProtocol() {
		return "SMB";
	}

	public String getName() {
		String name = file.getName();

		if(name.endsWith("/"))
			return name.substring(0, name.length()-1);
		return name;
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
	
	protected void setParent(AbstractFile parent) {
		this.parent = parent;	
		this.parentValSet = true;
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
	
	public boolean isSymlink() {
		// Symlinks are not supported under jCIFS (or in CIFS/SMB?)
		return false;
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
		if (dest instanceof SMBFile) {
			SMBFile destSMBFile = (SMBFile)dest;
			if(destSMBFile.fileURL.getHost().equals(this.fileURL.getHost())) 
				try{
//if(com.mucommander.Debug.ON) System.out.print("SMBFile.moveTo "+getAbsolutePath()+" to "+destSMBFile.getAbsolutePath());
					file.renameTo(destSMBFile.file);
//if(com.mucommander.Debug.ON) System.out.print("SMBFile.moveTo TRUE");
					return true;
				}
				catch(SmbException e) {
//if(com.mucommander.Debug.ON) System.out.print("SMBFile.moveTo FALSE");
					return false;
				}
		}
		
		return false;
	}

	public void delete() throws IOException {
		file.delete();
	}

	
	public AbstractFile[] ls() throws IOException {
        try {
			SmbFile smbFiles[] = file.listFiles();
			
			if(smbFiles==null)
				throw new IOException();
			
			// Create SMBFile by recycling SmbFile instance and sharing parent instance
			// among children
			AbstractFile children[] = new AbstractFile[smbFiles.length];
			AbstractFile child;
			for(int i=0; i<smbFiles.length; i++) {
				child = AbstractFile.wrapArchive(new SMBFile(smbFiles[i].getCanonicalPath(), false));
				child.setParent(this);
				children[i] = child;
				
//				children[i] = AbstractFile.getAbstractFile(smbFiles[i].getCanonicalPath(), this);
			}
			
			return children;
		}
		catch(SmbAuthException e) {
			throw new AuthException(fileURL, e.getMessage());
		}
	}

	
	public void mkdir(String name) throws IOException {
		// Unlike java.io.File.mkdir(), SmbFile does not return a boolean value
		// to indicate if the folder could be created
		new SmbFile(privateURL+SEPARATOR+name).mkdir();
	}
}