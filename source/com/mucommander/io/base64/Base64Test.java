/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
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

package com.mucommander.io.base64;

import junit.framework.TestCase;

import java.io.IOException;
import java.util.Random;

/**
 * A test case for the <code>com.mucommander.io.base64</code> package.
 *
 * @author Maxence Bernard
 */
public class Base64Test extends TestCase {

    /**
     * Tests base64 encoding and decoding on a known sequence, and ensures that the result are as expected.
     *
     * @throws IOException should not happen
     */
    public void testKnownSequence() throws IOException {
        String decodedSequence = "The quick brown fox jumps over the lazy dog.";
        String encodedSequence = "VGhlIHF1aWNrIGJyb3duIGZveCBqdW1wcyBvdmVyIHRoZSBsYXp5IGRvZy4=";

        assertEquals(encodedSequence, Base64Encoder.encode(decodedSequence));
        assertEquals(decodedSequence, Base64Decoder.decode(encodedSequence));
    }

    /**
     * Successively encodes and decodes randomly-generated strings of varying length and content, and verifies that
     * the resulting string remains the same as the original.
     *
     * @throws IOException should not happen
     */
    public void testRandomStringIntegrity() throws IOException {
        Random random = new Random();

        StringBuffer sb;
        String s;
        int slen;
        // Repeats the test
        for(int i=0; i<100; i++) {
            // Generates a string with:
            // - a random length of up to 1000 characters
            // - random contents, where each byte's value is randomly chosen between 0 and 255
            slen = random.nextInt(1000);

            sb = new StringBuffer();
            for(int j=0; j<slen; j++)
                sb.append((char)random.nextInt(256));

            s = sb.toString();

            assertEquals(s, Base64Decoder.decode(Base64Encoder.encode(s)));
        }
    }

    /**
     * Validates that <code>java.io.IOException</code> is properly thrown by {@link Base64InputStream}
     * when a character out of the base64 range is encountered. All such characters are successively tested.
     */
    public void testInvalidCharacters() {
        char c;
        boolean exceptionCaught;

        for(c=0; c<256; c++) {
            // Skip allowed Base64 characters, including the special '=' character used for padding
            if((c>='0' && c<='9') || (c>='A' && c<='Z') || (c>='a' && c<='z') || c=='+' || c=='/' || c=='=')
                continue;

            exceptionCaught = false;

            try {
                Base64Decoder.decode(c+"===");      // Add padding at the end
            }
            catch(IOException e) {
                exceptionCaught = true;
            }

            assertTrue(exceptionCaught);
        }
    }

    /**
     * Validates that <code>java.io.IOException</code> is properly thrown by {@link Base64InputStream}
     * when the provided Base64 string's length is not a multiple of 4 (i.e. is not properly padded with '=').
     */
    public void testInvalidLength() {
        String invalidLengthStrings[] = {
            "a", "ab", "abc",
            "=", "a=", "a==", "ab=",
            "0000a", "0000ab", "0000abc",
            "0000a=", "0000a==", "0000ab="
        };

        boolean exceptionCaught;

        for(int i=0; i<invalidLengthStrings.length; i++) {
            exceptionCaught = false;

            try {
                Base64Decoder.decode(invalidLengthStrings[i]);
            }
            catch(IOException e) {
                exceptionCaught = true;
            }

            assertTrue(exceptionCaught);
        }
    }
}
