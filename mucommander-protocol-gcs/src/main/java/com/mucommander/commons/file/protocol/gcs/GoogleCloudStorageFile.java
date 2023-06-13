/**
 * This file is part of muCommander, http://www.mucommander.com
 * <p>
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.commons.file.protocol.gcs;

import com.google.cloud.storage.*;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.util.PathUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class GoogleCloudStorageFile extends GoogleCloudStorageBucket {

    private static final String DUMMY_FILE_NAME = ".";

    private Blob blob;

    GoogleCloudStorageFile(FileURL url) {
        super(url);
    }

    GoogleCloudStorageFile(FileURL url, Bucket bucket, Blob blob) {
        super(url, bucket);
        this.blob = blob;
    }

    private Blob getBlob() {
        if (blob == null) {
            // Try to find blob if bucket itself exist
            if (getBucket() != null) {
                blob = getBucket().get(getBlobPath());
            }
        }

        return blob;
    }

    private String getBlobPath() {
        // Remove first separator if any
        var pathWithBucket = PathUtils.removeLeadingSeparator(fileURL.getPath());
        // Remove bucket name from path
        return pathWithBucket.substring(pathWithBucket.indexOf(getSeparator()) + 1);
    }

    @Override
    protected Stream<GoogleCloudStorageAbstractFile> listDir() {
        if (getBucket() == null || getBlob() == null) {
            throw new IllegalStateException("Cannot list directory that doesn't exist, path " + getURL());
        }
        var files = getBucket().list(
                // List all blobs in the given folder, i.e. all with given blob name prefix
                Storage.BlobListOption.prefix(getBlob().getName()),
                Storage.BlobListOption.delimiter(getSeparator()));
        return StreamSupport.stream(files.iterateAll().spliterator(), false)
                .map(this::toFile);
    }

    @Override
    public long getDate() {
        if (getBlob() == null || getBucket() == null) {
            return 0;
        }

        var updateOffsetTime = getBlob().getUpdateTimeOffsetDateTime() != null ?
                // Read blob creation date, or use at least bucket last update date (typically for directories)
                getBlob().getUpdateTimeOffsetDateTime() : getBucket().getUpdateTimeOffsetDateTime();

        return updateOffsetTime != null ? updateOffsetTime.toInstant().toEpochMilli() : 0;
    }

    @Override
    public long getSize() {
        return getBlob() != null ? getBlob().getSize() : 0;
    }

    @Override
    public boolean exists() {
        return getBlob() != null && (getBlob().isDirectory() || getBlob().exists());
    }

    @Override
    public boolean isDirectory() {
        return getBlob() != null && getBlob().isDirectory();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (getBlob() == null) {
            throw new IOException("Underlying blob doesn't exist " + getURL());
        }
        try {
            return Channels.newInputStream(getBlob().reader());
        } catch (Exception ex) {
            throw new IOException("Unable to read file " + getURL(), ex);
        }
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        try {
            var blobId = BlobId.of(getBucketName(), getBlobPath());
            var blobInfo = BlobInfo.newBuilder(blobId).build();
            // Any change to the blob creates a new blob in Cloud Storage, the fresh blob will be fetched later
            blob = null;

            return Channels.newOutputStream(
                    // Let the library detect the content
                    getStorageService().writer(blobInfo, Storage.BlobWriteOption.detectContentType()));
        } catch (Exception ex) {
            throw new IOException("Unable to write file " + getURL(), ex);
        }
    }

    @Override
    public void mkdir() throws IOException {
        var bucketName = getBucketName();
        // Create dummy file in the folder, because folder cannot exist without file in GCS
        var blobPath = PathUtils.removeTrailingSeparator(getBlobPath()) + getSeparator() + DUMMY_FILE_NAME;
        try {
            var blobId = BlobId.of(bucketName, blobPath);
            var blobInfo = BlobInfo.newBuilder(blobId).setContentType("text/plain").build();
            // Cannot set blob because this blob is dummy file not the folder itself
            getStorageService().create(blobInfo);
        } catch (Exception ex) {
            throw new IOException("Unable to create folder " + blobPath + " in bucket " + bucketName, ex);
        }
    }

    @Override
    public void mkfile() throws IOException {
        var bucketName = getBucketName();
        var blobPath = getBlobPath();
        try {
            var blobId = BlobId.of(bucketName, blobPath);
            var blobInfo = BlobInfo.newBuilder(blobId).setContentType("text/plain").build();
            // The new blob represents created file
            blob = getStorageService().create(blobInfo);
        } catch (Exception ex) {
            throw new IOException("Unable to create file " + blobPath + " in bucket " + bucketName, ex);
        }
    }

    @Override
    public void delete() throws IOException {
        if (getBlob() == null) {
            // Expecting the missing blob here is an error
            throw new IOException("Unable to find the file to delete, file " + getURL());
        }
        var blobName = getBlob().getName();
        try {
            // Directories exists only when there are files present, we cannot delete them
            if (isDirectory() || getBlob().delete()) {
                // The blob was deleted
                blob = null;
            } else {
                throw new IllegalStateException("File " + blobName + " wasn't deleted, it's probably missing");
            }
        } catch (Exception ex) {
            throw new IOException("Unable to delete file " + blobName, ex);
        }
    }
}
