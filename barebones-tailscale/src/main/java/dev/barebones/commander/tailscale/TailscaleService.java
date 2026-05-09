/*
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 */
package dev.barebones.commander.tailscale;

/**
 * Single-slot holder for the discovered {@link TailscaleClient}. The
 * Activator probes for the {@code tailscale} binary on startup; the
 * UI reads via {@link #client()} and hides Tailscale menu items if
 * the result is null (i.e. Tailscale is not installed).
 */
public final class TailscaleService {

    private static volatile TailscaleClient client;

    private TailscaleService() {
    }

    static void install(TailscaleClient c) {
        client = c;
    }

    public static TailscaleClient client() {
        return client;
    }
}
