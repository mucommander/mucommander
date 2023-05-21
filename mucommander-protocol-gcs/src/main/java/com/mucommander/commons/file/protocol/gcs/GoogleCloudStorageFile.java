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
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.mucommander.commons.file.*;
import com.mucommander.commons.file.filter.FileFilter;
import com.mucommander.commons.file.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class GoogleCloudStorageFile extends GoogleCloudStorageAbstractFile {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleCloudStorageFile.class);

    private final Bucket bucket; //TODO final and eager init?
    private final Blob blob;

    private GoogleCloudStorageFile parent;

    GoogleCloudStorageFile(FileURL url, Bucket bucket, Blob blob) {
        super(url);
        this.bucket = bucket;
        this.blob = blob;
    }

    /**
     * TODO
     *
     * @param url
     * @return
     */
    static GoogleCloudStorageFile from(FileURL url) throws IOException {
        // TODO ??
        var bucket = GoogleCloudStorageAbstractFile.getBucket(url);
        var blob = GoogleCloudStorageAbstractFile.getBlob(url);

        return new GoogleCloudStorageFile(url, bucket, blob);
    }

    @Override
    public AbstractFile[] ls(FileFilter filter) throws IOException, UnsupportedFileOperationException {
        var files = bucket.list(Storage.BlobListOption.prefix(blob.getName()), Storage.BlobListOption.delimiter(CLOUD_STORAGE_DIRECTORY_DELIMITER));

        var children = StreamSupport.stream(files.iterateAll().spliterator(), false)
                .map(this::toFile)
                .collect(Collectors.toList());

        var childrenArray = new AbstractFile[children.size()];
        children.toArray(childrenArray);
        return childrenArray;
    }

    @Override
    public AbstractFile[] ls() throws IOException, UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.LIST_CHILDREN);
    }

    private GoogleCloudStorageFile toFile(Blob blob) {
        // FIXME
        var url = (FileURL) getURL().clone();
        var parentPath = PathUtils.removeTrailingSeparator(url.getPath()) + AbstractFile.DEFAULT_SEPARATOR; // FIXME
        url.setPath(parentPath + blob.getName());
        var result = new GoogleCloudStorageFile(url, bucket, blob);
        result.setParent(this);
        return result;
    }

    @Override
    public MonitoredFile toMonitoredFile() {
        return new GoogleCloudStorageMonitoredFile(this);
    }

    @Override
    public long getDate() {
        // fixme NPE
        var updateOffsetTime = blob.getUpdateTimeOffsetDateTime() != null ?
                // Read blob creation date, or use at least bucket last update date
                blob.getUpdateTimeOffsetDateTime() : bucket.getUpdateTimeOffsetDateTime();

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
