/*
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 */
package dev.barebones.commander.secret;

/**
 * Single-slot holder for the active {@link SecretStore}. The
 * {@link Activator} installs one on startup based on OS detection
 * and the {@code barebones.secretStore} system-property override;
 * everything else reads via {@link #store()}.
 *
 * Returns {@code null} until the Activator runs. Callers (the
 * credentials writer / reader paths) check for null and fall back
 * to "no persistent storage" semantics — the user has to re-enter
 * passwords each session.
 */
public final class SecretStoreService {

    private static volatile SecretStore store;

    private SecretStoreService() {
    }

    public static void install(SecretStore s) {
        store = s;
    }

    public static SecretStore store() {
        return store;
    }
}
