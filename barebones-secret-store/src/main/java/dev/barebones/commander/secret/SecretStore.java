/*
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 */
package dev.barebones.commander.secret;

import java.io.IOException;
import java.util.Optional;

/**
 * SPI for credential / passphrase storage. One concrete impl per
 * backend: macOS Keychain, Linux libsecret, or a passphrase-derived
 * AES-GCM blob on disk for headless / no-keychain environments.
 *
 * Secrets are passed as {@code char[]} rather than {@code String}
 * so callers can zero them after use — {@code String} interning
 * would otherwise leave the secret pinned in the heap until GC.
 *
 * Implementations that hold native resources (JNA pointers,
 * derived key material) should release them in {@link #close()}.
 * The application calls close() during shutdown.
 */
public interface SecretStore extends AutoCloseable {

    /**
     * Stores or replaces the secret under {@code ref}. Implementations
     * MUST overwrite any existing secret for the same ref atomically.
     *
     * @throws IOException if the backend rejects the write (keychain
     *         locked, libsecret D-Bus failure, disk full, etc).
     */
    void store(SecretRef ref, char[] secret) throws IOException;

    /**
     * Returns the secret for {@code ref}, or {@link Optional#empty()}
     * if the entry doesn't exist.
     *
     * @throws IOException if the backend errors while reading
     *         (keychain access denied, libsecret D-Bus failure).
     */
    Optional<char[]> lookup(SecretRef ref) throws IOException;

    /**
     * Deletes the secret for {@code ref}. No-op if the entry doesn't
     * exist (matches keychain semantics — there's nothing to fail on).
     *
     * @throws IOException if the backend errors while deleting an
     *         entry that does exist.
     */
    void delete(SecretRef ref) throws IOException;

    /**
     * Human-readable backend name for logs / preferences UI.
     */
    String backendName();

    /**
     * Release any native resources or cached key material. Idempotent.
     * Default is no-op for backends that own no native state.
     */
    @Override
    default void close() throws IOException {
    }
}
