/*
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 */
package dev.barebones.commander.tailscale;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Thin wrapper around the {@code tailscale} CLI. Detects whether the
 * binary is installed and, if so, runs {@code tailscale status --json}
 * and parses the output via {@link TailscaleStatusParser}.
 *
 * No JNI, no embedded Go runtime — we shell out via
 * {@code ProcessBuilder(List<String>)}, the same defence-against-
 * shell-injection pattern as {@link dev.barebones.commander.mount}.
 *
 * The {@code tailscale file cp} (Taildrop send) helper is also here
 * because it mirrors the same call shape.
 */
public final class TailscaleClient {

    private static final long DEFAULT_TIMEOUT_SECONDS = 5L;

    private final Path binary;
    private final long timeoutSeconds;

    public TailscaleClient(Path binary) {
        this(binary, DEFAULT_TIMEOUT_SECONDS);
    }

    public TailscaleClient(Path binary, long timeoutSeconds) {
        if (binary == null) {
            throw new IllegalArgumentException("binary must not be null");
        }
        if (timeoutSeconds <= 0) {
            throw new IllegalArgumentException("timeoutSeconds must be positive");
        }
        this.binary = binary;
        this.timeoutSeconds = timeoutSeconds;
    }

    /**
     * Locate {@code tailscale} on PATH and the well-known macOS
     * Containers location used by the GUI install. Returns null if
     * tailscale isn't installed — callers should hide tailscale UI.
     */
    public static Path locateBinary() {
        String pathEnv = System.getenv("PATH");
        if (pathEnv != null) {
            for (String dir : pathEnv.split(java.io.File.pathSeparator)) {
                Path candidate = Path.of(dir, "tailscale");
                if (Files.isExecutable(candidate)) {
                    return candidate;
                }
            }
        }
        // macOS GUI install ships the CLI inside the app bundle.
        Path macGui = Path.of("/Applications/Tailscale.app/Contents/MacOS/Tailscale");
        if (Files.isExecutable(macGui)) {
            return macGui;
        }
        return null;
    }

    public List<TailscalePeer> peers() throws IOException, InterruptedException {
        String json = run(List.of(binary.toString(), "status", "--json"));
        return TailscaleStatusParser.parse(json);
    }

    /**
     * Invokes {@code tailscale file cp <localPath> <peerDnsName>:}
     * (the Taildrop send shape). Returns the CLI's exit code; 0 means
     * the file was queued for delivery.
     */
    public int sendFile(Path localPath, String peerDnsName) throws IOException, InterruptedException {
        if (localPath == null) {
            throw new IllegalArgumentException("localPath must not be null");
        }
        if (peerDnsName == null || peerDnsName.isBlank()) {
            throw new IllegalArgumentException("peerDnsName must not be blank");
        }
        ProcessBuilder pb = new ProcessBuilder(List.of(
            binary.toString(),
            "file", "cp",
            localPath.toString(),
            peerDnsName + ":"
        ));
        Process p = pb.start();
        p.getOutputStream().close();
        if (!p.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
            p.destroyForcibly();
            throw new IOException("tailscale file cp timed out after " + timeoutSeconds + "s");
        }
        return p.exitValue();
    }

    private String run(List<String> argv) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(argv);
        pb.redirectErrorStream(false);
        Process p = pb.start();
        p.getOutputStream().close();
        byte[] outBytes = p.getInputStream().readAllBytes();
        if (!p.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
            p.destroyForcibly();
            throw new IOException("tailscale command timed out after " + timeoutSeconds + "s");
        }
        if (p.exitValue() != 0) {
            throw new IOException(
                "tailscale exited " + p.exitValue() + ": " +
                new String(p.getErrorStream().readAllBytes(), StandardCharsets.UTF_8));
        }
        return new String(outBytes, StandardCharsets.UTF_8);
    }
}
