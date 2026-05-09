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

public class LinuxMountCommandTest {

    private final LinuxMountCommand cmd = new LinuxMountCommand();

    @Test
    public void nfsv4() {
        MountSpec spec = new MountSpec(MountKind.NFSV4, "nfs.example.com",
            "/exports/data", Path.of("/mnt/nfs"), null, 0);
        assertEquals(cmd.mountArgv(spec), List.of(
            "mount.nfs4",
            "nfs.example.com:/exports/data",
            "/mnt/nfs"));
    }

    @Test
    public void nfsv3() {
        MountSpec spec = new MountSpec(MountKind.NFSV3, "old-nfs",
            "/data", Path.of("/mnt/old"), null, 0);
        assertEquals(cmd.mountArgv(spec), List.of(
            "mount.nfs",
            "old-nfs:/data",
            "/mnt/old"));
    }

    @Test
    public void smbWithUser() {
        MountSpec spec = new MountSpec(MountKind.SMB, "fileserver",
            "share", Path.of("/mnt/share"), "alice", 0);
        assertEquals(cmd.mountArgv(spec), List.of(
            "mount", "-t", "cifs",
            "//fileserver/share", "/mnt/share",
            "-o", "user=alice"));
    }

    @Test
    public void smbAnonymous() {
        MountSpec spec = new MountSpec(MountKind.SMB, "public",
            "guest", Path.of("/mnt/guest"), null, 0);
        assertEquals(cmd.mountArgv(spec), List.of(
            "mount", "-t", "cifs",
            "//public/guest", "/mnt/guest"));
    }

    @Test
    public void sshfsWithUserAndPort() {
        MountSpec spec = new MountSpec(MountKind.SSHFS, "host.example.com",
            "/home/alice", Path.of("/mnt/home-alice"), "alice", 2222);
        assertEquals(cmd.mountArgv(spec), List.of(
            "sshfs", "-p", "2222",
            "alice@host.example.com:/home/alice", "/mnt/home-alice"));
    }

    @Test
    public void sshfsDefaultPort() {
        MountSpec spec = new MountSpec(MountKind.SSHFS, "h",
            "/x", Path.of("/mnt/x"), "u", 0);
        assertEquals(cmd.mountArgv(spec), List.of(
            "sshfs",
            "u@h:/x", "/mnt/x"));
    }

    @Test
    public void unmountNfsUsesUmount() {
        MountSpec spec = new MountSpec(MountKind.NFSV4, "h",
            "/x", Path.of("/mnt/x"), null, 0);
        assertEquals(cmd.unmountArgv(spec), List.of("umount", "/mnt/x"));
    }

    @Test
    public void unmountSshfsUsesFusermount() {
        MountSpec spec = new MountSpec(MountKind.SSHFS, "h",
            "/x", Path.of("/mnt/x"), null, 0);
        assertEquals(cmd.unmountArgv(spec), List.of("fusermount", "-u", "/mnt/x"));
    }

    /**
     * Shell metacharacters in user-supplied fields must end up as
     * single argv entries, not split into new arguments. This is
     * THE injection-defence regression test for the helper.
     */
    @Test
    public void shellMetacharsStayContained() {
        MountSpec evilUser = new MountSpec(MountKind.SSHFS, "host",
            "/x", Path.of("/mnt/evil"), "alice; rm -rf /", 0);
        List<String> argv = cmd.mountArgv(evilUser);
        // The whole "alice; rm -rf /@host:/x" lives in ONE argv slot.
        assertEquals(argv.get(argv.size() - 2), "alice; rm -rf /@host:/x");
    }
}
