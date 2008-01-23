/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
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

package com.mucommander.conf;

/**
 * Test case for the {@link ValueIterator} class.
 * @author Nicolas Rinaudo
 */
public class ValueIteratorTest extends ValueListTest {
    /**
     * Tests the {@link ValueIterator#nextValue()} method.
     * @param values test data.
     */
    protected void testStringValues(ValueList values) {
        ValueIterator iterator;

        iterator = values.valueIterator();
        for(int i = 0; i < 7; i++) {
            assertTrue(iterator.hasNext());
            assertEquals(Integer.toString(i + 1), iterator.nextValue());
        }
        assertFalse(iterator.hasNext());
    }

    /**
     * Tests the {@link ValueIterator#nextIntegerValue()} method.
     * @param values test data.
     */
    protected void testIntegerValues(ValueList values) {
        ValueIterator iterator;

        iterator = values.valueIterator();
        for(int i = 0; i < 7; i++) {
            assertTrue(iterator.hasNext());
            assertEquals(i + 1, iterator.nextIntegerValue());
        }
        assertFalse(iterator.hasNext());
    }

    /**
     * Tests the {@link ValueIterator#nextLongValue()} method.
     * @param values test data.
     */
    protected void testLongValues(ValueList values) {
        ValueIterator iterator;

        iterator = values.valueIterator();
        for(int i = 0; i < 7; i++) {
            assertTrue(iterator.hasNext());
            assertEquals(i + 1, iterator.nextLongValue());
        }
    }

    /**
     * Tests the {@link ValueIterator#nextFloatValue()} method.
     * @param values test data.
     */
    protected void testFloatValues(ValueList values) {
        ValueIterator iterator;

        iterator = values.valueIterator();
        for(int i = 0; i < 7; i++) {
            assertTrue(iterator.hasNext());
            assertEquals(i + 1.5, iterator.nextFloatValue(), 0);
        }
    }

    /**
     * Tests the {@link ValueIterator#nextDoubleValue()} method.
     * @param values test data.
     */
    protected void testDoubleValues(ValueList values) {
        ValueIterator iterator;

        iterator = values.valueIterator();
        for(int i = 0; i < 7; i++) {
            assertTrue(iterator.hasNext());
            assertEquals(i + 1.5, iterator.nextDoubleValue(), 0);
        }
    }

    /**
     * Tests the {@link ValueIterator#nextBooleanValue()} method.
     * @param values test data.
     */
    protected void testBooleanValues(ValueList values) {
        ValueIterator iterator;

        iterator = values.valueIterator();
        for(int i = 0; i < 7; i++) {
            assertTrue(iterator.hasNext());
            assertEquals(i % 2 == 0, iterator.nextBooleanValue());
        }
    }

    /**
     * Tests the {@link ValueIterator#nextListValue()} method.
     * @param values    test data.
     * @param separator separator to use when creating list values.
     */
    protected void testListValues(ValueList values, String separator) {
        ValueIterator iterator;

        iterator = values.valueIterator();

        assertTrue(iterator.hasNext());
        testStringValues(iterator.nextListValue(separator));

        assertTrue(iterator.hasNext());
        testIntegerValues(iterator.nextListValue(separator));

        assertTrue(iterator.hasNext());
        testLongValues(iterator.nextListValue(separator));

        assertTrue(iterator.hasNext());
        testFloatValues(iterator.nextListValue(separator));

        assertTrue(iterator.hasNext());
        testDoubleValues(iterator.nextListValue(separator));

        assertTrue(iterator.hasNext());
        testBooleanValues(iterator.nextListValue(separator));

        assertFalse(iterator.hasNext());
    }
}
