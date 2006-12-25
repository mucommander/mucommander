package com.mucommander.file;

import com.mucommander.Debug;
import com.mucommander.auth.AuthException;
import com.mucommander.io.FileTransferException;
import com.mucommander.io.RandomAccessInputStream;
import jcifs.smb.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * SMBFile represents a file shared through the SMB protocol.
 *
 * @author Maxence Bernard
 */
public class SMBFile extends AbstractFile {

    private SmbFile file;
//    private String privateURL;

    private AbstractFile parent;
    private boolean parentValSet;

    private boolean isDirectory;
    private boolean isDirectoryValSet;

//    private String name;

    private final static String SEPARATOR = DEFAULT_SEPARATOR;


    static {
        // Silence jCIFS's output if not in debug mode
        // To quote jCIFS's documentation : "0 - No log messages are printed -- not even crticial exceptions."
        if(!Debug.ON)
            System.setProperty("jcifs.util.loglevel", "0");
    }


    public SMBFile(FileURL fileURL) throws IOException {
//        this(fileURL, null, true);
        this(fileURL, null);

        // Forces SmbFile to trigger an SmbAuthException if access to the file requires authentication.
        // This test comes at a cost, so it's only performed in the public constructor used by AbstractFile.
        try {
            this.file.exists();
        }
        catch(SmbAuthException e) {
if(Debug.ON) Debug.trace("caught "+e);

            throw new AuthException(fileURL, e.getMessage());
        }
    }


//    private SMBFile(FileURL fileURL, SmbFile smbFile, boolean addAuthInfo) throws IOException {
    private SMBFile(FileURL fileURL, SmbFile smbFile) throws IOException {
        super(fileURL);

//        CredentialsManager.authenticate(fileURL, addAuthInfo);

//        this.privateURL = fileURL.getStringRep(true);

        // if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("privateURL= "+privateURL);

        //		// Unlike java.io.File, SmbFile throws an SmbException
        //		// when file doesn't exist.
        //		// All SMB workgroups, servers, shares, or directories URLs require a trailing slash '/'. 
        //		// Regular SMB files can have a trailing slash as well, so let's add a trailing slash.
        //		this.file = new SmbFile(privateURL.endsWith("/")?privateURL:privateURL+"/");

//if(Debug.ON) Debug.trace("fileURL="+fileURL.getStringRep(true));

        if(smbFile==null)
//            this.file = new SmbFile(privateURL);
            this.file = new SmbFile(fileURL.getStringRep(true));
        else
            this.file = smbFile;

//        // Cache SmbFile.getName()'s return value which parses name each time it is called
//        this.name = file.getName();
//        if(name.endsWith("/"))
//            name = name.substring(0, name.length()-1);
    }


    /////////////////////////////////////////
    // AbstractFile methods implementation //
    /////////////////////////////////////////

    public long getDate() {
        try {
            return file.lastModified();
        }
        catch(SmbException e) {
            return 0;
        }
    }

    public boolean changeDate(long lastModified) {
        try {
            // SmbFile.setLastModified() returns "jcifs.smb.SmbAuthException: Access is denied" exceptions
            // don't know if it's a bug in the library or a server limitation (tested with Samba)
            file.setLastModified(lastModified);
            return true;
        }
        catch(SmbException e) {
            if(com.mucommander.Debug.ON) { com.mucommander.Debug.trace("return false "+e);}
            return false;
        }
    }

    public long getSize() {
        try {
            return file.length();
        }
        catch(SmbException e) {
            return 0;
        }
    }


    public AbstractFile getParent() {
        if(!parentValSet) {
            try {
                FileURL parentURL = fileURL.getParent();
                // If parent URL as returned by fileURL.getParent() is null and URL's host is not null,
                // create an 'smb://' parent to browse network workgroups
                if(parentURL==null) {
                    if(fileURL.getHost()!=null)
                        parentURL = new FileURL(FileProtocols.SMB+"://");
                    else
                        return null;    // This file is already smb://
                }

//                this.parent = new SMBFile(parentURL, null, false);
//                parentURL.setCredentials(fileURL.getCredentials());
                this.parent = new SMBFile(parentURL, null);

                return parent;
            }
            catch(IOException e) {
                // this.parent and returned parent will be null
            }
            finally {
                this.parentValSet = true;
            }
        }

        return this.parent;
    }

    public void setParent(AbstractFile parent) {
        this.parent = parent;
        this.parentValSet = true;
    }

    public boolean exists() {
        // Unlike java.io.File, SmbFile.exists() can throw an SmbException
        try {
            return file.exists();
        }
        catch(IOException e) {
            if(e instanceof SmbAuthException)
                return true;
            return false;
        }

    }

    public boolean canRead() {
        // Unlike java.io.File, SmbFile.canRead() can throw an SmbException
        try {
            return file.canRead();
        }
        catch(SmbException e) {
            return false;
        }
    }

    public boolean canWrite() {
        // Unlike java.io.File, SmbFile.canWrite() can throw an SmbException
        try {
            return file.canWrite();
        }
        catch(SmbException e) {
            return false;
        }
    }

    public boolean canExecute() {
        // No such thing in SMB, return false
        return false;
    }


    public boolean setReadable(boolean readable) {
        return false;
    }

    public boolean setWritable(boolean writable) {
        try {
            if(writable)
                file.setReadWrite();
            else
                file.setReadOnly();

            return true;
        }
        catch(SmbException e) {
            return false;
        }
    }

    public boolean setExecutable(boolean executable) {
        return false;
    }

    public boolean canSetPermissions() {
        return true;    // return true even though file permissions handling is not partial (writable permissions only)       
    }

    public boolean isDirectory() {
        // Cache SmbFile.isDirectory()'s return value as this method triggers network calls
        // (calls exists() which checks file existence on the server) and will report
        // false if connection is lost.
        if(!isDirectoryValSet) {
            try {
                this.isDirectory = file.isDirectory();
                this.isDirectoryValSet = true;
            }
            catch(SmbException e) {
                return false;
            }
        }
        return this.isDirectory;
    }

    public boolean isSymlink() {
        // Symlinks are not supported with jCIFS (or in CIFS/SMB?)
        return false;
    }

    public InputStream getInputStream() throws IOException {
//        return new SmbFileInputStream(privateURL);
        return new SmbFileInputStream(getURL().getStringRep(true));
    }

    public RandomAccessInputStream getRandomAccessInputStream() throws IOException {
        return new SMBRandomAccessInputStream(new SmbRandomAccessFile(file, "r"));
    }

    public OutputStream getOutputStream(boolean append) throws IOException {
//        return new SmbFileOutputStream(privateURL, append);
        return new SmbFileOutputStream(getURL().getStringRep(true), append);
    }

    public void delete() throws IOException {
        file.delete();
    }


    public AbstractFile[] ls() throws IOException {
        try {
            SmbFile smbFiles[] = file.listFiles();

            if(smbFiles==null)
                throw new IOException();

            // Count the number of files to exclude: excluded files are those that are not file share/ not browsable
            // (Printers, named pipes, comm ports)
            int nbSmbFiles = smbFiles.length;
            int nbSmbFilesToExclude = 0;
            int smbFileType;
            for(int i=0; i<nbSmbFiles; i++) {
                smbFileType = smbFiles[i].getType();
                if(smbFileType==SmbFile.TYPE_PRINTER || smbFileType==SmbFile.TYPE_NAMED_PIPE || smbFileType==SmbFile.TYPE_COMM)
                    nbSmbFilesToExclude++;
            }

            // Create SMBFile by using SmbFile instance and sharing parent instance among children
            AbstractFile children[] = new AbstractFile[nbSmbFiles-nbSmbFilesToExclude];
            AbstractFile child;
            FileURL childURL;
            SmbFile smbFile;
            int currentIndex = 0;
            for(int i=0; i<nbSmbFiles; i++) {
                smbFile = smbFiles[i];
                smbFileType = smbFile.getType();
                if(smbFileType==SmbFile.TYPE_PRINTER || smbFileType==SmbFile.TYPE_NAMED_PIPE || smbFileType==SmbFile.TYPE_COMM)
                    continue;
                
//                child = FileFactory.wrapArchive(new SMBFile(new FileURL(smbFile.getCanonicalPath(), fileURL), smbFile, false));
                childURL = new FileURL(smbFile.getCanonicalPath(), fileURL);

                child = FileFactory.wrapArchive(new SMBFile(childURL, smbFile));
                child.setParent(this);
                children[currentIndex++] = child;
            }

            return children;
        }
        catch(SmbAuthException e) {
//if(Debug.ON) Debug.trace("caught "+e);
            
            throw new AuthException(fileURL, e.getMessage());
        }
    }


    public void mkdir(String name) throws IOException {
        // Unlike java.io.File.mkdir(), SmbFile does not return a boolean value
        // to indicate if the folder could be created
//        new SmbFile(privateURL+SEPARATOR+name).mkdir();
        new SmbFile(getURL().getStringRep(true)+SEPARATOR+name).mkdir();
    }


    public long getFreeSpace() {
        try {
            return file.getDiskFreeSpace();
        }
        catch(SmbException e) {
            // Error occured, return -1 (not available)
            return -1;
        }
    }

    public long getTotalSpace() {
        // No way to retrieve this information with jCIFS/SMB, return -1 (not available)
        return -1;
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    public boolean isHidden() {
        try {
            return file.isHidden();
        }
        catch(SmbException e) {
            return false;
        }
    }


    public void copyTo(AbstractFile destFile) throws FileTransferException {
        // File can only be copied by SMB if the destination is on an SMB share (but not necessarily on the same host)
        if(!destFile.fileURL.getProtocol().equals(FileProtocols.SMB)) {
            super.copyTo(destFile);
            return;
        }

        // If file is an archive file, retrieve the enclosed file, which is likely to be an SMBFile but not necessarily
        // (may be an ArchiveEntryFile)
        if(destFile instanceof AbstractArchiveFile)
            destFile = ((AbstractArchiveFile)destFile).getProxiedFile();

        // If destination file is not an SMBFile (for instance an archive entry), SmbFile.copyTo() won't work
        // so use the default copyTo() implementation instead
        if(!(destFile instanceof SMBFile)) {
            super.copyTo(destFile);
            return;
        }

        // Reuse the destination SmbFile instance
        SmbFile destSmbFile = ((SMBFile)destFile).file;

        try {
            // Copy the SMB file
            file.copyTo(destSmbFile);
        }
        catch(SmbException e) {
            throw new FileTransferException(FileTransferException.UNKNOWN_REASON);    // Report that the copy failed
        }
    }


    /**
     * Overrides {@link AbstractFile#moveTo(AbstractFile)} to support server-to-server move if the destination file
     * uses SMB.
     */
    public void moveTo(AbstractFile destFile) throws FileTransferException  {
        // File can only be moved directly if the destination if it is on an SMB share
        // (but not necessarily on the same host).
        // Use the default moveTo() implementation if the destination file doesn't use the same protocol (webdav/webdavs)
        // or is not on the same host
        if(!destFile.fileURL.getProtocol().equals(FileProtocols.SMB)) {
            super.moveTo(destFile);
            return;
        }

        // If file is an archive file, retrieve enclosed file, which is likely to be an SMBFile but not necessarily
        // (may be an ArchiveEntryFile)
        if(destFile instanceof AbstractArchiveFile)
            destFile = ((AbstractArchiveFile)destFile).getProxiedFile();

        // If destination file is not an SMBFile (for instance an archive entry), SmbFile.renameTo() won't work,
        // so use the default moveTo() implementation instead
        if(!(destFile instanceof SMBFile)) {
            super.moveTo(destFile);
            return;
        }

        // Move file
        try {
            file.renameTo(((SMBFile)destFile).file);
        }
        catch(SmbException e) {
            throw new FileTransferException(FileTransferException.UNKNOWN_REASON);    // Report that move failed
        }
    }


    public boolean equals(Object f) {
        if(!(f instanceof SMBFile))
            return super.equals(f);		// could be equal to a ZipArchiveFile

        // SmbFile's equals method is just perfect: compares canonical paths
        // and IP addresses
        return file.equals(((SMBFile)f).file);
    }


    /**
     * SMBRandomAccessInputStream extends RandomAccessInputStream to provide random access to an <code>SMBFile</code>'s
     * content.
     */
    public class SMBRandomAccessInputStream extends RandomAccessInputStream {

        private SmbRandomAccessFile raf;

        public SMBRandomAccessInputStream(SmbRandomAccessFile raf) {
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
