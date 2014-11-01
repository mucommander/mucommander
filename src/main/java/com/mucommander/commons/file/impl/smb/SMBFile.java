/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.mucommander.commons.file.impl.smb;

import com.mucommander.commons.file.*;
import com.mucommander.commons.file.filter.FilenameFilter;
import com.mucommander.commons.io.RandomAccessInputStream;
import com.mucommander.commons.io.RandomAccessOutputStream;
import jcifs.smb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;


/**
 * SMBFile provides access to files located on an SMB/CIFS server.
 * <p>
 * The associated {@link FileURL} scheme is {@link FileProtocols#SMB}. The host part of the URL designates the
 * SMB server. Credentials are specified in the login and password parts. The path separator is '/'.
 * </p>
 * <p>
 * Here are a few examples of valid SMB URLs:
 * <code>
 * smb://server/path/to/file<br>
 * smb://domain;username:password@server/path/to/file<br>
 * smb://workgroup/<br>
 * </code>
 * </p>
 * <p>
 * The special 'smb://' URL represents the SMB root and lists all workgroups that are available on the network,
 * akin to Windows' network neighborhood.
 * </p>
 * <p>
 * Access to SMB files is provided by the <code>jCIFS</code> library distributed under the LGPL license.
 * The {@link #getUnderlyingFileObject()} method allows to retrieve a <code>jcifs.smb.SmbFile</code> instance
 * corresponding to this <code>SMBFile</code>.
 * </p>
 *
 * @author Maxence Bernard
 */
 public class SMBFile extends ProtocolFile {
    private static final Logger LOGGER = LoggerFactory.getLogger(SMBFile.class);

    private SmbFile file;
    private FilePermissions permissions;

    private AbstractFile parent;
    private boolean parentValSet;

    /** Bit mask that indicates which permissions can be changed. Only the 'write' permission for 'user' access can
     *  be changed. */
    private final static PermissionBits CHANGEABLE_PERMISSIONS = new GroupedPermissionBits(128);   // -w------- (200 octal)

    
    protected SMBFile(FileURL fileURL) throws IOException {
        this(fileURL, null);
    }

    protected SMBFile(FileURL fileURL, SmbFile smbFile) throws IOException {
        super(fileURL);

        if(!fileURL.containsCredentials())
            throw new AuthException(fileURL, "Authentication required");

        if(smbFile==null) {
            while(true) {
                file = createSmbFile(fileURL);

                // The following test comes at a cost, so it's only used by the public constructor, SmbFile instances
                // created by this class are considered OK.
                try {
                    // SmbFile requires a trailing slash for directories otherwise listFiles() will throw an SmbException.
                    // As we cannot guarantee that the path will contain a trailing slash for directories, we test if the
                    // SmbFile is a directory and if it doesn't contain a trailing slash, we create a new SmbFile with
                    // a trailing slash.
                    // SmbFile.isDirectory() will throw an SmbAuthException if access to the file requires different credentials.
                    if(file.isDirectory() && !getURL().getPath().endsWith("/")) {
                        // Add trailing slash and loop to create a new SmbFile
                        fileURL.setPath(fileURL.getPath()+'/');
                        continue;
                    }

                    break;
                }
                catch(SmbException e) {
                    // SmbFile.isDirectory() threw an exception. We distinguish 2 types of SmbException:
                    // 1) SmbAuthException, caused by a credentials problem -> turn it into an AuthException and throw it
                    // 2) any other SmbException -> this may happen if access to the file was denied for example, this
                    //    shouldn't prevent this SMBFile from being created.

                    // 1) Create an AuthException out of the SmbAuthException and throw it
                    if(e instanceof SmbAuthException)
                        throw new AuthException(fileURL, e.getMessage());

                    // 2) Swallow the exception to let this SMBFile be created
                    break;
                }
            }
        }
        else {                      // The private constructor was called directly
            file = smbFile;
        }

        permissions = new SMBFilePermissions(file);
    }


    /**
     * Creates and returns a <code>jcifs.smb.SmbFile</code> for the given location. The credentials contained by
     * the {@link FileURL} (if any) are passed along to the <code>SmbFile</code>.
     *
     * @param url the location to the SmbFile file to create
     * @return an SmbFile corresponding to the given location
     * @throws MalformedURLException if an error occurred while creating the SmbFile instance
     */
    private static SmbFile createSmbFile(FileURL url) throws MalformedURLException {
        Credentials credentials = url.getCredentials();
        if(credentials==null)
            return new SmbFile(url.toString(false));

        // Extract the domain (if any) from the username
        String login = credentials.getLogin();
        String domain;
        int domainStart = login.indexOf(";");
        if(domainStart!=-1) {
            domain = login.substring(0, domainStart);
            login = login.substring(domainStart+1, login.length());
        }
        else {
            domain = null;
        }

        // A NtlmPasswordAuthentication is created from the FileURL credentials and passed to a specific SmbFile constructor.
        // The reason for doing this rather than using the SmbFile(String) constructor is that SmbFile uses java.net.URL
        // for the URL parsing which is unable to properly parse urls where the password contains a '@' character,
        // such as smb://user:p@ssword@host/path . 
        return new SmbFile(url.toString(false), new NtlmPasswordAuthentication(domain, login, credentials.getPassword()));
    }


    /**
     * Background information: <code>jcifs.smb.SmbFile</code> is a tad cumbersome to work with because it requires its
     * file path to end with '/' when the file is a directory and vice-versa.
     * This method ensures that the path of the current <code>jcifs.smb.SmbFile</code> instance matches the
     * <code>directory</code> argument and if not, recreates it with the proper path.
     *
     * @param directory true if the current <code>jcifs.smb.SmbFile</code> designates a directory
     */
    private void checkSmbFile(boolean directory) {
        try {
            String path = file.getURL().getPath();
            boolean endsWithSeparator = path.endsWith("/");

            if(directory) {
                if(!endsWithSeparator) {
                    fileURL.setPath(path+"/");
                    file = createSmbFile(fileURL);
                }
            }
            else {
                if(endsWithSeparator) {
                    fileURL.setPath(removeTrailingSeparator(path));
                    file = createSmbFile(fileURL);
                }
            }
        }
        catch(MalformedURLException e) {
            // This should never happen. If some reason wicked reason it ever did, SmbFile would just not be changed.
        }
    }

    /**
     * Sets the time period during which attributes values (e.g. isDirectory, last modified, ...) are cached by
     * jcifs.smb.SmbFile. The higher this value, the lower the number of network requests but also the longer it takes
     * before those attributes can be refreshed.
     *
     * @param period time period during which attributes values are cached, in milliseconds
     */
    public static void setAttributeCachingPeriod(long period) {
        jcifs.Config.setProperty("jcifs.smb.client.attrExpirationPeriod", ""+period);
    }


    /////////////////////////////////////////
    // AbstractFile methods implementation //
    /////////////////////////////////////////

    @Override
    public long getDate() {
        try {
            return file.lastModified();
        }
        catch(SmbException e) {
            return 0;
        }
    }

    @Override
    public void changeDate(long lastModified) throws IOException {
        file.setLastModified(lastModified);
    }

    @Override
    public long getSize() {
        try {
            return file.length();
        }
        catch(SmbException e) {
            return 0;
        }
    }

    @Override
    public AbstractFile getParent() {
        if(!parentValSet) {
            FileURL parentURL = fileURL.getParent();
            if(parentURL!=null) {
                parent = FileFactory.getFile(parentURL);
                // Note: parent may be null if it can't be resolved
            }
            // Note: do not make the special smb:// file a parent of smb://host/, this would cause parent unit tests to fail

            parentValSet = true;
        }

        return parent;
    }

    @Override
    public void setParent(AbstractFile parent) {
        this.parent = parent;
        this.parentValSet = true;
    }

    @Override
    public boolean exists() {
        // Unlike java.io.File, SmbFile.exists() can throw an SmbException
        try {
            return file.exists();
        }
        catch(IOException e) {
            LOGGER.info("Exception caught while calling SmbFile#exists(): " +   e.getMessage());

            return e instanceof SmbAuthException;
        }
    }

    @Override
    public FilePermissions getPermissions() {
        return permissions;
    }

    @Override
    public PermissionBits getChangeablePermissions() {
        return CHANGEABLE_PERMISSIONS;
    }

    @Override
    public void changePermission(int access, int permission, boolean enabled) throws IOException {
        if(access!=USER_ACCESS || permission!=WRITE_PERMISSION)
            throw new IOException();

        if(enabled)
            file.setReadWrite();
        else
            file.setReadOnly();
    }

    /**
     * Always returns <code>null</code>, this information is not available unfortunately.
     */
    @Override
    public String getOwner() {
        return null;
    }

    /**
     * Always returns <code>false</code>, this information is not available unfortunately.
     */
    @Override
    public boolean canGetOwner() {
        return false;
    }

    /**
     * Always returns <code>null</code>, this information is not available unfortunately.
     */
    @Override
    public String getGroup() {
        return null;
    }

    /**
     * Always returns <code>false</code>, this information is not available unfortunately.
     */
    @Override
    public boolean canGetGroup() {
        return false;
    }

    @Override
    public boolean isDirectory() {
        try {
            return file.isDirectory();
        }
        catch(SmbException e) {
            return false;
        }
    }

    @Override
    public boolean isSymlink() {
        // Symlinks are not supported by jCIFS (or maybe by CIFS/SMB?)
        return false;
    }

    @Override
    public boolean isSystem() {
        return false;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new SmbFileInputStream(file);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return new SmbFileOutputStream(file, false);
    }

    @Override
    public OutputStream getAppendOutputStream() throws IOException {
        return new SmbFileOutputStream(file, true);
    }

    @Override
    public RandomAccessInputStream getRandomAccessInputStream() throws IOException {
        // This needs to be checked explicitly (SmbRandomAccessFile can be created even if the file does not exist)
        if(!exists())
            throw new IOException();

//        // Explicitly allow the file to be read/write/delete by another random access file while this one is open
//        return new SMBRandomAccessInputStream(new SmbRandomAccessFile(fileURL.toString(true), "r", SmbFile.FILE_SHARE_READ | SmbFile.FILE_SHARE_WRITE | SmbFile.FILE_SHARE_DELETE));
        return new SMBRandomAccessInputStream(new SmbRandomAccessFile(file, "r"));
    }

    @Override
    public RandomAccessOutputStream getRandomAccessOutputStream() throws IOException {
//        // Explicitly allow the file to be read/write/delete by another random access file while this one is open
//        return new SMBRandomAccessOutputStream(new SmbRandomAccessFile(fileURL.toString(true), "rw", SmbFile.FILE_SHARE_READ | SmbFile.FILE_SHARE_WRITE | SmbFile.FILE_SHARE_DELETE));
        return new SMBRandomAccessOutputStream(new SmbRandomAccessFile(file, "rw"));
    }

    @Override
    public void delete() throws IOException {
        file.delete();
        checkSmbFile(false);
    }

    @Override
    public AbstractFile[] ls() throws IOException {
        return ls(null);
    }

    @Override
    public void mkdir() throws IOException {
        // Ensure that the jcifs.smb.SmbFile's path ends with a '/' otherwise it will throw an exception
        checkSmbFile(true);

        // Note: unlike java.io.File.mkdir(), SmbFile does not return a boolean value
        // to indicate if the folder could be created
        file.mkdir();
    }

    @Override
    public void copyRemotelyTo(AbstractFile destFile) throws IOException {
        // Throw an exception if the file cannot be renamed to the specified destination.
        // This method fails in situations where SmbFile#copyTo() doesn't, for instance:
        // - when the destination file exists (the destination is simply overwritten)
        // - when the source file doesn't exist
        checkCopyRemotelyPrerequisites(destFile, false, false);

        // Reuse the destination SmbFile instance
        SmbFile destSmbFile = ((SMBFile)destFile).file;

        // Remotely copy the file
        file.copyTo(destSmbFile);

        // Ensure that the destination jcifs.smb.SmbFile's path is consistent with its new directory/non-directory state
        ((SMBFile)destFile).checkSmbFile(file.isDirectory());
    }

    /**
     * Implementation notes: server-to-server renaming will work if the destination file also uses the 'SMB' scheme.
     * Hosts do not necessarily have to be the same for this operation to succeed.
     */
    @Override
    public void renameTo(AbstractFile destFile) throws IOException {
        // Throw an exception if the file cannot be renamed to the specified destination.
        // This method fails in situations where SFTPFile#renameTo() doesn't, for instance:
        // - when the source and destination are the same
        // - when the source file doesn't exist
        checkRenamePrerequisites(destFile, true, true);

        // Attempt to move the file using jcifs.smb.SmbFile#renameTo.

        boolean isDirectory = file.isDirectory();

        // SmbFile#renameTo() throws an IOException if the destination exists (instead of overwriting the file)
        if(destFile.exists())
            destFile.delete();

        // Rename the file
        file.renameTo(((SMBFile)destFile).file);

        // Ensure that the destination jcifs.smb.SmbFile's path is consistent with its new directory/non-directory state
        ((SMBFile)destFile).checkSmbFile(isDirectory);
    }

    @Override
    public long getFreeSpace() throws IOException {
        return file.getDiskFreeSpace();
    }

    /**
     * Always throws {@link UnsupportedFileOperationException} when called.
     *
     * @throws UnsupportedFileOperationException, always
     */
    @Override
    @UnsupportedFileOperation
    public long getTotalSpace() throws UnsupportedFileOperationException {
        // No way to retrieve this information with jCIFS
        throw new UnsupportedFileOperationException(FileOperation.GET_TOTAL_SPACE);
    }

    /**
     * Returns a <code>jcifs.smb.SmbFile</code> instance corresponding to this file.
     */
    @Override
    public Object getUnderlyingFileObject() {
        return file;
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    @Override
    public AbstractFile[] ls(FilenameFilter filenameFilter) throws IOException {
        try {
            SmbFile smbFiles[] = file.listFiles(filenameFilter==null?null:new SMBFilenameFilter(filenameFilter));

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
            FileURL childURL;
            SmbFile smbFile;
            int currentIndex = 0;

            for(int i=0; i<nbSmbFiles; i++) {
                smbFile = smbFiles[i];
                smbFileType = smbFile.getType();
                if(smbFileType==SmbFile.TYPE_PRINTER || smbFileType==SmbFile.TYPE_NAMED_PIPE || smbFileType==SmbFile.TYPE_COMM)
                    continue;

                // Note: properties and credentials are cloned for every children's url
                childURL = (FileURL)fileURL.clone();
                childURL.setHost(smbFile.getServer());
                childURL.setPath(smbFile.getURL().getPath());

                // Use SMBFile private constructor to recycle the SmbFile instance
                children[currentIndex++] = FileFactory.getFile(childURL, this, smbFile);
            }

            return children;
        }
        catch(SmbAuthException e) {
            throw new AuthException(fileURL, e.getMessage());
        }
    }

    @Override
    public boolean isHidden() {
        try {
            return file.isHidden();
        }
        catch(SmbException e) {
            return false;
        }
    }


    @Override
    public boolean equalsCanonical(Object f) {
        if(!(f instanceof SMBFile))
            return super.equalsCanonical(f);		// could be equal to an AbstractArchiveFile

        // SmbFile's equals method is just perfect: compares canonical paths
        // and IP addresses
        return file.equals(((SMBFile)f).file);
    }


    ///////////////////
    // Inner classes //
    ///////////////////

    /**
     * SMBRandomAccessInputStream extends RandomAccessInputStream to provide random read access to an SMBFile.
     */
    public static class SMBRandomAccessInputStream extends RandomAccessInputStream {

        private SmbRandomAccessFile raf;

        public SMBRandomAccessInputStream(SmbRandomAccessFile raf) {
            this.raf = raf;
        }

        @Override
        public int read() throws IOException {
            return raf.read();
        }

        @Override
        public int read(byte b[], int off, int len) throws IOException {
            return raf.read(b, off, len);
        }

        @Override
        public void close() throws IOException {
            raf.close();
        }

        public long getOffset() throws IOException {
            return raf.getFilePointer();
        }

        public long getLength() throws IOException {
            return raf.length();
        }

        public void seek(long offset) throws IOException {
            raf.seek(offset);
        }
    }

    /**
     * SMBRandomAccessOutputStream extends RandomAccessOutputStream to provide random write access to an SMBFile.
     */
    public static class SMBRandomAccessOutputStream extends RandomAccessOutputStream {

        private SmbRandomAccessFile raf;

        public SMBRandomAccessOutputStream(SmbRandomAccessFile raf) {
            this.raf = raf;
        }

        @Override
        public void write(int i) throws IOException {
            raf.write(i);
        }

        @Override
        public void write(byte b[]) throws IOException {
            raf.write(b);
        }

        @Override
        public void write(byte b[], int off, int len) throws IOException {
            raf.write(b, off, len);
        }

        @Override
        public void close() throws IOException {
            raf.close();
        }

        public long getOffset() throws IOException {
            return raf.getFilePointer();
        }

        public long getLength() throws IOException {
            return raf.length();
        }

        public void seek(long offset) throws IOException {
            raf.seek(offset);
        }

        @Override
        public void setLength(long newLength) throws IOException {
            raf.setLength(newLength);

            // jCIFS doesn't automatically position the offset to the end of the file when it is truncated.
            // We have to do it ourselves to honour this method's contract.   
            if(getOffset()>newLength)
                raf.seek(newLength);
        }
    }


    /**
     * A Permissions implementation for SMBFile.
     */
    private static class SMBFilePermissions extends IndividualPermissionBits implements FilePermissions {

        private SmbFile file;

        private final static PermissionBits MASK = new GroupedPermissionBits(384);  // rw------- (300 octal)

        public SMBFilePermissions(SmbFile file) {
            this.file = file;
        }

        public boolean getBitValue(int access, int type) {
            if(access!=USER_ACCESS)
                return false;

            try {
                if(type==READ_PERMISSION)
                    return file.canRead();
                else if(type==WRITE_PERMISSION)
                    return file.canWrite();
                else
                    return false;
            }
            // Unlike java.io.File, SmbFile#canRead() and SmbFile#canWrite() can throw an SmbException
            catch(SmbException e) {
                return false;
            }
        }

        public PermissionBits getMask() {
            return MASK;
        }
    }


    /**
     * Turns a {@link FilenameFilter} into a {@link jcifs.smb.SmbFilenameFilter}.
     */
    private static class SMBFilenameFilter implements jcifs.smb.SmbFilenameFilter {

        private FilenameFilter filter;

        private SMBFilenameFilter(FilenameFilter filter) {
            this.filter = filter;
        }


        ////////////////////////////////////////////////
        // jicfs.smb.SmbFilenameFilter implementation //
        ////////////////////////////////////////////////

        public boolean accept(SmbFile dir, String name) throws SmbException {
            return filter.accept(name);
        }
    }
}
