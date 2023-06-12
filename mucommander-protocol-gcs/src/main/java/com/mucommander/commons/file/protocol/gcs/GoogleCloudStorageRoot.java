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

import com.google.cloud.storage.Bucket;
import com.mucommander.commons.file.FileURL;

import java.io.IOException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Representation of the Root for the CloudStorage. This root folder lists CloudStorage buckets, so it is an additional
 * layer over the bucket content.
 *
 * @author miroslav.spak
 */
public class GoogleCloudStorageRoot extends GoogleCloudStorageAbstractFile {

    protected GoogleCloudStorageRoot(FileURL url) {
        super(url);
    }

    @Override
    public boolean isDirectory() {
        return true;
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public boolean isRoot() {
        return true;
    }

    @Override
    protected Stream<GoogleCloudStorageAbstractFile> listDir() throws IOException {
        var buckets = getStorageService().list();

        return StreamSupport.stream(buckets.iterateAll().spliterator(), false)
                .map(this::toFile);
    }

    /**
     * Transforms single {@link Bucket} to the internal representation of {@link GoogleCloudStorageBucket} directory.
     */
    private GoogleCloudStorageBucket toFile(Bucket bucket) {
        return toFile(
                parentPath -> parentPath + bucket.getName(),
                url -> new GoogleCloudStorageBucket(url, bucket));
    }
}
