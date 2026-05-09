/*
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 */
package dev.barebones.commander.commons.file.protocol.s3;

import dev.barebones.commander.commons.file.AbstractFile;
import dev.barebones.commander.commons.file.AuthenticationType;
import dev.barebones.commander.commons.file.Credentials;
import dev.barebones.commander.commons.file.DefaultSchemeHandler;
import dev.barebones.commander.commons.file.DefaultSchemeParser;
import dev.barebones.commander.commons.file.FileFactory;
import dev.barebones.commander.commons.file.FileURL;
import dev.barebones.commander.commons.file.SchemeHandler;
import dev.barebones.commander.commons.file.osgi.FileProtocolService;
import dev.barebones.commander.commons.file.osgi.FileProtocolServiceTracker;
import dev.barebones.commander.commons.file.protocol.ProtocolProvider;

import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * End-to-end tests against a real S3-compatible endpoint provided
 * by a LocalStack Docker container. Verifies that the AWS-SDK-v2-
 * backed module actually moves bytes across the wire — a unit-test
 * suite alone can't catch URL-shape mismatches, signature errors,
 * or path-style addressing regressions.
 *
 * Skipped when Docker isn't available (e.g. on the macos-15 GitHub
 * runner). Runs unconditionally on ubuntu-latest where the runner
 * ships with Docker.
 */
public class S3LocalStackIntegrationTest {

    private static final DockerImageName LOCALSTACK_IMAGE =
        DockerImageName.parse("localstack/localstack:3.7.2");

    private LocalStackContainer container;
    private S3ProtocolProvider provider;
    private String bucketName;

    @BeforeClass
    public void startLocalStack() throws Exception {
        if (!DockerClientFactory.instance().isDockerAvailable()) {
            throw new SkipException(
                "Docker is not available on this runner; skipping LocalStack S3 integration tests.");
        }

        container = new LocalStackContainer(LOCALSTACK_IMAGE)
            .withServices(LocalStackContainer.Service.S3);
        container.start();

        // FileURL needs a SchemeHandler registered for "s3" before
        // it will parse one. The FileProtocolServiceTracker does this
        // at runtime; for an isolated test we register directly.
        SchemeHandler handler = new DefaultSchemeHandler(
            new DefaultSchemeParser(), 443, "/",
            AuthenticationType.AUTHENTICATION_REQUIRED, null);
        Method m = FileURL.class.getDeclaredMethod("registerHandler",
            String.class, SchemeHandler.class);
        m.setAccessible(true);
        m.invoke(null, "s3", handler);

        // FileFactory needs a ProtocolProvider for "s3" too.
        provider = new S3ProtocolProvider();
        FileProtocolServiceTracker.register(new FileProtocolService() {
            @Override public String getSchema() { return "s3"; }
            @Override public ProtocolProvider getProtocolProvider() { return provider; }
            @Override public SchemeHandler getSchemeHandler() { return handler; }
        });

        bucketName = "test-" + UUID.randomUUID().toString().substring(0, 8);
    }

    @AfterClass(alwaysRun = true)
    public void stopLocalStack() {
        if (container != null) {
            container.stop();
        }
    }

    private FileURL urlFor(String path) throws Exception {
        // LocalStack S3 endpoint = http://<host>:<port>/, path-style.
        java.net.URI endpoint = container.getEndpointOverride(LocalStackContainer.Service.S3);
        FileURL url = FileURL.getFileURL(
            "s3://" + endpoint.getHost() + path);
        url.setPort(endpoint.getPort());
        url.setCredentials(new Credentials(
            container.getAccessKey(), container.getSecretKey()));
        url.setProperty(S3ProtocolProvider.PROPERTY_REGION, container.getRegion());
        url.setProperty(S3ProtocolProvider.PROPERTY_PATH_STYLE, "true");
        url.setProperty(S3ProtocolProvider.PROPERTY_USE_HTTPS, "false");
        return url;
    }

    @Test
    public void fullLifecycle() throws Exception {
        // mkdir on a bucket URL → bucket exists; root.ls() lists it.
        AbstractFile bucket = FileFactory.getFile(urlFor("/" + bucketName + "/"));
        assertTrue(bucket instanceof S3Bucket);
        bucket.mkdir();
        assertTrue(bucket.exists(), "bucket should exist after mkdir");

        AbstractFile root = FileFactory.getFile(urlFor("/"));
        AbstractFile[] buckets = root.ls();
        Set<String> names = new HashSet<>();
        for (AbstractFile b : buckets) {
            names.add(b.getName());
        }
        assertTrue(names.contains(bucketName),
            "root listing should include " + bucketName + " but got " + names);

        // put → object exists, sized, lists in the bucket
        AbstractFile object = FileFactory.getFile(urlFor("/" + bucketName + "/hello.txt"));
        assertTrue(object instanceof S3Object);
        byte[] payload = "hello s3 world".getBytes(StandardCharsets.UTF_8);
        try (OutputStream os = object.getOutputStream()) {
            os.write(payload);
        }

        AbstractFile[] children = bucket.ls();
        assertEquals(children.length, 1, "bucket should contain exactly one object");
        assertEquals(children[0].getName(), "hello.txt");
        assertEquals(children[0].getSize(), payload.length);

        // get → bytes round-trip
        try (InputStream is = object.getInputStream()) {
            byte[] got = is.readAllBytes();
            assertEquals(got, payload);
        }

        // mkdir on an object URL → empty zero-byte object with trailing /
        AbstractFile dir = FileFactory.getFile(urlFor("/" + bucketName + "/sub/"));
        dir.mkdir();
        AbstractFile[] afterMkdir = bucket.ls();
        names = new HashSet<>();
        for (AbstractFile c : afterMkdir) {
            names.add(c.getName());
        }
        assertTrue(names.contains("hello.txt"), "still has the file");
        assertTrue(names.contains("sub"), "now also has the prefix " + names);

        // put inside the prefix; bucket-level ls() shouldn't surface it
        // (delimiter='/'); the prefix-level ls() should.
        AbstractFile nested = FileFactory.getFile(urlFor("/" + bucketName + "/sub/inner.txt"));
        try (OutputStream os = nested.getOutputStream()) {
            os.write("nested".getBytes(StandardCharsets.UTF_8));
        }
        AbstractFile[] innerListing = dir.ls();
        assertEquals(innerListing.length, 1);
        assertEquals(innerListing[0].getName(), "inner.txt");

        // delete the file → it goes away
        nested.delete();
        AbstractFile[] afterDelete = dir.ls();
        assertEquals(afterDelete.length, 0);

        // delete the original
        object.delete();
    }

    @Test
    public void pagedListingAcrossContinuationTokens() throws Exception {
        String bucket2 = "paged-" + UUID.randomUUID().toString().substring(0, 8);
        AbstractFile bucket = FileFactory.getFile(urlFor("/" + bucket2 + "/"));
        bucket.mkdir();

        // Put 1500 objects. ListObjectsV2 default page size = 1000;
        // a correct paging implementation walks all of them.
        int objectCount = 1500;
        byte[] tiny = new byte[]{1};
        for (int i = 0; i < objectCount; i++) {
            AbstractFile o = FileFactory.getFile(
                urlFor("/" + bucket2 + "/k" + String.format("%05d", i)));
            try (OutputStream os = o.getOutputStream()) {
                os.write(tiny);
            }
        }

        AbstractFile[] listing = bucket.ls();
        assertEquals(listing.length, objectCount,
            "paged listing should return ALL objects, got " + listing.length);
    }

    @Test
    public void renameCopiesAndDeletes() throws Exception {
        String bucket3 = "rename-" + UUID.randomUUID().toString().substring(0, 8);
        AbstractFile bucket = FileFactory.getFile(urlFor("/" + bucket3 + "/"));
        bucket.mkdir();

        AbstractFile src = FileFactory.getFile(urlFor("/" + bucket3 + "/source.txt"));
        try (OutputStream os = src.getOutputStream()) {
            os.write("rename me".getBytes(StandardCharsets.UTF_8));
        }
        AbstractFile dest = FileFactory.getFile(urlFor("/" + bucket3 + "/dest.txt"));
        src.renameTo(dest);

        // dest exists with the right bytes; src is gone.
        try (InputStream is = dest.getInputStream()) {
            assertEquals(new String(is.readAllBytes(), StandardCharsets.UTF_8), "rename me");
        }
        AbstractFile[] children = bucket.ls();
        Set<String> names = new HashSet<>();
        for (AbstractFile c : children) {
            names.add(c.getName());
        }
        assertTrue(names.contains("dest.txt"));
        assertFalse(names.contains("source.txt"),
            "source.txt should be gone after rename, got " + names);
    }

    @Test
    public void largeUploadGoesThroughTransferManager() throws Exception {
        // 40 MiB > the SPILL_THRESHOLD (32 MiB) hardcoded in
        // SpillingPutOutputStream — exercises the temp-file +
        // S3TransferManager multipart path.
        String bucket4 = "large-" + UUID.randomUUID().toString().substring(0, 8);
        AbstractFile bucket = FileFactory.getFile(urlFor("/" + bucket4 + "/"));
        bucket.mkdir();

        AbstractFile big = FileFactory.getFile(urlFor("/" + bucket4 + "/big.bin"));
        int size = 40 * 1024 * 1024;
        byte[] chunk = new byte[64 * 1024];
        Arrays.fill(chunk, (byte) 0x42);
        try (OutputStream os = big.getOutputStream()) {
            int written = 0;
            while (written < size) {
                int len = Math.min(chunk.length, size - written);
                os.write(chunk, 0, len);
                written += len;
            }
        }
        // Round-trip the size + a sample of the contents.
        AbstractFile[] children = bucket.ls();
        assertEquals(children.length, 1);
        assertEquals(children[0].getSize(), size);

        try (InputStream is = big.getInputStream()) {
            byte[] firstChunk = is.readNBytes(chunk.length);
            assertEquals(firstChunk, chunk, "first chunk content mismatch");
        }
    }

    /** Smoke: in-memory PUT path produces the same bytes (pre-spill threshold). */
    @Test
    public void smallUploadStaysInMemory() throws Exception {
        String bucket5 = "small-" + UUID.randomUUID().toString().substring(0, 8);
        AbstractFile bucket = FileFactory.getFile(urlFor("/" + bucket5 + "/"));
        bucket.mkdir();

        AbstractFile o = FileFactory.getFile(urlFor("/" + bucket5 + "/tiny.txt"));
        byte[] payload = "x".repeat(1024).getBytes(StandardCharsets.UTF_8);
        try (OutputStream os = o.getOutputStream()) {
            os.write(payload);
        }
        try (InputStream is = o.getInputStream();
             ByteArrayOutputStream sink = new ByteArrayOutputStream()) {
            is.transferTo(sink);
            assertEquals(sink.toByteArray(), payload);
        }
    }
}
