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
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.UnsupportedFileOperationException;
import com.mucommander.commons.file.filter.FileFilter;
import com.mucommander.commons.file.util.PathUtils;

import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * TODO
 */
public class GoogleCloudStorageBucket extends GoogleCloudStorageAbstractFile {

    private final Bucket bucket; // TODO final and eager init?

    /**
     * TODO
     *
     * @param url
     * @return
     */
    static GoogleCloudStorageBucket from(FileURL url) throws IOException {
        // TODO ??
        var bucket = GoogleCloudStorageAbstractFile.getBucket(url);
        return new GoogleCloudStorageBucket(url, bucket);
    }

    GoogleCloudStorageBucket(FileURL url, Bucket bucket) {
        super(url);
        this.bucket = bucket;
    }

    @Override
    public boolean isDirectory() {
        // Bucket is always represented as directory
        return true;
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public AbstractFile[] ls() throws IOException, UnsupportedFileOperationException {
        var files = bucket.list(Storage.BlobListOption.currentDirectory());

        var children = StreamSupport.stream(files.iterateAll().spliterator(), false)
                .map(this::toFile)
                .collect(Collectors.toList());

        var childrenArray = new AbstractFile[children.size()];
        children.toArray(childrenArray);
        return childrenArray;
    }

    @Override
    public AbstractFile[] ls(FileFilter filter) throws IOException, UnsupportedFileOperationException {
        //FIXME
        var files = bucket.list(Storage.BlobListOption.currentDirectory());

        var children = StreamSupport.stream(files.iterateAll().spliterator(), false)
                .map(this::toFile)
                .collect(Collectors.toList());

        var childrenArray = new AbstractFile[children.size()];
        children.toArray(childrenArray);
        return childrenArray;
    }

    private GoogleCloudStorageFile toFile(Blob blob) {
        var url = (FileURL) getURL().clone();
        var parentPath = PathUtils.removeTrailingSeparator(url.getPath()) + AbstractFile.DEFAULT_SEPARATOR; // FIXME
        url.setPath(parentPath + blob.getName());
        var result = new GoogleCloudStorageFile(url, bucket, blob);
        result.setParent(this);
        return result;
    }

    @Override
    public long getDate() {
        // TODO check NPE
        return bucket.getUpdateTimeOffsetDateTime().toInstant().toEpochMilli();
    }
}
