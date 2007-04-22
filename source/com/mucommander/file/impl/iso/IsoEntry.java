package com.mucommander.file.impl.iso;

import com.mucommander.file.ArchiveEntry;

/**
 * IsoEntry encapsulates an ISO entry.
 *
 * @author Xavier Martin
 */
public class IsoEntry extends ArchiveEntry {

    private String path;
    private long date;
    private int size;
    private boolean isDirectory;

    private long extent;

    public IsoEntry(String path, long date, int size, boolean isDirectory, long extent) {
        super(null);
        this.path = path;
        this.date = date;
        this.size = size;
        this.isDirectory = isDirectory;
        this.extent = extent;
    }

    public long getExtent() {
        return this.extent;
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