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


	////////////////////////////////////////
	// AbstractArchiveFile implementation //
	////////////////////////////////////////
	
	protected ArchiveEntry[] getEntries() throws IOException {
		if(this.entries==null) {
			TarInputStream tin = openTarStream();

			// Load TAR entries
			Vector entriesV = new Vector();
			com.ice.tar.TarEntry entry;
			while ((entry=tin.getNextEntry())!=null) {
				entriesV.add(new TarEntry(entry));
			}
			tin.close();

			addMissingDirectoryEntries(entriesV);

			this.entries = new TarEntry[entriesV.size()];
			entriesV.toArray(entries);
		}

		return this.entries;
	}


	InputStream getEntryInputStream(ArchiveEntry entry) throws IOException {
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