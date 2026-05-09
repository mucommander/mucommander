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

import java.nio.file.Path;

import static org.testng.Assert.assertThrows;

public class TailscaleClientTest {

    @Test
    public void rejectsNullBinary() {
        assertThrows(IllegalArgumentException.class, () -> new TailscaleClient(null));
    }

    @Test
    public void rejectsZeroTimeout() {
        assertThrows(IllegalArgumentException.class,
            () -> new TailscaleClient(Path.of("/usr/bin/tailscale"), 0));
    }

    @Test
    public void sendFileRejectsNullPath() {
        TailscaleClient c = new TailscaleClient(Path.of("/usr/bin/tailscale"));
        assertThrows(IllegalArgumentException.class,
            () -> c.sendFile(null, "peer.ts.net"));
    }

    @Test
    public void sendFileRejectsBlankPeer() {
        TailscaleClient c = new TailscaleClient(Path.of("/usr/bin/tailscale"));
        assertThrows(IllegalArgumentException.class,
            () -> c.sendFile(Path.of("/tmp/x"), ""));
    }
}
