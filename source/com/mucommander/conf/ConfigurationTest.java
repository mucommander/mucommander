/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
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

package com.mucommander.conf;

import junit.framework.TestCase;

/**
 * A test case for the {@link Configuration} class.
 *
 * @author Maxence Bernard
 */
public class ConfigurationTest extends TestCase {

    /** The Configuration instance that is being tested. Intialized each time a test is performed */
    private Configuration conf;

    /**
     * Creates a fresh {@link Configuration} instance each time a test is performed.
     */
    protected void setUp() throws Exception {
        conf = new Configuration();
    }


    /**
     * Tests the <code>getVariable()</code> and <code>setVariable</code> methods with string values.
     */
    public void testStringValues() {
        String testVar1 = "stringVar1";

        // Assert that getVariable returns null when a variable has no value
        assertNull(conf.getVariable(testVar1));

        // Assert that the default value is properly returned by getVariable when the variable has no value, and that
        // the variable is set to the default value and returned by further calls to getVariable.
        String value1 = "string1";
        assertEquals(value1, conf.getVariable(testVar1, value1));
        assertEquals(value1, conf.getVariable(testVar1));

        // Assert that a new value replaces the previous value, and that the default value passed to getVariable
        // is not returned anymore.
        String value2 = "string2";
        assertTrue(conf.setVariable(testVar1, value2));
        assertEquals(value2, conf.getVariable(testVar1));
        assertEquals(value2, conf.getVariable(testVar1, value1));

        String testVar2 = "stringVar2";

        // Assert that setVariable properly sets the value when the variable has no previous value
        assertTrue(conf.setVariable(testVar2, value1));
        assertEquals(value1, conf.getVariable(testVar2));

        // Assert that setVariable returns false when the specified value is equal to the current value
        assertFalse(conf.setVariable(testVar2, value1));

        // Special tests for the null value
        assertTrue(conf.setVariable(testVar2, null));
        assertEquals(null, conf.getVariable(testVar2));
        assertEquals(null, conf.getVariable(testVar2, null));
        assertFalse(conf.setVariable(testVar2, null));
    }

    /**
     * Tests the <code>getVariable()</code> and <code>setVariable</code> methods with boolean values.
     */
    public void testBooleanValues() {
        String testVar1 = "booleanVar1";

        // Assert that getVariable returns null when a variable has no value
        assertNull(conf.getVariable(testVar1));

        // Assert that the default value is properly returned by getVariable when the variable has no value, and that
        // the variable is set to the default value and returned by further calls to getVariable.
        boolean value1 = true;
        assertEquals(value1, conf.getVariable(testVar1, value1));
        assertEquals(value1, conf.getBooleanVariable(testVar1));

        // Assert that a new value replaces the previous value, and that the default value passed to getVariable
        // is not returned anymore.
        boolean value2 = false;
        assertTrue(conf.setVariable(testVar1, value2));
        assertEquals(value2, conf.getBooleanVariable(testVar1));
        assertEquals(value2, conf.getVariable(testVar1, value1));

        String testVar2 = "booleanVar2";

        // Assert that setVariable properly sets the value when the variable has no previous value
        assertTrue(conf.setVariable(testVar2, value1));
        assertEquals(value1, conf.getBooleanVariable(testVar2));

        // Assert that setVariable returns false when the specified value is equal to the current value
        assertFalse(conf.setVariable(testVar2, value1));
    }

    /**
     * Tests the <code>getVariable()</code> and <code>setVariable</code> methods with integer (int) values.
     */
    public void testIntegerValues() {
        String testVar1 = "intVar1";

        // Assert that getVariable returns null when a variable has no value
        assertNull(conf.getVariable(testVar1));

        // Assert that the default value is properly returned by getVariable when the variable has no value, and that
        // the variable is set to the default value and returned by further calls to getVariable.
        int value1 = 7;
        assertEquals(value1, conf.getVariable(testVar1, value1));
        assertEquals(value1, conf.getIntegerVariable(testVar1));

        // Assert that a new value replaces the previous value, and that the default value passed to getVariable
        // is not returned anymore.
        int value2 = 27;
        assertTrue(conf.setVariable(testVar1, value2));
        assertEquals(value2, conf.getIntegerVariable(testVar1));
        assertEquals(value2, conf.getVariable(testVar1, value1));

        String testVar2 = "intVar2";

        // Assert that setVariable properly sets the value when the variable has no previous value
        assertTrue(conf.setVariable(testVar2, value1));
        assertEquals(value1, conf.getIntegerVariable(testVar2));

        // Assert that setVariable returns false when the specified value is equal to the current value
        assertFalse(conf.setVariable(testVar2, value1));

        // Assert that min and max int values are properly preserved
        assertTrue(conf.setVariable(testVar2, Integer.MIN_VALUE));
        assertEquals(Integer.MIN_VALUE, conf.getIntegerVariable(testVar2));

        assertTrue(conf.setVariable(testVar2, Integer.MAX_VALUE));
        assertEquals(Integer.MAX_VALUE, conf.getIntegerVariable(testVar2));

        // Assert that NumberFormatException is thrown when the value can not be parsed as an int
        boolean numberFormatExceptionThrown = false;
        try {
            conf.setVariable(testVar2, "notAnInt");
            conf.getIntegerVariable(testVar2);
        }
        catch(NumberFormatException e) {
            numberFormatExceptionThrown = true;
        }

        assertTrue("NumberFormatException should be thrown", numberFormatExceptionThrown);
    }


    /**
     * Tests the <code>getVariable()</code> and <code>setVariable</code> methods with long (int) values.
     */
    public void testLongValues() {
        String testVar1 = "longVar1";

        // Assert that getVariable returns null when a variable has no value
        assertNull(conf.getVariable(testVar1));

        // Assert that the default value is properly returned by getVariable when the variable has no value, and that
        // the variable is set to the default value and returned by further calls to getVariable.
        long value1 = 7;
        assertEquals(value1, conf.getVariable(testVar1, value1));
        assertEquals(value1, conf.getLongVariable(testVar1));

        // Assert that a new value replaces the previous value, and that the default value passed to getVariable
        // is not returned anymore.
        long value2 = 27;
        assertTrue(conf.setVariable(testVar1, value2));
        assertEquals(value2, conf.getLongVariable(testVar1));
        assertEquals(value2, conf.getVariable(testVar1, value1));

        String testVar2 = "longVar2";

        // Assert that setVariable properly sets the value when the variable has no previous value
        assertTrue(conf.setVariable(testVar2, value1));
        assertEquals(value1, conf.getLongVariable(testVar2));

        // Assert that setVariable returns false when the specified value is equal to the current value
        assertFalse(conf.setVariable(testVar2, value1));

        // Assert that min and max long values are properly preserved
        assertTrue(conf.setVariable(testVar2, Long.MIN_VALUE));
        assertEquals(Long.MIN_VALUE, conf.getLongVariable(testVar2));

        assertTrue(conf.setVariable(testVar2, Long.MAX_VALUE));
        assertEquals(Long.MAX_VALUE, conf.getLongVariable(testVar2));

        // Assert that NumberFormatException is thrown when the value can not be parsed as a long
        boolean numberFormatExceptionThrown = false;
        try {
            conf.setVariable(testVar2, "notALong");
            conf.getLongVariable(testVar2);
        }
        catch(NumberFormatException e) {
            numberFormatExceptionThrown = true;
        }

        assertTrue("NumberFormatException should be thrown", numberFormatExceptionThrown);
    }


    /**
     * Tests the <code>getVariable()</code> and <code>setVariable</code> methods with float values.
     */
    public void testFloatValues() {
        String testVar1 = "floatVar1";

        // Assert that getVariable returns null when a variable has no value
        assertNull(conf.getVariable(testVar1));

        // Assert that the default value is properly returned by getVariable when the variable has no value, and that
        // the variable is set to the default value and returned by further calls to getVariable.
        float value1 = 7;
        assertTrue(value1==conf.getVariable(testVar1, value1));
        assertTrue(value1==conf.getFloatVariable(testVar1));

        // Assert that a new value replaces the previous value, and that the default value passed to getVariable
        // is not returned anymore.
        float value2 = 27;
        assertTrue(conf.setVariable(testVar1, value2));
        assertTrue(value2==conf.getFloatVariable(testVar1));
        assertTrue(value2==conf.getVariable(testVar1, value1));

        String testVar2 = "floatVar2";

        // Assert that setVariable properly sets the value when the variable has no previous value
        assertTrue(conf.setVariable(testVar2, value1));
        assertTrue(value1==conf.getFloatVariable(testVar2));

        // Assert that setVariable returns false when the specified value is equal to the current value
        assertFalse(conf.setVariable(testVar2, value1));

        // Assert that min and max float values are properly preserved
        assertTrue(conf.setVariable(testVar2, Float.MIN_VALUE));
        assertTrue(Float.MIN_VALUE==conf.getFloatVariable(testVar2));

        assertTrue(conf.setVariable(testVar2, Float.MAX_VALUE));
        assertTrue(Float.MAX_VALUE==conf.getFloatVariable(testVar2));

        // Assert that NumberFormatException is thrown when the value can not be parsed as a float
        boolean numberFormatExceptionThrown = false;
        try {
            conf.setVariable(testVar2, "notAFloat");
            conf.getFloatVariable(testVar2);
        }
        catch(NumberFormatException e) {
            numberFormatExceptionThrown = true;
        }

        assertTrue("NumberFormatException should be thrown", numberFormatExceptionThrown);
    }


    /**
     * Tests the <code>getVariable()</code> and <code>setVariable</code> methods with double (float) values.
     */
    public void testDoubleValues() {
        String testVar1 = "doubleVar1";

        // Assert that getVariable returns null when a variable has no value
        assertNull(conf.getVariable(testVar1));

        // Assert that the default value is properly returned by getVariable when the variable has no value, and that
        // the variable is set to the default value and returned by further calls to getVariable.
        double value1 = 7;
        assertTrue(value1==conf.getVariable(testVar1, value1));
        assertTrue(value1==conf.getDoubleVariable(testVar1));

        // Assert that a new value replaces the previous value, and that the default value passed to getVariable
        // is not returned anymore.
        double value2 = 27;
        assertTrue(conf.setVariable(testVar1, value2));
        assertTrue(value2==conf.getDoubleVariable(testVar1));
        assertTrue(value2==conf.getVariable(testVar1, value1));

        String testVar2 = "doubleVar2";

        // Assert that setVariable properly sets the value when the variable has no previous value
        assertTrue(conf.setVariable(testVar2, value1));
        assertTrue(value1==conf.getDoubleVariable(testVar2));

        // Assert that setVariable returns false when the specified value is equal to the current value
        assertFalse(conf.setVariable(testVar2, value1));

        // Assert that min and max double values are properly preserved
        assertTrue(conf.setVariable(testVar2, Double.MIN_VALUE));
        assertTrue(Double.MIN_VALUE==conf.getDoubleVariable(testVar2));

        assertTrue(conf.setVariable(testVar2, Double.MAX_VALUE));
        assertTrue(Double.MAX_VALUE==conf.getDoubleVariable(testVar2));

        // Assert that NumberFormatException is thrown when the value can not be parsed as a double
        boolean numberFormatExceptionThrown = false;
        try {
            conf.setVariable(testVar2, "notADouble");
            conf.getDoubleVariable(testVar2);
        }
        catch(NumberFormatException e) {
            numberFormatExceptionThrown = true;
        }

        assertTrue("NumberFormatException should be thrown", numberFormatExceptionThrown);
    }
}
