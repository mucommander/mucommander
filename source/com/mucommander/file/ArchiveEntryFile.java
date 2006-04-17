package com.mucommander.file;

import java.io.*;

import java.util.Vector;


/**
 *
 *
 * @author Maxence Bernard
 */
public class ArchiveEntryFile extends AbstractFile {

	protected AbstractArchiveFile archiveFile;
	
	protected AbstractFile parent;
	
	protected ArchiveEntry entry;


	protected ArchiveEntryFile(AbstractArchiveFile archiveFile, ArchiveEntry entry) {
		super(archiveFile.getURL());
		this.archiveFile = archiveFile;
		this.entry = entry;
	}
	
	
	/**
	 * Returns the underlying ArchiveEntry instance.
	 */
	public ArchiveEntry getEntry() {
		return entry;
	}


	/////////////////////////////////
	// AbstractFile implementation //
	/////////////////////////////////

	public String getName() {
        // Extract the entry's filename from path 
		String entryName = entry.getPath();
		if(entryName.charAt(entryName.length()-1)=='/')
			entryName = entryName.substring(0,entryName.length()-1);
		int pos = entryName.lastIndexOf('/');
		return pos==-1?entryName:entryName.substring(pos+1,entryName.length());
	}
	
	public long getDate() {
		return entry.getDate();
	}
	
	public boolean changeDate(long lastModified) {
		// Entries are read-only
		return false;
	}

	public long getSize() {
		return entry.getSize();
	}
	
	public boolean isDirectory() {
		return entry.isDirectory();
	}

	public InputStream getInputStream() throws IOException {
		return archiveFile.getEntryInputStream(entry);
	}
	
	public AbstractFile[] ls() throws IOException {
/*
		ArchiveEntry entries[] = archiveFile.getEntries();
		Vector subFiles = new Vector();
		
		// Return the entries the given entry contains (entries of depth+1)
		String entryPath = entry.getPath();
		int depth = entry.getDepth()+1;
		ArchiveEntry subEntry;
		String subEntryPath;
		ArchiveEntryFile subEntryFile;
		for(int i=0; i<entries.length; i++) {
			subEntry = entries[i];
			subEntryPath = subEntry.getPath();
			if (subEntryPath.startsWith(entryPath) && subEntry.getDepth()==depth) {
				subEntryFile = new ArchiveEntryFile(archiveFile, subEntry);
				subEntryFile.setParent(this);
				subFiles.add(subEntryFile);
			}
		}

		AbstractFile subFilesArray[] = new AbstractFile[subFiles.size()];
		subFiles.toArray(subFilesArray);
		return subFilesArray;
*/

		return archiveFile.ls(this);
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
	
	public void setParent(AbstractFile parent) {
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

	public long getFreeSpace() {
		// All archive formats are read-only (for now)
		return 0;
	}

	public long getTotalSpace() {
		// We consider archive files as volumes, thus return the archive file's size
		return archiveFile.getSize();
	}	
}