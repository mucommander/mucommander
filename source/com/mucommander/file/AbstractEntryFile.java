package com.mucommander.file;

import java.io.*;

public abstract class AbstractEntryFile extends AbstractFile {

	protected AbstractFile archiveFile;
	
	protected AbstractFile parent;
	

	protected AbstractEntryFile(AbstractFile archiveFile) {
		this.archiveFile = archiveFile;
	}


	/////////////////////////////////////////
	// AbstractFile methods implementation //
	/////////////////////////////////////////

	public String getProtocol() {
		return archiveFile.getProtocol();
	}
	
	public String getAbsolutePath() {
		String path = getParent().getAbsolutePath(true)+getName();
		String separator = getSeparator();

		// Append a trailing separator character for directories
		if(isDirectory() && !path.endsWith(getSeparator()))
			return path+separator;
	
		return path;
	}

	public String getSeparator() {
		return archiveFile.getSeparator();
	}

	public AbstractFile getParent() {
		return parent;
	}
	
	protected void setParent(AbstractFile parent) {
		this.parent = parent;	
	}
	
	public boolean exists() {
		// Entry file should always exist since entries can only be created by the enclosing archive file.
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

	public boolean isSymlink() {
		return false;
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

	public void mkdir(String name) throws IOException {
		throw new IOException();
	}

}