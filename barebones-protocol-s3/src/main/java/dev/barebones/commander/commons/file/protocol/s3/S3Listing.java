/*
 * Copyright (C) 2002-2026 muCommander contributors
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 */
package dev.barebones.commander.commons.file.protocol.s3;

import dev.barebones.commander.commons.file.AbstractFile;
import dev.barebones.commander.commons.file.FileFactory;
import dev.barebones.commander.commons.file.FileURL;

import software.amazon.awssdk.services.s3.model.CommonPrefix;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Shared {@code ListObjectsV2}-with-delimiter logic used by both
 * {@link S3Bucket} (listing the bucket root) and {@link S3Object}
 * (listing a "directory" prefix). Pulled out so the request-paging
 * loop has one home.
 */
final class S3Listing {

    private S3Listing() {
    }

    /**
     * Lists the immediate children of {@code prefix} in {@code bucket}
     * using delimiter='/'. Keys that end in '/' (or that show up only
     * as common prefixes) become pseudo-directories; the rest become
     * objects. Pages through all results.
     */
    static AbstractFile[] listChildrenAsFiles(
            S3Connection connection, FileURL parentURL,
            String bucket, String prefix) throws IOException {

        List<AbstractFile> out = new ArrayList<>();
        String continuationToken = null;
        try {
            do {
                ListObjectsV2Request.Builder req = ListObjectsV2Request.builder()
                    .bucket(bucket)
                    .prefix(prefix)
                    .delimiter("/");
                if (continuationToken != null) {
                    req.continuationToken(continuationToken);
                }
                ListObjectsV2Response resp = connection.client().listObjectsV2(req.build());

                // Common prefixes → pseudo-directories
                for (CommonPrefix cp : resp.commonPrefixes()) {
                    String childKey = cp.prefix();
                    out.add(buildChildFile(parentURL, bucket, childKey, true, 0L, 0L));
                }
                // Objects → files (skip the prefix itself if S3 echoes it)
                for (software.amazon.awssdk.services.s3.model.S3Object obj : resp.contents()) {
                    String childKey = obj.key();
                    if (childKey.equals(prefix)) {
                        continue;
                    }
                    long size = obj.size() != null ? obj.size() : 0L;
                    long lastModified = obj.lastModified() != null
                        ? obj.lastModified().toEpochMilli() : 0L;
                    boolean isDir = childKey.endsWith("/");
                    out.add(buildChildFile(parentURL, bucket, childKey, isDir, size, lastModified));
                }
                continuationToken = Boolean.TRUE.equals(resp.isTruncated())
                    ? resp.nextContinuationToken() : null;
            } while (continuationToken != null);
        } catch (S3Exception e) {
            throw S3File.toIOException(e, parentURL);
        }
        return out.toArray(new AbstractFile[0]);
    }

    private static AbstractFile buildChildFile(
            FileURL parentURL, String bucket, String key,
            boolean isDirectory, long size, long lastModified) {
        FileURL childURL = (FileURL) parentURL.clone();
        childURL.setPath("/" + bucket + "/" + key);
        AbstractFile child = FileFactory.getFile(childURL);
        // FileFactory builds an S3Object/S3Bucket; the listing's
        // size + last-modified are authoritative — stash them so a
        // second HEAD isn't needed for table rendering.
        if (child instanceof S3Object o) {
            o.setListingMetadata(size, lastModified, isDirectory);
        }
        return child;
    }
}
