
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

	private ZipOutputStream zos;
	private boolean firstEntry = true;
	
	protected ZipArchiver(OutputStream outputStream) {
		this.zos = new ZipOutputStream(outputStream);
	}


	/**
	 * Overrides Archiver's no-op setComment method as Zip supports archive comment.
	 */
	public void setComment(String comment) {
		zos.setComment(comment);
	} 
	

	/////////////////////////////
	// Archiver implementation //
	/////////////////////////////

	public OutputStream createEntry(String entryPath, AbstractFile file) throws IOException {
		// Start by closing the previous entry
		if(!firstEntry)
			zos.closeEntry();

		boolean isDirectory = file.isDirectory();
		
		// Create the entry and use the provided file's date
		ZipEntry entry = new ZipEntry(normalizePath(entryPath, isDirectory));
		// Use provided file's size and date
		entry.setTime(file.getDate());
		entry.setSize(file.getSize());
		
		// Add the entry
		zos.putNextEntry(entry);

		if(firstEntry)
			firstEntry = false;
		
		// Return the OutputStream that allows to write to the entry, only if it isn't a directory 
		return isDirectory?null:zos;
	}


	public void close() throws IOException {
		if(!firstEntry)
			zos.closeEntry();
		
		zos.close();
	}
}