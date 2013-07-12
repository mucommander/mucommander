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


package com.mucommander.commons.file.impl.hadoop;

import com.mucommander.commons.file.*;
import com.mucommander.commons.file.filter.FilenameFilter;
import com.mucommander.commons.io.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.fs.permission.FsPermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This abstact class provides access to the Hadoop virtual filesystem, which, like the muCommander file API, provides a
 * unified access to a number of file protocols.
 *
 * <p>{@link ProtocolFile} is fully implemented by <code>HadoopFile</code>. All is left for subclasses is to implement
 * the abstract methods defined in this class.</p>
 *
 * @see HDFSFile
 * @see S3File
 * @author Maxence Bernard
 */
public abstract class HadoopFile extends ProtocolFile {
    private static final Logger LOGGER = LoggerFactory.getLogger(HadoopFile.class);

    /** The Hadoop FileSystem object */
    private FileSystem fs;
    /** The Hadoop */
    private Path path;

    /** Holds file attributes */
    private HadoopFileAttributes fileAttributes;

    /** Cached parent file instance, null if not created yet or if this file has no parent */
    private AbstractFile parent;
    /** Has the parent file been determined yet? */
    private boolean parentValSet;

    /** True if this file is currently being written */
    private boolean isWriting;

    /** Default Hadoop Configuration, whose values are fetched from XML configuration files. */
    protected final static Configuration DEFAULT_CONFIGURATION = new Configuration();
    

    protected HadoopFile(FileURL url) throws IOException {
        this(url, null, null);
    }

    protected HadoopFile(FileURL url, FileSystem fs, FileStatus fileStatus) throws IOException {
        super(url);

        if(fs==null) {
            try {
                this.fs = getHadoopFileSystem(url);
            }
            catch(IOException e) {
                throw e;
            }
            catch(Exception e) {
                // FileSystem implementations throw IllegalArgumentException under various circumstances
                throw new IOException(e.getMessage());
            }
        }
        else {
            this.fs = fs;
        }

        if(fileStatus==null) {
            this.path = new Path(fileURL.getPath());
            this.fileAttributes = new HadoopFileAttributes();
        }
        else {
            this.fileAttributes = new HadoopFileAttributes(fileStatus);
            this.path = fileStatus.getPath();
        }
    }

    private OutputStream getOutputStream(boolean append) throws IOException {
        OutputStream out = new CounterOutputStream(
            append?fs.append(path):fs.create(path, true),
            new ByteCounter() {
                @Override
                public synchronized void add(long nbBytes) {
                    fileAttributes.addToSize(nbBytes);
                    fileAttributes.setDate(System.currentTimeMillis());
                }
            }
        ) {
            @Override
            public void close() throws IOException {
                super.close();
                isWriting = false;
            }
        };

        // Update local attributes
        fileAttributes.setExists(true);
        fileAttributes.setDate(System.currentTimeMillis());
        fileAttributes.setSize(0);

        isWriting = true;

        return out;
    }


    /////////////////////////////////
    // AbstractFile implementation //
    /////////////////////////////////

    @Override
    public AbstractFile getParent() {
        if(!parentValSet) {
            FileURL parentFileURL = this.fileURL.getParent();
            if(parentFileURL!=null)
                parent = FileFactory.getFile(fileURL.getParent());

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
    public Object getUnderlyingFileObject() {
        return fileAttributes;
    }

    // File attributes manipulation

    @Override
    public boolean exists() {
        return fileAttributes.exists();
    }

    @Override
    public boolean isDirectory() {
        return fileAttributes.isDirectory();
    }

    /**
     * Always returns <code>false</code>, Hadoop filesystems have no symlink support.
     *
     * @return returns <code>false</code>, Hadoop filesystems have no symlink support.
     */
    @Override
    public boolean isSymlink() {
        // No support for symlinks
        return false;
    }

    @Override
    public boolean isSystem() {
        return false;
    }

    @Override
    public long getDate() {
        return fileAttributes.getDate();
    }

    @Override
    public long getSize() {
        return fileAttributes.getSize();
    }

    @Override
    public PermissionBits getChangeablePermissions() {
        return FilePermissions.FULL_PERMISSION_BITS;
    }

    @Override
    public FilePermissions getPermissions() {
        return fileAttributes.getPermissions();
    }

    @Override
    public String getOwner() {
        return fileAttributes.getOwner();
    }

    @Override
    public boolean canGetOwner() {
        return true;
    }

    @Override
    public String getGroup() {
        return fileAttributes.getGroup();
    }

    @Override
    public boolean canGetGroup() {
        return true;
    }


    // Supported file operations

    @Override
    public void mkdir() throws IOException {
        if(exists() || !fs.mkdirs(path))
            throw new IOException();

        // Update local attributes
        fileAttributes.setExists(true);
        fileAttributes.setDirectory(true);
        fileAttributes.setDate(System.currentTimeMillis());
        fileAttributes.setSize(0);
    }

    @Override
    public void delete() throws IOException {
        if(!fs.delete(path, false))
            throw new IOException();

        // Update local attributes
        fileAttributes.setExists(false);
        fileAttributes.setDirectory(false);
        fileAttributes.setSize(0);
    }

    @Override
    public void renameTo(AbstractFile destFile) throws IOException {
        // Throw an exception if the file cannot be renamed to the specified destination
        checkRenamePrerequisites(destFile, false, false);

        // Delete the destination if it already exists as FileSystem#rename would otherwise fail.
        // Note: HadoopFile#delete() does not delete directories recursively (good).
        if(destFile.exists())
            destFile.delete();

        if(!fs.rename(path, ((HadoopFile)destFile).path))
            throw new IOException();

        // Update destination file attributes by fetching them from the server
        ((HadoopFileAttributes)destFile.getUnderlyingFileObject()).fetchAttributes();

        // Update this file's attributes locally
        fileAttributes.setExists(false);
        fileAttributes.setDirectory(false);
        fileAttributes.setSize(0);
    }

    @Override
    public void changeDate(long lastModified) throws IOException {
        // Note: setTimes seems to fail on HDFS directories.
        fs.setTimes(path, lastModified, lastModified);

        // Update local attributes
        fileAttributes.setDate(lastModified);
    }

    @Override
    public void changePermission(int access, int permission, boolean enabled) throws IOException {
        changePermissions(ByteUtils.setBit(getPermissions().getIntValue(), (permission << (access*3)), enabled));
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return fs.open(path);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return getOutputStream(false);
    }

    @Override
    public RandomAccessInputStream getRandomAccessInputStream() throws IOException {
        return new HadoopRandomAccessInputStream(fs.open(path), getSize());
    }

    @Override
    public AbstractFile[] ls() throws IOException {
        return ls(null);
    }


    // Unsupported file operations

    @Override
    @UnsupportedFileOperation
    public OutputStream getAppendOutputStream() throws IOException {
        // Currently not supported by any of the filesystems (S3, HDFS)
        throw new UnsupportedFileOperationException(FileOperation.APPEND_FILE);
    }

    @Override
    @UnsupportedFileOperation
    public RandomAccessOutputStream getRandomAccessOutputStream() throws IOException, UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.RANDOM_WRITE_FILE);
    }

    /**
     * Always throws {@link UnsupportedFileOperationException} when called.
     *
     * @throws UnsupportedFileOperationException, always
     */
    @Override
    @UnsupportedFileOperation
    public void copyRemotelyTo(AbstractFile destFile) throws UnsupportedFileOperationException {
        // TODO: implement for S3
        throw new UnsupportedFileOperationException(FileOperation.COPY_REMOTELY);
    }

    /**
     * Always throws {@link UnsupportedFileOperationException} when called.
     *
     * @throws UnsupportedFileOperationException, always
     */
    @Override
    @UnsupportedFileOperation
    public long getFreeSpace() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.GET_FREE_SPACE);
    }

    /**
     * Always throws {@link UnsupportedFileOperationException} when called.
     *
     * @throws UnsupportedFileOperationException, always
     */
    @Override
    @UnsupportedFileOperation
    public long getTotalSpace() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.GET_TOTAL_SPACE);
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    @Override
    public AbstractFile[] ls(FilenameFilter filter) throws IOException {
        // We need to ensure that the file is a directory: if it isn't listStatus returns an empty array but doesn't
        // throw an exception
        if(!exists() || !isDirectory())
            throw new IOException();

        FileStatus[] statuses = filter==null
                ?fs.listStatus(path)
                :fs.listStatus(path, new HadoopFilenameFilter(filter));

        int nbChildren = statuses==null?0:statuses.length;
        AbstractFile[] children = new AbstractFile[nbChildren];
        String parentPath = fileURL.getPath();
        if(!parentPath.endsWith("/"))
            parentPath += "/";
        FileURL childURL;
        FileStatus childStatus;

        for(int i=0; i<nbChildren; i++) {
            childStatus = statuses[i];

            childURL = (FileURL)fileURL.clone();
            childURL.setPath(parentPath + childStatus.getPath().getName());

            children[i] = FileFactory.getFile(childURL, this, fs, childStatus);
        }

        return children;
    }

    @Override
    public void changePermissions(int permissions) throws IOException, UnsupportedFileOperationException {
       fs.setPermission(path, new FsPermission((short)permissions));

        // Update local attributes
        fileAttributes.setPermissions(new SimpleFilePermissions(permissions));
    }


    //////////////////////
    // Abstract methods //
    //////////////////////

    /**
     * Returns a Hadoop {@link FileSystem} instance for the specified realm.
     *
     * @param realm authentication realm
     * @return a Hadoop {@link FileSystem} instance for the specified realm.
     * @throws IOException if the FileSystem failed to be instantiated
     */
    protected abstract FileSystem getHadoopFileSystem(FileURL realm) throws IOException;

    /**
     * Sets default file attributes values for the file represented by the given URL. The atributes that need to be
     * set are those that are protocol-specific.
     *
     * @param url URL of the file for which to set attributes
     * @param atts the file attributes to set
     */
    protected abstract void setDefaultFileAttributes(FileURL url, HadoopFileAttributes atts);


    ///////////////////
    // Inner classes //
    ///////////////////

    /**
     * HadoopFileAttributes provides getters and setters for Hadoop file attributes. By extending
     * <code>SyncedFileAttributes</code>, this class caches attributes for a certain amount of time
     * after which fresh values are retrieved from the server.
     */
    class HadoopFileAttributes extends SyncedFileAttributes {

        private final static int TTL = 60000;

        // this constructor is called by the public constructor
        private HadoopFileAttributes() throws AuthException {
            super(TTL, false);       // no initial update

            fetchAttributes();      // throws AuthException if no or bad credentials
            updateExpirationDate(); // declare the attributes as 'fresh'
        }

        // this constructor is called by #ls()
        private HadoopFileAttributes(FileStatus fileStatus) {
            super(TTL, false);   // no initial update

            setAttributes(fileStatus);
            setExists(true);

            updateExpirationDate(); // declare the attributes as 'fresh'
        }

        private void fetchAttributes() throws AuthException {
            // Do not update attributes while the file is being written, as they are not reflected immediately on the
            // name node.
            if(isWriting)
                return;

            try {
                setAttributes(fs.getFileStatus(path));
                setExists(true);
            }
            catch(IOException e) {
                // File doesn't exist on the server
                setExists(false);
                setDefaultFileAttributes(getURL(), this);

                // Rethrow AuthException
                if(e instanceof AuthException)
                    throw (AuthException)e;
            }
        }

        /**
         * Sets the file attributes using the values contained in the specified J2SSH FileAttributes instance.
         *
         * @param fileStatus FileStatus instance that contains the file attributes values to use
         */
        private void setAttributes(FileStatus fileStatus) {
            setDirectory(fileStatus.isDir());
            setDate(fileStatus.getModificationTime());
            setSize(fileStatus.getLen());
            setPermissions(new SimpleFilePermissions(
               fileStatus.getPermission().toShort() & PermissionBits.FULL_PERMISSION_INT
            ));
            setOwner(fileStatus.getOwner());
            setGroup(fileStatus.getGroup());
        }

        /**
         * Increments the size attribute's value by the given number of bytes.
         *
         * @param increment number of bytes to add to the current size attribute's value
         */
        private void addToSize(long increment) {
            setSize(getSize()+increment);
        }


        /////////////////////////////////////////
        // SyncedFileAttributes implementation //
        /////////////////////////////////////////

        @Override
        public void updateAttributes() {
            try {
                fetchAttributes();
            }
            catch(Exception e) {        // AuthException
                LOGGER.info("Failed to update attributes", e);
            }
        }
    }

    /**
     * Turns a Hadoop {@link FSDataInputStream} into a {@link RandomAccessInputStream}.
     */
    private static class HadoopRandomAccessInputStream extends RandomAccessInputStream {

        private FSDataInputStream in;
        private long length;

        private HadoopRandomAccessInputStream(FSDataInputStream in, long length) {
            this.in = in;
            this.length = length;
        }

        public long getOffset() throws IOException {
            return in.getPos();
        }

        public long getLength() throws IOException {
            return length;
        }

        public void seek(long offset) throws IOException {
            in.seek(offset);
        }

        @Override
        public int read() throws IOException {
            return in.read();
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return in.read(b, off, len);
        }

        @Override
        public void close() throws IOException {
        }
    }

    /**
     * Turns a {@link FilenameFilter} into a Hadoop {@link PathFilter}.
     */
    private static class HadoopFilenameFilter implements PathFilter {

        private FilenameFilter filenameFilter;

        private HadoopFilenameFilter(FilenameFilter filenameFilter) {
            this.filenameFilter = filenameFilter;
        }


        ///////////////////////////////
        // PathFilter implementation //
        ///////////////////////////////
                                       
        public boolean accept(Path path) {
            return filenameFilter.accept(path.getName());
        }
    }
}
