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

import java.util.Enumeration;
import java.util.Vector;

/**
 * A test case for the {@link Configuration} class.
 * @author Nicolas Rinaudo
 */
public class ConfigurationSectionTest {
    // - Test constants ------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    private static final String VARIABLE1 = "variable1";
    private static final String VARIABLE2 = "variable2";
    private static final String VALUE1    = "value1";
    private static final String VALUE2    = "value2";
    private static final String SECTION1  = "section1";
    private static final String SECTION2  = "section2";



    // - Instance fields -----------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    private ConfigurationSection section;



    // - Initialisation ------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    @BeforeMethod
    public void setUp() {
        section = new ConfigurationSection();
    }



    // - Test code -----------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Tests the behaviour of the various variable related methods.
     */
    @Test
    public void testVariables() {
        Enumeration<String> variables;
        String              name;

        assert !section.hasVariables();

        assert section.setVariable(VARIABLE1, VALUE1);
        assert VALUE1.equals(section.getVariable(VARIABLE1));
        assert section.hasVariables();

        assert !section.setVariable(VARIABLE1, VALUE1);
        assert section.setVariable(VARIABLE1, VALUE2);
        assert VALUE2.equals(section.getVariable(VARIABLE1));
        assert section.hasVariables();

        variables = section.variableNames();
        assert variables.hasMoreElements();
        assert VARIABLE1.equals(variables.nextElement());
        assert !variables.hasMoreElements();

        assert VALUE2.equals(section.removeVariable(VARIABLE1));
        assert !section.hasVariables();
        assert !section.variableNames().hasMoreElements();

        assert section.setVariable(VARIABLE1, VALUE1);
        assert section.setVariable(VARIABLE2, VALUE2);
        assert section.hasVariables();

        variables = section.variableNames();
        assert variables.hasMoreElements();
        name = variables.nextElement();
        assert variables.hasMoreElements();
        if(name.equals(VARIABLE1))
            assert VARIABLE2.equals(variables.nextElement());
        else if(name.equals(VARIABLE2))
            assert VARIABLE1.equals(variables.nextElement());
        else
            throw new AssertionError();

        assert VALUE1.equals(section.removeVariable(VARIABLE1));
        assert VALUE2.equals(section.removeVariable(VARIABLE2));
        assert !section.hasVariables();
    }

    /**
     * Tests the behaviour of the various section related methods.
     */
    @Test
    public void testSections() {
        ConfigurationSection subSection1;
        Enumeration<String>  sections;
        String               name;

        assert !section.hasSections();
        subSection1 = section.addSection(SECTION1);
        assert subSection1 != null;
        assert subSection1.equals(section.getSection(SECTION1));
        assert subSection1.equals(section.addSection(SECTION1));
        assert section.hasSections();

        assert subSection1.equals(section.removeSection(SECTION1));
        assert !section.hasSections();

        subSection1 = section.addSection(SECTION1);
        assert subSection1 != null;
        section.removeSection(subSection1);
        assert !section.hasSections();

        assert section.addSection(SECTION1) != null;
        sections = section.sectionNames();
        assert sections.hasMoreElements();
        assert SECTION1.equals(sections.nextElement());
        assert !sections.hasMoreElements();

        assert section.addSection(SECTION2) != null;
        sections = section.sectionNames();
        assert sections.hasMoreElements();
        name = sections.nextElement();
        assert sections.hasMoreElements();
        if(name.equals(SECTION1))
            assert SECTION2.equals(sections.nextElement());
        else if(name.equals(SECTION2))
            assert SECTION1.equals(sections.nextElement());
        else
            throw new AssertionError();
    }

    /**
     * Tests value casting.
     */
    @Test
    public void testCasting() {
        Vector<String> listValue;

        // Tests integer casting.
        assert 10 == ConfigurationSection.getIntegerValue("10");
        assert 0 == ConfigurationSection.getIntegerValue(null);
        assert "10".equals(ConfigurationSection.getValue(10));
        assert "0".equals(ConfigurationSection.getValue(0));

        // Tests long casting.
        assert 10 == ConfigurationSection.getLongValue("10");
        assert 0 == ConfigurationSection.getLongValue(null);
        assert "10".equals(ConfigurationSection.getValue(10l));
        assert "0".equals(ConfigurationSection.getValue(0l));

        // Tests float casting.
        assert 10.5 == ConfigurationSection.getFloatValue("10.5");
        assert 0 == ConfigurationSection.getFloatValue(null);
        assert "10.5".equals(ConfigurationSection.getValue(10.5));
        assert "0.0".equals(ConfigurationSection.getValue(0.0));

        // Tests double casting.
        assert 10.5 == ConfigurationSection.getDoubleValue("10.5");
        assert 0 == ConfigurationSection.getDoubleValue(null);
        assert "10.5".equals(ConfigurationSection.getValue(10.5d));
        assert "0.0".equals(ConfigurationSection.getValue(0.0d));

        // Tests boolean casting
        assert ConfigurationSection.getBooleanValue("true");
        assert !ConfigurationSection.getBooleanValue("false");
        assert !ConfigurationSection.getBooleanValue(null);
        assert "true".equals(ConfigurationSection.getValue(true));
        assert "false".equals(ConfigurationSection.getValue(false));

        // Test list casting.
        listValue = new Vector<String>();
        for(int i = 0; i < 7; i++)
            listValue.add(Integer.toString(i));
        assert listValue.equals(ConfigurationSection.getListValue(ValueList.toString(listValue, ";"), ";"));
        assert null == ConfigurationSection.getListValue(null, ";");
        assert ValueList.toString(listValue, ";").equals(ConfigurationSection.getValue(listValue, ";"));
    }
}
