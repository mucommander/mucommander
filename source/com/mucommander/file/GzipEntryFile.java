
package com.mucommander.file;

import java.io.*;
import java.util.zip.GZIPInputStream;


public class GzipEntryFile extends AbstractEntryFile {

	/**
	 * Creates a GzipEntryFile around the given file.
	 */
	public GzipEntryFile(GzipArchiveFile archiveFile) {
		super(archiveFile);
		this.parent = archiveFile;
	}


	/////////////////////////////////////////
	// AbstractFile methods implementation //
	/////////////////////////////////////////
	
	public long getDate() {
		return archiveFile.getDate();
	}
	
	public long getSize() {
		return -1;
	}
	
	public boolean isDirectory() {
		return false;
	}

	public AbstractFile[] ls() throws IOException {
		return new AbstractFile[0];
	}

	public String getName() {
		String name = archiveFile.getName();
		String nameLC = name.toLowerCase();
		if(name.endsWith(".tgz"))
			return name.substring(0, name.lastIndexOf(".tgz"))+".tar";
		else if(name.endsWith(".gz"))
			return name.substring(0, name.lastIndexOf(".gz"));
		else
			return name;
	}
	
	public InputStream getInputStream() throws IOException {
		return new GZIPInputStream(archiveFile.getInputStream());
	}
}