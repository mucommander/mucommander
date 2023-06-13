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
            // FIXME NPE
            blob = getBucket().get(getBlobPath());
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
        // TODO check NPE
        var files = getBucket().list(
                Storage.BlobListOption.prefix(getBlob().getName()),
                Storage.BlobListOption.delimiter(getSeparator()));
        return StreamSupport.stream(files.iterateAll().spliterator(), false)
                .map(this::toFile);
    }

    @Override
    public long getDate() {
        // fixme NPE
        if (getBlob() == null) {
            return 0;
        }

        var updateOffsetTime = getBlob().getUpdateTimeOffsetDateTime() != null ?
                // Read blob creation date, or use at least bucket last update date
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
        // FIXME try?
        // TODO missing file or folder?
        // FIXME read of empty - changed file
        return Channels.newInputStream(getBlob().reader());
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        // TODO missing?
        // TODO error when creating?
        var blobId = BlobId.of(getBucketName(), getBlobPath());
        var blobInfo = BlobInfo.newBuilder(blobId).build();

        return Channels.newOutputStream(getStorageService().writer(blobInfo, Storage.BlobWriteOption.detectContentType()));
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
        // FIXME npe?
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
