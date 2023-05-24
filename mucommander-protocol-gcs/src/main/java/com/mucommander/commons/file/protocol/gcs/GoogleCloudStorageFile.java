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

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.util.PathUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;

public class GoogleCloudStorageFile extends GoogleCloudStorageBucket {
    private Blob blob;

    GoogleCloudStorageFile(FileURL url) {
        super(url);
    }

    GoogleCloudStorageFile(FileURL url, Blob blob, Storage storageService) {
        super(url, storageService);
        this.blob = blob;
    }

    private Blob getBlob() {
        if (blob == null) {
            var shortPath = PathUtils.removeLeadingSeparator(fileURL.getPath());
            var blobPath = shortPath.substring(shortPath.indexOf(CLOUD_STORAGE_DIRECTORY_DELIMITER));
            // TODO check
            blob = getBucket().get(blobPath);
        }

        return blob;
    }

    @Override
    public AbstractFile[] ls() throws IOException {
        var files = getBucket().list(Storage.BlobListOption.prefix(getBlob().getName()), Storage.BlobListOption.delimiter(CLOUD_STORAGE_DIRECTORY_DELIMITER));
        return toFilesArray(files.iterateAll());
    }

    @Override
    public long getDate() {
        // fixme NPE
        var updateOffsetTime = getBlob().getUpdateTimeOffsetDateTime() != null ?
                // Read blob creation date, or use at least bucket last update date
                getBlob().getUpdateTimeOffsetDateTime() : getBucket().getUpdateTimeOffsetDateTime();

        return updateOffsetTime != null ? updateOffsetTime.toInstant().toEpochMilli() : 0;
    }

    @Override
    public long getSize() {
        // TODO NPE check, unknown size?
        return getBlob() != null ? getBlob().getSize() : 0;
    }

    @Override
    public boolean exists() {
        // TODO Check NPE?
        // TODO Folder always exists
        return getBlob() != null && (getBlob().isDirectory() || getBlob().exists());
    }

    @Override
    public boolean isDirectory() {
        // FIXME NPE
        return getBlob().isDirectory();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        // FIXME try?
        // TODO missing file or folder?
        return Channels.newInputStream(getBlob().reader());
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        // FIXME try?
        // TODO missing?
        var bucketName = getBucketName();
        var shortPath = PathUtils.removeLeadingSeparator(fileURL.getPath());
        var blobName = shortPath.substring(shortPath.indexOf(CLOUD_STORAGE_DIRECTORY_DELIMITER) + 1);
        var blobId = BlobId.of(bucketName, blobName);
        var blobInfo = BlobInfo.newBuilder(blobId).build();

        return Channels.newOutputStream(getStorageService().writer(blobInfo, Storage.BlobWriteOption.detectContentType()));
    }

    @Override
    public void mkdir() throws IOException {
        // TODO unify
        var bucketName = getBucketName();
        var shortPath = PathUtils.removeLeadingSeparator(fileURL.getPath());
        var blobPath = shortPath.substring(shortPath.indexOf(CLOUD_STORAGE_DIRECTORY_DELIMITER) + 1);
        var blobName = blobPath + "/."; // TODO dummy file?
        try {
            var blobId = BlobId.of(bucketName, blobName);
            var blobInfo = BlobInfo.newBuilder(blobId).setContentType("text/plain").build();
            // Cannot set blob because this blob is dummy file not the folder itself
            getStorageService().create(blobInfo);
        } catch (Exception ex) {
            throw new IOException("Unable to create folder " + blobPath + " in bucket " + bucketName, ex);
        }
    }

    @Override
    public void mkfile() throws IOException {
        // TODO unify
        var bucketName = getBucketName();
        var shortPath = PathUtils.removeLeadingSeparator(fileURL.getPath());
        var blobName = shortPath.substring(shortPath.indexOf(CLOUD_STORAGE_DIRECTORY_DELIMITER) + 1);
        try {
            var blobId = BlobId.of(bucketName, blobName);
            var blobInfo = BlobInfo.newBuilder(blobId).setContentType("text/plain").build();
            // The new blob represents created file
            blob = getStorageService().create(blobInfo);
        } catch (Exception ex) {
            throw new IOException("Unable to create file " + blobName + " in bucket " + bucketName, ex);
        }
    }

    @Override
    public void delete() throws IOException {
        var blobName = getBlob().getName();
        try {
            if (getBlob().delete()) {
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
