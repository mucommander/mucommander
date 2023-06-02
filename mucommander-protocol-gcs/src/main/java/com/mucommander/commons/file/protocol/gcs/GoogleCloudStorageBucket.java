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

import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * TODO
 */
public class GoogleCloudStorageBucket extends GoogleCloudStorageAbstractFile {

    private Bucket bucket;

    GoogleCloudStorageBucket(FileURL url) {
        super(url);
    }

    GoogleCloudStorageBucket(FileURL url, Bucket bucket, Storage storageService) {
        super(url, storageService);
        this.bucket = bucket;
    }

    protected String getBucketName() {
        // Find the first part of the path that represents bucket name
        return fileURL.getPath().replaceAll("/([^/]+)/?.*", "$1");
    }

    protected Bucket getBucket() {
        if (bucket == null) {
            // TODO check
            bucket = getStorageService().get(getBucketName());
        }

        return bucket;
    }

    @Override
    public boolean isDirectory() {
        // Bucket is always represented as directory
        return true;
    }

    @Override
    public boolean exists() {
        // FIXME
        return getBucket() != null && getBucket().exists();
    }

    @Override
    public AbstractFile[] ls() throws IOException {
        var files = getBucket().list(Storage.BlobListOption.currentDirectory());
        return toFilesArray(files.iterateAll());
    }

    protected AbstractFile[] toFilesArray(Iterable<Blob> blobs) {
        var children = StreamSupport.stream(blobs.spliterator(), false)
                .map(this::toFile)
                .collect(Collectors.toList());

        var childrenArray = new AbstractFile[children.size()];
        children.toArray(childrenArray);
        return childrenArray;
    }

    private GoogleCloudStorageFile toFile(Blob blob) {
        // FIXME
        var url = (FileURL) getURL().clone();
        var blobPath = AbstractFile.DEFAULT_SEPARATOR + fileURL.getPath().replaceAll("/([^/]+)/?.*", "$1")
                + AbstractFile.DEFAULT_SEPARATOR + blob.getName();
        url.setPath(blobPath);
        var result = new GoogleCloudStorageFile(url, getBucket(), blob, getStorageService());
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
        var location = "europe-west1"; // todo from connection
        try {
            // TODO set location only if provided
            getStorageService().create(BucketInfo.newBuilder(getBucketName()).setLocation(location).build());
        } catch (Exception ex) {
            throw new IOException("Unable to create bucket " + getBucketName(), ex);
        }
    }

    @Override
    public void delete() throws IOException {
        try {
            if (getBucket().delete()) {
                // The bucket was deleted
                bucket = null;
            } else {
                throw new IllegalStateException("Bucket " + getBucketName() + " wasn't deleted, it's probably missing");
            }
        } catch (Exception ex) {
            throw new IOException("Unable to delete bucket " + getBucketName(), ex);
        }
    }
}
