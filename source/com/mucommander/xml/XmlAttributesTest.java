/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
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

package com.mucommander.xml;

import junit.framework.TestCase;

import java.util.Iterator;

/**
 * Runs test on the {@link XmlWriterAttributes} class.
 * @author Nicolas Rinaudo
 */
public class XmlAttributesTest extends TestCase {
    // - Test constants --------------------------------------------------
    // -------------------------------------------------------------------
    /** Name of the first test attribute. */
    private static final String TEST_ATTRIBUTE_1 = "attribute1";
    /** Name of the second test attribute. */
    private static final String TEST_ATTRIBUTE_2 = "attribute2";
    /** First value of the test attribute. */
    private static final String TEST_VALUE_1     = "value1";
    /** Second value of the test attribute. */
    private static final String TEST_VALUE_2     = "value2";



    // - Instance fields -------------------------------------------------
    // -------------------------------------------------------------------
    /** Instance used to test the XmlAttributes class. */
    private XmlAttributes attributes;



    // - Initialisation --------------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Initialises the test case.
     */
    public void setUp() {attributes = new XmlAttributes();}



    // - Test code -------------------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Runs the basic tests.
     */
    public void testAttributes() {
        // Makes sure an attribute that has been added is properly retrieved.
        attributes.add(TEST_ATTRIBUTE_1, TEST_VALUE_1);
        assertEquals(TEST_VALUE_1, attributes.getValue(TEST_ATTRIBUTE_1));

        // Makes sure an attribute that has been overwritten is properly retrieved.
        attributes.add(TEST_ATTRIBUTE_1, TEST_VALUE_2);
        assertEquals(TEST_VALUE_2, attributes.getValue(TEST_ATTRIBUTE_1));

        // Makes sure the clear method works.
        attributes.clear();
        assertNull(attributes.getValue(TEST_ATTRIBUTE_1));
    }

    /**
     * Runs tests on the {@link XmlAttributes#names()} method.
     */
    public void testNames() {
        Iterator names;
        String   buffer;

        // Makes sure the names method works on an empty set of attributes.
        names = attributes.names();
        assertFalse(names.hasNext());

        // Makes sure the names method works on a set of attributes that only contains
        // one element.
        attributes.add(TEST_ATTRIBUTE_1, TEST_VALUE_1);
        names = attributes.names();
        assertTrue(names.hasNext());
        assertEquals(TEST_ATTRIBUTE_1, buffer = (String)names.next());
        assertEquals(TEST_VALUE_1, attributes.getValue(buffer));
        assertFalse(names.hasNext());

        // Makes sure the names method works on a set of attributes that contains more
        // than one element.
        attributes.add(TEST_ATTRIBUTE_2, TEST_VALUE_2);
        names = attributes.names();
        assertTrue(names.hasNext());
        checkAttribute((String)names.next());
        assertTrue(names.hasNext());
        checkAttribute((String)names.next());
        assertFalse(names.hasNext());

        // Makes sure the iterator is read-only.
        names = attributes.names();
        try {
            names.remove();
            fail();
        }
        catch(Exception e) {}
    }

    /**
     * Makes sure the specified attribute name has the right value.
     */
    private void checkAttribute(String name) {
        if(name.equals(TEST_ATTRIBUTE_1))
            assertEquals(TEST_VALUE_1, attributes.getValue(TEST_ATTRIBUTE_1));
        else if(name.equals(TEST_ATTRIBUTE_2))
            assertEquals(TEST_VALUE_2, attributes.getValue(TEST_ATTRIBUTE_2));
        else
            fail();
    }
}
