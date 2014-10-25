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

package com.mucommander.commons.conf;

/**
 * Test case for the {@link ValueIterator} class.
 * @author Nicolas Rinaudo
 */
public class ValueIteratorTest extends ValueListTest {
    /**
     * Tests the {@link ValueIterator#nextValue()} method.
     * @param values test data.
     */
    @Override
    protected void testStringValues(ValueList values) {
        ValueIterator iterator;

        iterator = values.valueIterator();
        for(int i = 0; i < 7; i++) {
            assert iterator.hasNext();
            assert Integer.toString(i + 1).equals(iterator.nextValue());
        }
        assert !iterator.hasNext();
    }

    /**
     * Tests the {@link ValueIterator#nextIntegerValue()} method.
     * @param values test data.
     */
    @Override
    protected void testIntegerValues(ValueList values) {
        ValueIterator iterator;

        iterator = values.valueIterator();
        for(int i = 0; i < 7; i++) {
            assert iterator.hasNext();
            assert i + 1 == iterator.nextIntegerValue();
        }
        assert !iterator.hasNext();
    }

    /**
     * Tests the {@link ValueIterator#nextLongValue()} method.
     * @param values test data.
     */
    @Override
    protected void testLongValues(ValueList values) {
        ValueIterator iterator;

        iterator = values.valueIterator();
        for(int i = 0; i < 7; i++) {
            assert iterator.hasNext();
            assert i + 1 == iterator.nextLongValue();
        }
    }

    /**
     * Tests the {@link ValueIterator#nextFloatValue()} method.
     * @param values test data.
     */
    @Override
    protected void testFloatValues(ValueList values) {
        ValueIterator iterator;

        iterator = values.valueIterator();
        for(int i = 0; i < 7; i++) {
            assert iterator.hasNext();
            assert i + 1.5 == iterator.nextFloatValue();
        }
    }

    /**
     * Tests the {@link ValueIterator#nextDoubleValue()} method.
     * @param values test data.
     */
    @Override
    protected void testDoubleValues(ValueList values) {
        ValueIterator iterator;

        iterator = values.valueIterator();
        for(int i = 0; i < 7; i++) {
            assert iterator.hasNext();
            assert i + 1.5 == iterator.nextDoubleValue();
        }
    }

    /**
     * Tests the {@link ValueIterator#nextBooleanValue()} method.
     * @param values test data.
     */
    @Override
    protected void testBooleanValues(ValueList values) {
        ValueIterator iterator;

        iterator = values.valueIterator();
        for(int i = 0; i < 7; i++) {
            assert iterator.hasNext();
            assert (i % 2 == 0) == iterator.nextBooleanValue();
        }
    }

    /**
     * Tests the {@link ValueIterator#nextListValue(String)} method.
     * @param values    test data.
     * @param separator separator to use when creating list values.
     */
    @Override
    protected void testListValues(ValueList values, String separator) {
        ValueIterator iterator;

        iterator = values.valueIterator();

        assert iterator.hasNext();
        testStringValues(iterator.nextListValue(separator));

        assert iterator.hasNext();
        testIntegerValues(iterator.nextListValue(separator));

        assert iterator.hasNext();
        testLongValues(iterator.nextListValue(separator));

        assert iterator.hasNext();
        testFloatValues(iterator.nextListValue(separator));

        assert iterator.hasNext();
        testDoubleValues(iterator.nextListValue(separator));

        assert iterator.hasNext();
        testBooleanValues(iterator.nextListValue(separator));

        assert !iterator.hasNext();
    }
}
