/*
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 */
package dev.barebones.commander.commons.file.protocol.sftp;

/**
 * Controls how SFTP connections handle unknown / mismatched server
 * host keys. Mirrors OpenSSH's {@code StrictHostKeyChecking} option
 * because JSch maps directly to it.
 *
 * Default is {@link #ASK}. Override at startup with
 * {@code -Dbarebones.sftp.hostKey=yes|ask|no}.
 */
public enum HostKeyPolicy {

    /**
     * Reject any connection to a host whose key isn't already in
     * the known-hosts file. JSch throws {@code JSchException}.
     * Use this in unattended / scripted environments.
     */
    YES("yes"),

    /**
     * Default. Accept known hosts silently. For unknown / changed
     * keys, prompt the user (via {@link HostKeyPrompter}); on
     * accept the key is persisted to the known-hosts file.
     */
    ASK("ask"),

    /**
     * Accept any host key without prompting. **MITM-able.** Only
     * for legacy or explicitly-opt-in scenarios where the user
     * accepts the risk. Equivalent to OpenSSH's
     * {@code StrictHostKeyChecking=no}.
     */
    NO("no");

    private final String jschValue;

    HostKeyPolicy(String jschValue) {
        this.jschValue = jschValue;
    }

    /** The string JSch's {@code session.setConfig("StrictHostKeyChecking", ...)} expects. */
    public String jschValue() {
        return jschValue;
    }

    /** Resolves the active policy from the system property. */
    public static HostKeyPolicy fromSystemProperty() {
        String value = System.getProperty("barebones.sftp.hostKey", "ask").trim().toLowerCase();
        for (HostKeyPolicy p : values()) {
            if (p.jschValue.equals(value)) {
                return p;
            }
        }
        throw new IllegalArgumentException(
            "barebones.sftp.hostKey must be one of yes|ask|no, got: " + value);
    }
}
