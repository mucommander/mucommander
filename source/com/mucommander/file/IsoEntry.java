package com.mucommander.file;

public class IsoEntry extends ArchiveEntry {

    private String path;
    private long date;
    private int size;
    private boolean isDirectory;

    private long extent;

    IsoEntry(String path, long date, int size, boolean isDirectory, long extent) {
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

    String getPath() {
        return path;
    }

    long getDate() {
        return date;
    }

    public long getSize() {
        return size;
    }

    boolean isDirectory() {
        return isDirectory;
    }

    int getPermissions() {
        return AbstractFile.READ_MASK | AbstractFile.WRITE_MASK;
    }
}