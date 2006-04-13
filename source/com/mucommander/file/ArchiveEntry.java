
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
	 * Creates and returns a new archive entry using the given name.
	 */
	abstract ArchiveEntry createEntry(String name);
}