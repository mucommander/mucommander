
package com.mucommander.file;


/**
 * TarEntry encapsulates a JavaTar library's tar entry.
 *
 * @author Maxence Bernard
 */
class TarEntry extends ArchiveEntry {
	
	private com.ice.tar.TarEntry tarEntry;
	
	TarEntry(com.ice.tar.TarEntry tarEntry) {
		super(tarEntry);
		this.tarEntry = tarEntry;
	}
	
	/////////////////////////////////////
	// Abstract methods implementation //
	/////////////////////////////////////
		
	String getPath() {
		return tarEntry.getName();
	}
	
	long getDate() {
		return tarEntry.getModTime().getTime();
	}
	
	void setDate(long date) {
		tarEntry.setModTime(date);
	}


	long getSize() {
		return tarEntry.getSize();
	}

	boolean isDirectory() {
		return tarEntry.isDirectory();
	}
	
	ArchiveEntry createDirectoryEntry(String path) {
		return new TarEntry(new com.ice.tar.TarEntry(path));
	}
}