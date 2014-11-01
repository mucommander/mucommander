/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.bookmark;

import com.mucommander.commons.io.base64.Base64Decoder;
import com.mucommander.commons.io.base64.Base64Encoder;

import java.io.IOException;

/**
 * This class provides provides simple XOR symmetrical encryption using a static hard-coded key, coupled with Base64
 * encoding so that encrypted strings only use alphanumeric characters and thus can be embedded in text formats such
 * as XML.
 *
 * <p><b>Disclaimer</b>: this obviously is weak encryption at most, the key used being static and public, and XOR
 * encryption being easy to crack. This doesn't aim or pretend to be anything more than a way to scramble text
 * without requiring a master password in the application.</p>
 *
 * @author Maxence Bernard
 */
public class XORCipher {


    /** Long enough key (256 bytes) to avoid having too much redundancy in small text strings. */
    public final static int NOT_SO_PRIVATE_KEY[] = {
        161, 220, 156, 76, 177, 174, 56, 37, 98, 93, 224, 19, 160, 95, 69, 140,
        91, 138, 33, 114, 248, 57, 179, 17, 54, 172, 249, 58, 26, 181, 167, 231,
        241, 185, 218, 174, 37, 102, 100, 26, 16, 214, 119, 29, 118, 151, 135, 175,
        245, 247, 160, 188, 77, 173, 109, 255, 73, 44, 186, 211, 117, 236, 204, 58,
        246, 210, 128, 33, 234, 218, 82, 188, 78, 229, 180, 108, 247, 200, 3, 142,
        206, 45, 165, 111, 96, 72, 76, 81, 238, 186, 240, 167, 185, 152, 68, 228,
        87, 142, 145, 7, 74, 12, 106, 94, 15, 218, 155, 71, 87, 136, 58, 40,
        246, 94, 7, 89, 29, 0, 78, 204, 70, 220, 240, 127, 59, 184, 109, 106
    };


    /**
     * Cyphers the given byte array using XOR symmetrical encryption with a static hard-coded key.
     *
     * @param b the byte array to encrypt/decrypt
     * @return the encrypted/decrypted byte array
     */
    private static byte[] xor(byte[] b) {
        int len = b.length;
        int keyLen = NOT_SO_PRIVATE_KEY.length;

        byte[] result = new byte[len];
        for(int i=0; i<len; i++)
            result[i] = (byte)(b[i]^NOT_SO_PRIVATE_KEY[i%keyLen]);

        return result;
    }

    /**
     * Encrypts the given String using XOR cipher followed by Base64 encoding. The returned String will only contain
     * alphanumeric characters.
     *
     * @param s the String to encrypt
     * @return a XOR-Base64 encrypted String
     */
    public static String encryptXORBase64(String s) {
        // Todo:
        // Important: String.getBytes() returns bytes in the platform's default encoding, which might vary across
        // platforms. This may potentially cause problems when decrypting a string on a different platform from the one
        // which served to encrypt it.
        // It is however too late to change as it could prevent existing encrypted strings (credentials file) from being
        // loaded after the application is updated.
        return Base64Encoder.encode(xor(s.getBytes()));
    }


    /**
     * Decrypts the given XOR-Base64 encrypted String and throws an IOException if the given String is not properly
     * Base64-encoded.
     *
     * @param s a XOR-Base64 encrypted String
     * @return the decrypted String
     * @throws IOException if the given String is not properly Base64-encoded
     */
    public static String decryptXORBase64(String s) throws IOException {
        // Todo:
        // Important: new String() creates a string using the platform's default encoding, which might vary across
        // platforms. This may potentially cause problems when decrypting a string on a different platform from the one
        // which served to encrypt it.
        // It is however too late to change as it could prevent existing encrypted strings (credentials file) from being
        // loaded after the application is updated.
        return new String(xor(Base64Decoder.decodeAsBytes(s)));
    }
}
