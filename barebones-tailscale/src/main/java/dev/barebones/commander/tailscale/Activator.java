/*
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 */
package dev.barebones.commander.tailscale;

import java.nio.file.Path;

/**
 * Phase-2-style static register() entry point. Locates the tailscale
 * binary on $PATH (or the macOS GUI install location); installs a
 * {@link TailscaleClient} on {@link TailscaleService} when found.
 *
 * No-op when tailscale is not installed — the UI hides Tailscale
 * menu items in that case.
 */
public final class Activator {

    private Activator() {
    }

    public static void register() {
        Path binary = TailscaleClient.locateBinary();
        if (binary != null) {
            TailscaleService.install(new TailscaleClient(binary));
        }
    }
}
