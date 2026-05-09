/*
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 */
package dev.barebones.commander.mount;

import java.util.ArrayList;
import java.util.List;

/**
 * Linux mount command builder. NFSv3/4 use {@code mount.nfs} /
 * {@code mount.nfs4} (typically requires root or a setuid helper);
 * SMB uses {@code mount -t cifs}; SSHFS uses the {@code sshfs} FUSE
 * binary which runs unprivileged.
 *
 * Each spec field is passed as its own argv entry — no shell, no
 * concatenation, no quoting needed.
 */
public final class LinuxMountCommand implements MountCommand {

    @Override
    public List<String> mountArgv(MountSpec spec) {
        return switch (spec.kind()) {
            case NFSV4 -> nfsArgv(spec, "mount.nfs4");
            case NFSV3 -> nfsArgv(spec, "mount.nfs");
            case SMB   -> smbArgv(spec);
            case SSHFS -> sshfsArgv(spec);
        };
    }

    @Override
    public List<String> unmountArgv(MountSpec spec) {
        return switch (spec.kind()) {
            case NFSV4, NFSV3, SMB -> List.of("umount", spec.mountpoint().toString());
            // SSHFS unmount is fusermount -u (Linux) — a regular umount
            // on a FUSE mount can fail if the FUSE daemon is still
            // holding it.
            case SSHFS -> List.of("fusermount", "-u", spec.mountpoint().toString());
        };
    }

    private static List<String> nfsArgv(MountSpec spec, String binary) {
        return List.of(
                binary,
                spec.host() + ":" + spec.remotePath(),
                spec.mountpoint().toString()
        );
    }

    private static List<String> smbArgv(MountSpec spec) {
        // mount -t cifs //host/share /mnt -o user=alice
        // username goes through a typed -o option, not embedded in
        // the URL — keeps shell metacharacters in the username from
        // changing argument boundaries.
        List<String> argv = new ArrayList<>();
        argv.add("mount");
        argv.add("-t");
        argv.add("cifs");
        argv.add("//" + spec.host() + "/" + spec.remotePath());
        argv.add(spec.mountpoint().toString());
        if (spec.username() != null && !spec.username().isBlank()) {
            argv.add("-o");
            argv.add("user=" + spec.username());
        }
        return List.copyOf(argv);
    }

    private static List<String> sshfsArgv(MountSpec spec) {
        // sshfs [-p PORT] user@host:/path /mnt
        List<String> argv = new ArrayList<>();
        argv.add("sshfs");
        if (spec.port() > 0) {
            argv.add("-p");
            argv.add(Integer.toString(spec.port()));
        }
        String userHost = spec.username() != null && !spec.username().isBlank()
                ? spec.username() + "@" + spec.host()
                : spec.host();
        argv.add(userHost + ":" + spec.remotePath());
        argv.add(spec.mountpoint().toString());
        return List.copyOf(argv);
    }
}
