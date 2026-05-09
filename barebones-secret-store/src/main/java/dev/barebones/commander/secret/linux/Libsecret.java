/*
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 */
package dev.barebones.commander.secret.linux;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.PointerByReference;

import java.util.Arrays;
import java.util.List;

/**
 * Minimal JNA bindings for libsecret-1's "simple" sync API. The
 * functions used:
 *
 * <ul>
 *   <li>{@code secret_password_store_sync(SecretSchema *, ...)} —
 *       store a secret under a schema's attributes.</li>
 *   <li>{@code secret_password_lookup_sync(SecretSchema *, ...)} —
 *       look up by schema + attributes; returns NULL if missing.</li>
 *   <li>{@code secret_password_clear_sync(SecretSchema *, ...)} —
 *       delete by schema + attributes.</li>
 *   <li>{@code secret_password_free(gchar *)} — frees a string
 *       returned by lookup_sync.</li>
 * </ul>
 *
 * The variadic attribute lists are terminated by a {@code NULL}
 * pointer; we always pass exactly two attrs (service, account)
 * followed by null.
 */
public interface Libsecret extends Library {

    Libsecret INSTANCE = Native.load("secret-1", Libsecret.class);

    /** Constant attribute type tag — both attrs we use are SecretSchemaAttributeType.STRING (0). */
    int SECRET_SCHEMA_ATTRIBUTE_STRING = 0;

    /** SECRET_SCHEMA_NONE = 0; we don't bind to D-Bus by name. */
    int SECRET_SCHEMA_NONE = 0;

    /** GError* returned by sync calls when they fail. */
    @Structure.FieldOrder({"domain", "code", "message"})
    class GError extends Structure {
        public int domain;
        public int code;
        public Pointer message;
        public GError() { super(); }
        public GError(Pointer p) { super(p); read(); }
        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("domain", "code", "message");
        }
    }

    /**
     * Store a secret. On success returns 1; on failure returns 0 and
     * fills {@code error} with a {@link GError}.
     */
    int secret_password_store_sync(
        Pointer schema,         // SecretSchema *
        Pointer collection,     // SECRET_COLLECTION_DEFAULT == NULL
        String label,
        String password,
        Pointer cancellable,    // NULL
        PointerByReference error,
        // Attribute list — terminated by NULL pointer
        String attr1Name, String attr1Value,
        String attr2Name, String attr2Value,
        Pointer terminator);

    /**
     * Look up a secret. Returns the secret string (must be freed
     * with {@code secret_password_free}) or NULL on missing / error.
     * On error, fills {@code error}.
     */
    Pointer secret_password_lookup_sync(
        Pointer schema,
        Pointer cancellable,
        PointerByReference error,
        String attr1Name, String attr1Value,
        String attr2Name, String attr2Value,
        Pointer terminator);

    /** Delete a secret. Returns 1 on success (or "didn't exist"), 0 on error. */
    int secret_password_clear_sync(
        Pointer schema,
        Pointer cancellable,
        PointerByReference error,
        String attr1Name, String attr1Value,
        String attr2Name, String attr2Value,
        Pointer terminator);

    /** Frees a secret string returned by lookup_sync. */
    void secret_password_free(Pointer secret);

    /** Free a GError. */
    void g_error_free(Pointer error);

    /**
     * Builds a SecretSchema dynamically. The simpler path is to use
     * the helper {@code secret_schema_new} which takes a variadic
     * list — JNA can call it with a fixed shape.
     */
    Pointer secret_schema_new(
        String name,
        int flags,
        String attr1Name, int attr1Type,
        String attr2Name, int attr2Type,
        Pointer terminator);

    /** Decrement refcount on the schema returned by secret_schema_new. */
    void secret_schema_unref(Pointer schema);
}
