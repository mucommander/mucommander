package com.mucommander.file;

import java.io.*;
import java.util.*;
import java.util.zip.*;


public abstract class AbstractArchiveFile extends AbstractFile implements ArchiveFile {

	/** Wrapped around file */
	protected AbstractFile file;


	/**
	 * Creates an ArchiveFile around the given file.
	 */
	protected AbstractArchiveFile(AbstractFile file) {
		super(file.getURL());
		this.file = file;
	}


	/////////////////////////////////////////
	// AbstractFile methods implementation //
	/////////////////////////////////////////
	
	public String getName() {
		return file.getName();
	}

	public String getAbsolutePath() {
		return file.getAbsolutePath();
	}

	public String getSeparator() {
		return file.getSeparator();
	}

	public long getDate() {
		return file.getDate();
	}
	
	public boolean changeDate(long date) {
		return file.changeDate(date);
	}
	
	public long getSize() {
		return file.getSize();
	}
	
	public AbstractFile getParent() {
		return file.getParent();
	}
	
	public void setParent(AbstractFile parent) {
		this.file.setParent(parent);	
	}	

	public boolean exists() {
		return file.exists();
	}
	
	public boolean canRead() {
		return file.canRead();
	}
	
	public boolean canWrite() {
		return file.canWrite();
	}

	public boolean isBrowsable() {
		return true;
	}
	
	public boolean isDirectory() {
		return false;
	}

	public boolean isHidden() {
		return file.isHidden();
	}

	public boolean isSymlink() {
		return file.isSymlink();
	}

	public InputStream getInputStream() throws IOException {
		return file.getInputStream();
	}
	
	public InputStream getInputStream(long skipBytes) throws IOException {
		return file.getInputStream(skipBytes);
	}
	
	public OutputStream getOutputStream(boolean append) throws IOException {
		return file.getOutputStream(append);
	}
		
	public boolean moveTo(AbstractFile dest) throws IOException  {
		return file.moveTo(dest);
	}

	public void delete() throws IOException {
		file.delete();
	}

	public void mkdir(String name) throws IOException {
		// By default archive entries are read-only, let's throw an exception
		throw new IOException();
	}
}