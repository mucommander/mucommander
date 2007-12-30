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

package com.mucommander.file.impl.nfs;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.file.FileProtocols;
import com.mucommander.file.FileURL;
import com.mucommander.file.filter.FilenameFilter;
import com.mucommander.io.FileTransferException;
import com.mucommander.io.RandomAccessInputStream;
import com.mucommander.io.RandomAccessOutputStream;
import com.mucommander.process.AbstractProcess;
import com.sun.xfile.XFile;
import com.sun.xfile.XFileInputStream;
import com.sun.xfile.XFileOutputStream;
import com.sun.xfile.XRandomAccessFile;

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
 * nfs://192.168.1.1:2049/stuff/somefile<br>
 * </code>
 *
 * <p>Access to NFS files is provided by the <code>Yanfs</code> library (formerly WebNFS) distributed under the BSD
 * license. The {@link #getUnderlyingFileObject()} method allows to retrieve a <code>com.sun.xfile.XFile</code> instance
 * corresponding to this NFSFile.
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

    /** Name of the NFS version property */
    public final static String NFS_VERSION_PROPERTY_NAME = "version";

    /** NFS version 2 */
    public final static String NFS_VERSION_2 = "v2";

    /** NFS version 3 */
    public final static String NFS_VERSION_3 = "v3";

    /** Default NFS version */
    public final static String DEFAULT_NFS_VERSION = NFS_VERSION_2;

    /** Name of the NFS transport protocol property */
    public final static String NFS_PROTOCOL_PROPERTY_NAME = "protocol";

    /** 'Auto' transport protocol: TCP is tried first and if the connection cannot be established, falls back to UDP */
    public final static String NFS_PROTOCOL_AUTO = "Auto";

    /** TCP transport protocol */
    public final static String NFS_PROTOCOL_TCP = "TCP";

    /** UDP transport protocol */
    public final static String NFS_PROTOCOL_UDP = "UDP";

    /** Default transport protocol */
    public final static String DEFAULT_NFS_PROTOCOL = NFS_PROTOCOL_AUTO;


    /**
     * Creates a new instance of NFSFile.
     */
    public NFSFile(FileURL fileURL) {
        super(fileURL);

        // Create the NFS URL used by XFile.

        // The general syntax for NFS URLs is : nfs://<host>:<port><url-path>, as specified by RFC 2054
        // Additionaly, XFile allows some special flags to be used in the port part of the URL to specify connection
        // properties. Those flags must be placed after the port, and before the colon character delimiting the end of
        // the port part.
        // Here's the list of allowed flags (quoted from com.sun.nfs.NfsURL):
        // vn	- NFS version, e.g. "v3"
        // u	- Force UDP - normally TCP is preferred
        // t	- Force TDP - don't fall back to UDP
        // m    - Force Mount protocol.  Normally public filehandle is preferred
        //
        // Example: nfs://server:123v2um/path : use port 123 with NFS v2 over UDP and Mount protocol
        //
        // The 'm' flag must be specified, otherwise regular NFS shares (i.e. non WebNFS-enabled ones) that don't
        // specify a public filehandle will fail. However, using this flag has two unfortunate consequences:
        // - the NFS version fails to be properly negociated as it normally does (try v3 then fall back on v2): the
        //  NFS version must be specified in the URL.
        // - an extra slash character must be added before the path part, otherwise it is considered as relative to
        //  the public filehandle and will thus fail to resolve.
        //
        // These issues might get fixed in Yanfs someday. When that happens, this code might be simplified.

        // Determines the NFS version (v2 or v3) to be used, based on the version property
        String nfsVersion = fileURL.getProperty(NFS_VERSION_PROPERTY_NAME);
        if(nfsVersion==null)
            nfsVersion = DEFAULT_NFS_VERSION;

        // Determines the NFS transport protocol (Auto, TCP or UDP) to be used, based on the protocol property
        String nfsProtocol = fileURL.getProperty(NFS_PROTOCOL_PROPERTY_NAME);
        nfsProtocol = NFS_PROTOCOL_TCP.equals(nfsProtocol)?"t":NFS_PROTOCOL_UDP.equals(nfsProtocol)?"u":"";

        // Omit port part if none is contained in the FileURL or if it is 2049
        int port = fileURL.getPort();
        String portString = port==-1||port==2049?"":""+port;

        // Create the XFile instance with the weird NFS url
        this.file = new XFile("nfs://"+fileURL.getHost()+":"+portString+nfsVersion+nfsProtocol+"m"+"/"+fileURL.getPath());

        // Retrieve the absolute path from the FileURL and NOT from the XFile instance which will return those weird flags
        this.absPath = fileURL.toString();
        // Remove trailing separator (if any)
        this.absPath = absPath.endsWith(SEPARATOR)?absPath.substring(0,absPath.length()-1):absPath;
    }


    /////////////////////////////////////////
    // AbstractFile methods implementation //
    /////////////////////////////////////////

    public long getDate() {
        return file.lastModified();
    }

    /**
     * Always returns <code>false</code> (date cannot be changed).
     */
    public boolean canChangeDate() {
        return false;
    }

    /**
     * Always returns <code>false</code> (date cannot be changed).
     */
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
        if(access!=USER_ACCESS)
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
        return file.isDirectory();
    }

    /**
     * Always returns <code>false</code> (symlinks are not detected).
     */
    public boolean isSymlink() {
        // Yanfs is unable to detect symlinks at this time
        return false;
    }

    public AbstractFile[] ls() throws IOException {
        return ls((FilenameFilter)null);
    }

    public void mkdir() throws IOException {
        if(!new XFile(absPath).mkdir())
            throw new IOException();
    }

    public InputStream getInputStream() throws IOException {
        return new XFileInputStream(file);
    }

    public OutputStream getOutputStream(boolean append) throws IOException {
        return new XFileOutputStream(absPath, append);
    }

    /**
     * Returns <code>true</code>: {@link #getRandomAccessInputStream()} is implemented.
     *
     * @return true
     */
    public boolean hasRandomAccessInputStream() {
        return true;
    }

    public RandomAccessInputStream getRandomAccessInputStream() throws IOException {
        return new NFSRandomAccessInputStream(new XRandomAccessFile(file, "r"));
    }

    /**
     * Returns <code>false</code>: {@link #getRandomAccessOutputStream()} is implemented but the returned
     * <code>RandomAccessOutputStream</code> is not fully functional.
     *
     * @return false
     */
    public boolean hasRandomAccessOutputStream() {
        return false;
    }

    /**
     * <b>Warning:</b> the returned {@link com.mucommander.file.impl.nfs.NFSFile.NFSRandomAccessOutputStream} instance
     * is not fully functional, its {@link com.mucommander.file.impl.nfs.NFSFile.NFSRandomAccessOutputStream#setLength(long)}
     * method has a limitation.
     *
     * @return a RandomAccessOutputStream that is not fully functional
     * @throws IOException if the file could not be opened for random write access
     */
    public RandomAccessOutputStream getRandomAccessOutputStream() throws IOException {
        return new NFSRandomAccessOutputStream(new XRandomAccessFile(file, "rw"));
    }

    public void delete() throws IOException {
        boolean ret = file.delete();

        if(!ret)
            throw new IOException();
    }

    /**
     * Always returns <code>-1</code> (not available)
     */
    public long getFreeSpace() {
        // XFile has no method to provide that information
        return -1;
    }

    /**
     * Always returns <code>-1</code> (not available)
     */
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

    /**
     * Always returns <code>false</code>.
     */
    public boolean canRunProcess() {
        return false;
    }

    /**
     * Always throws an <code>IOException</code>.
     */
    public AbstractProcess runProcess(String[] tokens) throws IOException {
        throw new IOException();
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    public AbstractFile[] ls(FilenameFilter filenameFilter) throws IOException {
        String names[] = file.list();

        if(names==null)
            throw new IOException();

        if(filenameFilter!=null)
            names = filenameFilter.filter(names);

        AbstractFile children[] = new AbstractFile[names.length];
        FileURL childURL;
        NFSFile child;
        String baseURLPath = fileURL.getPath();
        if(!baseURLPath.endsWith("/"))
            baseURLPath += SEPARATOR;

        for(int i=0; i<names.length; i++) {
            // Clone this file's URL with the connection properties and set the child file's path
            childURL = (FileURL)fileURL.clone();
            childURL.setPath(baseURLPath+names[i]);

            // Create the child NFSFile using this file as a parent
            child = new NFSFile(childURL);
            child.setParent(this);

            // Wrap archives
            children[i] = FileFactory.wrapArchive(child);
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

        // If destination file is not an NFSFile nor has an NFSFile ancestor (for instance an archive entry),
        // server renaming won't work so use the default moveTo() implementation instead
        destFile = destFile.getTopAncestor();
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
     * NFSRandomAccessInputStream extends RandomAccessInputStream to provide random read access to an NFSFile.
     */
    public static class NFSRandomAccessInputStream extends RandomAccessInputStream {

        private XRandomAccessFile raf;

        public NFSRandomAccessInputStream(XRandomAccessFile raf) {
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
     * NFSRandomAccessOutputStream extends RandomAccessOutputStream to provide random write access to an NFSFile.
     *
     * <p><b>Warning:</b> this RandomAccessOutputStream is not fully functional, the {@link #setLength(long)} has a
     * limitation.
     */
    public static class NFSRandomAccessOutputStream extends RandomAccessOutputStream {

        private XRandomAccessFile raf;

        public NFSRandomAccessOutputStream(XRandomAccessFile raf) {
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

        /**
         * <b>Warning:</b> this method is only capable of expanding the file, not truncating it.
         * It will throw an <code>IOException</code> whenever the <code>newLength</code> parameter is greater than
         * the current length reported by {@link #getLength()}.
         *
         * @param newLength the new file's length
         * @throws IOException If an I/O error occurred while trying to change the file's length
         */
        public void setLength(long newLength) throws IOException {
            // This operation is supported only if the new length is greater (or equal) than the current length
            long currentLength = getLength();
            if(newLength<currentLength)
                throw new IOException();

            if(newLength==currentLength)
                return;

            // Extend the file's length by seeking to the end and writing a byte
            seek(newLength-1);
            write(0);
        }
    }
}
