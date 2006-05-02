
package com.mucommander.file;


/**
 *
 * @author Maxence Bernard
 */
class SimpleEntry extends ArchiveEntry {

    private String path;
    private long date;
    private long size;
    private boolean isDirectory;
	
    SimpleEntry(String path, long date, long size, boolean isDirectory) {
        super(null);
        this.path = path;
        this.date = date;
        this.size = size;
        this.isDirectory = isDirectory;
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
	
    long getSize() {
        return size;
    }

    boolean isDirectory() {
        return isDirectory;
    }
}
