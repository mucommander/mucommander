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
import dev.barebones.commander.secret.SecretStore;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Passphrase-derived AES-GCM credential store. Used as the fallback
 * when no OS keychain is available (headless Linux without libsecret;
 * unattended servers; CI; environments where keychain access is
 * denied).
 *
 * On-disk format (single file, atomically replaced via tmp + rename):
 * <pre>
 *   [magic 4]   = "BCSF"  (Barebones-Commander Secret File)
 *   [version 1] = 0x01
 *   [salt 16]   = PBKDF2 salt (regenerated only on rekey)
 *   [iter 4]    = PBKDF2 iteration count (BE int)
 *   [iv 12]     = AES-GCM IV (NEW per save)
 *   [tag 4]     = ciphertext length (BE int)
 *   [cipher N]  = AES-GCM(key, iv, plaintext, AAD = magic||version)
 *
 *   plaintext = repeated:
 *     [u16 service-len][service-utf8]
 *     [u16 account-len][account-utf8]
 *     [u16 secret-len][secret-utf8]
 * </pre>
 *
 * Entries live in memory between {@link #store} / {@link #lookup}
 * calls; the file is rewritten on every {@link #store} or
 * {@link #delete}. AES-GCM provides confidentiality + integrity in
 * one step. The key is derived from the user's passphrase via
 * PBKDF2-HMAC-SHA256 (310k iterations — current OWASP guidance) and
 * lives only in {@code keyMaterial} for the process lifetime.
 *
 * On Linux/macOS the file is created with mode {@code 0600}. On
 * Windows we don't bother (Windows' default ACL on the user
 * profile is acceptable; the store isn't intended as a primary
 * Windows backend).
 */
public final class AesGcmFileSecretStore implements SecretStore {

    private static final byte[] MAGIC = {'B', 'C', 'S', 'F'};
    private static final byte VERSION = 0x01;
    private static final int SALT_LEN = 16;
    private static final int IV_LEN = 12;
    private static final int GCM_TAG_BITS = 128;
    private static final int PBKDF2_ITERATIONS = 310_000;
    private static final int KEY_BITS = 256;

    /** Single shared SecureRandom — instantiating per-call wastes
     *  entropy-pool init and trips SpotBugs' DMI_RANDOM_USED_ONLY_ONCE. */
    private static final SecureRandom RANDOM = new SecureRandom();

    private final Path file;
    private final byte[] keyMaterial;
    private final byte[] salt;
    private final ReentrantLock lock = new ReentrantLock();
    private Map<String, char[]> entries;

    private AesGcmFileSecretStore(Path file, byte[] keyMaterial, byte[] salt,
                                  Map<String, char[]> entries) {
        this.file = Objects.requireNonNull(file, "file");
        this.keyMaterial = Objects.requireNonNull(keyMaterial, "keyMaterial");
        this.salt = Objects.requireNonNull(salt, "salt");
        this.entries = Objects.requireNonNull(entries, "entries");
    }

    /**
     * Opens (or initialises) the store file with the given passphrase.
     *
     * If the file doesn't exist, a fresh salt is generated and the
     * store starts empty. If it exists, the passphrase is used to
     * derive the AES key and decrypt the existing entries —
     * {@link IOException} is thrown on wrong passphrase (AES-GCM
     * authentication failure).
     *
     * The {@code passphrase} array is consumed for key derivation
     * and may be zeroed by the caller on return.
     */
    public static AesGcmFileSecretStore open(Path file, char[] passphrase) throws IOException {
        Objects.requireNonNull(file, "file");
        Objects.requireNonNull(passphrase, "passphrase");
        if (passphrase.length == 0) {
            throw new IllegalArgumentException("passphrase must not be empty");
        }
        if (Files.exists(file)) {
            return openExisting(file, passphrase);
        }
        return createNew(file, passphrase);
    }

    private static AesGcmFileSecretStore createNew(Path file, char[] passphrase) throws IOException {
        byte[] salt = new byte[SALT_LEN];
        RANDOM.nextBytes(salt);
        byte[] key = derive(passphrase, salt, PBKDF2_ITERATIONS);
        AesGcmFileSecretStore s = new AesGcmFileSecretStore(file, key, salt, new HashMap<>());
        s.persist();
        return s;
    }

    private static AesGcmFileSecretStore openExisting(Path file, char[] passphrase) throws IOException {
        byte[] all = Files.readAllBytes(file);
        try (DataInputStream in = new DataInputStream(new java.io.ByteArrayInputStream(all))) {
            byte[] magic = new byte[MAGIC.length];
            in.readFully(magic);
            if (!Arrays.equals(magic, MAGIC)) {
                throw new IOException("not a barebones secret-store file: bad magic");
            }
            byte version = in.readByte();
            if (version != VERSION) {
                throw new IOException("unsupported secret-store version: " + version);
            }
            byte[] salt = new byte[SALT_LEN];
            in.readFully(salt);
            int iterations = in.readInt();
            byte[] iv = new byte[IV_LEN];
            in.readFully(iv);
            int cipherLen = in.readInt();
            byte[] cipher = new byte[cipherLen];
            in.readFully(cipher);

            byte[] key = derive(passphrase, salt, iterations);
            byte[] plaintext;
            try {
                plaintext = decrypt(key, iv, cipher);
            } catch (GeneralSecurityException e) {
                throw new IOException("could not decrypt secret store " +
                    "(wrong passphrase, or file is corrupt)", e);
            }
            Map<String, char[]> entries = parseEntries(plaintext);
            return new AesGcmFileSecretStore(file, key, salt, entries);
        }
    }

    @Override
    public void store(SecretRef ref, char[] secret) throws IOException {
        Objects.requireNonNull(ref, "ref");
        Objects.requireNonNull(secret, "secret");
        lock.lock();
        try {
            entries.put(keyOf(ref), secret.clone());
            persist();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Optional<char[]> lookup(SecretRef ref) {
        Objects.requireNonNull(ref, "ref");
        lock.lock();
        try {
            char[] s = entries.get(keyOf(ref));
            return s == null ? Optional.empty() : Optional.of(s.clone());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void delete(SecretRef ref) throws IOException {
        Objects.requireNonNull(ref, "ref");
        lock.lock();
        try {
            char[] removed = entries.remove(keyOf(ref));
            if (removed != null) {
                Arrays.fill(removed, '\0');
                persist();
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String backendName() {
        return "aes-gcm-file";
    }

    /**
     * Zero the in-memory key material and the cached entries. After
     * close() the store is unusable; subsequent operations will hit
     * a corrupt key and fail to decrypt.
     */
    @Override
    public void close() {
        lock.lock();
        try {
            Arrays.fill(keyMaterial, (byte) 0);
            for (char[] secret : entries.values()) {
                Arrays.fill(secret, '\0');
            }
            entries.clear();
        } finally {
            lock.unlock();
        }
    }

    private static String keyOf(SecretRef ref) {
        return ref.service() + " " + ref.account();
    }

    private void persist() throws IOException {
        byte[] plaintext = encodeEntries();
        byte[] iv = new byte[IV_LEN];
        RANDOM.nextBytes(iv);
        byte[] cipher;
        try {
            cipher = encrypt(keyMaterial, iv, plaintext);
        } catch (GeneralSecurityException e) {
            throw new IOException("AES-GCM encrypt failed", e);
        }

        // Write to a tmp file and atomic-rename so a crash mid-write
        // doesn't corrupt an existing valid store.
        Path tmp = file.resolveSibling(file.getFileName().toString() + ".tmp");
        Files.createDirectories(file.toAbsolutePath().getParent());
        try (OutputStream os = Files.newOutputStream(tmp);
             DataOutputStream out = new DataOutputStream(os)) {
            out.write(MAGIC);
            out.writeByte(VERSION);
            out.write(salt);
            out.writeInt(PBKDF2_ITERATIONS);
            out.write(iv);
            out.writeInt(cipher.length);
            out.write(cipher);
        }
        try {
            Files.setPosixFilePermissions(tmp,
                PosixFilePermissions.fromString("rw-------"));
        } catch (UnsupportedOperationException ignored) {
            // Windows; default ACL is acceptable.
        }
        Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING,
            StandardCopyOption.ATOMIC_MOVE);
    }

    private byte[] encodeEntries() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(baos)) {
            for (Map.Entry<String, char[]> e : entries.entrySet()) {
                String[] parts = e.getKey().split(" ", 2);
                writeUtf16(out, parts[0]);
                writeUtf16(out, parts[1]);
                writeChars(out, e.getValue());
            }
        }
        return baos.toByteArray();
    }

    private static void writeUtf16(DataOutputStream out, String s) throws IOException {
        byte[] b = s.getBytes(StandardCharsets.UTF_8);
        if (b.length > 0xFFFF) {
            throw new IOException("string too long: " + b.length);
        }
        out.writeShort(b.length);
        out.write(b);
    }

    private static void writeChars(DataOutputStream out, char[] chars) throws IOException {
        ByteBuffer bb = StandardCharsets.UTF_8.encode(CharBuffer.wrap(chars));
        if (bb.remaining() > 0xFFFF) {
            throw new IOException("secret too long: " + bb.remaining());
        }
        out.writeShort(bb.remaining());
        byte[] tmp = new byte[bb.remaining()];
        bb.get(tmp);
        out.write(tmp);
    }

    private static Map<String, char[]> parseEntries(byte[] plaintext) throws IOException {
        Map<String, char[]> out = new HashMap<>();
        try (DataInputStream in = new DataInputStream(
                new java.io.ByteArrayInputStream(plaintext))) {
            while (in.available() > 0) {
                String service = readUtf16(in);
                String account = readUtf16(in);
                char[] secret = readChars(in);
                out.put(service + " " + account, secret);
            }
        }
        return out;
    }

    private static String readUtf16(DataInputStream in) throws IOException {
        int len = in.readUnsignedShort();
        byte[] b = new byte[len];
        in.readFully(b);
        return new String(b, StandardCharsets.UTF_8);
    }

    private static char[] readChars(DataInputStream in) throws IOException {
        int len = in.readUnsignedShort();
        byte[] b = new byte[len];
        in.readFully(b);
        CharBuffer cb = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(b));
        char[] chars = new char[cb.remaining()];
        cb.get(chars);
        return chars;
    }

    private static byte[] derive(char[] passphrase, byte[] salt, int iterations) throws IOException {
        try {
            SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            PBEKeySpec spec = new PBEKeySpec(passphrase, salt, iterations, KEY_BITS);
            try {
                return f.generateSecret(spec).getEncoded();
            } finally {
                spec.clearPassword();
            }
        } catch (GeneralSecurityException e) {
            throw new IOException("PBKDF2 key derivation failed", e);
        }
    }

    private static byte[] encrypt(byte[] key, byte[] iv, byte[] plaintext)
            throws GeneralSecurityException {
        Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKey sk = new SecretKeySpec(key, "AES");
        c.init(Cipher.ENCRYPT_MODE, sk, new GCMParameterSpec(GCM_TAG_BITS, iv));
        // Bind the magic + version into the AAD so a future format
        // change can't be silently downgraded by a tampered file.
        c.updateAAD(MAGIC);
        c.updateAAD(new byte[]{VERSION});
        return c.doFinal(plaintext);
    }

    private static byte[] decrypt(byte[] key, byte[] iv, byte[] cipher)
            throws GeneralSecurityException {
        Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKey sk = new SecretKeySpec(key, "AES");
        c.init(Cipher.DECRYPT_MODE, sk, new GCMParameterSpec(GCM_TAG_BITS, iv));
        c.updateAAD(MAGIC);
        c.updateAAD(new byte[]{VERSION});
        return c.doFinal(cipher);
    }

    /** Exposed for tests; never call from production code. */
    static Set<PosixFilePermission> readonlyPermissions() {
        return PosixFilePermissions.fromString("rw-------");
    }

    /** Exposed for tests so the test can assert on the in-memory state. */
    int entryCountForTest() {
        lock.lock();
        try {
            return entries.size();
        } finally {
            lock.unlock();
        }
    }
}
