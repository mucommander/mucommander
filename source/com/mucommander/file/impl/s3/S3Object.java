/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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

package com.mucommander.file.impl.s3;

import com.mucommander.auth.AuthException;
import com.mucommander.file.*;
import com.mucommander.io.BlockRandomInputStream;
import com.mucommander.io.FileTransferException;
import com.mucommander.io.FilteredOutputStream;
import com.mucommander.io.RandomAccessInputStream;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * <code>S3Object</code> represents an Amazon S3 object.
 *
 * @author Maxence Bernard
 */
public class S3Object extends S3File {

    private String bucketName;
    private S3ObjectFileAttributes atts;

    /** Maximum size of an S3 object (5GB) */
    private final static long MAX_OBJECT_SIZE = 5368709120l;


    protected S3Object(FileURL url, S3Service service, String bucketName) throws AuthException {
        super(url, service);

        this.bucketName = bucketName;
        atts = new S3ObjectFileAttributes();
    }

    protected S3Object(FileURL url, S3Service service, String bucketName, org.jets3t.service.model.S3Object object) throws AuthException {
        super(url, service);

        this.bucketName = bucketName;
        atts = new S3ObjectFileAttributes(object);
    }

    protected String getObjectKey() {
        String urlPath = fileURL.getPath();
        // Strip out the bucket name from the path
        return urlPath.substring(bucketName.length()+2, urlPath.length());
    }

    protected String getObjectKey(boolean wantTrailingSeparator) {
        String objectKey = getObjectKey();
        return wantTrailingSeparator?addTrailingSeparator(objectKey):removeTrailingSeparator(objectKey);
    }


    ///////////////////////////
    // S3File implementation //
    ///////////////////////////

    @Override
    public FileAttributes getFileAttributes() {
        return atts;
    }


    /////////////////////////////////
    // ProtocolFile implementation //
    /////////////////////////////////

    @Override
    public String getOwner() {
        return null;
    }

    @Override
    public boolean canGetOwner() {
        return false;
    }

    @Override
    public AbstractFile[] ls() throws IOException {
        return listObjects(bucketName, getObjectKey(true), this);
    }

    @Override
    public void mkdir() throws IOException {
        if(exists())
            throw new IOException();

        try {
            atts.setAttributes(service.putObject(bucketName, new org.jets3t.service.model.S3Object(getObjectKey(true))));
            atts.setExists(true);
            atts.updateExpirationDate();
        }
        catch(S3ServiceException e) {
            throw getIOException(e);
        }
    }

    @Override
    public void delete() throws IOException {
        // Note: DELETE on a non-existing resource is a successful request, so we need this check
        if(!exists())
            throw new IOException();

        try {
            // Make sure that the directory is empty, abort if not.
            // Note that we must not count the parent directory (this file).
            if(service.listObjectsChunked(bucketName, getObjectKey(isDirectory()), "/", 2, null, false).getObjects().length>=2) {
                throw new IOException("Directory not empty");
            }
            service.deleteObject(bucketName, getObjectKey(isDirectory()));

            // Update file attributes locally
            atts.setExists(false);
            atts.setDirectory(false);
            atts.setSize(0);
        }
        catch(S3ServiceException e) {
            throw getIOException(e);
        }
    }

    @Override
    public void renameTo(AbstractFile destFile) throws IOException {
        copyTo(destFile);
        delete();
    }

    @Override
    public void copyRemotelyTo(AbstractFile destFile) throws IOException {
        checkCopyRemotelyPrerequisites(destFile, true, false);

        // TODO: check bucket location

        S3Object destObjectFile = (S3Object)destFile.getAncestor(S3Object.class);
        try {
            boolean isDirectory = isDirectory();
            org.jets3t.service.model.S3Object destObject = new org.jets3t.service.model.S3Object(destObjectFile.getObjectKey(isDirectory));

            destObject.addAllMetadata(
                    service.copyObject(bucketName, getObjectKey(isDirectory), destObjectFile.bucketName, destObject, false)
            );

            // Update destination file attributes
            destObjectFile.atts.setAttributes(destObject);
            destObjectFile.atts.setExists(true);
        }
        catch(S3ServiceException e) {
            throw getIOException(e);
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return getInputStream(0);
    }

    @Override
    public InputStream getInputStream(long offset) throws IOException {
        try {
            return service.getObject(bucketName, getObjectKey(false), null, null, null, null, offset==0?null:offset, null).getDataInputStream();
        }
        catch(S3ServiceException e) {
            throw getIOException(e);
        }
    }

    @Override
    public RandomAccessInputStream getRandomAccessInputStream() throws IOException {
        if(!exists())
            throw new IOException();

        return new S3ObjectRandomAccessInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        final AbstractFile tempFile = FileFactory.getTemporaryFile(false);
        final OutputStream tempOut = tempFile.getOutputStream();

        // Update local attributes temporarily
        atts.setExists(true);
        atts.setSize(0);
        atts.setDirectory(false);

        // Return an OutputStream to a temporary file that will be copied to the S3 object when the stream is closed.
        // The object's length has to be declared in the PUT request's headers and this is the only way to do so.
        return new FilteredOutputStream(tempOut) {
            @Override
            public void close() throws IOException {
                tempOut.close();

                InputStream tempIn = tempFile.getInputStream();
                try {
                    long tempFileSize = tempFile.getSize();

                    org.jets3t.service.model.S3Object object = new org.jets3t.service.model.S3Object(getObjectKey(false));
                    object.setDataInputStream(tempIn);
                    object.setContentLength(tempFileSize);

                    // Transfer to S3 and update local file attributes
                    atts.setAttributes(service.putObject(bucketName, object));
                    atts.setExists(true);
                    atts.updateExpirationDate();
                }
                catch(S3ServiceException e) {
                    throw getIOException(e);
                }
                finally {
                    try {
                        tempIn.close();
                    }
                    catch(IOException e) {
                        // Do not re-throw the exception to prevent exceptions caught in the catch block from being replaced
                    }

                    try {
                        tempFile.delete();
                    }
                    catch(IOException e) {
                        // Do not re-throw the exception to prevent exceptions caught in the catch block from being replaced
                    }
                }
            }
        };
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    @Override
    public void copyStream(InputStream in, boolean append) throws FileTransferException {
        if(append) {
//            throw new UnsupportedFileOperationException(FileOperation.APPEND_FILE);
            throw new FileTransferException(FileTransferException.READING_SOURCE);
        }

        // If the InputStream has random access, we can upload the object directly without having to go through
        // getOutputStream() since we know the object's length already.
        if(in instanceof RandomAccessInputStream) {
            try {
                org.jets3t.service.model.S3Object object = new org.jets3t.service.model.S3Object(getObjectKey(false));
                object.setDataInputStream(in);
                try {
                    object.setContentLength(((RandomAccessInputStream)in).getLength());
                }
                catch(IOException e) {
                    throw new FileTransferException(FileTransferException.READING_SOURCE);
                }

                atts.setAttributes(service.putObject(bucketName, object));
                atts.setExists(true);
                atts.updateExpirationDate();
            }
            catch(S3ServiceException e) {
                throw new FileTransferException(FileTransferException.UNKNOWN_REASON);
            }
            finally {
                try {
                    in.close();
                }
                catch(IOException e) {
                    // Do not re-throw the exception to prevent exceptions caught in the catch block from being replaced
                }
            }
        }
        // Default to AbstractFile's implementation which calls getOutputStream()
        else {
            super.copyStream(in, append);
        }
    }

    ///////////////////
    // Inner classes //
    ///////////////////

    private class S3ObjectRandomAccessInputStream extends BlockRandomInputStream {

        /** Amount of data returned  */
        private final static int BLOCK_SIZE = 4096;

        /** Length of the S3 object */
        private long length;

        protected S3ObjectRandomAccessInputStream() {
            super(BLOCK_SIZE);

            length = getSize();
        }


        ///////////////////////////////////////////
        // BlockRandomInputStream implementation //
        ///////////////////////////////////////////

        @Override
        protected int readBlock(long fileOffset, byte[] block, int blockLen) throws IOException {
            try {
                InputStream in = service.getObject(bucketName, getObjectKey(false), null, null, null, null, fileOffset, fileOffset+BLOCK_SIZE)
                    .getDataInputStream();

                // Read up to blockLen bytes
                try {
                    int totalRead = 0;
                    int read;
                    while(totalRead<blockLen) {
                        read = in.read(block, totalRead, blockLen-totalRead);
                        if(read==-1)
                            break;

                        totalRead += read;
                    }

                    return totalRead;
                }
                finally {
                    in.close();
                }
            }
            catch(S3ServiceException e) {
                throw getIOException(e);
            }
        }

        public long getLength() throws IOException {
            return length;
        }

        @Override
        public void close() throws IOException {
            // No-op, the underlying stream is already closed
        }
    }


    /**
     * S3ObjectFileAttributes provides getters and setters for S3 object attributes. By extending
     * <code>SyncedFileAttributes</code>, this class caches attributes for a certain amount of time
     * after which fresh values are retrieved from the server.
     *
     * @author Maxence Bernard
     */
    private class S3ObjectFileAttributes extends SyncedFileAttributes {

        private final static int TTL = 60000;

        private S3ObjectFileAttributes() throws AuthException {
            super(TTL, false);      // no initial update

            fetchAttributes();      // throws AuthException if no or bad credentials
            updateExpirationDate(); // declare the attributes as 'fresh'
        }

        private S3ObjectFileAttributes(org.jets3t.service.model.S3Object object) throws AuthException {
            super(TTL, false);      // no initial update

            setAttributes(object);
            setExists(true);

            updateExpirationDate(); // declare the attributes as 'fresh'
        }

        private void setAttributes(org.jets3t.service.model.S3Object object) {
            // TODO
            setPermissions(new SimpleFilePermissions(PermissionBits.FULL_PERMISSION_INT));
            setOwner(null);
            setGroup(null);

            setDirectory(object.getKey().endsWith("/"));
            setDate(object.getLastModifiedDate().getTime());
            setSize(object.getContentLength());
        }

        private void fetchAttributes() throws AuthException {
            try {
                    setAttributes(service.getObjectDetails(bucketName, getObjectKey(), null, null, null, null));
                setExists(true);
            }
            catch(S3ServiceException e) {
                // File doesn't exist on the server
                setExists(false);
                setDirectory(false);
                setDate(0);
                setSize(0);
                // TODO
                setPermissions(new SimpleFilePermissions(PermissionBits.FULL_PERMISSION_INT));

                int code = e.getResponseCode();
                if(code==401 || code==403)
                    throw new AuthException(fileURL);
            }
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
                FileLogger.fine("Failed to update attributes", e);
            }
        }
    }
}
