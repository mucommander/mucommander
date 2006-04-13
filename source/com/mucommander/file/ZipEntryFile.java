package com.mucommander.file;

import java.io.*;


/**
 * 
 *
 * @author Maxence Bernard
 */
public class ZipEntryFile extends AbstractEntryFile {

	private ZipEntry zipEntry;
	
	protected ZipEntryFile(ZipArchiveFile archiveFile, AbstractFile parent, ZipEntry zipEntry) {
		super(archiveFile);
		this.parent = parent;
		this.zipEntry = zipEntry;
	}


	/**
	 * Returns the ZipEntry associated with this ZipEntryFile
	 */
	public ZipEntry getZipEntry() {
		return zipEntry;
	}

	
	/////////////////////////////////////////
	// AbstractFile methods implementation //
	/////////////////////////////////////////
	
	public String getName() {
		// ZipEntry name is actually the full path within the ZipFile		
        String entryName = zipEntry.getPath();
		if(entryName.charAt(entryName.length()-1)=='/')
			entryName = entryName.substring(0,entryName.length()-1);
		int pos = entryName.lastIndexOf('/');
		return pos==-1?entryName:entryName.substring(pos+1,entryName.length());
	}
	
	public long getDate() {
		return zipEntry.getDate();
	}
	
	public boolean changeDate(long lastModified) {
		// Entries are read-only
		return false;
	}

	public long getSize() {
		return zipEntry.getSize();
	}
	
	public boolean isDirectory() {
		return zipEntry.isDirectory();
	}

	public InputStream getInputStream() throws IOException {
		return ((ZipArchiveFile)archiveFile).getEntryInputStream(zipEntry);
	}
	
	public AbstractFile[] ls() throws IOException {
		return ((ZipArchiveFile)archiveFile).ls(this);
	}
}