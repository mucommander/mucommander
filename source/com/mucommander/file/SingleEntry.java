
package com.mucommander.file;


/**
 *
 * @author Maxence Bernard
 */
class SingleEntry extends ArchiveEntry {

	private String path;
	private long date;
	private long size;
	
	SingleEntry(String path, long date, long size) {
		super(null);
		this.path = path;
		this.date = date;
		this.size = size;
	}
	
	/////////////////////////////////////
	// Abstract methods implementation //
	/////////////////////////////////////
		
	String getPath() {
		return path;
	}
	
	long getDate() {
		return date;
	}
	
	void setDate(long date) {
		this.date = date;
	}

	long getSize() {
		return size;
	}

	boolean isDirectory() {
		return false;
	}
	
	ArchiveEntry createDirectoryEntry(String path) {
		// No-op
		return null;
	}
}