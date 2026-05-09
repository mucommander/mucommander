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
 * Single-slot holder for the platform's {@link MountExecutor}. The
 * Activator installs one on startup; the UI reads via {@link #executor()}.
 *
 * Returns {@code null} on platforms where mount isn't supported
 * (Windows / BSD / etc) — callers should hide the mount UI in that
 * case rather than treating it as an error.
 */
public final class MountService {

    private static volatile MountExecutor executor;

    private MountService() {
    }

    static void install(MountExecutor exec) {
        executor = exec;
    }

    public static MountExecutor executor() {
        return executor;
    }
}
