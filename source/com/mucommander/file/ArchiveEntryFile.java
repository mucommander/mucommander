package com.mucommander.file;

import com.mucommander.file.filter.FileFilter;
import com.mucommander.file.filter.FilenameFilter;
import com.mucommander.io.FileTransferException;
import com.mucommander.io.RandomAccessInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


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
	
    public boolean getPermission(int access, int permission) {
        return (getPermissions() & (permission << (access*3))) != 0;
    }

    public boolean setPermission(int access, int permission, boolean enabled) {
        // Permissions cannot be changed
        return false;
    }

    public boolean canGetPermission(int access, int permission) {
        // Use entry's permissions mask
        return (entry.getPermissionsMask() & (permission << (access*3))) != 0;
    }

    public boolean canSetPermission(int access, int permission) {
        // Permissions cannot be changed
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

    public RandomAccessInputStream getRandomAccessInputStream() throws IOException {
        // No random access for archive entries unfortunately
        throw new IOException();
    }

    /**
     * Returns the same ArchiveEntry instance as {@link #getEntry()}.
     */
    public Object getUnderlyingFileObject() {
        return entry;
    }

    public boolean canRunProcess() {
        return false;
    }

    public com.mucommander.process.AbstractProcess runProcess(String[] tokens) throws IOException {
        throw new IOException();
    }
    
    ////////////////////////
    // Overridden methods //
    ////////////////////////

    public int getPermissions() {
        // Return entry's permissions mask
        return entry.getPermissions();
    }
    
    public String getSeparator() {
        return archiveFile.getSeparator();
    }

    public boolean moveTo(AbstractFile dest) throws FileTransferException {
        // Archive entries are read-only
        throw new FileTransferException(FileTransferException.UNKNOWN_REASON);
    }

    public int getMoveToHint(AbstractFile destFile) {
        return MUST_NOT_HINT;
    }
}
