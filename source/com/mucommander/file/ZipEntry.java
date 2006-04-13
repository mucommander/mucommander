
package com.mucommander.file;


/**
 * ZipEntry encapsulates java.util.zip zip entries.
 *
 * @author Maxence Bernard
 */
class ZipEntry extends ArchiveEntry {
	
	private java.util.zip.ZipEntry zipEntry;
	
	ZipEntry(java.util.zip.ZipEntry zipEntry) {
		super(zipEntry);
		this.zipEntry = zipEntry;
	}
	
	/////////////////////////////////////
	// Abstract methods implementation //
	/////////////////////////////////////
		
	String getPath() {
		return zipEntry.getName();
	}
	
	long getDate() {
		return zipEntry.getTime();
	}
	
	void setDate(long date) {
		zipEntry.setTime(date);
	}

	long getSize() {
		return zipEntry.getSize();
	}

	boolean isDirectory() {
		return zipEntry.isDirectory();
	}
	
	ArchiveEntry createEntry(String name) {
		return new ZipEntry(new java.util.zip.ZipEntry(name));
	}
}