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
 * macOS mount command builder. NFS uses {@code mount_nfs} (handles
 * v2/v3/v4 via {@code -o vers=}); SMB uses {@code mount -t smbfs}
 * with a {@code //user@host/share} URL; SSHFS uses macFUSE's
 * {@code sshfs} binary if installed (otherwise the dialog should
 * surface "macFUSE not installed").
 */
public final class MacOSMountCommand implements MountCommand {

    @Override
    public List<String> mountArgv(MountSpec spec) {
        return switch (spec.kind()) {
            case NFSV4 -> nfsArgv(spec, "vers=4");
            case NFSV3 -> nfsArgv(spec, "vers=3");
            case SMB   -> smbArgv(spec);
            case SSHFS -> sshfsArgv(spec);
        };
    }

    @Override
    public List<String> unmountArgv(MountSpec spec) {
        return List.of("umount", spec.mountpoint().toString());
    }

    private static List<String> nfsArgv(MountSpec spec, String versOption) {
        return List.of(
                "mount_nfs",
                "-o", versOption,
                spec.host() + ":" + spec.remotePath(),
                spec.mountpoint().toString()
        );
    }

    private static List<String> smbArgv(MountSpec spec) {
        // mount -t smbfs //user@host/share /Volumes/...
        String userPart = spec.username() != null && !spec.username().isBlank()
                ? spec.username() + "@"
                : "";
        return List.of(
                "mount",
                "-t", "smbfs",
                "//" + userPart + spec.host() + "/" + spec.remotePath(),
                spec.mountpoint().toString()
        );
    }

    private static List<String> sshfsArgv(MountSpec spec) {
        // Same call shape as Linux; macFUSE provides /usr/local/bin/sshfs.
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
