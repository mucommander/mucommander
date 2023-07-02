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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.cloud.storage.*;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.util.PathUtils;

/**
 * Representation of the Cloud Storage Blob as a File/Folder.
 *
 * @author miroslav.spak
 */
public class GoogleCloudStorageFile extends GoogleCloudStorageBucket {

    private static final String DUMMY_FILE_NAME = ".";
    private static final String EMPTY_FILE_CONTENT_TYPE = "text/plain";

    private Blob blob;

    GoogleCloudStorageFile(FileURL url) {
        super(url);
    }

    GoogleCloudStorageFile(FileURL url, Bucket bucket, Blob blob) {
        super(url, bucket);
        this.blob = blob;
    }

    /**
     * Tries to receive blob from the Google Cloud Storage service.
     *
     * @return blob for this object path (i.e., {@link GoogleCloudStorageFile#fileURL}), can be <b>null</b> if blob
     *         doesn't exist
     */
    private Blob getBlob() {
        // Get Blob file from the bucket
        if (blob == null && getBucket() != null) {
            blob = getBucket().get(getBlobPath());
        }

        // Directories are not returned using bucket#get()
        if (blob == null && getBucket() != null) {
            // Try to find this blob in the parent directory
            blob = listGcsDir(getBlobPath(getURL().getParent()))
                    .filter(blob -> Objects.equals(getBlobName(blob), getURL().getFilename()))
                    .findFirst()
                    .orElse(null);
        }

        return blob;
    }

    /**
     * Finds the path of the Blob in the GCS Bucket from the given fileURL. I.e., full path without Bucket name.
     */
    protected static String getBlobPath(FileURL url) {
        // Find second part of the path that represents blob path (without bucket name)
        var matcher = BUCKER_NAME_BLOB_PATH_PATTERN.matcher(url.getPath());
        return Optional.of(matcher)
                .filter(Matcher::find)
                .map(match -> match.group(2))
                .orElse("");
    }

    /**
     * Finds the path of the current Blob in the GCS Bucket.
     */
    protected String getBlobPath() {
        return getBlobPath(getURL());
    }

    @Override
    protected Stream<GoogleCloudStorageAbstractFile> listDir() {
        if (getBucket() == null || getBlob() == null) {
            throw new IllegalStateException("Cannot list directory that doesn't exist, path " + getURL());
        }
        // List all blobs in the given folder, i.e. all with given blob name prefix
        return listGcsDir(getBlob().getName())
                .map(this::toFile);
    }

    /**
     * Returns stream of the blobs in the directory on the given path
     */
    private Stream<Blob> listGcsDir(String path) {
        var files = getBucket().list(
                // List all blobs in the given folder by string path
                Storage.BlobListOption.prefix(path),
                Storage.BlobListOption.currentDirectory());
        return StreamSupport.stream(files.iterateAll().spliterator(), false)
                // Blob name in bucket equals to its path, and sometimes Google API returns parent folder in the result
                .filter(blob -> !path.equals(blob.getName()));
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
            var blobInfo = BlobInfo.newBuilder(blobId).setContentType(EMPTY_FILE_CONTENT_TYPE).build();
            // Cannot set blob because this blob is a dummy file not the folder itself
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
            var blobInfo = BlobInfo.newBuilder(blobId).setContentType(EMPTY_FILE_CONTENT_TYPE).build();
            // The new blob represents created file
            blob = getStorageService().create(blobInfo);
        } catch (Exception ex) {
            throw new IOException("Unable to create file " + blobPath + " in bucket " + bucketName, ex);
        }
    }

    @Override
    public void delete() throws IOException {
        if (getBlob() == null) {
            // Expecting the missing blob here - it is an error
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
