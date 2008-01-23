/**
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

import junit.framework.TestCase;

import java.util.Enumeration;
import java.util.Vector;

/**
 * A test case for the {@link Configuration} class.
 * @author Nicolas Rinaudo
 */
public class ConfigurationSectionTest extends TestCase {
    // - Test constants --------------------------------------------------------
    // -------------------------------------------------------------------------
    private static final String VARIABLE1 = "variable1";
    private static final String VARIABLE2 = "variable2";
    private static final String VALUE1    = "value1";
    private static final String VALUE2    = "value2";
    private static final String SECTION1  = "section1";
    private static final String SECTION2  = "section2";



    // - Instance fields -------------------------------------------------------
    // -------------------------------------------------------------------------
    private ConfigurationSection section;



    // - Initialisation --------------------------------------------------------
    // -------------------------------------------------------------------------
    public void setUp() {section = new ConfigurationSection();}



    // - Test code -------------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Tests the behaviour of the various variable related methods.
     */
    public void testVariables() {
        Enumeration variables;
        String      name;

        assertFalse(section.hasVariables());

        assertTrue(section.setVariable(VARIABLE1, VALUE1));
        assertEquals(VALUE1, section.getVariable(VARIABLE1));
        assertTrue(section.hasVariables());

        assertFalse(section.setVariable(VARIABLE1, VALUE1));
        assertTrue(section.setVariable(VARIABLE1, VALUE2));
        assertEquals(VALUE2, section.getVariable(VARIABLE1));
        assertTrue(section.hasVariables());

        variables = section.variableNames();
        assertTrue(variables.hasMoreElements());
        assertEquals(VARIABLE1, variables.nextElement());
        assertFalse(variables.hasMoreElements());

        assertEquals(VALUE2, section.removeVariable(VARIABLE1));
        assertFalse(section.hasVariables());
        assertFalse(section.variableNames().hasMoreElements());

        assertTrue(section.setVariable(VARIABLE1, VALUE1));
        assertTrue(section.setVariable(VARIABLE2, VALUE2));
        assertTrue(section.hasVariables());

        variables = section.variableNames();
        assertTrue(variables.hasMoreElements());
        name = (String)variables.nextElement();
        assertTrue(variables.hasMoreElements());
        if(name.equals(VARIABLE1))
            assertEquals(VARIABLE2, variables.nextElement());
        else if(name.equals(VARIABLE2))
            assertEquals(VARIABLE1, variables.nextElement());
        else
            fail();

        assertEquals(VALUE1, section.removeVariable(VARIABLE1));
        assertEquals(VALUE2, section.removeVariable(VARIABLE2));
        assertFalse(section.hasVariables());
    }

    /**
     * Tests the behaviour of the various section related methods.
     */
    public void testSections() {
        ConfigurationSection subSection1;
        ConfigurationSection subSection2;
        Enumeration          sections;
        String               name;

        assertFalse(section.hasSections());
        assertNotNull(subSection1 = section.addSection(SECTION1));
        assertEquals(subSection1, section.getSection(SECTION1));
        assertEquals(subSection1, section.addSection(SECTION1));
        assertTrue(section.hasSections());

        assertEquals(subSection1, section.removeSection(SECTION1));
        assertFalse(section.hasSections());

        assertNotNull(subSection1 = section.addSection(SECTION1));
        section.removeSection(subSection1);
        assertFalse(section.hasSections());

        assertNotNull(subSection1 = section.addSection(SECTION1));
        sections = section.sectionNames();
        assertTrue(sections.hasMoreElements());
        assertEquals(SECTION1, sections.nextElement());
        assertFalse(sections.hasMoreElements());

        assertNotNull(subSection2 = section.addSection(SECTION2));
        sections = section.sectionNames();
        assertTrue(sections.hasMoreElements());
        name = (String)sections.nextElement();
        assertTrue(sections.hasMoreElements());
        if(name.equals(SECTION1))
            assertEquals(SECTION2, sections.nextElement());
        else if(name.equals(SECTION2))
            assertEquals(SECTION1, sections.nextElement());
        else
            fail();        
    }

    /**
     * Tests value casting.
     */
    public void testCasting() {
        Vector listValue;

        // Tests integer casting.
        assertEquals(10, ConfigurationSection.getIntegerValue("10"));
        assertEquals(0, ConfigurationSection.getIntegerValue(null));
        assertEquals("10", ConfigurationSection.getValue(10));
        assertEquals("0", ConfigurationSection.getValue(0));

        // Tests long casting.
        assertEquals(10, ConfigurationSection.getLongValue("10"));
        assertEquals(0, ConfigurationSection.getLongValue(null));
        assertEquals("10", ConfigurationSection.getValue(10l));
        assertEquals("0", ConfigurationSection.getValue(0l));

        // Tests float casting.
        assertEquals(10.5, ConfigurationSection.getFloatValue("10.5"), 0);
        assertEquals(0, ConfigurationSection.getFloatValue(null), 0);
        assertEquals("10.5", ConfigurationSection.getValue(10.5));
        assertEquals("0.0", ConfigurationSection.getValue(0.0));

        // Tests double casting.
        assertEquals(10.5, ConfigurationSection.getDoubleValue("10.5"), 0);
        assertEquals(0, ConfigurationSection.getDoubleValue(null), 0);
        assertEquals("10.5", ConfigurationSection.getValue(10.5d));
        assertEquals("0.0", ConfigurationSection.getValue(0.0d));

        // Tests boolean casting
        assertTrue(ConfigurationSection.getBooleanValue("true"));
        assertFalse(ConfigurationSection.getBooleanValue("false"));
        assertFalse(ConfigurationSection.getBooleanValue(null));
        assertEquals("true", ConfigurationSection.getValue(true));
        assertEquals("false", ConfigurationSection.getValue(false));

        // Test list casting.
        listValue = new Vector();
        for(int i = 0; i < 7; i++)
            listValue.add(Integer.toString(i));
        assertEquals(listValue, ConfigurationSection.getListValue(ValueList.toString(listValue, ";"), ";"));
        assertEquals(null, ConfigurationSection.getListValue(null, ";"));
        assertEquals(ValueList.toString(listValue, ";"), ConfigurationSection.getValue(listValue, ";"));
    }
}
