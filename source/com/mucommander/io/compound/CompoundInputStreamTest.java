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

package com.mucommander.io.compound;

import com.mucommander.io.StreamUtils;
import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

/**
 * A test case for {@link CompoundInputStream}.
 *
 * @author Maxence Bernard
 */
public class CompoundInputStreamTest extends TestCase {

    /** Test strings */
    private final static String[] TEST_STRINGS = {
        "",
        "this",
        "is",
        "a",
        "",
        "test",
        ""
    };

    /** Concatenation of the strings contained by {@link #TEST_STRINGS} */
    private final static String TEST_FLATTENED_STRINGS;
    
    static {
        StringBuffer sb = new StringBuffer();
        for (String testString : TEST_STRINGS)
            sb.append(testString);

        TEST_FLATTENED_STRINGS = sb.toString();
    }


    /**
     * Returns a test InputStream iterator.
     *
     * @return a test InputStream iterator.
     */
    private static Iterator<ByteArrayInputStream> getTestInputStreamIterator() {
        Vector<ByteArrayInputStream> v = new Vector<ByteArrayInputStream>();

        for (String testString : TEST_STRINGS) 
            v.add(new ByteArrayInputStream(testString.getBytes()));

        return v.iterator();
    }


    /**
     * Tests {@link CompoundInputStream} in merged mode.
     *
     * @throws IOException should not happen.
     */
    public void testMerged() throws IOException {
        CompoundInputStream in = new IteratorCompoundInputStream(getTestInputStreamIterator(), true);

        assertTrue(in.isMerged());

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        StreamUtils.copyStream(in, bout);
        assertEquals(TEST_FLATTENED_STRINGS, bout.toString());

        assertFalse(in.advanceInputStream());
    }

    /**
     * Tests {@link CompoundInputStream} in unmerged mode.
     *
     * @throws IOException should not happen.
     */
    public void testUnmerged() throws IOException {
        CompoundInputStream in = new IteratorCompoundInputStream(getTestInputStreamIterator(), false);

        assertFalse(in.isMerged());

        for(int i=0; i<TEST_STRINGS.length; i++) {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            StreamUtils.copyStream(in, bout);
            assertEquals(TEST_STRINGS[i], bout.toString());

            // Try to advance to the next stream
            assertEquals(i!=TEST_STRINGS.length-1, in.advanceInputStream());
        }
    }
}
