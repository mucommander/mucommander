/*
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 */
package dev.barebones.commander.secret;

import dev.barebones.commander.secret.linux.LibsecretSecretStore;
import dev.barebones.commander.secret.macos.KeychainSecretStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Phase-2-style register entry point. Picks a {@link SecretStore}
 * impl based on the OS and the {@code barebones.secretStore} system
 * property override, installs it on {@link SecretStoreService}.
 *
 * Selection order:
 * <ol>
 *   <li>If {@code -Dbarebones.secretStore=<name>} is set, use that
 *       backend (or fail loudly if it can't load — no silent fallback).
 *       Names: {@code macos-keychain}, {@code linux-libsecret},
 *       {@code aes-gcm-file}, {@code none}.</li>
 *   <li>On macOS: {@link KeychainSecretStore}.</li>
 *   <li>On Linux with libsecret-1 installed:
 *       {@link LibsecretSecretStore}.</li>
 *   <li>Otherwise: install nothing. The credentials writer/parser
 *       sees {@code SecretStoreService.store() == null} and treats
 *       passwords as not-persisted (user re-enters per session).
 *       The AES-GCM fallback is opt-in via the system property
 *       because it requires a passphrase from the user; auto-prompt
 *       on app start would be a hostile UX.</li>
 * </ol>
 *
 * The aes-gcm-file backend reads its passphrase from
 * {@code -Dbarebones.secretStore.passphrase=...} when explicitly
 * selected; an empty passphrase is rejected.
 */
public final class Activator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    private Activator() {
    }

    public static void register() {
        SecretStore store = pickStore();
        if (store != null) {
            SecretStoreService.install(store);
            LOGGER.info("SecretStore: using {}", store.backendName());
        } else {
            LOGGER.info("SecretStore: none — credentials will not be persisted");
        }
    }

    private static SecretStore pickStore() {
        String forced = System.getProperty("barebones.secretStore");
        if (forced != null && !forced.isBlank()) {
            return forceLoad(forced.trim());
        }
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("mac") || os.contains("darwin")) {
            return tryLoadMacOS();
        }
        if (os.contains("linux")) {
            return tryLoadLibsecret();
        }
        return null;
    }

    private static SecretStore forceLoad(String name) {
        return switch (name) {
            case "macos-keychain" -> {
                if (!KeychainSecretStore.isAvailable()) {
                    throw new IllegalStateException(
                        "barebones.secretStore=macos-keychain but Security.framework is unavailable");
                }
                yield new KeychainSecretStore();
            }
            case "linux-libsecret" -> {
                if (!LibsecretSecretStore.isAvailable()) {
                    throw new IllegalStateException(
                        "barebones.secretStore=linux-libsecret but libsecret-1 is unavailable");
                }
                yield new LibsecretSecretStore();
            }
            case "aes-gcm-file" -> openAesGcmFile();
            case "none" -> null;
            default -> throw new IllegalArgumentException(
                "unknown barebones.secretStore: '" + name +
                "' (expected one of macos-keychain | linux-libsecret | aes-gcm-file | none)");
        };
    }

    private static SecretStore tryLoadMacOS() {
        try {
            if (KeychainSecretStore.isAvailable()) {
                return new KeychainSecretStore();
            }
        } catch (UnsatisfiedLinkError e) {
            LOGGER.warn("macOS Keychain unavailable, falling through to no-store: {}", e.getMessage());
        }
        return null;
    }

    private static SecretStore tryLoadLibsecret() {
        try {
            if (LibsecretSecretStore.isAvailable()) {
                return new LibsecretSecretStore();
            }
        } catch (UnsatisfiedLinkError e) {
            LOGGER.warn("Linux libsecret unavailable, falling through to no-store: {}", e.getMessage());
        }
        return null;
    }

    private static SecretStore openAesGcmFile() {
        String passphrase = System.getProperty("barebones.secretStore.passphrase");
        if (passphrase == null || passphrase.isEmpty()) {
            throw new IllegalStateException(
                "barebones.secretStore=aes-gcm-file requires " +
                "-Dbarebones.secretStore.passphrase=<value>");
        }
        java.nio.file.Path file = java.nio.file.Path.of(
            System.getProperty("user.home", "."),
            ".barebones-commander", "credentials.bin");
        try {
            return dev.barebones.commander.secret.aesgcm.AesGcmFileSecretStore.open(
                file, passphrase.toCharArray());
        } catch (java.io.IOException e) {
            throw new IllegalStateException(
                "could not open AES-GCM secret store at " + file, e);
        }
    }
}
