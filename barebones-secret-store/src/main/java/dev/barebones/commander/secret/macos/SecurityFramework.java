/*
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 */
package dev.barebones.commander.secret.macos;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * JNA binding for the subset of macOS {@code Security.framework}
 * that we need for keychain-backed credential storage.
 *
 * The functions chosen are the legacy "keychain item" API
 * ({@code SecKeychainAddGenericPassword} et al.) rather than the
 * modern {@code SecItem*} family because they're easier to bind
 * via JNA — the modern family takes {@code CFDictionaryRef} and
 * needs CoreFoundation marshalling. The legacy API is still
 * supported on macOS 14 (Sonoma) and 15 (Sequoia) and works on
 * the OS user's default keychain.
 */
public interface SecurityFramework extends Library {

    SecurityFramework INSTANCE = Native.load("Security", SecurityFramework.class);

    /** errSecSuccess */
    int OK = 0;
    /** errSecItemNotFound */
    int ITEM_NOT_FOUND = -25300;

    /**
     * Adds a new generic-password item to the user's default keychain.
     *
     * @return errSecSuccess (0) on success, errSecDuplicateItem (-25299)
     *         if an item already exists, or another negative status.
     */
    int SecKeychainAddGenericPassword(
        Pointer keychain,                          // null = default
        int serviceNameLength,
        byte[] serviceName,                        // UTF-8
        int accountNameLength,
        byte[] accountName,                        // UTF-8
        int passwordLength,
        byte[] passwordData,                       // UTF-8
        PointerByReference itemRef);               // out, may be null

    /**
     * Looks up a generic-password item in the user's default keychain.
     */
    int SecKeychainFindGenericPassword(
        Pointer keychain,                          // null = default
        int serviceNameLength,
        byte[] serviceName,                        // UTF-8
        int accountNameLength,
        byte[] accountName,                        // UTF-8
        IntByReference passwordLength,             // out
        PointerByReference passwordData,           // out (use SecKeychainItemFreeContent)
        PointerByReference itemRef);               // out (use CFRelease)

    /** Removes the keychain item that was located by Find. */
    int SecKeychainItemDelete(Pointer itemRef);

    /** Frees a buffer allocated by SecKeychainFindGenericPassword. */
    int SecKeychainItemFreeContent(Pointer attrList, Pointer data);

    /**
     * CoreFoundation CFRelease — decrements the refcount of any CFTypeRef.
     * The itemRef returned by SecKeychainFindGenericPassword is a
     * CFTypeRef and needs CFRelease to avoid leaking it.
     *
     * Lives on the CoreFoundation library, but JNA binds it through
     * the same Library instance because dlsym walks the global
     * namespace on macOS.
     */
    void CFRelease(Pointer cf);
}
