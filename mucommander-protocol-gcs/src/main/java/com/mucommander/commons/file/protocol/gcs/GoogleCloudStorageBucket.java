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
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.UnsupportedFileOperationException;
import com.mucommander.commons.file.util.PathUtils;

import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * TODO
 */
public class GoogleCloudStorageBucket extends GoogleCloudStorageAbstractFile {

    private Bucket bucket;

    GoogleCloudStorageBucket(FileURL url, Bucket bucket, Storage storageService) {
        super(url, storageService);
        this.bucket = bucket;
    }

    private Bucket getBucket() {
        if (bucket == null) {
            // TODO jde to jinak?
            // Find the first part of the path that represents bucket name
            var bucketName = fileURL.getPath().replaceAll("/([^/]+)/?.*", "$1");
            // TODO check
            bucket = getStorageService().get(bucketName);
        }

        return bucket;
    }

    GoogleCloudStorageBucket(FileURL url) {
        super(url);
    }

    @Override
    public boolean isDirectory() {
        // Bucket is always represented as directory
        return true;
    }

    @Override
    public boolean exists() {
        // FIXME
        return getBucket() != null;
    }

    @Override
    public AbstractFile[] ls() throws IOException, UnsupportedFileOperationException {
        var files = getBucket().list(Storage.BlobListOption.currentDirectory());

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
        var result = new GoogleCloudStorageFile(url, blob, getStorageService());
        result.setParent(this);
        return result;
    }

    @Override
    public long getDate() {
        // TODO check NPE
        return getBucket().getUpdateTimeOffsetDateTime().toInstant().toEpochMilli();
    }

    @Override
    public void mkdir() throws IOException {
        // FIXME unify
        var location = "europe-west1"; // todo from connection
        var bucketName = fileURL.getPath().replaceAll("/([^/]+)/?.*", "$1");
        try {
            // TODO set location only if provided
            getStorageService().create(BucketInfo.newBuilder(bucketName).setLocation(location).build());
        } catch (Exception ex){
            throw new IOException("Unable to create bucket " + bucketName, ex);
        }
    }

    @Override
    public void delete() throws IOException {
        // FIXME unify
        var bucketName = fileURL.getPath().replaceAll("/([^/]+)/?.*", "$1");
        try {
            if (getBucket().delete()) {
                // The bucket was deleted
                bucket = null;
            } else {
                throw new IllegalStateException("Bucket " + bucketName + " wasn't deleted, it's probably missing");
            }
        } catch (Exception ex){
            throw new IOException("Unable to delete bucket " + bucketName, ex);
        }
    }
}
