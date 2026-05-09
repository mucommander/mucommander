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

import dev.barebones.commander.commons.file.FileURL;

import java.util.Objects;

/**
 * Decomposes a barebones {@link FileURL} into its S3-specific parts:
 * <ul>
 *   <li>endpoint host + port (e.g. {@code s3.us-east-1.amazonaws.com}
 *       or {@code minio.local:9000})</li>
 *   <li>bucket (first path segment, may be empty for the root)</li>
 *   <li>object key (everything after the bucket; may be empty)</li>
 * </ul>
 *
 * Pure value object — does no I/O, no client construction. Lives
 * apart from {@link S3Connection} so the URL parsing is unit-testable
 * without spinning up an S3Client.
 */
public final class S3FileURL {

    private final String endpointHost;
    private final int endpointPort;
    private final String bucket;
    private final String key;

    public S3FileURL(String endpointHost, int endpointPort, String bucket, String key) {
        this.endpointHost = Objects.requireNonNull(endpointHost, "endpointHost");
        this.endpointPort = endpointPort;
        this.bucket = bucket == null ? "" : bucket;
        this.key = key == null ? "" : key;
    }

    public String endpointHost() { return endpointHost; }
    public int endpointPort() { return endpointPort; }
    public String bucket() { return bucket; }
    public String key() { return key; }

    public boolean isRoot() {
        return bucket.isEmpty();
    }

    public boolean isBucketRoot() {
        return !bucket.isEmpty() && key.isEmpty();
    }

    public boolean isObject() {
        return !bucket.isEmpty() && !key.isEmpty();
    }

    public static S3FileURL parse(FileURL url) {
        Objects.requireNonNull(url, "url");
        String host = url.getHost();
        int port = url.getPort();
        String path = url.getPath();
        if (path == null || path.isEmpty() || "/".equals(path)) {
            return new S3FileURL(host, port, "", "");
        }
        // Drop the leading slash; first segment is the bucket; remainder is the key.
        String trimmed = path.startsWith("/") ? path.substring(1) : path;
        int slash = trimmed.indexOf('/');
        if (slash < 0) {
            return new S3FileURL(host, port, trimmed, "");
        }
        String bucket = trimmed.substring(0, slash);
        String key = trimmed.substring(slash + 1);
        return new S3FileURL(host, port, bucket, key);
    }
}
