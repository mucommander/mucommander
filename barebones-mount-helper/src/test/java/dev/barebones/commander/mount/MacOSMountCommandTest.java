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
import java.util.List;

import static org.testng.Assert.assertEquals;

public class MacOSMountCommandTest {

    private final MacOSMountCommand cmd = new MacOSMountCommand();

    @Test
    public void nfsv4UsesVers4Option() {
        MountSpec spec = new MountSpec(MountKind.NFSV4, "nfs.example.com",
            "/exports/data", Path.of("/Volumes/data"), null, 0);
        assertEquals(cmd.mountArgv(spec), List.of(
            "mount_nfs",
            "-o", "vers=4",
            "nfs.example.com:/exports/data",
            "/Volumes/data"));
    }

    @Test
    public void nfsv3UsesVers3Option() {
        MountSpec spec = new MountSpec(MountKind.NFSV3, "old",
            "/data", Path.of("/Volumes/old"), null, 0);
        assertEquals(cmd.mountArgv(spec), List.of(
            "mount_nfs",
            "-o", "vers=3",
            "old:/data",
            "/Volumes/old"));
    }

    @Test
    public void smbWithUserGoesIntoUrl() {
        MountSpec spec = new MountSpec(MountKind.SMB, "fileserver",
            "share", Path.of("/Volumes/share"), "alice", 0);
        assertEquals(cmd.mountArgv(spec), List.of(
            "mount", "-t", "smbfs",
            "//alice@fileserver/share", "/Volumes/share"));
    }

    @Test
    public void smbAnonymous() {
        MountSpec spec = new MountSpec(MountKind.SMB, "public",
            "guest", Path.of("/Volumes/g"), null, 0);
        assertEquals(cmd.mountArgv(spec), List.of(
            "mount", "-t", "smbfs",
            "//public/guest", "/Volumes/g"));
    }

    @Test
    public void sshfsCustomPort() {
        MountSpec spec = new MountSpec(MountKind.SSHFS, "h", "/x",
            Path.of("/Volumes/x"), "u", 2200);
        assertEquals(cmd.mountArgv(spec), List.of(
            "sshfs", "-p", "2200",
            "u@h:/x", "/Volumes/x"));
    }

    @Test
    public void unmountAlwaysUmount() {
        MountSpec spec = new MountSpec(MountKind.SSHFS, "h", "/x",
            Path.of("/Volumes/x"), null, 0);
        // macOS's umount handles FUSE mounts (no fusermount on macOS).
        assertEquals(cmd.unmountArgv(spec), List.of("umount", "/Volumes/x"));
    }
}
