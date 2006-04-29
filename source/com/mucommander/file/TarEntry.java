
package com.mucommander.file;


/**
 * TarEntry encapsulates a JavaTar library's tar entry.
 *
 * @author Maxence Bernard
 */
class TarEntry extends ArchiveEntry {
	
	private org.apache.tools.tar.TarEntry tarEntry;
	
	TarEntry(org.apache.tools.tar.TarEntry tarEntry) {
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
	
	long getSize() {
		return tarEntry.getSize();
	}

	boolean isDirectory() {
		return tarEntry.isDirectory();
	}
}