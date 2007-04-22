package com.mucommander.file.impl.nfs;

import com.mucommander.file.*;
import com.mucommander.file.filter.FilenameFilter;
import com.mucommander.io.FileTransferException;
import com.mucommander.io.RandomAccessInputStream;
import com.mucommander.process.AbstractProcess;
import com.sun.xfile.XFile;
import com.sun.xfile.XFileInputStream;
import com.sun.xfile.XFileOutputStream;
import com.sun.xfile.XRandomAccessFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * NFSFile provides access to files located on an NFS/WebNFS server.
 *
 * <p>The associated {@link FileURL} protocol is {@link FileProtocols#NFS}. The host part of the URL designates the
 * NFS server. The path separator is '/'.
 *
 * <p>Here are a few examples of valid NFS URLs:
 * <code>
 * nfs://garfield/stuff/<br>
 * nfs://192.168.1.1/stuff/somefile<br>
 * </code>
 *
 * <p>Access to NFS files is provided by the <code>Yanfs</code> library (formerly WebNFS) distributed under the BSD
 * license. The {@link #getUnderlyingFileObject()} method allows to retrieve a <code>com.sun.xfile.XFile</code> instance
 * corresponding to this NFSFile.
 *
 * <p><b>Important:</b> this class has received only limited testing and is thus considered as EXPERIMENTAL.
 * In particular, it is not clear at this time whether the Yanfs library is able to access regular NFS servers, or only
 * the servers that declare a public filehandle. See RFC 2224 for more info about NFS URLs and the use of
 * public filehandles.
 *
 * @author Maxence Bernard
 */
public class NFSFile extends AbstractFile {

    /** Underlying file instance */
    private XFile file;

    private String absPath;

    /** Caches the parent folder, initially null until getParent() gets called */
    private AbstractFile parent;
    /** Indicates whether the parent folder instance has been retrieved and cached or not (parent can be null) */
    private boolean parentValueSet;

    public final static String SEPARATOR = "/";


    /**
     * Creates a new instance of LocalFile.
     */
    public NFSFile(FileURL fileURL) {
        super(fileURL);

        this.file = new XFile(fileURL.toString(false));

        this.absPath = file.getAbsolutePath();
        // removes trailing separator (if any)
        this.absPath = absPath.endsWith(SEPARATOR)?absPath.substring(0,absPath.length()-1):absPath;

    }


    /////////////////////////////////////////
    // AbstractFile methods implementation //
    /////////////////////////////////////////

    public long getDate() {
        return file.lastModified();
    }

    public boolean changeDate(long lastModified) {
        // XFile has no method for that purpose
        return false;
    }

    public long getSize() {
        return file.length();
    }

    public AbstractFile getParent() {
        // Retrieve parent AbstractFile and cache it
        if (!parentValueSet) {
            FileURL parentURL = getURL().getParent();
            if(parentURL != null) {
                parent = FileFactory.getFile(parentURL);
            }
            parentValueSet = true;
        }
        return parent;
    }

    public void setParent(AbstractFile parent) {
        this.parent = parent;
        this.parentValueSet = true;
    }

    public boolean exists() {
        return file.exists();
    }

    public boolean getPermission(int access, int permission) {
        if(access!= USER_ACCESS)
            return false;

        if(permission==READ_PERMISSION)
            return file.canRead();
        else if(permission==WRITE_PERMISSION)
            return file.canWrite();

        return false;
    }

    public boolean setPermission(int access, int permission, boolean enabled) {
        // XFile has no method for that purpose
        return false;
    }

    public boolean canGetPermission(int access, int permission) {
        // Read and write permissions for 'user' access
        return access==USER_ACCESS && (permission==READ_PERMISSION || permission==WRITE_PERMISSION);
    }

    public boolean canSetPermission(int access, int permission) {
        // XFile has no method for that purpose
        return false;
    }

    public boolean isDirectory() {
        return file.isDirectory();
    }

    public boolean isSymlink() {
        // Note: this value must not be cached as its value can change over time (canonical path can change)
        NFSFile parent = (NFSFile)getParent();
        String canonPath = getCanonicalPath(false);
        if(parent==null || canonPath==null)
            return false;
        else {
            String parentCanonPath = parent.getCanonicalPath(true);
            return !canonPath.equals(parentCanonPath+getName());
        }
    }

    public AbstractFile[] ls() throws IOException {
        return ls((FilenameFilter)null);
    }

    public void mkdir(String name) throws IOException {
        if(!new File(absPath+SEPARATOR+name).mkdir())
            throw new IOException();
    }

    public InputStream getInputStream() throws IOException {
        return new XFileInputStream(file);
    }

    public RandomAccessInputStream getRandomAccessInputStream() throws IOException {
        return new NFSRandomAccessInputStream(new XRandomAccessFile(file, "r"));
    }

    public OutputStream getOutputStream(boolean append) throws IOException {
        return new XFileOutputStream(absPath, append);
    }

    public void delete() throws IOException {
        boolean ret = file.delete();

        if(!ret)
            throw new IOException();
    }

    public long getFreeSpace() {
        // XFile has no method to provide that information
        return -1;
    }

    public long getTotalSpace() {
        // XFile has no method to provide that information
        return -1;
    }

    /**
     * Returns a <code>com.sun.xfile.XFile</code> instance corresponding to this file.
     */
    public Object getUnderlyingFileObject() {
        return file;
    }


    public boolean canRunProcess() {
        return false;
    }

    public AbstractProcess runProcess(String[] tokens) throws IOException {
        throw new IOException();
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    public String getCanonicalPath() {
        try {
            return file.getCanonicalPath();
        }
        catch(IOException e) {
            return absPath;
        }
    }


    public AbstractFile[] ls(FilenameFilter filenameFilter) throws IOException {
        String names[] = file.list();

        if(names==null)
            throw new IOException();

        if(filenameFilter!=null)
            names = filenameFilter.filter(names);

        AbstractFile children[] = new AbstractFile[names.length];
        for(int i=0; i<names.length; i++) {
            // Retrieves an AbstractFile (NFSFile or archive) and reuse this file as parent
            children[i] = FileFactory.getFile(absPath+SEPARATOR+names[i], this);
        }

        return children;
    }


    /**
     * Overrides {@link AbstractFile#moveTo(AbstractFile)} to move/rename the file directly if the destination file
     * is also an NFSFile.
     */
    public boolean moveTo(AbstractFile destFile) throws FileTransferException {
        if(!destFile.getURL().getProtocol().equals(FileProtocols.NFS)) {
            return super.moveTo(destFile);
        }

        // If file is an archive file, retrieve the enclosed file, which is likely to be an NFSFile but not necessarily
        // (may be an ArchiveEntryFile)
        if(destFile instanceof AbstractArchiveFile)
            destFile = ((AbstractArchiveFile)destFile).getProxiedFile();

        // If destination file is not a LocalFile (for instance an archive entry), renaming won't work
        // so use the default moveTo() implementation instead
        if(!(destFile instanceof NFSFile)) {
            return super.moveTo(destFile);
        }

        // Move file
        return file.renameTo(((NFSFile)destFile).file);
    }


    /**
     * Overridden for performance reasons.
     */
    public int getPermissionGetMask() {
        return 384;       // rw------- (300 octal)
    }

    /**
     * Overridden for performance reasons.
     */
    public int getPermissionSetMask() {
        return 0;         // --------- (0 octal)
    }


    ///////////////////
    // Inner classes //
    ///////////////////

    /**
     * NFSRandomAccessInputStream extends RandomAccessInputStream to provide random access to a
     *  <code>NFSFile</code>'s content.
     */
    public class NFSRandomAccessInputStream extends RandomAccessInputStream {

        private XRandomAccessFile raf;

        public NFSRandomAccessInputStream(XRandomAccessFile raf) {
            this.raf = raf;
        }

        public int read() throws IOException {
            return raf.read();
        }

        public int read(byte b[]) throws IOException {
            return raf.read(b);
        }

        public int read(byte b[], int off, int len) throws IOException {
            return raf.read(b, off, len);
        }

        public void close() throws IOException {
            raf.close();
        }

        public long getOffset() throws IOException {
            return raf.getFilePointer();
        }

        public long getLength() throws IOException {
            return raf.length();
        }

        public void seek(long pos) throws IOException {
            raf.seek(pos);
        }
    }
}