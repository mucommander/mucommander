/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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

package com.mucommander.commons.io.bom;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A test case for the <code>com.mucommander.io.bom</code> package.
 *
 * @author Maxence Bernard
 */
public class BOMTest extends TestCase implements BOMConstants {

    /**
     * Tests {@link BOM} comparison methods.
     */
    public void testBOMComparisons() {
        // Tests BOM#sigStartsWith method
        assertTrue(UTF8_BOM.sigStartsWith(new byte[]{(byte)0xEF, (byte)0xBB}));
        assertTrue(UTF8_BOM.sigStartsWith(new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF}));
        assertFalse(UTF8_BOM.sigStartsWith(new byte[]{(byte)0xAA}));
        assertFalse(UTF8_BOM.sigStartsWith(new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF, (byte)0xAA}));

        // Tests BOM#sigEquals method
        assertTrue(UTF8_BOM.sigEquals(UTF8_BOM.getSignature()));
        assertFalse(UTF8_BOM.sigEquals(UTF16_LE_BOM.getSignature()));

        // Tests BOM#equals method
        assertTrue(UTF8_BOM.equals(UTF8_BOM));
        assertFalse(UTF8_BOM.equals(UTF16_LE_BOM));
        assertFalse(UTF8_BOM.equals(new Object()));
    }

    /**
     * Tests proper detection of known BOMs.
     *
     * @throws IOException should normally not happen
     */
    public void testBOMInputStream() throws IOException {
        BOMInputStream bomIn;
        byte[] b;

        for (BOM bom : SUPPORTED_BOMS) {
            bomIn = getBOMInputStream(bom.getSignature());
            assertEquals(bom, bomIn.getBOM());
            assertEOF(bomIn);
        }

        // UTF-8 BOM, plus one byte after
        b = new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF, (byte)0x27};
        bomIn = getBOMInputStream(b);
        assertEquals(UTF8_BOM, bomIn.getBOM());
        assertStreamEquals(new byte[]{(byte)0x27}, bomIn);
        assertEOF(bomIn);

        // Not a known BOM
        b = new byte[]{(byte)0xEF, (byte)0xBB, (byte)0x27};
        bomIn = getBOMInputStream(b);
        assertNull(bomIn.getBOM());
        assertStreamEquals(b, bomIn);

        // Empty stream, BOM should be null
        b = new byte[]{};
        bomIn = getBOMInputStream(b);
        assertNull(bomIn.getBOM());
        assertEOF(bomIn);

        // BOMs should not match
        b = UTF16_BE_BOM.getSignature();
        bomIn = getBOMInputStream(b);
        assertNotSame(UTF8_BOM, bomIn.getBOM());
        assertEOF(bomIn);
    }

    /**
     * Tests {@link BOM#getInstance(String)}.
     */
    public void testBOMResolution() {
        for (BOM bom : SUPPORTED_BOMS) {
            // Test case variations
            assertEquals(bom, BOM.getInstance(bom.getEncoding().toLowerCase()));
            assertEquals(bom, BOM.getInstance(bom.getEncoding().toUpperCase()));
        }

        // Test non-UTF encodings
        assertNull(BOM.getInstance("ISO-8859-1"));
        assertNull(BOM.getInstance("Shift_JIS"));

        // Test UTF aliases
        assertEquals(BOMConstants.UTF16_BE_BOM, BOM.getInstance("UnicodeBig"));
        assertEquals(BOMConstants.UTF16_BE_BOM, BOM.getInstance("UnicodeBigUnmarked"));
        assertEquals(BOMConstants.UTF16_BE_BOM, BOM.getInstance("UTF-16"));
        assertEquals(BOMConstants.UTF16_LE_BOM, BOM.getInstance("UnicodeLittle"));
        assertEquals(BOMConstants.UTF16_LE_BOM, BOM.getInstance("UnicodeLittleUnmarked"));
        assertEquals(BOMConstants.UTF32_BE_BOM, BOM.getInstance("UTF-32"));
    }

    /**
     * Tests {@link BOMWriter}.
     *
     * @throws IOException should not happen
     */
    public void testBOMWriter() throws IOException {
        String testString = "This is a test";
        ByteArrayOutputStream baos;
        BOMWriter bomWriter;
        BOMInputStream bomIn;

        for (BOM bom : SUPPORTED_BOMS) {
            baos = new ByteArrayOutputStream();
            bomWriter = new BOMWriter(baos, bom.getEncoding());
            bomWriter.write(testString);
            bomWriter.close();

            bomIn = getBOMInputStream(baos.toByteArray());
            assertEquals(bom, bomIn.getBOM());
            assertStreamEquals(testString.getBytes(bom.getEncoding()), bomIn);
            assertEOF(bomIn);
        }
    }


    ////////////////////
    // Helper methods //
    ////////////////////

    private BOMInputStream getBOMInputStream(byte b[]) throws IOException {
        return new BOMInputStream(new ByteArrayInputStream(b));
    }

    private void assertEOF(InputStream in) throws IOException {
        assertEquals(-1, in.read());
        // Again
        assertEquals(-1, in.read());
    }

    private void assertStreamEquals(byte bytes[], InputStream in) throws IOException {
        for (byte b : bytes)
            assertEquals(b, (byte) (in.read() & 0xFF));

        assertEOF(in);
    }
}
