package com.mucommander.file;

import java.io.*;
import java.util.*;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
// do not import java.util.zip.ZipEntry !

/**
 * 
 *
 * @author Maxence Bernard
 */
public class ZipArchiveFile extends AbstractArchiveFile {

	/** Zip entries contained by this zip file, loaded once for all when needed for the first time */
	private ZipEntry entries[];


	/**
	 * Creates a ZipArchiveFile around the given file.
	 */
	public ZipArchiveFile(AbstractFile file) {
		super(file);
	}


	/**
	 * Loads all entries contained in the Zip file.
	 */
	private void loadEntries() throws IOException {
long start = System.currentTimeMillis();

		// Load all zip entries
		Vector entriesV = new Vector();
		
		// If the underlying file is a local file, use the ZipFile.getEntries() method as it 
		// is *way* faster than using ZipInputStream to iterate over the entries.
		// Note: under Mac OS X at least, ZipFile.getEntries() method is native
		if(file instanceof FSFile) {
			Enumeration entriesEnum = new ZipFile(getAbsolutePath()).entries();
			while(entriesEnum.hasMoreElements())
				entriesV.add(new ZipEntry((java.util.zip.ZipEntry)entriesEnum.nextElement()));
		}
		else {
			// works but it is *way* slower
			ZipInputStream zin = new ZipInputStream(file.getInputStream());
			java.util.zip.ZipEntry entry;
			while ((entry=zin.getNextEntry())!=null) {
	//if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("found entry "+entry.getPath());
				entriesV.add(new ZipEntry(entry));
			}
			zin.close();
		}

if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("entries loaded in "+(System.currentTimeMillis()-start)+" ms");
start = System.currentTimeMillis();

		addMissingDirectoryEntries(entriesV);

if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("entries checked in "+(System.currentTimeMillis()-start)+" ms");

		entries = new ZipEntry[entriesV.size()];
		entriesV.toArray(entries);
	}


	/**
	 *  Returns top level (depth==0) zip entries.
	 */
	public AbstractFile[] ls() throws IOException {
		if (entries==null)
			loadEntries();
		Vector subFiles = new Vector();
		
		for(int i=0; i<entries.length; i++) {
			if (getEntryDepth(entries[i].getPath())==0) {
				subFiles.add(AbstractFile.wrapArchive(new ZipEntryFile(this, this, entries[i])));
			}
		}
		
		AbstractFile subFilesArray[] = new AbstractFile[subFiles.size()];
		subFiles.toArray(subFilesArray);
		return subFilesArray;
	}

	/**
	 * Returns the entries the given entry contains.
	 */
	public AbstractFile[] ls(ZipEntryFile entryFile) throws IOException {
		if (entries==null)
			loadEntries();
		Vector subFiles = new Vector();
		
		// Return the entries the given entry contains (entries of depth+1)
		String entryPath = entryFile.getZipEntry().getPath();
		int depth = getEntryDepth(entryPath)+1;
		ZipEntry subEntry;
		String subEntryPath;
		for(int i=0; i<entries.length; i++) {
			subEntry = entries[i];
			subEntryPath = subEntry.getPath();
			if (subEntryPath.startsWith(entryPath) && getEntryDepth(subEntryPath)==depth)
				subFiles.add(AbstractFile.wrapArchive(new ZipEntryFile(this, entryFile, subEntry)));
		}

		AbstractFile subFilesArray[] = new AbstractFile[subFiles.size()];
		subFiles.toArray(subFilesArray);
		return subFilesArray;
	}


	/**
	 * Returns an InputStream to read from the given entry.
	 */
	public InputStream getEntryInputStream(ZipEntry entry) throws IOException {
		// If the underlying file is a local file, use the ZipFile.getInputStream() method as it 
		// is *way* faster than using ZipInputStream and looking for the entry
		if (file instanceof FSFile) {
			return new ZipFile(getAbsolutePath()).getInputStream((java.util.zip.ZipEntry)entry.getEntry());
		}
		// works but it is *way* slower
		else {
			ZipInputStream zin = new ZipInputStream(file.getInputStream());
			java.util.zip.ZipEntry tempEntry;
			String entryPath = entry.getPath();
			// Iterate until we find the entry we're looking for
			while ((tempEntry=zin.getNextEntry())!=null)
				if (tempEntry.getName().equals(entryPath)) // That's the one, return it
					return zin;
			return null;
		}
	}
}