/*
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 */
package dev.barebones.commander.mount;

import java.util.List;

/**
 * Builds the OS-specific argv for mount / unmount of a {@link MountSpec}.
 *
 * Implementations return a {@link List}{@code <String>} suitable for
 * passing straight to {@code new ProcessBuilder(argv)} — never a single
 * concatenated command string. This is the structural defence against
 * shell-injection from attacker-controlled host / share / username
 * fields. The same {@code no-tls-bypass} CI gate from Phase 5 keeps a
 * sibling watch on TLS regressions.
 */
public interface MountCommand {

    /**
     * Returns the argv to mount the spec. Never returns null; throws
     * {@link UnsupportedOperationException} if this OS implementation
     * does not handle {@code spec.kind()}.
     */
    List<String> mountArgv(MountSpec spec);

    /**
     * Returns the argv to unmount the previously-mounted spec.
     */
    List<String> unmountArgv(MountSpec spec);
}
