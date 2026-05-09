/*
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 */
package dev.barebones.commander.mount;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class MountRegistryDrainTest {

    private final MountRegistry registry = MountRegistry.instance();

    @AfterMethod(alwaysRun = true)
    public void resetRegistry() {
        for (MountSpec s : registry.active()) {
            registry.recordUnmounted(s);
        }
    }

    /**
     * The drain should call unmount on every active spec and clear
     * the registry. We use {@code /usr/bin/true} as a stand-in for
     * the real mount tool so the test runs without root.
     */
    @Test
    public void drainsAllAndEmptiesRegistry() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        Path truePath = locateTrue();

        MountCommand cmd = new MountCommand() {
            @Override
            public List<String> mountArgv(MountSpec spec) {
                throw new UnsupportedOperationException();
            }
            @Override
            public List<String> unmountArgv(MountSpec spec) {
                calls.incrementAndGet();
                return List.of(truePath.toString());
            }
        };
        MountExecutor exec = new MountExecutor(cmd);

        Path mp1 = Files.createTempDirectory("mount-drain-1");
        Path mp2 = Files.createTempDirectory("mount-drain-2");
        try {
            registry.recordMounted(new MountSpec(MountKind.NFSV4, "h1", "/x", mp1, null, 0));
            registry.recordMounted(new MountSpec(MountKind.SMB,   "h2", "/y", mp2, null, 0));

            registry.drainAtShutdown(exec);

            assertEquals(calls.get(), 2, "unmount should be invoked once per active mount");
            assertTrue(registry.active().isEmpty(), "registry should be empty after drain");
        } finally {
            Files.deleteIfExists(mp1);
            Files.deleteIfExists(mp2);
        }
    }

    /**
     * A failing unmount (non-zero exit) must not block the drain
     * from continuing — the registry should still be empty.
     */
    @Test
    public void continuesPastFailures() throws Exception {
        Path falsePath = locateFalse();
        MountCommand cmd = new MountCommand() {
            @Override
            public List<String> mountArgv(MountSpec spec) {
                throw new UnsupportedOperationException();
            }
            @Override
            public List<String> unmountArgv(MountSpec spec) {
                return List.of(falsePath.toString());
            }
        };
        MountExecutor exec = new MountExecutor(cmd);

        Path mp1 = Files.createTempDirectory("mount-drain-fail-1");
        Path mp2 = Files.createTempDirectory("mount-drain-fail-2");
        try {
            registry.recordMounted(new MountSpec(MountKind.NFSV4, "h1", "/x", mp1, null, 0));
            registry.recordMounted(new MountSpec(MountKind.SMB,   "h2", "/y", mp2, null, 0));

            registry.drainAtShutdown(exec);

            assertTrue(registry.active().isEmpty(),
                "registry should be empty even when unmount returns non-zero");
        } finally {
            Files.deleteIfExists(mp1);
            Files.deleteIfExists(mp2);
        }
    }

    @Test
    public void emptyRegistryIsNoOp() {
        Path truePath = locateTrue();
        MountCommand cmd = new MountCommand() {
            @Override
            public List<String> mountArgv(MountSpec spec) {
                throw new UnsupportedOperationException();
            }
            @Override
            public List<String> unmountArgv(MountSpec spec) {
                return List.of(truePath.toString());
            }
        };
        registry.drainAtShutdown(new MountExecutor(cmd));
        assertTrue(registry.active().isEmpty());
    }

    private static Path locateTrue() {
        for (String p : new String[]{"/usr/bin/true", "/bin/true"}) {
            if (new File(p).canExecute()) {
                return Path.of(p);
            }
        }
        throw new IllegalStateException("no /bin/true on this platform");
    }

    private static Path locateFalse() {
        for (String p : new String[]{"/usr/bin/false", "/bin/false"}) {
            if (new File(p).canExecute()) {
                return Path.of(p);
            }
        }
        throw new IllegalStateException("no /bin/false on this platform");
    }
}
