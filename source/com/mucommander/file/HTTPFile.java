package com.mucommander.file;

import java.io.*;
import java.net.*;
import java.util.Vector;


/**
 */
public class HTTPFile extends AbstractFile implements RemoteFile {

	/** File separator is '/' for urls */
	private final static String SEPARATOR = "/";
	
	protected String name;
	protected String absPath;
	protected URL url;
	protected long date;
//	private long size = -1;

	protected AbstractFile parent;	
	
	
	/**
	 * Creates a new instance of HTTPFile.
	 */
	public HTTPFile(String absPath) throws MalformedURLException {
		this(new URL(absPath));
	}


	protected HTTPFile(URL url) {
		this.url = url;
		
//		this.absPath = url.toString();
		this.absPath = url.toExternalForm();
		int urlLen = absPath.length();
		int pos = absPath.lastIndexOf('/');
		this.name = URLDecoder.decode(absPath.substring(pos<7?7:pos+1, absPath.endsWith(SEPARATOR)?urlLen-1:urlLen));

/*
		int urlLen = absPath.length();
		// Remove ending '/' character(s)
		while(absPath.charAt(urlLen-1)=='/')
			absPath = absPath.substring(0, --urlLen);
		int lastSlashPos = absPath.lastIndexOf('/');
		// Determine local file name
		this.fileName = java.net.URLDecoder.decode(absPath.substring(lastSlashPos==-1||lastSlashPos<7?7:lastSlashPos+1, urlLen));
*/
		
		this.date = System.currentTimeMillis();
	} 


	protected void setParent(AbstractFile parent) {
		this.parent = parent;
	}

	
	public String getName() {
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

	public long getDate() {
		return date;
	}
	
	public long getSize() {
		return 0;
	}
	
	public AbstractFile getParent() {
//		if(file==null)
//			return null;
//		
//		String parent = file.getParent();
//        // SmbFile.getParent() never returns null
//		if(parent.equals("smb://"))
//            return null;
//        
//		return new SMBFile(parent);
		
		return null;
	}
	
	
	public boolean exists() {
		try {
			url.openStream().close();
			return true;
		}
		catch(Exception e) {
			return false;
		}
	}
	
	public boolean canRead() {
		return true;
	}
	
	public boolean canWrite() {
		return false;
	}
	
	public boolean isHidden() {
		return false;
	}

	public boolean isDirectory() {
		return false;
	}
	
	
	public boolean equals(Object f) {
		if(!(f instanceof HTTPFile))
			return super.equals(f);		// could be equal to a ZipArchiveFile
		
		return ((HTTPFile)f).getAbsolutePath().equals(absPath);
	}
	
	
	
	public InputStream getInputStream() throws IOException {
		return url.openStream();
	}
	
	public OutputStream getOutputStream(boolean append) throws IOException {
		return null;
	}
		
	public boolean moveTo(AbstractFile dest) throws IOException  {
		return false;
	}

	public void delete() throws IOException {
		throw new IOException();
	}

	public AbstractFile[] ls() throws IOException {
		throw new IOException();
	}

	public void mkdir(String name) throws IOException {
		throw new IOException();
	}
}