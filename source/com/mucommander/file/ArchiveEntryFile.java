package com.mucommander.file;

import com.mucommander.file.filter.FilenameFilter;
import com.mucommander.file.filter.FileFilter;

import java.io.*;


/**
 *
 *
 * @author Maxence Bernard
 */
public class ArchiveEntryFile extends AbstractFile {

    protected AbstractArchiveFile archiveFile;
	
    protected AbstractFile parent;
	
    protected ArchiveEntry entry;


    protected ArchiveEntryFile(AbstractArchiveFile archiveFile, ArchiveEntry entry, FileURL fileURL) {
        super(fileURL);
        this.archiveFile = archiveFile;
        this.entry = entry;
    }
	
	
    /**
     * Returns the underlying ArchiveEntry instance.
     */
    public ArchiveEntry getEntry() {
        return entry;
    }


    /////////////////////////////////
    // AbstractFile implementation //
    /////////////////////////////////

    public long getDate() {
        return entry.getDate();
    }
	
    public boolean changeDate(long lastModified) {
        // Archive entries are read-only
        return false;
    }

    public long getSize() {
        return entry.getSize();
    }
	
    public boolean isDirectory() {
        return entry.isDirectory();
    }

    public AbstractFile[] ls() throws IOException {
        return archiveFile.ls(this, null, null);
    }

    public AbstractFile[] ls(FilenameFilter filter) throws IOException {
        return archiveFile.ls(this, filter, null);
    }
	
    public AbstractFile[] ls(FileFilter filter) throws IOException {
        return archiveFile.ls(this, null, filter);
    }

    public AbstractFile getParent() {
        return parent;
    }
	
    public void setParent(AbstractFile parent) {
        this.parent = parent;	
    }
	
    public boolean exists() {
        // Entry file should always exist since entries can only be created by the enclosing archive file.
        return true;
    }
	
    public boolean canRead() {
        return true;
    }
	
    public boolean canWrite() {
        // Archive entries are read-only
        return false;
    }

    public boolean isSymlink() {
        return false;
    }

    public OutputStream getOutputStream(boolean append) throws IOException {
        // Archive entries are read-only
        throw new IOException();
    }
	
    public void delete() throws IOException {
        // Archive entries are read-only
        throw new IOException();
    }

    public void mkdir(String name) throws IOException {
        // Archive entries are read-only
        throw new IOException();
    }

    public long getFreeSpace() {
        // All archive formats are read-only (for now)
        return 0;
    }

    public long getTotalSpace() {
        // We consider archive files as volumes, thus return the archive file's size
        return archiveFile.getSize();
    }

    public InputStream getInputStream() throws IOException {
        return archiveFile.getEntryInputStream(entry);
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    public String getSeparator() {
        return archiveFile.getSeparator();
    }

    public void moveTo(AbstractFile dest) throws IOException  {
        // Archive entries are read-only
        throw new IOException();
    }

    public int getMoveToHint(AbstractFile destFile) {
        return MUST_NOT_HINT;
    }

/*
    public String getName() {
        return entry.getName();
    }


    public String getAbsolutePath() {
        String path = getParent().getAbsolutePath(true)+getName();
        String separator = getSeparator();

        // Append a trailing separator character for directories
        if(isDirectory() && !path.endsWith(getSeparator()))
            return path+separator;

        return path;
    }
*/
}
