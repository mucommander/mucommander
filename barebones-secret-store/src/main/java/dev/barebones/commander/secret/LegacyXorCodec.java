/*
 * Copyright (C) 2002-2026 muCommander contributors
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 */
package dev.barebones.commander.secret;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Legacy XOR-Base64 decoder, kept ONLY for one-shot migration of
 * pre-Phase-12 {@code credentials.xml} files. Phase 12 removed the
 * encrypt path; nothing in the project should be calling
 * {@link #decryptXorBase64} except the credentials migrator.
 *
 * Once we're confident that no production user still has an
 * unmigrated {@code credentials.xml} (a release or two after
 * Phase 12 ships), this class can be deleted.
 *
 * Cipher details (matches upstream muCommander's deleted
 * {@code XORCipher}): 256-byte hard-coded key, byte-wise XOR,
 * Base64-wrapped output. The key is below — public — and the cipher
 * provides essentially zero confidentiality, which is exactly why
 * Phase 12 replaces it.
 */
public final class LegacyXorCodec {

    private static final int[] KEY = {
        161, 220, 156, 76, 177, 174, 56, 37, 98, 93, 224, 19, 160, 95, 69, 140,
        91, 138, 33, 114, 248, 57, 179, 17, 54, 172, 249, 58, 26, 181, 167, 231,
        241, 185, 218, 174, 37, 102, 100, 26, 16, 214, 119, 29, 118, 151, 135, 175,
        245, 247, 160, 188, 77, 173, 109, 255, 73, 44, 186, 211, 117, 236, 204, 58,
        246, 210, 128, 33, 234, 218, 82, 188, 78, 229, 180, 108, 247, 200, 3, 142,
        206, 45, 165, 111, 96, 72, 76, 81, 238, 186, 240, 167, 185, 152, 68, 228,
        87, 142, 145, 7, 74, 12, 106, 94, 15, 218, 155, 71, 87, 136, 58, 40,
        246, 94, 7, 89, 29, 0, 78, 204, 70, 220, 240, 127, 59, 184, 109, 106
    };

    private LegacyXorCodec() {
    }

    private static byte[] xor(byte[] in) {
        byte[] out = new byte[in.length];
        int keyLen = KEY.length;
        for (int i = 0; i < in.length; i++) {
            out[i] = (byte) (in[i] ^ KEY[i % keyLen]);
        }
        return out;
    }

    /**
     * Decrypts a string previously produced by upstream's
     * {@code XORCipher.encryptXORBase64}. Used only for migration.
     *
     * Upstream's encrypt path called {@code String.getBytes()} with no
     * explicit charset, so the produced bytes are platform-default
     * (effectively UTF-8 on macOS / Linux). We decode here as UTF-8
     * — that matches every platform we've ever shipped on. ASCII
     * passwords (the common case) round-trip identically under
     * either charset.
     */
    public static String decryptXorBase64(String s) throws IOException {
        try {
            return new String(xor(Base64.getDecoder().decode(s)), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new IOException("not a valid XOR-Base64 ciphertext", e);
        }
    }
}
