/*
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 */
package dev.barebones.commander.commons.file.protocol.s3;

import dev.barebones.commander.commons.file.AuthenticationType;
import dev.barebones.commander.commons.file.DefaultSchemeHandler;
import dev.barebones.commander.commons.file.DefaultSchemeParser;
import dev.barebones.commander.commons.file.FileURL;
import dev.barebones.commander.commons.file.SchemeHandler;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.net.MalformedURLException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class S3FileURLTest {

    @BeforeClass
    public void registerS3Scheme() throws Exception {
        // FileURL.getFileURL needs a SchemeHandler registered for "s3"
        // before parsing. Hits the same FileURL.registerHandler that
        // the FileProtocolServiceTracker uses at runtime.
        SchemeHandler handler = new DefaultSchemeHandler(
            new DefaultSchemeParser(), 443, "/",
            AuthenticationType.AUTHENTICATION_REQUIRED, null);
        Method m = FileURL.class.getDeclaredMethod("registerHandler",
            String.class, SchemeHandler.class);
        m.setAccessible(true);
        m.invoke(null, "s3", handler);
    }

    @Test
    public void rootHasEmptyBucketAndKey() throws MalformedURLException {
        S3FileURL parsed = S3FileURL.parse(FileURL.getFileURL("s3://s3.amazonaws.com/"));
        assertTrue(parsed.isRoot());
        assertFalse(parsed.isBucketRoot());
        assertFalse(parsed.isObject());
        assertEquals(parsed.bucket(), "");
        assertEquals(parsed.key(), "");
    }

    @Test
    public void bucketRoot() throws MalformedURLException {
        S3FileURL parsed = S3FileURL.parse(FileURL.getFileURL("s3://s3.amazonaws.com/my-bucket/"));
        assertFalse(parsed.isRoot());
        assertTrue(parsed.isBucketRoot());
        assertFalse(parsed.isObject());
        assertEquals(parsed.bucket(), "my-bucket");
        assertEquals(parsed.key(), "");
    }

    @Test
    public void deepObject() throws MalformedURLException {
        S3FileURL parsed = S3FileURL.parse(
            FileURL.getFileURL("s3://s3.amazonaws.com/my-bucket/path/to/file.txt"));
        assertTrue(parsed.isObject());
        assertEquals(parsed.bucket(), "my-bucket");
        assertEquals(parsed.key(), "path/to/file.txt");
    }

    @Test
    public void directoryKeyKeepsTrailingSlash() throws MalformedURLException {
        S3FileURL parsed = S3FileURL.parse(
            FileURL.getFileURL("s3://s3.amazonaws.com/my-bucket/some/folder/"));
        assertTrue(parsed.isObject());
        assertEquals(parsed.bucket(), "my-bucket");
        assertEquals(parsed.key(), "some/folder/");
    }

    @Test
    public void minioStylePortAndPathStyle() throws MalformedURLException {
        S3FileURL parsed = S3FileURL.parse(
            FileURL.getFileURL("s3://minio.local:9000/photos/2026/img.jpg"));
        assertEquals(parsed.endpointHost(), "minio.local");
        assertEquals(parsed.endpointPort(), 9000);
        assertEquals(parsed.bucket(), "photos");
        assertEquals(parsed.key(), "2026/img.jpg");
    }
}
