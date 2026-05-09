/*
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 */
package dev.barebones.commander.mount;

import org.testng.annotations.Test;

import java.nio.file.Path;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

public class MountRegistryTest {

    @Test
    public void recordRoundTrip() {
        MountRegistry r = MountRegistry.instance();
        MountSpec spec = new MountSpec(MountKind.NFSV4, "h", "/x",
            Path.of("/tmp/mount-roundtrip"), null, 0);

        r.recordMounted(spec);
        assertTrue(r.active().contains(spec));

        r.recordUnmounted(spec);
        assertEquals(r.active().stream().filter(s -> s.equals(spec)).count(), 0);
    }

    @Test
    public void rejectsDuplicateMountpoint() {
        MountRegistry r = MountRegistry.instance();
        Path mp = Path.of("/tmp/mount-dup");
        MountSpec a = new MountSpec(MountKind.NFSV4, "h1", "/x", mp, null, 0);
        MountSpec b = new MountSpec(MountKind.SMB,   "h2", "/y", mp, null, 0);

        r.recordMounted(a);
        try {
            assertThrows(IllegalStateException.class, () -> r.recordMounted(b));
        } finally {
            r.recordUnmounted(a);
        }
    }
}
