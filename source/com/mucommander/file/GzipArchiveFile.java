package com.mucommander.file;

import java.io.*;

/**
 * 
 *
 * @author Maxence Bernard
 */
public class GzipArchiveFile extends AbstractArchiveFile {

	/**
	 * Creates a GzipArchiveFile around the given file.
	 */
	public GzipArchiveFile(AbstractFile file) {
		super(file);
	}


	/**
	 * Returns this Gzipped file's sole entry.
	 */
	public AbstractFile[] ls() throws IOException {
		return new AbstractFile[]{AbstractFile.wrapArchive(new GzipEntryFile(this))};
	}
}