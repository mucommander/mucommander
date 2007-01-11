
package com.mucommander.file.impl.tar;

import com.mucommander.file.ArchiveEntry;


/**
 * TarEntry encapsulates am org.apache.tools.tar.TarEntry tar entry.
 *
 * @author Maxence Bernard
 */
public class TarEntry extends ArchiveEntry {
	
    private org.apache.tools.tar.TarEntry tarEntry;
	
    public TarEntry(org.apache.tools.tar.TarEntry tarEntry) {
        super(tarEntry);
        this.tarEntry = tarEntry;
    }
	
    /////////////////////////////////////
    // Abstract methods implementation //
    /////////////////////////////////////
		
    public String getPath() {
        return tarEntry.getName();
    }
	
    public long getDate() {
        return tarEntry.getModTime().getTime();
    }
	
    public long getSize() {
        return tarEntry.getSize();
    }

    public boolean isDirectory() {
        return tarEntry.isDirectory();
    }

    public int getPermissions() {
        return tarEntry.getMode();
    }
}
