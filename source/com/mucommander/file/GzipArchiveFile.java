package com.mucommander.file;

import java.io.*;

/**
 * 
 *
 * @author Maxence Bernard
 */
public class GzipArchiveFile extends AbstractArchiveFile {

	/**
	 * Creates a GzipArchiveFile on top of the given file.
	 */
	public GzipArchiveFile(AbstractFile file) {
		super(file);
	}


	/**
	 * Returns the sole entry of this Gzip file.
	 */
	public AbstractFile[] ls() throws IOException {
		return new AbstractFile[]{AbstractFile.wrapArchive(new GzipEntryFile(this))};
	}
}