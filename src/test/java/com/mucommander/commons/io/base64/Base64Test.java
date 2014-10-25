/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.commons.io.base64;

import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Random;

/**
 * A test case for the <code>com.mucommander.commons.io.base64</code> package.
 *
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public class Base64Test {

    /**
     * Tests base64 encoding and decoding on known sequences, ensuring that base 64 encoding and decoding produces
     * the expected results.
     *
     * @throws IOException should not happen
     */
    @Test
    public void testKnownSequences() throws IOException {
        // On an ASCII sequence
        testKnownSequence("The quick brown fox jumps over the lazy dog.", "VGhlIHF1aWNrIGJyb3duIGZveCBqdW1wcyBvdmVyIHRoZSBsYXp5IGRvZy4=");

        // On a Unicode sequence. Note that the string has been encoded using UTF-8 bytes.
        testKnownSequence("どうもありがとうミスターロボット", "44Gp44GG44KC44GC44KK44GM44Go44GG44Of44K544K/44O844Ot44Oc44OD44OI");
    }

    /**
     * Tests base64 encoding and decoding on a known sequence, ensuring that base 64 encoding and decoding produces
     * the expected results.
     *
     * @param decodedSequence the base64-decoded string
     * @param encodedSequence the base64-encoding string
     * @throws IOException should not happen
     */
    private void testKnownSequence(String decodedSequence, String encodedSequence) throws IOException {
        assert encodedSequence.equals(Base64Encoder.encode(decodedSequence, "UTF-8", Base64Table.STANDARD_TABLE));
        assert decodedSequence.equals(Base64Decoder.decode(encodedSequence, "UTF-8", Base64Table.STANDARD_TABLE));
    }


    /**
     * Successively encodes and decodes randomly-generated strings of varying length and content, and verifies that
     * the resulting string remains the same as the original.
     *
     * @throws IOException should not happen
     */
    @Test
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

            assert s.equals(Base64Decoder.decode(Base64Encoder.encode(s, "UTF-8", Base64Table.STANDARD_TABLE)));
        }
    }

    /**
     * Validates that <code>java.io.IOException</code> is properly thrown by {@link Base64InputStream}
     * when a character out of the base64 range is encountered. All such characters are successively tested.
     */
    @Test
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

            assert exceptionCaught;
        }
    }

    /**
     * Validates that <code>java.io.IOException</code> is properly thrown by {@link Base64InputStream}
     * when the provided Base64 string's length is not a multiple of 4 (i.e. is not properly padded with '=').
     */
    @Test
    public void testInvalidLength() {
        String invalidLengthStrings[] = {
            "a", "ab", "abc",
            "=", "a=", "a==", "ab=",
            "0000a", "0000ab", "0000abc",
            "0000a=", "0000a==", "0000ab="
        };

        boolean exceptionCaught;

        for (String invalidLengthString : invalidLengthStrings) {
            exceptionCaught = false;

            try {
                Base64Decoder.decode(invalidLengthString);
            }
            catch (IOException e) {
                exceptionCaught = true;
            }

            assert exceptionCaught;
        }
    }


    /**
     * Tests {@link Base64Table} preset instances.
     *
     * @throws IOException should not happen
     */
    @Test
    public void testPresetTables() throws IOException {
        Base64Table[] tables = new Base64Table[] {
            Base64Table.STANDARD_TABLE,
            Base64Table.URL_SAFE_TABLE,
            Base64Table.FILENAME_SAFE_TABLE,
            Base64Table.REGEXP_SAFE_TABLE,
        };

        String sample = "The quick brown fox jumps over the lazy dog.";
        for(Base64Table table: tables) {
            // Ensure that the table passes Base64Table constructor's tests
            new Base64Table(table.getEncodingTable(), table.getPaddingChar());

            // Ensures that encoding followed by decoding yields the original string
            assert sample.equals(Base64Decoder.decode(Base64Encoder.encode(sample, "UTF-8", table), "UTF-8", table));
        }

    }

    /**
     * Tests {@link Base64Table#Base64Table(byte[], byte)} with invalid parameter values.
     */
    @Test
    public void testCustomBase64Table()  {
        testInvalidBase64Table(null, (byte)'a');
        testInvalidBase64Table(new byte[]{}, (byte)'a');

        byte[] validEncodingTable = Base64Table.STANDARD_TABLE.getEncodingTable();

        testInvalidBase64Table(validEncodingTable, (byte)'a');

        byte[] invalidEncodingTable = new byte[63];
        System.arraycopy(validEncodingTable, 0, invalidEncodingTable, 0, 63);

        testInvalidBase64Table(invalidEncodingTable, (byte)'a');

        invalidEncodingTable = new byte[64];
        System.arraycopy(validEncodingTable, 0, invalidEncodingTable, 0, 64);
        invalidEncodingTable[63] = 'b';

        testInvalidBase64Table(invalidEncodingTable, (byte)'a');

        // Test a valid custom Base64 table
        new Base64Table(new byte[]{
                'g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v',
                'Q','R','S','T','U','V','W','X','Y','Z','a','b','c','d','e','f',
                'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P',
                'w','x','y','z','0','1','2','3','4','5','6','7','8','9','@','!'
        }, (byte)'%');
    }


    /**
     * Tries to create a <code>Base64Table</code> with the specified parameters and asserts that it throws
     * an {@link IllegalArgumentException}.
     *
     * @param table the base64 character table. The array must be 64 bytes long and must not contain any duplicate values.
         * @param paddingChar the ASCII character used for padding. This character must not already be used in the table.
         */
    private void testInvalidBase64Table(byte[] table, byte paddingChar) {
        boolean exceptionThrown = false;
        try {
            new Base64Table(table, paddingChar);
        }
        catch(IllegalArgumentException e) {
            exceptionThrown = true;
        }

        assert exceptionThrown;
    }
}
