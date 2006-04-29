
package com.mucommander.file.archiver;

import com.mucommander.file.AbstractFile;

import java.io.OutputStream;
import java.io.IOException;


/**
 * Generic single file Archiver.
 *
 * @author Maxence Bernard
 */
class SingleFileArchiver extends Archiver {

	private boolean createEntryCalled;

	protected SingleFileArchiver(OutputStream outputStream) {
		super(outputStream);
	}


	/////////////////////////////
	// Archiver implementation //
	/////////////////////////////

	/**
	 * This method is a no-op, and does nothing but throw an IOException if it is called more than once,
	 * which should never be the case as this Archiver is only meant to store one file. 
	 */
	public OutputStream createEntry(String entryPath, AbstractFile file) throws IOException {
		if(!createEntryCalled)
			createEntryCalled = true;
		else
			throw new IOException();

		return outputStream;
	}
}