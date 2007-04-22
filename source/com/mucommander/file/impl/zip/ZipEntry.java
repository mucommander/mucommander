
package com.mucommander.file.impl.zip;

import com.mucommander.file.ArchiveEntry;


/**
 * ZipEntry encapsulates a <code>java.util.zip</code> Zip entry.
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
        return 292;     // r--r--r--
    }

    public int getPermissionsMask() {
        return 0;       // permissions should not be taken into acount
    }
}
