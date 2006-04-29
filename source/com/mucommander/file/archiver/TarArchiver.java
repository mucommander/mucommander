
package com.mucommander.file.archiver;

import com.mucommander.file.AbstractFile;

import com.ice.tar.TarOutputStream;
import com.ice.tar.TarEntry;

import java.io.OutputStream;
import java.io.IOException;


/**
 * Archiver implementation using the Tar archive format.
 *
 * @author Maxence Bernard
 */
class TarArchiver extends Archiver {

	protected TarArchiver(OutputStream outputStream) {
		super(new TarOutputStream(outputStream));
	}


	/////////////////////////////
	// Archiver implementation //
	/////////////////////////////

	public OutputStream createEntry(String entryPath, AbstractFile file) throws IOException {
		boolean isDirectory = file.isDirectory();
		
		// Create the entry and use the provided file's date
		TarEntry entry = new TarEntry(normalizePath(entryPath, isDirectory));
		entry.setModTime(file.getDate());
		
		// Add the entry
		((TarOutputStream)outputStream).putNextEntry(entry);
	
		// Return the OutputStream that allows to write to the entry, only if it isn't a directory 
		return isDirectory?null:outputStream;
	}
}