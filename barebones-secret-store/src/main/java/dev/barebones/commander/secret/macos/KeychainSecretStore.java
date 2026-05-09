/*
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 */
package dev.barebones.commander.secret.macos;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

import dev.barebones.commander.secret.SecretRef;
import dev.barebones.commander.secret.SecretStore;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

/**
 * macOS Keychain-backed {@link SecretStore} via JNA bindings to
 * {@code Security.framework}'s legacy generic-password API.
 *
 * Each {@link SecretRef} maps to one keychain item identified by the
 * (service, account) pair. On first store the user may see the
 * standard "barebones-commander wants to use your keychain" prompt;
 * on subsequent reads the keychain remembers our app and lets us
 * through silently.
 */
public final class KeychainSecretStore implements SecretStore {

    /**
     * Probe: try a no-op lookup to confirm the framework loads and
     * the user has a keychain. Throws {@link UnsatisfiedLinkError}
     * if Security.framework can't be loaded (only happens off-macOS).
     */
    public static boolean isAvailable() {
        try {
            // Just touching INSTANCE forces the native load — if we're
            // not on macOS or the framework isn't present this throws.
            return SecurityFramework.INSTANCE != null;
        } catch (UnsatisfiedLinkError e) {
            return false;
        }
    }

    @Override
    public void store(SecretRef ref, char[] secret) throws IOException {
        byte[] service = ref.service().getBytes(StandardCharsets.UTF_8);
        byte[] account = ref.account().getBytes(StandardCharsets.UTF_8);
        byte[] password = toUtf8Bytes(secret);
        try {
            // SecKeychainAddGenericPassword returns errSecDuplicateItem
            // (-25299) when the entry exists; in that case delete +
            // re-add so the contract "always replaces" holds.
            int status = SecurityFramework.INSTANCE.SecKeychainAddGenericPassword(
                null, service.length, service, account.length, account,
                password.length, password, null);
            if (status == -25299) {
                deleteByLookup(service, account);
                status = SecurityFramework.INSTANCE.SecKeychainAddGenericPassword(
                    null, service.length, service, account.length, account,
                    password.length, password, null);
            }
            if (status != SecurityFramework.OK) {
                throw new IOException(
                    "SecKeychainAddGenericPassword failed: status=" + status);
            }
        } finally {
            Arrays.fill(password, (byte) 0);
        }
    }

    @Override
    public Optional<char[]> lookup(SecretRef ref) throws IOException {
        byte[] service = ref.service().getBytes(StandardCharsets.UTF_8);
        byte[] account = ref.account().getBytes(StandardCharsets.UTF_8);
        IntByReference passwordLength = new IntByReference();
        PointerByReference passwordData = new PointerByReference();
        int status = SecurityFramework.INSTANCE.SecKeychainFindGenericPassword(
            null, service.length, service, account.length, account,
            passwordLength, passwordData, null);
        if (status == SecurityFramework.ITEM_NOT_FOUND) {
            return Optional.empty();
        }
        if (status != SecurityFramework.OK) {
            throw new IOException(
                "SecKeychainFindGenericPassword failed: status=" + status);
        }
        Pointer p = passwordData.getValue();
        try {
            byte[] bytes = p.getByteArray(0, passwordLength.getValue());
            char[] chars = utf8BytesToChars(bytes);
            Arrays.fill(bytes, (byte) 0);
            return Optional.of(chars);
        } finally {
            SecurityFramework.INSTANCE.SecKeychainItemFreeContent(null, p);
        }
    }

    @Override
    public void delete(SecretRef ref) throws IOException {
        byte[] service = ref.service().getBytes(StandardCharsets.UTF_8);
        byte[] account = ref.account().getBytes(StandardCharsets.UTF_8);
        deleteByLookup(service, account);
    }

    private static void deleteByLookup(byte[] service, byte[] account) throws IOException {
        IntByReference passwordLength = new IntByReference();
        PointerByReference passwordData = new PointerByReference();
        PointerByReference itemRef = new PointerByReference();
        int findStatus = SecurityFramework.INSTANCE.SecKeychainFindGenericPassword(
            null, service.length, service, account.length, account,
            passwordLength, passwordData, itemRef);
        if (findStatus == SecurityFramework.ITEM_NOT_FOUND) {
            return;
        }
        if (findStatus != SecurityFramework.OK) {
            throw new IOException(
                "SecKeychainFindGenericPassword (for delete) failed: status=" + findStatus);
        }
        try {
            int deleteStatus = SecurityFramework.INSTANCE.SecKeychainItemDelete(
                itemRef.getValue());
            if (deleteStatus != SecurityFramework.OK) {
                throw new IOException(
                    "SecKeychainItemDelete failed: status=" + deleteStatus);
            }
        } finally {
            // SecKeychainItemFreeContent frees the password DATA
            // buffer; CFRelease releases the CFTypeRef itemRef
            // itself. Both are required — calling only the first
            // leaks one CFTypeRef per delete.
            Pointer pwd = passwordData.getValue();
            if (pwd != null) {
                SecurityFramework.INSTANCE.SecKeychainItemFreeContent(null, pwd);
            }
            Pointer ref = itemRef.getValue();
            if (ref != null) {
                SecurityFramework.INSTANCE.CFRelease(ref);
            }
        }
    }

    @Override
    public String backendName() {
        return "macos-keychain";
    }

    private static byte[] toUtf8Bytes(char[] chars) {
        ByteBuffer bb = StandardCharsets.UTF_8.encode(CharBuffer.wrap(chars));
        byte[] out = new byte[bb.remaining()];
        bb.get(out);
        return out;
    }

    private static char[] utf8BytesToChars(byte[] bytes) {
        CharBuffer cb = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(bytes));
        char[] out = new char[cb.remaining()];
        cb.get(out);
        return out;
    }
}
