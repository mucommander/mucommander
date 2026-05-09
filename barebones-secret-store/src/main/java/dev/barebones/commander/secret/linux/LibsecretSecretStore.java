/*
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 */
package dev.barebones.commander.secret.linux;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

import dev.barebones.commander.secret.SecretRef;
import dev.barebones.commander.secret.SecretStore;

import java.io.IOException;
import java.util.Optional;

/**
 * Linux libsecret-backed {@link SecretStore}. Talks D-Bus to the
 * Secret Service running in the user session (gnome-keyring,
 * KWallet's Secret Service shim, KeePassXC, etc).
 *
 * Schema we use:
 * <pre>
 *   name = "dev.barebones.commander.Credentials"
 *   attrs = {service: STRING, account: STRING}
 * </pre>
 *
 * On failure to load {@code libsecret-1.so.0} (no libsecret
 * installed; uncommon enough on desktop Linux but standard on
 * minimal containers), {@link #isAvailable()} returns false and
 * the {@link dev.barebones.commander.secret.Activator} falls
 * through to the AES-GCM file backend.
 */
public final class LibsecretSecretStore implements SecretStore {

    private static final String SCHEMA_NAME = "dev.barebones.commander.Credentials";
    private static final String ATTR_SERVICE = "service";
    private static final String ATTR_ACCOUNT = "account";

    private final Pointer schema;

    public LibsecretSecretStore() {
        this.schema = Libsecret.INSTANCE.secret_schema_new(
            SCHEMA_NAME, Libsecret.SECRET_SCHEMA_NONE,
            ATTR_SERVICE, Libsecret.SECRET_SCHEMA_ATTRIBUTE_STRING,
            ATTR_ACCOUNT, Libsecret.SECRET_SCHEMA_ATTRIBUTE_STRING,
            null);
        if (schema == null || Pointer.nativeValue(schema) == 0L) {
            throw new IllegalStateException(
                "secret_schema_new returned NULL — libsecret broken?");
        }
    }

    public static boolean isAvailable() {
        try {
            return Libsecret.INSTANCE != null;
        } catch (UnsatisfiedLinkError e) {
            return false;
        }
    }

    @Override
    public void store(SecretRef ref, char[] secret) throws IOException {
        // libsecret takes a String for the password; converting char[]
        // to String here defeats the "zero after use" intent, but
        // libsecret has no char[] entry point. On Linux the secret
        // lives in the user's keyring daemon process anyway —
        // wiping it from our heap doesn't help much.
        String password = new String(secret);
        PointerByReference err = new PointerByReference();
        int ok = Libsecret.INSTANCE.secret_password_store_sync(
            schema, null,
            "barebones-commander: " + ref.account(),
            password,
            null, err,
            ATTR_SERVICE, ref.service(),
            ATTR_ACCOUNT, ref.account(),
            null);
        if (ok == 0) {
            throw fromError("secret_password_store_sync", err);
        }
    }

    @Override
    public Optional<char[]> lookup(SecretRef ref) throws IOException {
        PointerByReference err = new PointerByReference();
        Pointer p = Libsecret.INSTANCE.secret_password_lookup_sync(
            schema, null, err,
            ATTR_SERVICE, ref.service(),
            ATTR_ACCOUNT, ref.account(),
            null);
        if (p == null || Pointer.nativeValue(p) == 0L) {
            // Not found OR error — distinguish by checking GError.
            if (err.getValue() != null) {
                throw fromError("secret_password_lookup_sync", err);
            }
            return Optional.empty();
        }
        try {
            String s = p.getString(0);
            return Optional.of(s.toCharArray());
        } finally {
            Libsecret.INSTANCE.secret_password_free(p);
        }
    }

    @Override
    public void delete(SecretRef ref) throws IOException {
        PointerByReference err = new PointerByReference();
        int ok = Libsecret.INSTANCE.secret_password_clear_sync(
            schema, null, err,
            ATTR_SERVICE, ref.service(),
            ATTR_ACCOUNT, ref.account(),
            null);
        if (ok == 0) {
            throw fromError("secret_password_clear_sync", err);
        }
    }

    @Override
    public String backendName() {
        return "linux-libsecret";
    }

    private static IOException fromError(String fn, PointerByReference errRef) {
        Pointer p = errRef.getValue();
        String msg;
        if (p == null) {
            msg = fn + " failed (no GError)";
        } else {
            Libsecret.GError g = new Libsecret.GError(p);
            msg = fn + " failed: " +
                (g.message == null ? "(null)" : g.message.getString(0));
            Libsecret.INSTANCE.g_error_free(p);
        }
        return new IOException(msg);
    }
}
