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

	/**
	 * Creates a TarArchiveFile around the given file.
	 */
	public TarArchiveFile(AbstractFile file) {
		super(file);
	}


	/**
	 * Returns a TarInputStream which can be used to read TAR entries.
	 */
	private TarInputStream createTarStream() throws IOException {
		String ext = getExtension();
		InputStream inputStream = file.getInputStream();

// if(com.mucommander.Debug.ON) com.mucommander.Debug.trace(this+" inputStream="+inputStream+" fileClass="+file.getClass());
	
		if(ext!=null) {
			ext = ext.toLowerCase();
			// Gzip-compressed file
			if(ext.equals("tgz") || ext.equals("gz")) {
				// Note: this will fail for gz/tgz entries inside a tar file (IOException: Not in GZIP format),
				// why is a complete mystery: the gz/tgz entry can be extracted and then properly browsed
				inputStream = new GZIPInputStream(inputStream);
			}
			// Bzip2-compressed file
			else if(ext.equals("tbz2") || ext.equals("bz2")) {
				inputStream = new CBZip2InputStream(inputStream);
			}
		}

		return new TarInputStream(inputStream);
	}


	////////////////////////////////////////
	// AbstractArchiveFile implementation //
	////////////////////////////////////////
	
	protected Vector getEntries() throws IOException {
		// Note: JavaTar's FastTarStream can unfortunately not be used
		// because it fails on many tar files that TarInputStream can read
		// without any problem.
		TarInputStream tin = createTarStream();

		// Load TAR entries
		Vector entries = new Vector();
		com.ice.tar.TarEntry entry;
		while ((entry=tin.getNextEntry())!=null) {
			entries.add(new TarEntry(entry));
		}
		tin.close();

		return entries;
	}


	InputStream getEntryInputStream(ArchiveEntry entry) throws IOException {
		TarInputStream tin = createTarStream();
		com.ice.tar.TarEntry tempEntry;
		String entryPath = entry.getPath();
		while ((tempEntry=tin.getNextEntry())!=null) {
			if (tempEntry.getName().equals(entryPath))
				return tin;
		}

		return null;
	}
}