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
import com.google.cloud.storage.Storage;
import com.mucommander.commons.file.*;
import com.mucommander.commons.file.util.PathUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class GoogleCloudStorageFile extends GoogleCloudStorageAbstractFile {
    private Blob blob;

    GoogleCloudStorageFile(FileURL url) {
        super(url);
    }

    GoogleCloudStorageFile(FileURL url, Blob blob, Storage storageService) {
        super(url, storageService);
        this.blob = blob;
    }

    private Blob getBlob(){
        if (blob == null) {
            var shortPath = PathUtils.removeLeadingSeparator(fileURL.getPath());
            var bucketName = shortPath.substring(0, shortPath.indexOf(CLOUD_STORAGE_DIRECTORY_DELIMITER));
            var blobPath = shortPath.substring(shortPath.indexOf(CLOUD_STORAGE_DIRECTORY_DELIMITER));
            // TODO check
            blob = getStorageService().get(bucketName, blobPath);
        }

        return blob;
    }

    @Override
    public AbstractFile[] ls() throws IOException, UnsupportedFileOperationException {
        var shortPath = PathUtils.removeLeadingSeparator(fileURL.getPath());
        var bucketName = shortPath.substring(0, shortPath.indexOf(CLOUD_STORAGE_DIRECTORY_DELIMITER));

        var files = getStorageService().list(bucketName, Storage.BlobListOption.prefix(getBlob().getName()), Storage.BlobListOption.delimiter(CLOUD_STORAGE_DIRECTORY_DELIMITER));

        var children = StreamSupport.stream(files.iterateAll().spliterator(), false)
                .map(this::toFile)
                .collect(Collectors.toList());

        var childrenArray = new AbstractFile[children.size()];
        children.toArray(childrenArray);
        return childrenArray;
    }

    private GoogleCloudStorageFile toFile(Blob blob) {
        // FIXME
        var url = (FileURL) getURL().clone();
        var parentPath = PathUtils.removeTrailingSeparator(url.getPath()) + AbstractFile.DEFAULT_SEPARATOR; // FIXME
        url.setPath(parentPath + blob.getName());
        var result = new GoogleCloudStorageFile(url, blob, getStorageService());
        result.setParent(this);
        return result;
    }

    @Override
    public long getDate() {
        // fixme NPE
        var updateOffsetTime = blob.getUpdateTimeOffsetDateTime();
//                != null ?
//                // Read blob creation date, or use at least bucket last update date
//                blob.getUpdateTimeOffsetDateTime() : bucket.getUpdateTimeOffsetDateTime();

        return updateOffsetTime != null ? updateOffsetTime.toInstant().toEpochMilli() : 0;
    }

    @Override
    public long getSize() {
        // TODO NPE check, unknown size?
        return blob.getSize();
    }

    @Override
    public boolean exists() {
        // TODO Check NPE?
        // TODO Folder always exists
//        return blob.exists();
        return true;
    }

    @Override
    public boolean isDirectory() {
        // FIXME NPE
        return blob.isDirectory();
    }

    @Override
    public InputStream getInputStream() throws IOException, UnsupportedFileOperationException {
        // FIXME try?
        // TODO missing file or folder?
        return Channels.newInputStream(blob.reader());
    }
}
