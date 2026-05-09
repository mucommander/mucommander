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
import dev.barebones.commander.commons.file.Credentials;
import dev.barebones.commander.commons.file.FileURL;
import dev.barebones.commander.commons.file.protocol.ProtocolProvider;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bridges the barebones {@link FileURL} world to S3 by:
 * <ol>
 *   <li>Resolving (or creating) an {@link S3Connection} keyed by the
 *       URL's (endpoint, region, credentials) tuple — one S3Client
 *       per distinct connection target, reused across files.</li>
 *   <li>Picking the right concrete {@link S3File} subclass:
 *       {@link S3Root} for {@code s3://endpoint/}, {@link S3Bucket}
 *       for a bucket-only URL, {@link S3Object} for everything else.</li>
 * </ol>
 *
 * Connection cache key includes credentials so two URLs with
 * different access keys against the same endpoint don't share a
 * client (which would silently authorise as the wrong identity).
 */
public class S3ProtocolProvider implements ProtocolProvider, AutoCloseable {

    /** Properties accepted on the FileURL — these become S3Configuration options. */
    public static final String PROPERTY_REGION = "region";
    public static final String PROPERTY_PATH_STYLE = "pathStyle";
    public static final String PROPERTY_USE_HTTPS = "useHttps";

    private final ConcurrentHashMap<String, S3Connection> connections = new ConcurrentHashMap<>();

    /**
     * Closes every cached connection (releases the AWS SDK HTTP
     * client thread pools). Idempotent — entries are removed as we
     * go so a second call is a no-op.
     */
    @Override
    public void close() {
        for (S3Connection conn : connections.values()) {
            try {
                conn.close();
            } catch (RuntimeException ignored) {
                // shutdown — log channels may already be down.
            }
        }
        connections.clear();
    }

    @Override
    public AbstractFile getFile(FileURL url, Map<String, Object> instantiationParams) throws IOException {
        S3Connection conn = connectionFor(url);
        S3FileURL parsed = S3FileURL.parse(url);
        if (parsed.isRoot()) {
            return new S3Root(url, conn);
        }
        if (parsed.isBucketRoot()) {
            return new S3Bucket(url, conn);
        }
        return new S3Object(url, conn);
    }

    private S3Connection connectionFor(FileURL url) {
        Credentials creds = url.getCredentials();
        final String accessKey = creds != null ? creds.getLogin()    : "";
        final String secretKey = creds != null ? creds.getPassword() : "";
        final String region    = url.getProperty(PROPERTY_REGION);
        final boolean pathStyle = "true".equalsIgnoreCase(url.getProperty(PROPERTY_PATH_STYLE));
        final boolean useHttps  = !"false".equalsIgnoreCase(url.getProperty(PROPERTY_USE_HTTPS));
        final String host = url.getHost();
        final int port = url.getPort();
        // Cache key hashes the credentials material rather than embedding
        // it in plaintext: the key string travels into log output, heap
        // dumps, and ConcurrentHashMap.toString().
        String credentialsHash = sha256Hex(accessKey + "\0" + secretKey);
        String key = host + "|" + port + "|" + region + "|" + credentialsHash
            + "|" + pathStyle + "|" + useHttps;
        return connections.computeIfAbsent(key, k -> S3Connection.open(
            host, port, region,
            accessKey, secretKey,
            pathStyle, useHttps));
    }

    private static String sha256Hex(String input) {
        try {
            byte[] digest = java.security.MessageDigest.getInstance("SHA-256")
                .digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            // SHA-256 is mandatory in every JRE; this never fires.
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
