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

import junit.framework.TestCase;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Vector;

/**
 * A test case for {@link CompoundReader}.
 *
 * @author Maxence Bernard
 */
public class CompoundReaderTest extends TestCase {

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
     * Returns a test Reader iterator.
     *
     * @return a test Reader iterator.
     */
    private static Iterator<StringReader> getTestReaderIterator() {
        Vector<StringReader> v = new Vector<StringReader>();

        for (String testString : TEST_STRINGS)
            v.add(new StringReader(testString));

        return v.iterator();
    }

    private String copyReader(Reader reader) throws IOException {
        StringBuffer sb = new StringBuffer();
        int c;
        while((c=reader.read())!=-1)
            sb.append((char)c);

        return sb.toString();
    }
    
    /**
     * Tests {@link CompoundReader} in merged mode.
     *
     * @throws IOException should not happen.
     */
    public void testMerged() throws IOException {
        CompoundReader reader = new IteratorCompoundReader(getTestReaderIterator(), true);

        assertTrue(reader.isMerged());

        assertEquals(TEST_FLATTENED_STRINGS, copyReader(reader));

        assertFalse(reader.advanceReader());
    }

    /**
     * Tests {@link CompoundInputStream} in unmerged mode.
     *
     * @throws IOException should not happen.
     */
    public void testUnmerged() throws IOException {
        CompoundReader reader = new IteratorCompoundReader(getTestReaderIterator(), false);

        assertFalse(reader.isMerged());

        for(int i=0; i<TEST_STRINGS.length; i++) {
            assertEquals(TEST_STRINGS[i], copyReader(reader));

            // Try to advance to the next reader
            assertEquals(i!=TEST_STRINGS.length-1, reader.advanceReader());
        }
    }
}