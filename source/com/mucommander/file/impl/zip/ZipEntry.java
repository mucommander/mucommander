
package com.mucommander.file.impl.zip;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.ArchiveEntry;


/**
 * ZipEntry encapsulates a java.util.zip zip entry.
 *
 * @author Maxence Bernard
 */
public class ZipEntry extends ArchiveEntry {
	
    private java.util.zip.ZipEntry zipEntry;
	
    public ZipEntry(java.util.zip.ZipEntry zipEntry) {
        super(zipEntry);
        this.zipEntry = zipEntry;
    }
	
    /////////////////////////////////////
    // Abstract methods implementation //
    /////////////////////////////////////
		
    public String getPath() {
        return zipEntry.getName();
    }
	
    public long getDate() {
        return zipEntry.getTime();
    }
	
    public long getSize() {
        return zipEntry.getSize();
    }

    public boolean isDirectory() {
        return zipEntry.isDirectory();
    }

    public int getPermissions() {
        return AbstractFile.READ_MASK | AbstractFile.WRITE_MASK;
    }
}
