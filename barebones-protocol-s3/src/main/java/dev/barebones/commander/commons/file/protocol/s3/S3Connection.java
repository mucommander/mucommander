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

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.transfer.s3.S3TransferManager;

import java.net.URI;
import java.util.Objects;

/**
 * Holder for an {@link S3Client} configured for one (endpoint, region,
 * credentials) tuple. The protocol module creates one of these per
 * connected URL and reuses it across {@link S3File} subclasses for the
 * lifetime of the session.
 *
 * Two-tier credential model:
 * <ul>
 *   <li>If both access-key and secret-key are present in the URL,
 *       use {@link StaticCredentialsProvider}.</li>
 *   <li>Otherwise fall back to {@link DefaultCredentialsProvider}
 *       (env vars, ~/.aws/credentials, EC2/ECS instance role,
 *       container creds — whichever wins first).</li>
 * </ul>
 *
 * Endpoint override + path-style addressing are configured from the
 * URL's host so MinIO and other S3-compatible providers Just Work
 * without baking provider-specific code into the protocol.
 */
public final class S3Connection implements AutoCloseable {

    private final S3Client client;
    private final S3AsyncClient asyncClient;
    private final S3TransferManager transferManager;
    private final String defaultRegion;

    private S3Connection(S3Client client, S3AsyncClient asyncClient,
                         S3TransferManager transferManager, String defaultRegion) {
        this.client = Objects.requireNonNull(client, "client");
        this.asyncClient = Objects.requireNonNull(asyncClient, "asyncClient");
        this.transferManager = Objects.requireNonNull(transferManager, "transferManager");
        this.defaultRegion = Objects.requireNonNull(defaultRegion, "defaultRegion");
    }

    public S3Client client() {
        return client;
    }

    public S3TransferManager transferManager() {
        return transferManager;
    }

    public String defaultRegion() {
        return defaultRegion;
    }

    @Override
    public void close() {
        // Order: close higher-level managers before the underlying clients.
        try { transferManager.close(); } catch (RuntimeException ignored) { }
        try { asyncClient.close(); } catch (RuntimeException ignored) { }
        client.close();
    }

    /**
     * Builds an S3 client from the inputs in an
     * {@link dev.barebones.commander.commons.file.FileURL}. The URL
     * shape we accept:
     *
     * <pre>
     *   s3://[ACCESS_KEY:SECRET_KEY@]HOSTNAME[:PORT]/BUCKET[/KEY...]
     * </pre>
     *
     * with optional URL properties {@code region} and {@code pathStyle}.
     */
    public static S3Connection open(
            String endpointHost, int port, String region,
            String accessKey, String secretKey,
            boolean pathStyleAccess, boolean useHttps) {

        String regionName = region == null || region.isBlank() ? Region.US_EAST_1.id() : region;
        URI endpoint = buildEndpoint(endpointHost, port, useHttps);

        AwsCredentialsProvider creds;
        if (accessKey != null && !accessKey.isBlank()
                && secretKey != null && !secretKey.isBlank()) {
            creds = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey));
        } else {
            creds = DefaultCredentialsProvider.create();
        }

        S3Configuration s3Config = S3Configuration.builder()
            .pathStyleAccessEnabled(pathStyleAccess)
            .build();

        S3Client client = S3Client.builder()
            .region(Region.of(regionName))
            .credentialsProvider(creds)
            .endpointOverride(endpoint)
            .serviceConfiguration(s3Config)
            .build();

        // S3AsyncClient (Java/Netty-based) is used by S3TransferManager
        // to drive multipart uploads of large objects. Using the
        // CRT-based client would be faster but adds a heavyweight
        // native dep we don't yet need.
        S3AsyncClient asyncClient = S3AsyncClient.builder()
            .region(Region.of(regionName))
            .credentialsProvider(creds)
            .endpointOverride(endpoint)
            .serviceConfiguration(s3Config)
            .build();

        S3TransferManager transferManager = S3TransferManager.builder()
            .s3Client(asyncClient)
            .build();

        return new S3Connection(client, asyncClient, transferManager, regionName);
    }

    static URI buildEndpoint(String host, int port, boolean useHttps) {
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("S3 endpoint host must not be blank");
        }
        StringBuilder sb = new StringBuilder(useHttps ? "https://" : "http://");
        sb.append(host);
        if (port > 0 && port != (useHttps ? 443 : 80)) {
            sb.append(':').append(port);
        }
        return URI.create(sb.toString());
    }
}
