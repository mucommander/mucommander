package com.mucommander.file;

import java.io.*;
import java.util.Vector;


/**
 *
 *
 * @author Maxence Bernard
 */
public abstract class AbstractArchiveFile extends AbstractFile {

	/** Underlying file */
	protected AbstractFile file;


	/**
	 * Creates an AbstractArchiveFile on top of the given file.
	 */
	protected AbstractArchiveFile(AbstractFile file) {
		super(file.getURL());
		this.file = file;
	}


	/**
	 * Returns the AbstractFile instance this archive is wrapped around.
	 *
	 * @return the AbstractFile instance this archive is wrapped around
	 */
	public AbstractFile getEnclosedFile() {
		return file;
	}


	/**
	 * Checks all the given entries below top level (depth>0) and make sure they have a corresponding parent directory
	 * entry, and if not create it and add it to the entries Vector.
	 */
	protected static void addMissingDirectoryEntries(Vector entriesV) {
		int nbEntries = entriesV.size();
		for(int i=0; i<nbEntries; i++) {
			ArchiveEntry currentEntry = ((ArchiveEntry)entriesV.elementAt(i));
			String entryPath = currentEntry.getPath();	// entry path will include a trailing '/' if entry is a directory
			int entryDepth = currentEntry.getDepth();
// if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("checking entry #"+i+" "+entryPath+" depth="+entryDepth);
			// Entry is not at the top level
			if (entryDepth>0) {
				int slashPos = 0;
				for(int l=0; l<entryDepth; l++) {
					// Extract directory name at depth l
					String dirName = entryPath.substring(0, (slashPos=entryPath.indexOf('/', slashPos)+1));

// if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("checking for an existing entry for directory "+dirName);
					boolean entryFound = false;
					// Is there an entry for this directory ?
					for(int j=0; j<entriesV.size(); j++)
						if(((ArchiveEntry)entriesV.elementAt(j)).getPath().equals(dirName))
							entryFound = true;
	
					// An existing entry for this directory has been found, nothing to do, go to the next directory
					if(entryFound)
						continue;

					// Directory has no entry, let's manually create and add an ArchiveEntry for it
// if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("creating new entry for directory "+dirName);
					ArchiveEntry newEntry = currentEntry.createDirectoryEntry(dirName);
					newEntry.setDate(currentEntry.getDate());	// Use current entry's time, not accurate
					entriesV.add(newEntry);
				}
			}
		}
	}


	/**
	 *  Returns top level (depth==0) entries containted by this archive.
	 */
	public AbstractFile[] ls() throws IOException {
		ArchiveEntry entries[] = getEntries();

		Vector subFiles = new Vector();		
		AbstractFile entryFile;
		for(int i=0; i<entries.length; i++) {
			if (entries[i].getDepth()==0) {
				entryFile = AbstractFile.wrapArchive(new ArchiveEntryFile(this, entries[i]));
				entryFile.setParent(this);
				subFiles.add(entryFile);
			}
		}
		
		AbstractFile subFilesArray[] = new AbstractFile[subFiles.size()];
		subFiles.toArray(subFilesArray);
		return subFilesArray;
	}


	//////////////////////
	// Abstract methods //
	//////////////////////
	
	/**
	 * Returns an array of all the entries this archive file contains.
	 */
	abstract ArchiveEntry[] getEntries() throws IOException;

//	/**
//	 * Creates and returns an ArchiveEntryFile instance using the given ArchiveEntry.
//	 */
//	protected abstract ArchiveEntryFile createArchiveEntryFile(ArchiveEntry entry);
	
	/**
	 * Returns an InputStream to read from the given entry.
	 */
	abstract InputStream getEntryInputStream(ArchiveEntry entry) throws IOException;


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
		// All archive files are read-only (for now), let's throw an exception
		throw new IOException();
	}

	public long getFreeSpace() {
		// All archive files are read-only (for now), return 0
		return 0;
	}

	public long getTotalSpace() {
		// An archive is considered as a volume by itself, let's return the archive's size
		return file.getSize();
	}	
}