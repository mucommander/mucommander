/*
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 */
package dev.barebones.commander.secret.aesgcm;

import dev.barebones.commander.secret.SecretRef;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

public class AesGcmFileSecretStoreTest {

    private Path file;

    @BeforeMethod
    public void newFile() throws IOException {
        file = Files.createTempFile("aesgcm-test-", ".bin");
        Files.delete(file); // we want a non-existing path
    }

    @AfterMethod
    public void cleanup() throws IOException {
        Files.deleteIfExists(file);
        Files.deleteIfExists(file.resolveSibling(file.getFileName().toString() + ".tmp"));
    }

    @Test
    public void storeAndLookupRoundTrip() throws IOException {
        AesGcmFileSecretStore s = AesGcmFileSecretStore.open(file, "p4ssphrase".toCharArray());
        SecretRef ref = new SecretRef("barebones-commander", "sftp://alice@host:22");
        s.store(ref, "shh-very-secret".toCharArray());

        Optional<char[]> got = s.lookup(ref);
        assertTrue(got.isPresent());
        assertEquals(new String(got.get()), "shh-very-secret");
    }

    @Test
    public void persistsAcrossReopen() throws IOException {
        SecretRef ref = new SecretRef("svc", "acct");
        char[] passphrase = "p".toCharArray();

        AesGcmFileSecretStore s1 = AesGcmFileSecretStore.open(file, passphrase);
        s1.store(ref, "remembered".toCharArray());

        AesGcmFileSecretStore s2 = AesGcmFileSecretStore.open(file, passphrase);
        Optional<char[]> got = s2.lookup(ref);
        assertTrue(got.isPresent());
        assertEquals(new String(got.get()), "remembered");
    }

    @Test
    public void wrongPassphraseFailsToOpen() throws IOException {
        AesGcmFileSecretStore s = AesGcmFileSecretStore.open(file, "right".toCharArray());
        s.store(new SecretRef("svc", "acct"), "x".toCharArray());

        // AES-GCM auth tag mismatch on wrong key → IOException at open time.
        assertThrows(IOException.class,
            () -> AesGcmFileSecretStore.open(file, "wrong".toCharArray()));
    }

    @Test
    public void overwriteReplaces() throws IOException {
        AesGcmFileSecretStore s = AesGcmFileSecretStore.open(file, "p".toCharArray());
        SecretRef ref = new SecretRef("svc", "acct");
        s.store(ref, "first".toCharArray());
        s.store(ref, "second".toCharArray());
        assertEquals(new String(s.lookup(ref).get()), "second");
        assertEquals(s.entryCountForTest(), 1);
    }

    @Test
    public void deleteRemoves() throws IOException {
        AesGcmFileSecretStore s = AesGcmFileSecretStore.open(file, "p".toCharArray());
        SecretRef ref = new SecretRef("svc", "acct");
        s.store(ref, "x".toCharArray());
        s.delete(ref);
        assertFalse(s.lookup(ref).isPresent());
        assertEquals(s.entryCountForTest(), 0);
    }

    @Test
    public void deleteOfMissingIsNoop() throws IOException {
        AesGcmFileSecretStore s = AesGcmFileSecretStore.open(file, "p".toCharArray());
        s.delete(new SecretRef("never", "stored"));
        assertEquals(s.entryCountForTest(), 0);
    }

    @Test
    public void multipleEntries() throws IOException {
        AesGcmFileSecretStore s = AesGcmFileSecretStore.open(file, "p".toCharArray());
        s.store(new SecretRef("svc", "a"), "alpha".toCharArray());
        s.store(new SecretRef("svc", "b"), "beta".toCharArray());
        s.store(new SecretRef("svc", "c"), "gamma".toCharArray());
        assertEquals(s.entryCountForTest(), 3);
        assertEquals(new String(s.lookup(new SecretRef("svc", "b")).get()), "beta");
    }

    @Test
    public void unicodeSecretsRoundTrip() throws IOException {
        AesGcmFileSecretStore s = AesGcmFileSecretStore.open(file, "p".toCharArray());
        SecretRef ref = new SecretRef("svc", "acct");
        String secret = "パスワード🔐";
        s.store(ref, secret.toCharArray());
        assertEquals(new String(s.lookup(ref).get()), secret);
    }

    @Test
    public void emptyPassphraseRejected() {
        assertThrows(IllegalArgumentException.class,
            () -> AesGcmFileSecretStore.open(file, new char[0]));
    }

    @Test
    public void corruptFileThrowsAtOpen() throws IOException {
        AesGcmFileSecretStore s = AesGcmFileSecretStore.open(file, "p".toCharArray());
        s.store(new SecretRef("svc", "acct"), "x".toCharArray());
        // Flip a byte deep in the ciphertext — AES-GCM auth tag will fail.
        byte[] bytes = Files.readAllBytes(file);
        bytes[bytes.length - 1] ^= 0x42;
        Files.write(file, bytes);
        assertThrows(IOException.class,
            () -> AesGcmFileSecretStore.open(file, "p".toCharArray()));
    }
}
