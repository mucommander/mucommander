package com.mucommander.file;

import java.io.*;
import java.util.Vector;

public class CachedFile extends AbstractFile {

	private AbstractFile file;
	
	private String name;
	private String absPath;
	private String separator;
	private long date;
	private long size;
	private AbstractFile parent;
	private boolean exists;
	private boolean canRead;
	private boolean canWrite;
	private boolean isHidden;
	private boolean isDirectory;
	private boolean isSymlink;
	
	
	public CachedFile(AbstractFile file) {
		this.file = file;

		this.name = file.getName();
		this.absPath = file.getAbsolutePath();
		this.separator = file.getSeparator();
		this.date = file.getDate();
		this.size = file.getSize();
		this.parent = file.getParent();
		this.exists = file.exists();
		this.canRead = file.canRead();
		this.canWrite = file.canWrite();
		this.isHidden = file.isHidden();
		this.isDirectory = file.isDirectory();
		this.isSymlink = file.isSymlink();
	}
	

	public AbstractFile getOriginalFile() {
		return this.file;
	}
	
	
	/////////////////////////////////////////
	// AbstractFile methods implementation //
	/////////////////////////////////////////
	
	public String getName() {
		return this.name;
	}

	public String getAbsolutePath() {
		return this.absPath;
	}
	
	public String getSeparator() {
		return this.separator;
	}
	
	public long getDate() {
		return this.date;
	}

	public long getSize() {
		return this.size;
	}
		
	public AbstractFile getParent() {
		return this.parent;
	}
		
	public void setParent(AbstractFile parent) {
		this.parent = parent;
	}
	
	public boolean exists() {
		return this.exists;
	}
	
	public boolean canRead() {
		return this.canRead;
	}
	
	public boolean canWrite() {
		return this.canWrite;
	}
	
	public boolean isHidden() {
		return this.isHidden;
	}	

	public boolean isDirectory() {
		return this.isDirectory;
	}
	
	public boolean isSymlink() {
		return this.isSymlink;
	}
	
	public AbstractFile[] ls() throws IOException {
		return file.ls();
	}
	
	public void mkdir(String name) throws IOException {
		file.mkdir(name);
	}

	public InputStream getInputStream() throws IOException {
		return file.getInputStream();
	}
	
	public OutputStream getOutputStream(boolean append) throws IOException {
		return file.getOutputStream(append);
	}

	public boolean moveTo(AbstractFile dest) throws IOException {
		return file.moveTo(dest);
	}

	public void delete() throws IOException {
		file.delete();
	}
}
