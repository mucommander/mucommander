package com.mucommander.file;

import java.io.*;
import java.util.zip.ZipEntry;

public class ZipEntryFile extends AbstractFile {
	private ZipEntry zipEntry;
	private ZipArchiveFile zipArchive;
	private AbstractFile parent;
	
	// Sames as the FSFile one so that absolute paths have only one kind of separator
	protected final static String separator = File.separator;
	
	protected ZipEntryFile(ZipArchiveFile zipArchive, AbstractFile parent, ZipEntry zipEntry) {
		super();
		this.zipArchive = zipArchive;
		this.parent = parent;
		this.zipEntry = zipEntry;
	}


	protected void setParent(AbstractFile parent) {
		this.parent = parent;	
	}

	
	/**
	 * Returns the ZipEntry associated with this ZipEntryFile
	 */
	public ZipEntry getZipEntry() {
		return zipEntry;
	}

	public String getName() {
		// ZipEntry name is actually the full path within the ZipFile		
        String entryName = zipEntry.getName();
		if(entryName.charAt(entryName.length()-1)=='/')
			entryName = entryName.substring(0,entryName.length()-1);
		int pos = entryName.lastIndexOf('/');
		return pos==-1?entryName:entryName.substring(pos+1,entryName.length());
	}
	
	public String getAbsolutePath() {
		//	return zipArchive.getAbsolutePath()+getName();
		return parent.getAbsolutePath()+separator+getName();
	}

	public String getSeparator() {
		return zipArchive.getSeparator();
	}

	public long getDate() {
		return zipEntry.getTime();
	}
	
	public long getSize() {
		return zipEntry.getSize();
	}
	
	public AbstractFile getParent() {
		return parent;
	}
	
	public boolean exists() {
		// for now, ZipEntryFile instances should always exist as they can
		// only be created by ZipArchiveFile.ls()
		return true;
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
		return zipEntry.isDirectory();
	}

	public InputStream getInputStream() throws IOException {
		return zipArchive.getEntryInputStream(zipEntry);
	}
	
	public OutputStream getOutputStream(boolean append) throws IOException {
		throw new IOException();
	}
	
	public boolean moveTo(AbstractFile dest) throws IOException {
		return false;
	}

	public void delete() throws IOException {
		throw new IOException();
	}

	public AbstractFile[] ls() throws IOException {
		return zipArchive.ls(this);
	}

	public void mkdir(String name) throws IOException {
		throw new IOException();
	}
}