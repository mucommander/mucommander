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

package com.mucommander.conf;

import junit.framework.TestCase;

import java.util.Vector;

/**
 * A test case for the {@link ConfigurationEvent} class.
 * @author Nicolas Rinaudo
 */
public class ConfigurationEventTest extends TestCase {
    // - Test constants ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Name of the test variable. */
    private static final String  VARIABLE_NAME = "variable";
    /** Test string value. */
    private static final String  STRING_VALUE  = "value";
    /** Test list value. */
    private static final Vector  LIST_VALUE    = new Vector();
    /** Test integer value. */
    private static final int     INTEGER_VALUE = 10;
    /** Test long value. */
    private static final long    LONG_VALUE    = 15;
    /** Test float value. */
    private static final float   FLOAT_VALUE   = (float)10.5;
    /** Test double value. */
    private static final double  DOUBLE_VALUE  = 15.5;
    /** Test boolean value. */
    private static final boolean BOOLEAN_VALUE = true;



    // - Instance fields -----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Configuration instance used to create events. */
    private Configuration conf;



    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    static {
        for(int i = 0; i < 7; i++)
            LIST_VALUE.add(Integer.toString(i));
    }

    /**
     * Initialises the test case.
     */
    public void setUp() {conf = new Configuration();}



    // - Type specific tests -------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Tests string events.
     */
    public void testStringEvents() {
        ConfigurationEvent event;

        // Makes sure the value passed to the constructor is properly returned.
        event = new ConfigurationEvent(conf, VARIABLE_NAME, STRING_VALUE);
        assertEquals(STRING_VALUE, event.getValue());

        // Makes sure unset values are returned as null.
        event = new ConfigurationEvent(conf, VARIABLE_NAME, null);
        assertEquals(null, event.getValue());
    }

    /**
     * Tests list events.
     */
    public void testListEvents() {
        ConfigurationEvent event;

        // Makes sure the value passed to the constructor is properly returned.
        event = new ConfigurationEvent(conf, VARIABLE_NAME, ValueList.toString(LIST_VALUE, ";"));
        assertEquals(LIST_VALUE, event.getListValue(";"));

        // Makes sure unset values are returned as null.
        event = new ConfigurationEvent(conf, VARIABLE_NAME, null);
        assertEquals(null, event.getListValue(";"));
    }

    /**
     * Tests integer events.
     */
    public void testIntegerEvent() {
        ConfigurationEvent event;

        // Makes sure the value passed to the constructor is properly returned.
        event = new ConfigurationEvent(conf, VARIABLE_NAME, Integer.toString(INTEGER_VALUE));
        assertEquals(INTEGER_VALUE, event.getIntegerValue());

        // Makes sure unset values are returned as 0.
        event = new ConfigurationEvent(conf, VARIABLE_NAME, null);
        assertEquals(0, event.getIntegerValue());
    }

    /**
     * Tests long events.
     */
    public void testLongEvent() {
        ConfigurationEvent event;

        // Makes sure the value passed to the constructor is properly returned.
        event = new ConfigurationEvent(conf, VARIABLE_NAME, Long.toString(LONG_VALUE));
        assertEquals(LONG_VALUE, event.getLongValue());

        // Makes sure unset values are returned as 0.
        event = new ConfigurationEvent(conf, VARIABLE_NAME, null);
        assertEquals(0, event.getLongValue());
    }

    /**
     * Tests double events.
     */
    public void testDoubleEvent() {
        ConfigurationEvent event;

        // Makes sure the value passed to the constructor is properly returned.
        event = new ConfigurationEvent(conf, VARIABLE_NAME, Double.toString(DOUBLE_VALUE));
        assertEquals(DOUBLE_VALUE, event.getDoubleValue(), 0);

        // Makes sure unset values are returned as 0.
        event = new ConfigurationEvent(conf, VARIABLE_NAME, null);
        assertEquals(0, event.getDoubleValue(), 0);
    }

    /**
     * Tests float events.
     */
    public void testFloatEvent() {
        ConfigurationEvent event;

        // Makes sure the value passed to the constructor is properly returned.
        event = new ConfigurationEvent(conf, VARIABLE_NAME, Float.toString(FLOAT_VALUE));
        assertEquals(FLOAT_VALUE, event.getFloatValue(), 0);

        // Makes sure unset values are returned as 0.
        event = new ConfigurationEvent(conf, VARIABLE_NAME, null);
        assertEquals(0, event.getFloatValue(), 0);
    }

    /**
     * Tests boolean events.
     */
    public void testBooleanEvent() {
        ConfigurationEvent event;

        // Makes sure the value passed to the constructor is properly returned.
        event = new ConfigurationEvent(conf, VARIABLE_NAME, Boolean.toString(BOOLEAN_VALUE));
        assertEquals(BOOLEAN_VALUE, event.getBooleanValue());

        // Makes sure unset values are returned as 0.
        event = new ConfigurationEvent(conf, VARIABLE_NAME, null);
        assertEquals(false, event.getBooleanValue());
    }



    // - Misc. tests ---------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    public void testConstructor() {
        ConfigurationEvent event;

        // Makes sure the constructor initialises events properly.
        event = new ConfigurationEvent(conf, VARIABLE_NAME, STRING_VALUE);
        assertEquals(conf, event.getConfiguration());
        assertEquals(VARIABLE_NAME, event.getVariable());
    }
}
