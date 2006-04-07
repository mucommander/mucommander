
package com.mucommander.file;

import org.apache.tools.bzip2.CBZip2InputStream;
import java.io.*;

/**
 * 
 *
 * @author Maxence Bernard
 */
public class Bzip2EntryFile extends AbstractEntryFile {

	/**
	 * Creates a GzipEntryFile around the given file.
	 */
	public Bzip2EntryFile(Bzip2ArchiveFile archiveFile) {
		super(archiveFile);
		this.parent = archiveFile;
	}


	/////////////////////////////////////////
	// AbstractFile methods implementation //
	/////////////////////////////////////////
	
	public long getDate() {
		return archiveFile.getDate();
	}
	
	public boolean changeDate(long lastModified) {
		// Entries are read-only
		return false;
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
		String extension = archiveFile.getExtension();
		String name = archiveFile.getName();
		
		if(extension==null)
			return name;
			
		extension = extension.toLowerCase();
		
		if(extension.equals("tbz2"))
			return name.substring(0, name.length()-4)+"tar";

		if(extension.equals("bz2"))
			return name.substring(0, name.length()-4);
	
		return name;
	}
	
	
	public InputStream getInputStream() throws IOException {
		return new CBZip2InputStream(archiveFile.getInputStream());
	}
}