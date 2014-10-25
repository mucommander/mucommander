/**
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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Vector;

/**
 * A test case for the {@link ConfigurationEvent} class.
 * @author Nicolas Rinaudo
 */
public class ConfigurationEventTest {
    // - Test constants ------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /** Name of the test variable. */
    private static final String         VARIABLE_NAME = "variable";
    /** Test string value. */
    private static final String         STRING_VALUE  = "value";
    /** Test list value. */
    private static final Vector<String> LIST_VALUE    = new Vector<String>();
    /** Test integer value. */
    private static final int            INTEGER_VALUE = 10;
    /** Test long value. */
    private static final long           LONG_VALUE    = 15;
    /** Test float value. */
    private static final float          FLOAT_VALUE   = (float)10.5;
    /** Test double value. */
    private static final double         DOUBLE_VALUE  = 15.5;
    /** Test boolean value. */
    private static final boolean        BOOLEAN_VALUE = true;



    // - Instance fields -----------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /** Configuration instance used to create events. */
    private Configuration conf;



    // - Initialisation ------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    static {
        for(int i = 0; i < 7; i++)
            LIST_VALUE.add(Integer.toString(i));
    }

    /**
     * Initialises the test case.
     */
    @BeforeMethod
    public void setUp() {
        conf = new Configuration();
    }



    // - Type specific tests -------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Tests string events.
     */
    @Test
    public void testStringEvents() {
        ConfigurationEvent event;

        // Makes sure the value passed to the constructor is properly returned.
        event = new ConfigurationEvent(conf, VARIABLE_NAME, STRING_VALUE);
        assert STRING_VALUE.equals(event.getValue());

        // Makes sure unset values are returned as null.
        event = new ConfigurationEvent(conf, VARIABLE_NAME, null);
        assert null == event.getValue();
    }

    /**
     * Tests list events.
     */
    @Test
    public void testListEvents() {
        ConfigurationEvent event;

        // Makes sure the value passed to the constructor is properly returned.
        event = new ConfigurationEvent(conf, VARIABLE_NAME, ValueList.toString(LIST_VALUE, ";"));
        assert LIST_VALUE.equals(event.getListValue(";"));

        // Makes sure unset values are returned as null.
        event = new ConfigurationEvent(conf, VARIABLE_NAME, null);
        assert null == event.getListValue(";");
    }

    /**
     * Tests integer events.
     */
    @Test
    public void testIntegerEvent() {
        ConfigurationEvent event;

        // Makes sure the value passed to the constructor is properly returned.
        event = new ConfigurationEvent(conf, VARIABLE_NAME, Integer.toString(INTEGER_VALUE));
        assert INTEGER_VALUE == event.getIntegerValue();

        // Makes sure unset values are returned as 0.
        event = new ConfigurationEvent(conf, VARIABLE_NAME, null);
        assert 0 == event.getIntegerValue();
    }

    /**
     * Tests long events.
     */
    @Test
    public void testLongEvent() {
        ConfigurationEvent event;

        // Makes sure the value passed to the constructor is properly returned.
        event = new ConfigurationEvent(conf, VARIABLE_NAME, Long.toString(LONG_VALUE));
        assert LONG_VALUE == event.getLongValue();

        // Makes sure unset values are returned as 0.
        event = new ConfigurationEvent(conf, VARIABLE_NAME, null);
        assert 0 == event.getLongValue();
    }

    /**
     * Tests double events.
     */
    @Test
    public void testDoubleEvent() {
        ConfigurationEvent event;

        // Makes sure the value passed to the constructor is properly returned.
        event = new ConfigurationEvent(conf, VARIABLE_NAME, Double.toString(DOUBLE_VALUE));
        assert DOUBLE_VALUE == event.getDoubleValue();

        // Makes sure unset values are returned as 0.
        event = new ConfigurationEvent(conf, VARIABLE_NAME, null);
        assert 0 == event.getDoubleValue();
    }

    /**
     * Tests float events.
     */
    @Test
    public void testFloatEvent() {
        ConfigurationEvent event;

        // Makes sure the value passed to the constructor is properly returned.
        event = new ConfigurationEvent(conf, VARIABLE_NAME, Float.toString(FLOAT_VALUE));
        assert FLOAT_VALUE == event.getFloatValue();

        // Makes sure unset values are returned as 0.
        event = new ConfigurationEvent(conf, VARIABLE_NAME, null);
        assert 0 == event.getFloatValue();
    }

    /**
     * Tests boolean events.
     */
    @Test
    public void testBooleanEvent() {
        ConfigurationEvent event;

        // Makes sure the value passed to the constructor is properly returned.
        event = new ConfigurationEvent(conf, VARIABLE_NAME, Boolean.toString(BOOLEAN_VALUE));
        assert BOOLEAN_VALUE == event.getBooleanValue();

        // Makes sure unset values are returned as 0.
        event = new ConfigurationEvent(conf, VARIABLE_NAME, null);
        assert false == event.getBooleanValue();
    }



    // - Misc. tests ---------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Tests event creation.
     */
    @Test
    public void testConstructor() {
        ConfigurationEvent event;

        // Makes sure the constructor initialises events properly.
        event = new ConfigurationEvent(conf, VARIABLE_NAME, STRING_VALUE);
        assert conf.equals(event.getConfiguration());
        assert VARIABLE_NAME.equals(event.getVariable());
    }
}
