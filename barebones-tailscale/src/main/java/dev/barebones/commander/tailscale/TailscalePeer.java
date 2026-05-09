/*
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 */
package dev.barebones.commander.tailscale;

import java.util.List;
import java.util.Objects;

/**
 * One tailnet peer as reported by {@code tailscale status --json}.
 *
 * @param hostName    short hostname assigned by the user (e.g. "laptop")
 * @param dnsName     MagicDNS name (e.g. "laptop.tailnet.ts.net.");
 *                    trailing dot stripped during parsing
 * @param tailscaleIPs Tailscale IPs assigned (one or two: 100.x.y.z + IPv6)
 * @param os          coarse OS string ("linux", "macOS", "windows", ...)
 * @param online      true if the peer is currently reachable on the tailnet
 */
public record TailscalePeer(
        String hostName,
        String dnsName,
        List<String> tailscaleIPs,
        String os,
        boolean online
) {
    public TailscalePeer {
        Objects.requireNonNull(hostName, "hostName");
        Objects.requireNonNull(dnsName, "dnsName");
        Objects.requireNonNull(tailscaleIPs, "tailscaleIPs");
        Objects.requireNonNull(os, "os");
        tailscaleIPs = List.copyOf(tailscaleIPs);
    }
}
