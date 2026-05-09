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
 * Configurable timeouts for the SFTP transport, sourced from
 * {@code -D} system properties so operators can tune them without
 * a rebuild.
 *
 * Three knobs:
 * <ul>
 *   <li>{@code barebones.sftp.connectTimeoutMs} — TCP+SSH handshake
 *       cap. The previous hardcoded value was 5 000 ms which fails
 *       on high-latency links (cellular, transoceanic). Default 15 000.</li>
 *   <li>{@code barebones.sftp.readTimeoutMs} — socket SO_TIMEOUT
 *       applied to the SSH session. A non-zero value lets reads on
 *       a wedged server fail in bounded time instead of hanging the
 *       caller forever. Default 60 000.</li>
 *   <li>{@code barebones.sftp.serverAliveIntervalSec} — how often
 *       JSch sends an SSH keepalive. With a non-zero value JSch will
 *       also tear the session down after 3 missed replies. Default
 *       30 seconds.</li>
 * </ul>
 *
 * All three are independently overridable; invalid values fall back
 * to the default with a warning logged at the call site.
 */
final class SftpTimeouts {

    static final String CONNECT_PROP = "barebones.sftp.connectTimeoutMs";
    static final String READ_PROP = "barebones.sftp.readTimeoutMs";
    static final String SERVER_ALIVE_PROP = "barebones.sftp.serverAliveIntervalSec";

    static final int DEFAULT_CONNECT_MS = 15_000;
    static final int DEFAULT_READ_MS = 60_000;
    static final int DEFAULT_SERVER_ALIVE_SEC = 30;

    private SftpTimeouts() {
    }

    static int connectMs() {
        return readPositiveInt(CONNECT_PROP, DEFAULT_CONNECT_MS);
    }

    static int readMs() {
        return readNonNegativeInt(READ_PROP, DEFAULT_READ_MS);
    }

    static int serverAliveSec() {
        return readNonNegativeInt(SERVER_ALIVE_PROP, DEFAULT_SERVER_ALIVE_SEC);
    }

    private static int readPositiveInt(String prop, int defaultValue) {
        int v = readNonNegativeInt(prop, defaultValue);
        return v > 0 ? v : defaultValue;
    }

    private static int readNonNegativeInt(String prop, int defaultValue) {
        String raw = System.getProperty(prop);
        if (raw == null || raw.isBlank()) {
            return defaultValue;
        }
        try {
            int v = Integer.parseInt(raw.trim());
            return v >= 0 ? v : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
