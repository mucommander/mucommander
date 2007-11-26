/*
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

package com.mucommander.conf;

import junit.framework.TestCase;

import java.util.Vector;

/**
 * Test case for the {@link ValueList} class.
 * @author Nicolas Rinaudo
 */
public class ValueListTest extends TestCase {
    // - Test data generation ------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a <code>(0, 1, 2, 3, 4, 5, 6, 7)</code> vector.
     */
    private static Vector createIntegerData() {
        Vector data;

        data = new Vector();
        for(int i = 1; i < 8; i++)
            data.add(new Integer(i));
        return data;
    }

    /**
     * Creates a <code>(0.5, 1.5, 2.5, 3.5, 4.5, 5.5, 6.5, 7.5)</code> vector.
     */
    private static Vector createFloatData() {
        Vector data;

        data = new Vector();
        for(int i = 1; i < 8; i++)
            data.add(new Float(i + 0.5));
        return data;
    }

    /**
     * Creates a <code>(true, false, true, false, true, false, true)</code> vector.
     */
    private static Vector createBooleanData() {
        Vector data;

        data = new Vector();
        for(int i = 0; i < 7; i++)
            data.add(new Boolean(i % 2 == 0));
        return data;
    }



    // - Value casting tests -------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Tests the {@link ValueList#valueAt(int)} method.
     * @param values test data.
     */
    protected void testStringValues(ValueList values) {
        assertEquals(values.size(), 7);
        for(int i = 0; i < 7; i++)
            assertEquals(Integer.toString(i + 1), values.valueAt(i));
    }

    /**
     * Tests the {@link ValueList#integerValueAt(int)} method.
     * @param values test data.
     */
    protected void testIntegerValues(ValueList values) {
        assertEquals(values.size(), 7);
        for(int i = 0; i < 7; i++)
            assertEquals(i + 1, values.integerValueAt(i));
    }

    /**
     * Tests the {@link ValueList#longValueAt(int)} method.
     * @param values test data.
     */
    protected void testLongValues(ValueList values) {
        assertEquals(values.size(), 7);
        for(int i = 0; i < 7; i++)
            assertEquals(i + 1, values.longValueAt(i));
    }

    /**
     * Tests the {@link ValueList#floatValueAt(int)} method.
     * @param values test data.
     */
    protected void testFloatValues(ValueList values) {
        assertEquals(values.size(), 7);
        for(int i = 0; i < 7; i++)
            assertEquals(i + 1.5, values.floatValueAt(i), 0);
    }

    /**
     * Tests the {@link ValueList#doubleValueAt(int)} method.
     * @param values test data.
     */
    protected void testDoubleValues(ValueList values) {
        assertEquals(values.size(), 7);
        for(int i = 0; i < 7; i++)
            assertEquals(i + 1.5, values.doubleValueAt(i), 0);
    }

    /**
     * Tests the {@link ValueList#booleanValueAt(int)} method.
     * @param values test data.
     */
    protected void testBooleanValues(ValueList values) {
        assertEquals(values.size(), 7);
        for(int i = 0; i < 7; i++)
            assertEquals(i % 2 == 0, values.booleanValueAt(i));
    }

    /**
     * Tests the {@link ValueList#listValueAt(int,String)} method.
     * @param values    test data.
     * @param separator separator to use when creating list values.
     */
    protected void testListValues(ValueList values, String separator) {
        testStringValues(values.listValueAt(0, separator));
        testIntegerValues(values.listValueAt(1, separator));
        testLongValues(values.listValueAt(2, separator));
        testFloatValues(values.listValueAt(3, separator));
        testDoubleValues(values.listValueAt(4, separator));
        testBooleanValues(values.listValueAt(5, separator));
    }



    // - Unit tests ----------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Tests the {@link ValueList#valueAt(int)} method.
     */
    public void testStringValues() {testStringValues(new ValueList(ValueList.toString(createIntegerData(), ";"), ";"));}

    /**
     * Tests the {@link ValueList#integerValueAt(int)} method.
     */
    public void testIntegerValues() {testIntegerValues(new ValueList(ValueList.toString(createIntegerData(), ";"), ";"));}

    /**
     * Tests the {@link ValueList#longValueAt(int)} method.
     */
    public void testLongValues() {testLongValues(new ValueList(ValueList.toString(createIntegerData(), ";"), ";"));}

    /**
     * Tests the {@link ValueList#floatValueAt(int)} method.
     */
    public void testFloatValues() {testFloatValues(new ValueList(ValueList.toString(createFloatData(), ";"), ";"));}

    /**
     * Tests the {@link ValueList#doubleValueAt(int)} method.
     */
    public void testDoubleValues() {testDoubleValues(new ValueList(ValueList.toString(createFloatData(), ";"), ";"));}

    /**
     * Tests the {@link ValueList#booleanValueAt(int)} method.
     */
    public void testBooleanValues() {testBooleanValues(new ValueList(ValueList.toString(createBooleanData(), ";"), ";"));}

    /**
     * Tests the {@link ValueList#listValueAt(int)} method.
     */
    public void testListValues() {
        Vector    data;
        ValueList values;

        data = new Vector();
        data.add(ValueList.toString(createIntegerData(), ";"));
        data.add(ValueList.toString(createIntegerData(), ";"));
        data.add(ValueList.toString(createIntegerData(), ";"));
        data.add(ValueList.toString(createFloatData(), ";"));
        data.add(ValueList.toString(createFloatData(), ";"));
        data.add(ValueList.toString(createBooleanData(), ";"));

        testListValues(new ValueList(ValueList.toString(data, " - "), " - ") , ";");

    }
}
