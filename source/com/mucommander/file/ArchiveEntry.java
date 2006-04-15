
package com.mucommander.file;

/**
 * This abstract class encapsulates any 3rd-party archive entry.
 *
 * @author Maxence Bernard 
 */
abstract class ArchiveEntry {
	
	protected Object entry;
	
	/**
	 * Creates a new ArchiveEntry that encapsulates the given 3rd party entry.
	 */
	ArchiveEntry(Object entry) {
		this.entry = entry;
	}
		
	/**
	 * Returns the encapsulated entry.
	 */
	Object getEntry() {
		return entry;
	}


	/**
	 * Returns the depth of this entry based on the number of slash character ('/') occurrences its path contains.
	 * Minimum depth is 0.
	 */
	int getDepth() {
		int count=0;
		int pos=0;
		String path = getPath();

		while ((pos=path.indexOf('/', pos+1))!=-1)
			count++;
		
		// Directories in archives end with a '/'
		if(path.charAt(path.length()-1)=='/')
			count--;
		return count;	
	}


	//////////////////////
	// Abstract methods //
	//////////////////////
		
	/**
	 * Returns the encapsulated entry's path.
	 */
	abstract String getPath();
	
	/**
	 * Returns the encapsulated entry's date.
	 */
	abstract long getDate();

	/**
	 * Sets the encapsulated entry's date.
	 */
	abstract void setDate(long date);

	/**
	 * Returns the encapsulated entry's size.
	 */
	abstract long getSize();

	/**
	 * Returns <code>true</code> if the encapsulated entry is a directory.
	 */
	abstract boolean isDirectory();
	
	/**
	 * Creates and returns a new directory archive entry with the specified path.
	 */
	abstract ArchiveEntry createDirectoryEntry(String path);
}