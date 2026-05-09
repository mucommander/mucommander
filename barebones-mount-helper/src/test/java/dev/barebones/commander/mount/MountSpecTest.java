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

import static org.testng.Assert.assertThrows;

public class MountSpecTest {

    @Test
    public void rejectsBlankHost() {
        assertThrows(IllegalArgumentException.class, () ->
            new MountSpec(MountKind.NFSV4, "  ", "/x",
                Path.of("/mnt/x"), null, 0));
    }

    @Test
    public void rejectsBlankRemotePath() {
        assertThrows(IllegalArgumentException.class, () ->
            new MountSpec(MountKind.NFSV4, "h", "",
                Path.of("/mnt/x"), null, 0));
    }

    @Test
    public void rejectsRelativeMountpoint() {
        assertThrows(IllegalArgumentException.class, () ->
            new MountSpec(MountKind.NFSV4, "h", "/x",
                Path.of("relative/path"), null, 0));
    }

    @Test
    public void rejectsOutOfRangePort() {
        assertThrows(IllegalArgumentException.class, () ->
            new MountSpec(MountKind.SSHFS, "h", "/x",
                Path.of("/mnt/x"), null, 70_000));
    }

    @Test
    public void rejectsNegativePort() {
        assertThrows(IllegalArgumentException.class, () ->
            new MountSpec(MountKind.SSHFS, "h", "/x",
                Path.of("/mnt/x"), null, -1));
    }
}
