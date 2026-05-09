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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class HostKeyPrompterTest {

    private HostKeyPrompter saved;

    @BeforeMethod
    public void capture() {
        saved = HostKeyPrompter.current();
    }

    @AfterMethod
    public void restore() {
        HostKeyPrompter.setDefault(saved);
    }

    @Test
    public void setDefaultReplacesCurrent() {
        AtomicReference<String> seen = new AtomicReference<>();
        HostKeyPrompter rejecter = msg -> { seen.set(msg); return false; };
        HostKeyPrompter.setDefault(rejecter);

        boolean accepted = HostKeyPrompter.current().shouldAcceptHostKey("hello");
        assertFalse(accepted);
        assertEquals(seen.get(), "hello");
    }

    @Test
    public void promptAccepts() {
        HostKeyPrompter.setDefault(msg -> true);
        assertTrue(HostKeyPrompter.current().shouldAcceptHostKey("anything"));
    }

    @Test
    public void promptRejects() {
        HostKeyPrompter.setDefault(msg -> false);
        assertFalse(HostKeyPrompter.current().shouldAcceptHostKey("anything"));
    }
}
