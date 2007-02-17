
package com.mucommander.file;

/**
 *
 * @author Maxence Bernard
 */
public class SimpleArchiveEntry extends ArchiveEntry {

    private String path;
    private long date;
    private long size;
    private boolean isDirectory;
	
    public SimpleArchiveEntry(String path, long date, long size, boolean isDirectory) {
        super(null);
        this.path = path;
        this.date = date;
        this.size = size;
        this.isDirectory = isDirectory;
    }
	
    /////////////////////////////////////
    // Abstract methods implementation //
    /////////////////////////////////////
		
    public String getPath() {
        return path;
    }
	
    public long getDate() {
        return date;
    }
	
    public long getSize() {
        return size;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public int getPermissions() {
        return 292;     // r--r--r--
    }

    public int getPermissionsMask() {
        return 0;       // permissions should not be taken into acount
    }
}
