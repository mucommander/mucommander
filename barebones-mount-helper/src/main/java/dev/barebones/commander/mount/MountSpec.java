/*
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 */
package dev.barebones.commander.mount;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Description of a remote share to mount and where to mount it.
 *
 * @param kind         protocol family (NFSv3/v4, SMB, SSHFS)
 * @param host         remote host (DNS name or IP); never null/blank
 * @param remotePath   server-side share / export path; never null/blank
 *                     (e.g. "/exports/data" for NFS, "share" for SMB,
 *                     "/home/user" for SSHFS)
 * @param mountpoint   absolute local path the OS should mount to; never null
 * @param username     optional remote user (SMB / SSHFS); null if anonymous
 * @param port         optional non-default port (SSHFS / SMB); 0 == default
 */
public record MountSpec(
        MountKind kind,
        String host,
        String remotePath,
        Path mountpoint,
        String username,
        int port
) {
    public MountSpec {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(host, "host");
        Objects.requireNonNull(remotePath, "remotePath");
        Objects.requireNonNull(mountpoint, "mountpoint");
        if (host.isBlank()) {
            throw new IllegalArgumentException("host must not be blank");
        }
        if (remotePath.isBlank()) {
            throw new IllegalArgumentException("remotePath must not be blank");
        }
        if (!mountpoint.isAbsolute()) {
            throw new IllegalArgumentException("mountpoint must be absolute: " + mountpoint);
        }
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("port out of range: " + port);
        }
    }
}
