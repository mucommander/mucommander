/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.file.impl.smb;

import com.mucommander.Debug;
import com.mucommander.auth.AuthException;
import com.mucommander.auth.Credentials;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.file.FileProtocols;
import com.mucommander.file.FileURL;
import com.mucommander.io.FileTransferException;
import com.mucommander.io.RandomAccessInputStream;
import com.mucommander.io.RandomAccessOutputStream;
import jcifs.smb.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;


/**
 * SMBFile provides access to files located on an SMB/CIFS server.
 *
 * <p>The associated {@link FileURL} protocol is {@link FileProtocols#SMB}. The host part of the URL designates the
 * SMB server. Credentials are specified in the login and password parts. The path separator is '/'.
 *
 * <p>Here are a few examples of valid SMB URLs:
 * <code>
 * smb://garfield/stuff/somefile<br>
 * smb://john:p4sswd@garfield/stuff/somefile<br>
 * smb://workgroup/<br>
 * </code>
 *
 * <p>The special 'smb://' URL represents the SMB root and lists all workgroups that are available on the network,
 * akin to Windows' network neighborhood.
 *
 * <p>Access to SMB files is provided by the <code>jCIFS</code> library distributed under the LGPL license.
 * The {@link #getUnderlyingFileObject()} method allows to retrieve a <code>jcifs.smb.SmbFile</code> instance
 * corresponding to this SMBFile.
 *
 * @author Maxence Bernard
 */
 public class SMBFile extends AbstractFile {

    private SmbFile file;

    private AbstractFile parent;
    private boolean parentValSet;


    static {
        // Silence jCIFS's output if not in debug mode
        // To quote jCIFS's documentation : "0 - No log messages are printed -- not even crticial exceptions."
        if(!Debug.ON)
            System.setProperty("jcifs.util.loglevel", "0");

    }

    public SMBFile(FileURL fileURL) throws IOException {
        this(fileURL, null);
    }

    private SMBFile(FileURL fileURL, SmbFile smbFile) throws IOException {
        super(fileURL);

        if(!fileURL.containsCredentials())
            throw new AuthException(fileURL);

        if(smbFile==null) {         // Called by public constructor
            while(true) {
                file = createSmbFile(fileURL);

                // The following test comes at a cost, so it's only used by the public constructor, SmbFile instances
                // created by this class are considered OK.
                try {
                    // SmbFile requires a trailing slash for directories otherwise listFiles() will throw an SmbException.
                    // As we cannot guarantee that the path will contain a trailing slash for directories, test if the
                    // SmbFile is a directory and if it doesn't contain a trailing slash, create a new SmbFile.
                    // SmbFile.isDirectory() will throw an SmbAuthException if access to the file requires (new) credentials.
                    if(file.isDirectory() && !getURL().getPath().endsWith("/")) {
                        // Add trailing slash and loop to create a new SmbFile
                        fileURL.setPath(fileURL.getPath()+'/');
                        continue;
                    }

                    break;
                }
                catch(SmbException e) {
                    // Transform an SmbAuthException into an SmbFile exception
                    if(e instanceof SmbAuthException)
                        throw new AuthException(fileURL, e.getMessage());

                    // Re-throw the SmbException
                    throw e;
                }
            }
        }
        else {                      // Instanciated by this class
            file = smbFile;
        }
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

        // A NtlmPasswordAuthentication is created from the FileURL credentials and passed to a specific SmbFile constructor.
        // The reason for doing this rather than using the SmbFile(String) constructor is that SmbFile uses java.net.URL
        // for the URL parsing which is unable to properly parse urls where the password contains a '@' character,
        // such as smb://user:p@ssword@host/path . 
        return new SmbFile(url.toString(false), new NtlmPasswordAuthentication(null, credentials.getLogin(), credentials.getPassword()));
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

    public long getDate() {
        try {
            return file.lastModified();
        }
        catch(SmbException e) {
            return 0;
        }
    }

    public boolean canChangeDate() {
        return true;
    }

    public boolean changeDate(long lastModified) {
        try {
            file.setLastModified(lastModified);
            return true;
        }
        catch(SmbException e) {
            if(com.mucommander.Debug.ON) { com.mucommander.Debug.trace("Exception caught while changing date, returning false: "+e);}
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


    public AbstractFile getParent() throws IOException {
        if(!parentValSet) {
            FileURL parentURL = fileURL.getParent();
            // If parent URL as returned by fileURL.getParent() is null and URL's host is not null,
            // create an 'smb://' parent to browse network workgroups
            if(parentURL==null) {
                if(fileURL.getHost()!=null)
                    parentURL = new FileURL(FileProtocols.SMB+"://");
                else
                    return null;    // This file is already smb://
            }

            parent = new SMBFile(parentURL, null);
            parentValSet = true;
        }

        return parent;
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


    public boolean getPermission(int access, int permission) {
        if(access!= USER_ACCESS)
            return false;

        try {
            if(permission==READ_PERMISSION)
                return file.canRead();
            else if(permission==WRITE_PERMISSION)
                return file.canWrite();
            else
                return false;
        }
        // Unlike java.io.File, SmbFile#canRead() and SmbFile#canWrite() can throw an SmbException
        catch(SmbException e) {
            return false;
        }
    }

    public boolean setPermission(int access, int permission, boolean enabled) {
        if(access!= USER_ACCESS || permission!=WRITE_PERMISSION)
            return false;

        try {
            if(enabled)
                file.setReadWrite();
            else
                file.setReadOnly();

            return true;
        }
        catch(SmbException e) {
            return false;
        }
    }

    public boolean canGetPermission(int access, int permission) {
        return access== USER_ACCESS;    // Get permission support is limited to the user access type.
    }

    public boolean canSetPermission(int access, int permission) {
        // Set permission support is limited to the user access type, and only for the write permission flag.
        return access== USER_ACCESS && permission==WRITE_PERMISSION;
    }

    /**
     * Always returns <code>null</code>, this information is not available unfortunately.
     */
    public String getOwner() {
        return null;
    }

    /**
     * Always returns <code>false</code>, this information is not available unfortunately.
     */
    public boolean canGetOwner() {
        return false;
    }

    /**
     * Always returns <code>null</code>, this information is not available unfortunately.
     */
    public String getGroup() {
        return null;
    }

    /**
     * Always returns <code>false</code>, this information is not available unfortunately.
     */
    public boolean canGetGroup() {
        return false;
    }

    public boolean isDirectory() {
        try {
            return file.isDirectory();
        }
        catch(SmbException e) {
            return false;
        }
    }

    public boolean isSymlink() {
        // Symlinks are not supported by jCIFS (or maybe by CIFS/SMB?)
        return false;
    }

    public InputStream getInputStream() throws IOException {
        return new SmbFileInputStream(file);
    }

    public OutputStream getOutputStream(boolean append) throws IOException {
        return new SmbFileOutputStream(file, append);
    }

    public boolean hasRandomAccessInputStream() {
        return true;
    }

    public RandomAccessInputStream getRandomAccessInputStream() throws IOException {
        // This needs to be checked explicitely (SmbRandomAccessFile can be created even if the file does not exist)
        if(!exists())
            throw new IOException();

//        // Explicitely allow the file to be read/write/delete by another random access file while this one is open
//        return new SMBRandomAccessInputStream(new SmbRandomAccessFile(fileURL.toString(true), "r", SmbFile.FILE_SHARE_READ | SmbFile.FILE_SHARE_WRITE | SmbFile.FILE_SHARE_DELETE));
        return new SMBRandomAccessInputStream(new SmbRandomAccessFile(file, "r"));
    }

    public boolean hasRandomAccessOutputStream() {
        return true;
    }

    public RandomAccessOutputStream getRandomAccessOutputStream() throws IOException {
//        // Explicitely allow the file to be read/write/delete by another random access file while this one is open
//        return new SMBRandomAccessOutputStream(new SmbRandomAccessFile(fileURL.toString(true), "rw", SmbFile.FILE_SHARE_READ | SmbFile.FILE_SHARE_WRITE | SmbFile.FILE_SHARE_DELETE));
        return new SMBRandomAccessOutputStream(new SmbRandomAccessFile(file, "rw"));
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

                children[currentIndex++] = FileFactory.getFile(childURL, this);
            }

            return children;
        }
        catch(SmbAuthException e) {
            throw new AuthException(fileURL, e.getMessage());
        }
    }


    public void mkdir() throws IOException {
        // Ensure that the jcifs.smb.SmbFile's path ends with a '/' otherwise it will throw an exception
        checkSmbFile(true);

        // Note: unlike java.io.File.mkdir(), SmbFile does not return a boolean value
        // to indicate if the folder could be created
        file.mkdir();
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

    /**
     * Returns a <code>jcifs.smb.SmbFile</code> instance corresponding to this file.
     */
    public Object getUnderlyingFileObject() {
        return file;
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

    public boolean isHidden() {
        try {
            return file.isHidden();
        }
        catch(SmbException e) {
            return false;
        }
    }


    public boolean copyTo(AbstractFile destFile) throws FileTransferException {
        // File can only be copied by SMB if the destination is on an SMB share (but not necessarily on the same host)
        if(!destFile.getURL().getProtocol().equals(FileProtocols.SMB)) {
            return super.copyTo(destFile);
        }

        // If destination file is not an SMBFile nor has an SMBFile ancestor (for instance an archive entry),
        // SmbFile.copyTo() won't work so use the default copyTo() implementation instead
        destFile = destFile.getTopAncestor();
        if(!(destFile instanceof SMBFile)) {
            return super.copyTo(destFile);
        }

        // Reuse the destination SmbFile instance
        SmbFile destSmbFile = ((SMBFile)destFile).file;

        // Special tests to fail in situations where SmbFile#copyTo() does not, for instance:
        // - when the destination file exists (the destination is simply overwritten)
        // - when the source file doesn't exist
        checkCopyPrerequisites(destFile, false);

        // Everything cool, proceed with the copy
        try {
            // Copy the SMB file
            file.copyTo(destSmbFile);

            // Ensure that the destination jcifs.smb.SmbFile's path is consistent with its new directory/non-directory state
            ((SMBFile)destFile).checkSmbFile(file.isDirectory());

            return true;
        }
        catch(SmbException e) {
            throw new FileTransferException(FileTransferException.UNKNOWN_REASON);
        }
    }


    /**
     * Overrides {@link AbstractFile#moveTo(AbstractFile)} to support server-to-server move if the destination file
     * uses SMB.
     */
    public boolean moveTo(AbstractFile destFile) throws FileTransferException  {
        // File can only be moved directly if the destination if it is on an SMB share
        // (but not necessarily on the same host).
        // Use the default moveTo() implementation if the destination file doesn't use the same protocol (webdav/webdavs)
        // or is not on the same host
        if(!destFile.getURL().getProtocol().equals(FileProtocols.SMB)) {
            return super.moveTo(destFile);
        }

        // If destination file is not an SMBFile nor has an SMBFile ancestor (for instance an archive entry),
        // SmbFile.renameTo() won't work, so use the default moveTo() implementation instead
        destFile = destFile.getTopAncestor();
        if(!(destFile instanceof SMBFile)) {
            return super.moveTo(destFile);
        }

        // Special tests to fail in situations where SmbFile#renameTo() does not, for instance:
        // - when the source and destination are the same
        // - when the source file doesn't exist
        checkCopyPrerequisites(destFile, true);

        // Attempt to move the file using jcifs.smb.SmbFile#renameTo.
        try {
            boolean isDirectory = file.isDirectory();
            file.renameTo(((SMBFile)destFile).file);

            // Ensure that the destination jcifs.smb.SmbFile's path is consistent with its new directory/non-directory state
            ((SMBFile)destFile).checkSmbFile(isDirectory);

            return true;
        }
        catch(SmbException e) {
            throw new FileTransferException(FileTransferException.UNKNOWN_REASON);
        }
    }


    public boolean equals(Object f) {
        if(!(f instanceof SMBFile))
            return super.equals(f);		// could be equal to an AbstractArchiveFile

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
    public class SMBRandomAccessInputStream extends RandomAccessInputStream {

        private SmbRandomAccessFile raf;

        public SMBRandomAccessInputStream(SmbRandomAccessFile raf) {
            this.raf = raf;
        }

        public int read() throws IOException {
            return raf.read();
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

        public void seek(long offset) throws IOException {
            raf.seek(offset);
        }
    }

    /**
     * SMBRandomAccessOutputStream extends RandomAccessOutputStream to provide random write access to an SMBFile.
     */
    public class SMBRandomAccessOutputStream extends RandomAccessOutputStream {

        private SmbRandomAccessFile raf;

        public SMBRandomAccessOutputStream(SmbRandomAccessFile raf) {
            this.raf = raf;
        }

        public void write(int i) throws IOException {
            raf.write(i);
        }

        public void write(byte b[]) throws IOException {
            raf.write(b);
        }

        public void write(byte b[], int off, int len) throws IOException {
            raf.write(b, off, len);
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

        public void seek(long offset) throws IOException {
            raf.seek(offset);
        }

        public void setLength(long newLength) throws IOException {
            raf.setLength(newLength);
        }
    }
}
