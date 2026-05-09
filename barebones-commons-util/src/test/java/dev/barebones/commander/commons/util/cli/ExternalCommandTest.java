/*
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 */
package dev.barebones.commander.commons.util.cli;

import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

public class ExternalCommandTest {

    @Test
    public void exitZeroAndStdoutCaptured() throws Exception {
        ExternalCommand.Result r = ExternalCommand.run(
            List.of("/bin/echo", "hello world"), 5, TimeUnit.SECONDS);
        assertTrue(r.ok());
        assertEquals(r.exitCode(), 0);
        assertEquals(r.stdout(), "hello world\n");
        assertEquals(r.stderr(), "");
    }

    @Test
    public void nonZeroExitCaptured() throws Exception {
        ExternalCommand.Result r = ExternalCommand.run(
            List.of("/bin/sh", "-c", "exit 7"), 5, TimeUnit.SECONDS);
        assertEquals(r.exitCode(), 7);
        assertFalse(r.ok());
    }

    @Test
    public void stderrCapturedSeparately() throws Exception {
        ExternalCommand.Result r = ExternalCommand.run(
            List.of("/bin/sh", "-c", "echo OUT; echo ERR 1>&2"),
            5, TimeUnit.SECONDS);
        assertEquals(r.stdout(), "OUT\n");
        assertEquals(r.stderr(), "ERR\n");
    }

    /**
     * The whole reason this helper exists: a child that fills
     * stderr beyond the pipe buffer (~64 KiB) used to deadlock the
     * old "waitFor then read stderr" pattern. Concurrent drainers
     * fix it. We push 256 KiB to stderr to make sure.
     */
    @Test
    public void largeStderrDoesNotDeadlock() throws Exception {
        // Generate ~256 KiB of stderr by yes piped into head -c.
        ExternalCommand.Result r = ExternalCommand.run(
            List.of("/bin/sh", "-c",
                "yes \"this is some chatter for stderr\" | head -c 262144 1>&2"),
            10, TimeUnit.SECONDS);
        assertEquals(r.exitCode(), 0);
        assertEquals(r.stderr().length(), 262144);
    }

    @Test
    public void timeoutDestroysAndThrows() throws InterruptedException {
        try {
            ExternalCommand.run(List.of("/bin/sleep", "30"), 200, TimeUnit.MILLISECONDS);
            org.testng.Assert.fail("expected IOException for timeout");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("timed out"),
                "expected 'timed out' in: " + e.getMessage());
        }
    }

    @Test
    public void emptyArgvRejected() {
        assertThrows(IllegalArgumentException.class,
            () -> ExternalCommand.run(List.of(), 1, TimeUnit.SECONDS));
    }

    @Test
    public void zeroOrNegativeTimeoutRejected() {
        assertThrows(IllegalArgumentException.class,
            () -> ExternalCommand.run(List.of("/bin/echo"), 0, TimeUnit.SECONDS));
        assertThrows(IllegalArgumentException.class,
            () -> ExternalCommand.run(List.of("/bin/echo"), -1, TimeUnit.SECONDS));
    }

    @Test
    public void unknownCommandIOException() {
        // Non-existent binary path → ProcessBuilder.start throws IOException,
        // not a misleading "timed out".
        assertThrows(IOException.class, () ->
            ExternalCommand.run(
                List.of("/no/such/binary/exists"),
                1, TimeUnit.SECONDS));
    }
}
