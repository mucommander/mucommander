/*
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 */
package dev.barebones.commander.commons.file.protocol.sftp;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class SftpTimeoutsTest {

    @AfterMethod(alwaysRun = true)
    public void clearProps() {
        System.clearProperty(SftpTimeouts.CONNECT_PROP);
        System.clearProperty(SftpTimeouts.READ_PROP);
        System.clearProperty(SftpTimeouts.SERVER_ALIVE_PROP);
    }

    @Test
    public void defaultsWhenUnset() {
        assertEquals(SftpTimeouts.connectMs(), SftpTimeouts.DEFAULT_CONNECT_MS);
        assertEquals(SftpTimeouts.readMs(), SftpTimeouts.DEFAULT_READ_MS);
        assertEquals(SftpTimeouts.serverAliveSec(), SftpTimeouts.DEFAULT_SERVER_ALIVE_SEC);
    }

    @Test
    public void overridesApplied() {
        System.setProperty(SftpTimeouts.CONNECT_PROP, "30000");
        System.setProperty(SftpTimeouts.READ_PROP, "120000");
        System.setProperty(SftpTimeouts.SERVER_ALIVE_PROP, "10");
        assertEquals(SftpTimeouts.connectMs(), 30_000);
        assertEquals(SftpTimeouts.readMs(), 120_000);
        assertEquals(SftpTimeouts.serverAliveSec(), 10);
    }

    @Test
    public void zeroReadDisablesTimeout() {
        // Read of 0 means "no SO_TIMEOUT" — preserve the override.
        System.setProperty(SftpTimeouts.READ_PROP, "0");
        assertEquals(SftpTimeouts.readMs(), 0);
    }

    @Test
    public void zeroServerAliveDisablesKeepalive() {
        System.setProperty(SftpTimeouts.SERVER_ALIVE_PROP, "0");
        assertEquals(SftpTimeouts.serverAliveSec(), 0);
    }

    @Test
    public void zeroConnectFallsBackToDefault() {
        // Connect must be > 0 (JSch hangs forever on 0).
        System.setProperty(SftpTimeouts.CONNECT_PROP, "0");
        assertEquals(SftpTimeouts.connectMs(), SftpTimeouts.DEFAULT_CONNECT_MS);
    }

    @Test
    public void garbageFallsBackToDefault() {
        System.setProperty(SftpTimeouts.CONNECT_PROP, "not-a-number");
        System.setProperty(SftpTimeouts.READ_PROP, "");
        System.setProperty(SftpTimeouts.SERVER_ALIVE_PROP, "-1");
        assertEquals(SftpTimeouts.connectMs(), SftpTimeouts.DEFAULT_CONNECT_MS);
        assertEquals(SftpTimeouts.readMs(), SftpTimeouts.DEFAULT_READ_MS);
        assertEquals(SftpTimeouts.serverAliveSec(), SftpTimeouts.DEFAULT_SERVER_ALIVE_SEC);
    }
}
