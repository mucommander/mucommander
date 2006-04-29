
package com.mucommander.file.archiver;

import com.mucommander.file.AbstractFile;

import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;

import java.io.OutputStream;
import java.io.IOException;


/**
 * Archiver implementation using the Zip archive format.
 *
 * @author Maxence Bernard
 */
class ZipArchiver extends Archiver {

	protected ZipArchiver(OutputStream outputStream) {
		super(new ZipOutputStream(outputStream));
	}


	/////////////////////////////
	// Archiver implementation //
	/////////////////////////////

	public OutputStream createEntry(String entryPath, AbstractFile file) throws IOException {
		boolean isDirectory = file.isDirectory();
		
		// Create the entry and use the provided file's date
		ZipEntry entry = new ZipEntry(normalizePath(entryPath, isDirectory));
		entry.setTime(file.getDate());
		
		// Add the entry
		((ZipOutputStream)outputStream).putNextEntry(entry);
	
		// Return the OutputStream that allows to write to the entry, only if it isn't a directory 
		return isDirectory?null:outputStream;
	}
}