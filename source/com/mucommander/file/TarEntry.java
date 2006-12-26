
package com.mucommander.file;

import com.mucommander.Debug;


/**
 * TarEntry encapsulates am org.apache.tools.tar.TarEntry tar entry.
 *
 * @author Maxence Bernard
 */
class TarEntry extends ArchiveEntry {
	
    private org.apache.tools.tar.TarEntry tarEntry;
	
    TarEntry(org.apache.tools.tar.TarEntry tarEntry) {
        super(tarEntry);
if(Debug.ON) Debug.trace("name="+tarEntry.getName()+" mode="+tarEntry.getMode());
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

    int getPermissions() {
        return tarEntry.getMode();
    }
}
