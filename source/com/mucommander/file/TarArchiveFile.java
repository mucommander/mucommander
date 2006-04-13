package com.mucommander.file;

import java.io.*;
import java.util.Vector;

import com.ice.tar.TarInputStream;
import java.util.zip.GZIPInputStream;
import org.apache.tools.bzip2.CBZip2InputStream;


/**
 * 
 *
 * @author Maxence Bernard
 */
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
	 * Returns an InputStream which can be used to read TAR entries.
	 */
	private TarInputStream openTarStream() throws IOException {
		String ext = getExtension();
		InputStream inputStream = file.getInputStream();
		
		if(ext!=null) {
			ext = ext.toLowerCase();
			// TGZ file
			if(ext.equals("tgz") || ext.equals("gz"))
				inputStream = new GZIPInputStream(inputStream);
			// TBZ2 file
			else if(ext.equals("tbz2") || ext.equals("bz2"))
				inputStream = new CBZip2InputStream(inputStream);
		}
		
		// TAR-only file
		return new TarInputStream(inputStream);
	}


	/**
	 * Loads all entries contained in this TAR file.
	 */
	private void loadEntries() throws IOException {
		TarInputStream tin = openTarStream();

		// Load TAR entries
		Vector entriesV = new Vector();
		com.ice.tar.TarEntry entry;
		while ((entry=tin.getNextEntry())!=null) {
			entriesV.add(new TarEntry(entry));
		}
		tin.close();

		addMissingDirectoryEntries(entriesV);

		entries = new TarEntry[entriesV.size()];
		entriesV.toArray(entries);
	}


	/**
	 *  Returns top level (depth==0) entries.
	 */
	public AbstractFile[] ls() throws IOException {
		if (entries==null)
			loadEntries();
		Vector subFiles = new Vector();
		
		for(int i=0; i<entries.length; i++) {
			if (getEntryDepth(entries[i].getPath())==0) {
				subFiles.add(AbstractFile.wrapArchive(new TarEntryFile(this, this, entries[i])));
			}
		}
		
		AbstractFile subFilesArray[] = new AbstractFile[subFiles.size()];
		subFiles.toArray(subFilesArray);
		return subFilesArray;
	}

	/**
	 * Returns the entries the given entry contains.
	 */
	public AbstractFile[] ls(TarEntryFile entryFile) throws IOException {
		if (entries==null)
			loadEntries();
		Vector subFiles = new Vector();
		
		// Return entryPaththe entries the given entry contains (entries of depth+1)
		String entryPath = entryFile.getTarEntry().getPath();
		int level = getEntryDepth(entryPath)+1;
		TarEntry subEntry;
		String subEntryPath;
		for(int i=0; i<entries.length; i++) {
			subEntry = entries[i];
			subEntryPath = subEntry.getPath();
			if (subEntryPath.startsWith(entryPath) && getEntryDepth(subEntryPath)==level)
				subFiles.add(AbstractFile.wrapArchive(new TarEntryFile(this, entryFile, subEntry)));
		}

		AbstractFile subFilesArray[] = new AbstractFile[subFiles.size()];
		subFiles.toArray(subFilesArray);
		return subFilesArray;
	}


	/**
	 * Returns an InputStream to read from the given entry.
	 */
	public InputStream getEntryInputStream(TarEntry entry) throws IOException {
		TarInputStream tin = openTarStream();
		com.ice.tar.TarEntry tempEntry;
		String entryPath = entry.getPath();
		while ((tempEntry=tin.getNextEntry())!=null) {
			if (tempEntry.getName().equals(entryPath))
				return tin;
		}

		return null;
	}
}