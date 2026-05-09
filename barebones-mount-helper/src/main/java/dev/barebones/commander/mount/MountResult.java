/*
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 */
package dev.barebones.commander.mount;

/**
 * Outcome of a mount or unmount invocation.
 *
 * Stdout/stderr are captured verbatim — the dialog layer surfaces
 * stderr on failure rather than hiding it behind a generic message.
 */
public record MountResult(
        int exitCode,
        String stdout,
        String stderr
) {
    public boolean ok() {
        return exitCode == 0;
    }
}
