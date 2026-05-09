/*
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 */
package dev.barebones.commander.tailscale;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Parses the JSON shape produced by {@code tailscale status --json}.
 *
 * Stable surface (we extract):
 *   { "Self":  { "HostName", "DNSName", "TailscaleIPs", "OS", "Online" },
 *     "Peer": { "<key>": { same shape ... },
 *               "<key>": { ... } } }
 *
 * We only extract the fields the UI needs; tailscale's JSON has many
 * more (NetMap state, derp region, last-seen times, etc.) that we
 * intentionally ignore so adding more fields upstream doesn't break
 * the parse.
 */
public final class TailscaleStatusParser {

    private TailscaleStatusParser() {
    }

    public static List<TailscalePeer> parse(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        Object root;
        try {
            root = new JSONParser(JSONParser.MODE_PERMISSIVE).parse(json);
        } catch (ParseException e) {
            throw new IllegalArgumentException("invalid tailscale status JSON", e);
        }
        if (!(root instanceof JSONObject status)) {
            throw new IllegalArgumentException("expected JSON object at root");
        }
        Object peerMap = status.get("Peer");
        if (peerMap == null) {
            return List.of();
        }
        if (!(peerMap instanceof Map<?, ?> peers)) {
            throw new IllegalArgumentException("Peer field is not a JSON object");
        }
        List<TailscalePeer> out = new ArrayList<>(peers.size());
        for (Object value : peers.values()) {
            if (!(value instanceof JSONObject node)) {
                continue;
            }
            out.add(toPeer(node));
        }
        return Collections.unmodifiableList(out);
    }

    private static TailscalePeer toPeer(JSONObject n) {
        String host = stringOr(n, "HostName", "");
        String dns = stripTrailingDot(stringOr(n, "DNSName", ""));
        String os = stringOr(n, "OS", "");
        boolean online = booleanOr(n, "Online", false);

        Object ips = n.get("TailscaleIPs");
        List<String> ipList;
        if (ips instanceof JSONArray arr) {
            ipList = new ArrayList<>(arr.size());
            for (Object ip : arr) {
                if (ip != null) {
                    ipList.add(ip.toString());
                }
            }
        } else {
            ipList = List.of();
        }
        return new TailscalePeer(host, dns, ipList, os, online);
    }

    private static String stringOr(JSONObject n, String key, String fallback) {
        Object v = n.get(key);
        return v == null ? fallback : v.toString();
    }

    private static boolean booleanOr(JSONObject n, String key, boolean fallback) {
        Object v = n.get(key);
        if (v instanceof Boolean b) return b;
        return fallback;
    }

    private static String stripTrailingDot(String s) {
        return s.endsWith(".") ? s.substring(0, s.length() - 1) : s;
    }
}
