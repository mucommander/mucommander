package com.mucommander.file;

import java.io.*;


/**
 * 
 *
 * @author Maxence Bernard
 */
public class TarEntryFile extends AbstractEntryFile {

	private TarEntry tarEntry;

	protected TarEntryFile(TarArchiveFile archiveFile, AbstractFile parent, TarEntry tarEntry) {
		super(archiveFile);
		this.parent = parent;
		this.tarEntry = tarEntry;
	}


	/**
	 * Returns the TarEntry associated with this TarEntryFile
	 */
	public TarEntry getTarEntry() {
		return tarEntry;
	}

	
	/////////////////////////////////////////
	// AbstractFile methods implementation //
	/////////////////////////////////////////

	public String getName() {
        String entryName = tarEntry.getPath();
		if(entryName.charAt(entryName.length()-1)=='/')
			entryName = entryName.substring(0,entryName.length()-1);
		int pos = entryName.lastIndexOf('/');
		return pos==-1?entryName:entryName.substring(pos+1,entryName.length());
	}
	
	public long getDate() {
		return tarEntry.getDate();
	}
	
	public boolean changeDate(long lastModified) {
		// Entries are read-only
		return false;
	}
	
	public long getSize() {
		return tarEntry.getSize();
	}
	
	public boolean isDirectory() {
		return tarEntry.isDirectory();
	}

	public InputStream getInputStream() throws IOException {
		return ((TarArchiveFile)archiveFile).getEntryInputStream(tarEntry);
	}
	
	public AbstractFile[] ls() throws IOException {
		return ((TarArchiveFile)archiveFile).ls(this);
	}
}