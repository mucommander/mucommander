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


package com.mucommander.commons.file.impl.s3;

import com.mucommander.commons.file.*;
import com.mucommander.commons.io.BufferPool;
import com.mucommander.commons.io.FileTransferException;
import com.mucommander.commons.io.RandomAccessInputStream;
import com.mucommander.commons.io.StreamUtils;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.model.S3Owner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * <code>S3Object</code> represents an Amazon S3 object.
 *
 * @author Maxence Bernard
 */
public class S3Object extends S3File {
    private static final Logger LOGGER = LoggerFactory.getLogger(S3Object.class);

    private String bucketName;
    private S3ObjectFileAttributes atts;

    /** Maximum size of an S3 object (5GB) */
    private final static long MAX_OBJECT_SIZE = 5368709120l;

    // TODO: add support for ACL ? (would cost an extra request per object)
    /** Default permissions for S3 objects */
    private final static FilePermissions DEFAULT_PERMISSIONS = new SimpleFilePermissions(384);   // rw-------


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

    private String getObjectKey() {
        String urlPath = fileURL.getPath();
        // Strip out the bucket name from the path
        return urlPath.substring(bucketName.length()+2, urlPath.length());
    }

    private String getObjectKey(boolean wantTrailingSeparator) {
        String objectKey = getObjectKey();
        return wantTrailingSeparator?addTrailingSeparator(objectKey):removeTrailingSeparator(objectKey);
    }

    /**
     * Uploads the object contained in the given input stream to S3 by performing a 'PUT Object' request.
     * The input stream is always closed, whether the operation failed or succeeded.
     *
     * @param in the stream that contains the object to be uploaded
     * @param objectLength length of the object
     * @throws FileTransferException if an error occurred during the transfer
     */
    private void putObject(InputStream in, long objectLength) throws FileTransferException {
        try {
            // Init S3 object
            org.jets3t.service.model.S3Object object = new org.jets3t.service.model.S3Object(getObjectKey(false));
            object.setDataInputStream(in);
            object.setContentLength(objectLength);

            // Transfer to S3 and update local file attributes
            atts.setAttributes(service.putObject(bucketName, object));
            atts.setExists(true);
            atts.updateExpirationDate();
        }
        catch(S3ServiceException e) {
            throw new FileTransferException(FileTransferException.UNKNOWN_REASON);
        }
        finally {
            // Close the InputStream, no matter what
            try {
                in.close();
            }
            catch(IOException e) {
                // Do not re-throw the exception to prevent exceptions caught in the catch block from being replaced
            }
        }
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
        return atts.getOwner();
    }

    @Override
    public boolean canGetOwner() {
        return true;
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
            boolean isDirectory = isDirectory();
            if(isDirectory && service.listObjectsChunked(bucketName, getObjectKey(true), "/", 2, null, false).getObjects().length>1)
                throw new IOException("Directory not empty");

            service.deleteObject(bucketName, getObjectKey(isDirectory));

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

        S3Object destObjectFile = destFile.getAncestor(S3Object.class);

        try {
            // Let the COPY request fail if both objects are not located in the same region, saves 2 HEAD BUCKET requests.
//            // Ensure that both objects' bucket are located in the same region (null means US standard)
//            String sourceBucketLocation = service.getBucket(bucketName).getLocation();
//            String destBucketLocation = destObjectFile.service.getBucket(destObjectFile.bucketName).getLocation();
//            if((sourceBucketLocation==null && destBucketLocation!=null)
//            || (sourceBucketLocation!=null && destBucketLocation==null)
//            || !(sourceBucketLocation!=null && !sourceBucketLocation.equals(destBucketLocation))
//            || !destBucketLocation.equals(destBucketLocation))
//                throw new IOException();

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
            // Note: do *not* use S3ObjectRandomAccessInputStream if the object is to be read sequentially, as it would
            // add unnecessary billing overhead since it reads the object chunk by chunk, each in a separate GET request.
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
    @UnsupportedFileOperation
    public OutputStream getOutputStream() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.WRITE_FILE);

        // This stream is broken: close has no way to know if the transfer went through entirely. If it didn't
        // (close was called before the end of the input stream), the partially copied file will still be transferred
        // to S3, when it shouldn't.

//        final AbstractFile tempFile = FileFactory.getTemporaryFile(false);
//        final OutputStream tempOut = tempFile.getOutputStream();
//
//        // Update local attributes temporarily
//        atts.setExists(true);
//        atts.setSize(0);
//        atts.setDirectory(false);
//
//        // Return an OutputStream to a temporary file that will be copied to the S3 object when the stream is closed.
//        // The object's length has to be declared in the PUT request's headers and this is the only way to do so.
//        return new FilteredOutputStream(tempOut) {
//            @Override
//            public void close() throws IOException {
//                tempOut.close();
//
//                InputStream tempIn = tempFile.getInputStream();
//                try {
//                    long tempFileSize = tempFile.getSize();
//
//                    org.jets3t.service.model.S3Object object = new org.jets3t.service.model.S3Object(getObjectKey(false));
//                    object.setDataInputStream(tempIn);
//                    object.setContentLength(tempFileSize);
//
//                    // Transfer to S3 and update local file attributes
//                    atts.setAttributes(service.putObject(bucketName, object));
//                    atts.setExists(true);
//                    atts.updateExpirationDate();
//                }
//                catch(S3ServiceException e) {
//                    throw getIOException(e);
//                }
//                finally {
//                    try {
//                        tempIn.close();
//                    }
//                    catch(IOException e) {
//                        // Do not re-throw the exception to prevent exceptions caught in the catch block from being replaced
//                    }
//
//                    try {
//                        tempFile.delete();
//                    }
//                    catch(IOException e) {
//                        // Do not re-throw the exception to prevent exceptions caught in the catch block from being replaced
//                    }
//                }
//            }
//        };
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    @Override
    public void copyStream(InputStream in, boolean append, long length) throws FileTransferException {
        if(append) {
//            throw new UnsupportedFileOperationException(FileOperation.APPEND_FILE);
            throw new FileTransferException(FileTransferException.READING_SOURCE);
        }

        // TODO: compute md5 ?

        // If the length is known, we can upload the object directly without having to go through the tedious process
        // of copying the stream to a temporary file.
        if(length>=0) {
            putObject(in, length);
        }
        else {
            // Copy the stream to a temporary file so that we can know the object's length, which has to be declared
            // in the PUT request's headers (that is before the transfer is started).
            final AbstractFile tempFile;
            final OutputStream tempOut;
            try {
                tempFile = FileFactory.getTemporaryFile(false);
                tempOut = tempFile.getOutputStream();
            }
            catch(IOException e) {
                throw new FileTransferException(FileTransferException.OPENING_DESTINATION);
            }

            try {
                // Copy the stream to the temporary file
                try {
                    StreamUtils.copyStream(in, tempOut, IO_BUFFER_SIZE);
                }
                finally {
                    // Close the stream even if copyStream() threw an IOException
                    try {
                        tempOut.close();
                    }
                    catch(IOException e) {
                        // Do not re-throw the exception to prevent swallowing the exception thrown in the try block
                    }
                }

                InputStream tempIn;
                try {
                    tempIn = tempFile.getInputStream();
                }
                catch(IOException e) {
                    throw new FileTransferException(FileTransferException.OPENING_SOURCE);
                }

                putObject(tempIn, tempFile.getSize());
            }
            finally {
                // Delete the temporary file, no matter what.
                try {
                    tempFile.delete();
                }
                catch(IOException e) {
                    // Do not re-throw the exception to prevent exceptions caught in the catch block from being replaced
                }
            }
        }
    }

    ///////////////////
    // Inner classes //
    ///////////////////

    /**
     * Provides random read access to an S3 object by using GET Range requests with a start offset and no end.
     * The connection is closed and a new one opened when seeking is required.
     *
     * <p>
     * Note: At the time of this writing, a GET request on Amazon S3 costs the equivalent of 6666 bytes of data
     * transferred ($0.01 per 10,000 GET requests, $0.15 per GB transferred). If the object is being read and a seek is
     * requested to an offset that is less than 6666 bytes away from the current position going forward, the bytes
     * separating the current position to the new one are skipped (read and discarded), instead of closing the current
     * stream and opening a new one (which would cost 1 GET request). Doing so is cheaper (in $$$) and probably faster.
     * </p>
     */
    private class S3ObjectRandomAccessInputStream extends RandomAccessInputStream {

        /** Length of the S3 object */
        private long length;

        /** Current offset in the object stream */
        private long offset;

        /** Current object stream */
        private InputStream in;

        /** If the object is being read and a seek is requested to an offset that is less than this amount of bytes away
         * from the current position going forward, the bytes separating the current position to the new one are
         * skipped. */
        private final static int SKIP_BYTE_SIZE = 6666;

        protected S3ObjectRandomAccessInputStream() {
            length = getSize();
        }

        /**
         * Opens an input stream allowing to read the object and starting at the given offset. The current input stream
         * (if any) is closed.
         *
         * @param offset position at which to start reading the object
         * @throws IOException on error
         */
        private synchronized void openStream(long offset) throws IOException {
            // Nothing to do if the requested offset is the current offset
            if(in!=null && this.offset==offset)
                return;

            // If there is an open connection and the offset to reach is located between the current offset and
            // SKIP_SIZE, move to the said offset by skipping the difference instead of closing the connection and
            // opening a new one:
            if(in!=null && offset>this.offset && offset-this.offset< SKIP_BYTE_SIZE) {
                byte[] skipBuf = BufferPool.getByteArray(SKIP_BYTE_SIZE);    // Use a constant buffer size to always reuse the same instance
                try {
                    StreamUtils.readFully(in, skipBuf, 0, (int)(offset-this.offset));
                    this.offset = offset;
                }
                finally {
                    BufferPool.releaseByteArray(skipBuf);
                }
            }
            // If not, close the current connection
            else {
                if(in!=null) {
                    try {
                        in.close();
                    }
                    catch(IOException e) {
                        // Report the error but don't throw the exception
                        LOGGER.info("Failed to close connection", e);
                    }
                }

                try {
                    this.in = service.getObject(bucketName, getObjectKey(false), null, null, null, null, offset, null)
                        .getDataInputStream();
                    this.offset = offset;
                }
                catch(S3ServiceException e) {
                    throw getIOException(e);
                }
            }
        }


        ////////////////////////////////////////////
        // RandomAccessInputStream implementation //
        ////////////////////////////////////////////

        @Override
        public synchronized int read(byte[] b, int off, int len) throws IOException {
            if(in==null)
                openStream(0);

            int nbRead = in.read(b, off, len);
            if(nbRead>0)
                offset += nbRead;

            return nbRead;
        }

        @Override
        public synchronized int read() throws IOException {
            if(in==null)
                openStream(0);

            int i = in.read();
            if(i!=-1)
                offset++;

            return i;
        }

        public long getLength() throws IOException {
            return length;
        }

        public synchronized long getOffset() throws IOException {
            return offset;
        }

        public synchronized void seek(long offset) throws IOException {
            openStream(offset);
        }

        @Override
        public synchronized void close() throws IOException {
            if(in!=null) {
                try {
                    in.close();
                    // Let the IOException be thrown
                }
                finally {
                    // Further attempts to close the stream will be no-ops
                    in = null;
                    offset = 0;
                }
            }
        }
    }

//    /**
//     * Reads an S3 object block by block. Each block is read by issuing a GET request with a specified Range.
//     *
//     * <p>Note: A GET request on Amazon S3 costs the equivalent of 6KB of data transferred. Setting the block size too
//     * low will cause extra requests to be performed. Setting it too high will cause extra data to be transferred.</p>
//     */
//    private class S3ObjectRandomAccessInputStream extends BlockRandomInputStream {
//
//        /** Amount of data returned by each 'GET Object' request */
//        private final static int BLOCK_SIZE = 8192;
//
//        /** Length of the S3 object */
//        private long length;
//
//        protected S3ObjectRandomAccessInputStream() {
//            super(BLOCK_SIZE);
//
//            length = getSize();
//        }
//
//
//        ///////////////////////////////////////////
//        // BlockRandomInputStream implementation //
//        ///////////////////////////////////////////
//
//        @Override
//        protected int readBlock(long fileOffset, byte[] block, int blockLen) throws IOException {
//            try {
//                InputStream in = service.getObject(bucketName, getObjectKey(false), null, null, null, null, fileOffset, fileOffset+BLOCK_SIZE)
//                    .getDataInputStream();
//
//                // Read up to blockLen bytes
//                try {
//                    int totalRead = 0;
//                    int read;
//                    while(totalRead<blockLen) {
//                        read = in.read(block, totalRead, blockLen-totalRead);
//                        if(read==-1)
//                            break;
//
//                        totalRead += read;
//                    }
//
//                    return totalRead;
//                }
//                finally {
//                    in.close();
//                }
//            }
//            catch(S3ServiceException e) {
//                throw getIOException(e);
//            }
//        }
//
//        public long getLength() throws IOException {
//            return length;
//        }
//
//        @Override
//        public void close() throws IOException {
//            // No-op, the underlying stream is already closed
//        }
//    }


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
            setDirectory(object.getKey().endsWith("/"));
            setSize(object.getContentLength());
            setDate(object.getLastModifiedDate().getTime());
            setPermissions(DEFAULT_PERMISSIONS);
            // Note: owner is null for common prefix objects
            S3Owner owner = object.getOwner();
            setOwner(owner==null?null:owner.getDisplayName());
        }

        private void fetchAttributes() throws AuthException {
            try {
                setAttributes(service.getObjectDetails(bucketName, getObjectKey(), null, null, null, null));
                // Object does not exist on the server
                setExists(true);
            }
            catch(S3ServiceException e) {
                // Object does not exist on the server, or could not be retrieved
                setExists(false);

                setDirectory(false);
                setSize(0);
                setDate(0);
                setPermissions(FilePermissions.EMPTY_FILE_PERMISSIONS);
                setOwner(null);

                handleAuthException(e, fileURL);
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
                LOGGER.info("Failed to update attributes", e);
            }
        }
    }
}
