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

package com.mucommander.io.bom;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.IOException;

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
    public void testBOMInputStreamDetection() throws IOException {
        ByteArrayInputStream bais;
        BOM bom;
        for(int i=0; i<SUPPORTED_BOMS.length; i++) {
            bom = SUPPORTED_BOMS[i];
            bais = new ByteArrayInputStream(bom.getSignature());
            assertEquals(bom, new BOMInputStream(bais).getBOM());
        }

        // UTF-8 BOM plus one byte after
        bais = new ByteArrayInputStream(new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF, (byte)0x27});
        assertEquals(UTF8_BOM, new BOMInputStream(bais).getBOM());

        // Not a known BOM
        bais = new ByteArrayInputStream(new byte[]{(byte)0xEF, (byte)0xBB, (byte)0x27});
        assertNull(new BOMInputStream(bais).getBOM());

        // BOMs should not match
        bais = new ByteArrayInputStream(UTF16_BE_BOM.getSignature());
        assertNotSame(UTF8_BOM, new BOMInputStream(bais).getBOM());
    }

    /**
     * Tests the integrity of {@link BOMInputStream}'s data.
     *
     * @throws IOException should normally not happen
     */
    public void testBOMInputStreamIntegrity() throws IOException {
        // 3 first bytes corresponding to UTF-8 BOM should be discarded
        BOMInputStream bomIn = new BOMInputStream(new ByteArrayInputStream(new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF, (byte)0x27}));
        assertEquals(0x27, bomIn.read()&0xFF);
        assertEquals(-1, bomIn.read());

        // Not a known BOM, the byte sequence should remain the same
        bomIn = new BOMInputStream(new ByteArrayInputStream(new byte[]{(byte)0xEF, (byte)0xBB, (byte)0x27}));
        assertEquals(0xEF, bomIn.read()&0xFF);
        assertEquals(0xBB, bomIn.read()&0xFF);
        assertEquals(0x27, bomIn.read()&0xFF);
        assertEquals(-1, bomIn.read());
    }
}
