/*
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 */
package dev.barebones.commander.commons.file.protocol.s3;

import org.testng.annotations.Test;

import java.net.URI;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;

public class S3ConnectionEndpointTest {

    @Test
    public void httpsEndpointDropsDefault443() {
        URI uri = S3Connection.buildEndpoint("s3.amazonaws.com", 443, true);
        assertEquals(uri, URI.create("https://s3.amazonaws.com"));
    }

    @Test
    public void httpEndpointDropsDefault80() {
        URI uri = S3Connection.buildEndpoint("internal-s3", 80, false);
        assertEquals(uri, URI.create("http://internal-s3"));
    }

    @Test
    public void minioPortIncluded() {
        URI uri = S3Connection.buildEndpoint("minio.local", 9000, false);
        assertEquals(uri, URI.create("http://minio.local:9000"));
    }

    @Test
    public void zeroPortMeansDefault() {
        URI uri = S3Connection.buildEndpoint("s3.example.com", 0, true);
        assertEquals(uri, URI.create("https://s3.example.com"));
    }

    @Test
    public void blankHostRejected() {
        assertThrows(IllegalArgumentException.class,
            () -> S3Connection.buildEndpoint("  ", 9000, false));
    }
}
