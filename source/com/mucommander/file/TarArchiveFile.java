package com.mucommander.file;

import java.io.*;
import java.util.Vector;

import com.ice.tar.*;


public class TarArchiveFile extends AbstractArchiveFile {

	/** Entries contained by this tar file, loaded once for all when needed for the first time */
	private TarEntry entries[];


	/**
	 * Creates a TarArchiveFile around the given file.
	 */
	public TarArchiveFile(AbstractFile file) {
		super(file);
	}


	/**
	 * Loads all entries contained in this TAR file.
	 */
	private void loadEntries() throws IOException {
		TarInputStream tin = new TarInputStream(file.getInputStream());
		Vector entriesV = new Vector();
		TarEntry entry;
		while ((entry=tin.getNextEntry())!=null) {
if(com.mucommander.Debug.ON) System.out.println("TarArchiveFile.loadEntries(): found entry "+entry.getName());
			entriesV.add(entry);
		}
		tin.close();
			
		// Checks for all entries below top level that there are entries from parent folders and if not,
		// create those entries. This is a tedious but necessary process, otherwise some entries would
		//  simply not appear.
		int nbEntries = entriesV.size();
		for(int i=0; i<nbEntries; i++) {
			TarEntry currentEntry = ((TarEntry)entriesV.elementAt(i));
			String entryPath = currentEntry.getName();	// entry path will include a trailing '/' if entry is a directory
			int entryLevel = getEntryLevel(entryPath);
if(com.mucommander.Debug.ON) System.out.println("TarArchiveFile.loadEntries(): checking entry #"+i+" "+entryPath+" level="+entryLevel);
			// Entry is not directly visible
			if (entryLevel>0) {
				int slashPos = 0;
				for(int l=0; l<entryLevel; l++) {
					// Extract directory name at level l
					String dirName = entryPath.substring(0, (slashPos=entryPath.indexOf('/', slashPos)+1));

if(com.mucommander.Debug.ON) System.out.println("TarArchiveFile.loadEntries(): checking for an existing entry for directory "+dirName);
					boolean entryFound = false;
					// Is there an entry for this directory ?
					for(int j=0; j<entriesV.size(); j++)
						if(((TarEntry)entriesV.elementAt(j)).getName().equals(dirName))
							entryFound = true;
	
					// An existing entry for this directory has been found, nothing to do, go to the next directory
					if(entryFound)
						continue;

					// Directory has no entry, let's manually create and add a TarEntry for it
if(com.mucommander.Debug.ON) System.out.println("TarArchiveFile.loadEntries(): creating new entry for directory "+dirName);
					TarEntry newEntry = new TarEntry(dirName);
					newEntry.setModTime(currentEntry.getModTime().getTime());	// Let's use current entry's time, better that 01/01/70
					entriesV.add(newEntry);
				}
			}
		}

		entries = new TarEntry[entriesV.size()];
		entriesV.toArray(entries);
	}


	/**
	 *  Returns top (level 0) entries only
	 */
	public AbstractFile[] ls() throws IOException {
		if (entries==null)
			loadEntries();
		Vector subFiles = new Vector();
		
		AbstractFile file;
		for(int i=0; i<entries.length; i++) {
			if (getEntryLevel(entries[i].getName())==0) {
				subFiles.add(AbstractFile.wrapArchive(new TarEntryFile(this, this, entries[i])));
			}
		}
		
		AbstractFile subFilesArray[] = new AbstractFile[subFiles.size()];
		subFiles.toArray(subFilesArray);
		return subFilesArray;
	}

	/**
	 * Returns entries directly below given entry.
	 */
	public AbstractFile[] ls(TarEntryFile entryFile) throws IOException {
		if (entries==null)
			loadEntries();
		Vector subFiles = new Vector();
		
		// Returns the entries under the given one and of 1 level below
		String entryName = entryFile.getTarEntry().getName();
		int level = getEntryLevel(entryName)+1;
		TarEntry subEntry;
		String subEntryName;
		for(int i=0; i<entries.length; i++) {
			subEntry = entries[i];
			subEntryName = subEntry.getName();
			if (subEntryName.startsWith(entryName) && getEntryLevel(subEntryName)==level)
				subFiles.add(AbstractFile.wrapArchive(new TarEntryFile(this, entryFile, subEntry)));
		}

		AbstractFile subFilesArray[] = new AbstractFile[subFiles.size()];
		subFiles.toArray(subFilesArray);
		return subFilesArray;
	}


	/**
	 * Returns the level of an entry based on the number of '/' characters the entry's path contains.
	 * Top level is 0.
	 */
	private static int getEntryLevel(String name) {
		int count=0;
		int pos=0;

		while ((pos=name.indexOf('/', pos+1))!=-1)
			count++;
		
		// Directories end with a '/'
		if(name.charAt(name.length()-1)=='/')
			count--;
		return count;	
	}


	/**
	 * Returns an InputStream to read from the given entry.
	 */
	public InputStream getEntryInputStream(TarEntry entry) throws IOException {
		TarInputStream zin = new TarInputStream(file.getInputStream());
		TarEntry tempEntry;
		String entryName = entry.getName();
		while ((tempEntry=zin.getNextEntry())!=null) {
			if (tempEntry.getName().equals(entryName))
				return zin;
		}

		return null;
	}
}