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
import static org.testng.Assert.assertThrows;

public class HostKeyPolicyTest {

    @AfterMethod
    public void clearProperty() {
        System.clearProperty("barebones.sftp.hostKey");
    }

    @Test
    public void defaultIsAsk() {
        // Property unset → resolves to ASK.
        assertEquals(HostKeyPolicy.fromSystemProperty(), HostKeyPolicy.ASK);
    }

    @Test
    public void resolvesYes() {
        System.setProperty("barebones.sftp.hostKey", "yes");
        assertEquals(HostKeyPolicy.fromSystemProperty(), HostKeyPolicy.YES);
    }

    @Test
    public void resolvesAsk() {
        System.setProperty("barebones.sftp.hostKey", "ask");
        assertEquals(HostKeyPolicy.fromSystemProperty(), HostKeyPolicy.ASK);
    }

    @Test
    public void resolvesNo() {
        System.setProperty("barebones.sftp.hostKey", "no");
        assertEquals(HostKeyPolicy.fromSystemProperty(), HostKeyPolicy.NO);
    }

    @Test
    public void caseInsensitiveAndTrimmed() {
        System.setProperty("barebones.sftp.hostKey", "  YES  ");
        assertEquals(HostKeyPolicy.fromSystemProperty(), HostKeyPolicy.YES);
    }

    @Test
    public void invalidValueRejected() {
        System.setProperty("barebones.sftp.hostKey", "maybe");
        assertThrows(IllegalArgumentException.class, HostKeyPolicy::fromSystemProperty);
    }

    @Test
    public void jschValuesMatchOpenSshNames() {
        // JSch reads OpenSSH-style values; if the mapping ever drifts
        // every SFTP connection silently flips to a different policy.
        assertEquals(HostKeyPolicy.YES.jschValue(), "yes");
        assertEquals(HostKeyPolicy.ASK.jschValue(), "ask");
        assertEquals(HostKeyPolicy.NO.jschValue(), "no");
    }
}
