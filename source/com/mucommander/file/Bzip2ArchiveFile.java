package com.mucommander.file;

import java.io.*;

/**
 * 
 *
 * @author Maxence Bernard
 */
public class Bzip2ArchiveFile extends AbstractArchiveFile {

	/**
	 * Creates a BzipArchiveFile on top of the given file.
	 */
	public Bzip2ArchiveFile(AbstractFile file) {
		super(file);
	}


	/**
	 * Returns the sole entry of this Bzip file.
	 */
	public AbstractFile[] ls() throws IOException {
		return new AbstractFile[]{AbstractFile.wrapArchive(new Bzip2EntryFile(this))};
	}
}