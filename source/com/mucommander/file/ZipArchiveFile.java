package com.mucommander.file;

import java.io.*;
import java.util.*;
import java.util.zip.*;

public class ZipArchiveFile extends ArchiveFile {
	private AbstractFile file;

	private ZipEntry entries[];

	private final static int READ_BUFFER_SIZE = 8192;

	public ZipArchiveFile(AbstractFile file) {
		this.file = file;
	}

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
	
	public long getSize() {
		return file.getSize();
	}
	
	public AbstractFile getParent() {
		return file.getParent();
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

	public boolean isDirectory() {
		return true;
	}

	public boolean isHidden() {
		return file.isHidden();
	}

	public InputStream getInputStream() throws IOException {
		return file.getInputStream();
	}
	
	public OutputStream getOutputStream(boolean append) throws IOException {
		return file.getOutputStream(append);
	}
		
	public boolean moveTo(AbstractFile dest) throws IOException  {
		return file.moveTo(dest);
	}

	public void delete() throws IOException {
		// Although it's a folder, no need to recurse
		file.delete();
	}

	public void mkdir(String name) throws IOException {
	}

	/**
	 * Loads ZipEntries contained in the zip file.
	 */
	private void loadEntries() throws IOException {
		ZipInputStream zin = new ZipInputStream(file.getInputStream());
		Vector entriesV = new Vector();
		ZipEntry entry;
		while ((entry=zin.getNextEntry())!=null)
			entriesV.add(entry);
			
		entries = new ZipEntry[entriesV.size()];
		entriesV.toArray(entries);
		zin.close();
	}

	// Returns the zip entries of level 0 only
	public AbstractFile[] ls() throws IOException {
		if (entries==null)
			loadEntries();
		Vector subFiles = new Vector();
		
		AbstractFile file;
		for(int i=0; i<entries.length; i++) {
			if (getEntryLevel(entries[i].getName())==0) {
				subFiles.add(new ZipEntryFile(this, this, entries[i]));
			}
		}
		
		AbstractFile subFilesArray[] = new AbstractFile[subFiles.size()];
		subFiles.toArray(subFilesArray);
		return subFilesArray;
	}


	public AbstractFile[] ls(ZipEntryFile entryFile) throws IOException {
		if (entries==null)
			loadEntries();
		Vector subFiles = new Vector();
		
		// Returns the zip entries under the given one and of 1 level below
		String entryName = entryFile.getZipEntry().getName();
		int level = getEntryLevel(entryName)+1;
		ZipEntry subEntry;
		String subEntryName;
		for(int i=0; i<entries.length; i++) {
			subEntry = entries[i];
			subEntryName = subEntry.getName();
			if (subEntryName.startsWith(entryName) && getEntryLevel(subEntryName)==level) {
				subFiles.add(new ZipEntryFile(this, entryFile, subEntry));
			}
		}

		AbstractFile subFilesArray[] = new AbstractFile[subFiles.size()];
		subFiles.toArray(subFilesArray);
		return subFilesArray;
	}

	/**
	 * Returns the level of a ZipEntry based on the number of '/' characters.
	 * The highest level is 0.
	 */
	private static int getEntryLevel(String name) {
		int count=0;
		int pos=0;

		while ((pos=name.indexOf('/', pos+1))!=-1)
			count++;
		
		// Directories in a ZipFile end with a '/'
		if(name.charAt(name.length()-1)=='/')
			count--;
		return count;	
	}

	public InputStream getEntryInputStream(ZipEntry entry) throws IOException {
		// If this zip file is an FSFile, we use the ZipFile.getInputStream() method as it 
		// is way way way faster than without
		if (file instanceof FSFile) {
			return new ZipFile(getAbsolutePath()).getInputStream(entry);
		}
		// works but it is VERY slow!
		else {
			ZipInputStream zin = new ZipInputStream(file.getInputStream());
			ZipEntry tempEntry;
			String entryName = entry.getName();
			while ((tempEntry=zin.getNextEntry())!=null)
				if (tempEntry.getName().equals(entryName)) {
					return zin;
				}
			return null;
		}
	}
}