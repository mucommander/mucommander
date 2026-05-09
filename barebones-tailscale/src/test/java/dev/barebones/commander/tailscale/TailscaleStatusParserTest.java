/*
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 */
package dev.barebones.commander.tailscale;

import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

public class TailscaleStatusParserTest {

    private static String loadFixture(String name) throws IOException {
        try (InputStream in = TailscaleStatusParserTest.class.getResourceAsStream(name)) {
            Objects.requireNonNull(in, "fixture not found: " + name);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    public void parsesAllPeers() throws Exception {
        List<TailscalePeer> peers = TailscaleStatusParser.parse(loadFixture("status-sample.json"));
        assertEquals(peers.size(), 3);
    }

    @Test
    public void stripsTrailingDotFromDnsName() throws Exception {
        List<TailscalePeer> peers = TailscaleStatusParser.parse(loadFixture("status-sample.json"));
        for (TailscalePeer p : peers) {
            assertFalse(p.dnsName().endsWith("."),
                "DNS name should not end with '.': " + p.dnsName());
        }
    }

    @Test
    public void preservesIPv4AndIPv6() throws Exception {
        List<TailscalePeer> peers = TailscaleStatusParser.parse(loadFixture("status-sample.json"));
        TailscalePeer alice = peers.stream()
            .filter(p -> p.hostName().equals("alice-laptop"))
            .findFirst().orElseThrow();
        assertEquals(alice.tailscaleIPs(), List.of("100.64.0.10", "fd7a:115c:a1e0::a"));
    }

    @Test
    public void preservesOnlineFlag() throws Exception {
        List<TailscalePeer> peers = TailscaleStatusParser.parse(loadFixture("status-sample.json"));
        TailscalePeer offline = peers.stream()
            .filter(p -> p.hostName().equals("old-mac"))
            .findFirst().orElseThrow();
        assertFalse(offline.online());

        TailscalePeer online = peers.stream()
            .filter(p -> p.hostName().equals("alice-laptop"))
            .findFirst().orElseThrow();
        assertTrue(online.online());
    }

    @Test
    public void emptyJsonReturnsEmptyList() {
        assertEquals(TailscaleStatusParser.parse(""), List.of());
        assertEquals(TailscaleStatusParser.parse(null), List.of());
    }

    @Test
    public void noPeerSectionReturnsEmptyList() {
        assertEquals(TailscaleStatusParser.parse("{\"Self\":{\"HostName\":\"x\"}}"), List.of());
    }

    @Test
    public void invalidJsonThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> TailscaleStatusParser.parse("not valid json"));
    }
}
