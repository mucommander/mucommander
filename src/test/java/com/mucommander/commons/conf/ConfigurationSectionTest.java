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

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.*;

/**
 * A test case for the {@link Configuration} class.
 * @author Nicolas Rinaudo
 */
public class ConfigurationSectionTest {
    // - removeVariable tests -------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Provides test cases for {@link #testRemoveVariable(String)}.
     * @return test cases for {@link #testRemoveVariable(String)}.
     */
    @DataProvider(name = "removeVariable")
    public Iterator<Object[]> removeVariableCases() {
        List<Object[]> data;

        data = new ArrayList<Object[]>();

        data.add(new Object[] {"value"});
        data.add(new Object[] {""});
        data.add(new Object[] {null});

        return data.iterator();
    }

    /**
     * Tests the {@link ConfigurationSection#removeVariable(String)} method.
     * @param value value to which the variable should be set before being removed.
     */
    @Test(dataProvider = "setVariable")
    public void testRemoveVariable(String value) {
        ConfigurationSection section;

        section = new ConfigurationSection();

        assert section.setVariable("var", value);
        assertVariable(section, "var", value);

        assert value == null ? section.removeVariable("var") == null : section.removeVariable("var").equals(value);
    }



    // - setVariable tests ---------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Provides test cases for {@link #testSetVariable(String, String, boolean)}.
     * @return test cases for {@link #testSetVariable(String, String, boolean)}.
     */
    @DataProvider(name = "setVariable")
    public Iterator<Object[]> setVariableCases() {
        List<Object[]> data;

        data = new ArrayList<Object[]>();

        data.add(new Object[] {"value", "other", true});
        data.add(new Object[] {"value", "value", false});

        data.add(new Object[] {"value", null, true});
        data.add(new Object[] {"value", "",   true});

        return data.iterator();
    }

    /**
     * Makes sure that the specified variable has the specified value.
     * @param section section in which the variable to test is located.
     * @param var     name of the variable to test.
     * @param value   expected variable value.
     */
    private void assertVariable(ConfigurationSection section, String var, String value) {
        if(value == null || value.isEmpty())
            assert section.getVariable(var) == null: "Expected null but found " + section.getVariable(var);
        else {
            assert section.getVariable(var).equals(value): "Expected " + value + " but found " + section.getVariable(var);
        }
    }

    /**
     * Tests the {@link ConfigurationSection#setVariable(String, String)} method.
     * @param first    first value to which the test variable should be set.
     * @param second   second value to which the test variable should be set.
     * @param expected expected return value of the second call to
     *                 {@link ConfigurationSection#setVariable(String, String)}.
     */
    @Test(dataProvider = "setVariable")
    public void testSetVariable(String first, String second, boolean expected) {
        ConfigurationSection section;

        section = new ConfigurationSection();

        assert section.setVariable("var", first);
        assertVariable(section, "var", first);

        assert expected == section.setVariable("var", second);
        assertVariable(section, "var", second);
    }



    // - variableNames test --------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Utility method for {@link #testVariableNames()}.
     * @param section section to explore.
     * @param count   number of expected variable names.
     */
    private void assertVariableNames(ConfigurationSection section, int count) {
        // No expected variables, make sure that the section reflects that.
        if(count == 0) {
            assert !section.hasVariables();
            assert section.isEmpty();
            assert !section.variableNames().hasNext();
        }

        // Makes sure that the section contains exactly var1, var2..., var<count>.
        // We have to go through a set here: the order in which we'll iterate over the variable names is unreliable.
        else {
            Iterator<String> names;
            Set<String>      expectedNames;

            assert section.hasVariables();
            assert !section.isEmpty();

            // Populates a set will all the expected variable names.
            expectedNames = new HashSet<String>(count);
            for(int i = 0; i < count; i++)
                expectedNames.add("var" + i);

            // Makes sure that we can remove all of the section's variables, and that none remains afterward.
            names = section.variableNames();
            while(names.hasNext())
                assert expectedNames.remove(names.next());
            assert expectedNames.isEmpty();
        }
    }

    /**
     * Tests the {@link ConfigurationSection#variableNames()} method.
     */
    @Test
    public void testVariableNames() {
        ConfigurationSection section;

        section = new ConfigurationSection();

        // Create 10 variables.
        for(int i = 0; i < 10; i++)
            section.setVariable("var" + i, "value");

        assertVariableNames(section, 10);

        section.setVariable("var9", "");
        assertVariableNames(section, 9);

        section.setVariable("var8", null);
        assertVariableNames(section, 8);

        section.removeVariable("var7");
        assertVariableNames(section, 7);


        for(int i = 0; i < 7; i++)
            section.removeVariable("var" + i);
        assertVariableNames(section, 0);
    }


    // - Helper tests --------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Tests the list value helpers.
     */
    @Test
    public void testLists() {
        ValueList list;

        list = new ValueList("1,2,3,4", ",");
        assert ConfigurationSection.getListValue("1,2,3,4", ",").equals(list);
        assert ConfigurationSection.getValue(list, ",").equals("1,2,3,4");
    }

    /**
     * Tests the long value helpers.
     */
    @Test
    public void testLongs() {
        for(long i = 0; i < 10; i++) {
            assert ConfigurationSection.getLongValue(Long.toString(i)) == i;
            assert ConfigurationSection.getValue(i).equals(Long.toString(i));
        }

        assert ConfigurationSection.getLongValue(null) == 0;
    }

    /**
     * Tests the integer value helpers.
     */
    @Test
    public void testIntegers() {
        for(int i = 0; i < 10; i++) {
            assert ConfigurationSection.getIntegerValue(Integer.toString(i)) == i;
            assert ConfigurationSection.getValue(i).equals(Integer.toString(i));
        }

        assert ConfigurationSection.getIntegerValue(null) == 0;
    }
    
    /**
     * Tests the double value helpers.
     */
    @Test
    public void testDoubles() {
        for(int i = 0; i < 10; i++) {
            assert ConfigurationSection.getDoubleValue(i + ".5") == (i + 0.5d);
            assert ConfigurationSection.getValue((i + 0.5d)).equals(i + ".5");
        }

        assert ConfigurationSection.getDoubleValue(null) == 0f;
    }

    /**
     * Tests the float value helpers.
     */
    @Test
    public void testFloats() {
        for(int i = 0; i < 10; i++) {
            assert ConfigurationSection.getFloatValue(i + ".5") == (i + 0.5f);
            assert ConfigurationSection.getValue((i + 0.5f)).equals(i + ".5");
        }

        assert ConfigurationSection.getFloatValue(null) == 0f;
    }

    /**
     * Tests the boolean value helpers.
     */
    @Test
    public void testBooleans() {
        assert ConfigurationSection.getBooleanValue("true");
        assert ConfigurationSection.getValue(true).equals("true");

        assert !ConfigurationSection.getBooleanValue("false");
        assert !ConfigurationSection.getBooleanValue("!@#");
        assert !ConfigurationSection.getBooleanValue("");
        assert ConfigurationSection.getValue(false).equals("false");

        assert !ConfigurationSection.getBooleanValue(null);
    }



    // - Section test --------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Tests various section related method.
     */
    @Test
    public void testSections() {
        ConfigurationSection section;
        ConfigurationSection buffer;

        section = new ConfigurationSection();

        // Makes sure we can add a section.
        buffer = section.addSection("sect");
        assert buffer != null;
        assert section.hasSections();

        // Makes sure that adding the same section twice yields the same instance.
        assert section.addSection("sect") == buffer;
        assert section.hasSections();

        // Makes sure we can remove a section by name.
        assert section.removeSection("sect") == buffer;
        assert section.getSection("sect") == null;
        assert !section.hasSections();

        // Makes sure we can remove a section by value.
        buffer = section.addSection("sect");
        assert section.removeSection(buffer);
        assert buffer.getSection("sect") == null;
        assert !section.hasSections();
        assert !section.removeSection(buffer);
    }

    /**
     * Utility method for {@link #testSectionNames()}.
     * @param section section to explore.
     * @param count   number of expected section names.
     */
    private void assertSectionNames(ConfigurationSection section, int count) {
        // No expected variables, make sure that the section reflects that.
        if(count == 0) {
            assert !section.hasSections();
            assert section.isEmpty();
            assert !section.sectionNames().hasNext();
        }

        // Makes sure that the section contains exactly sect1, sect2..., sect<count>.
        // We have to go through a set here: the order in which we'll iterate over the variable names is unreliable.
        else {
            Iterator<String> names;
            Set<String>      expectedNames;

            assert section.hasSections();
            assert !section.isEmpty();

            // Populates a set will all the expected variable names.
            expectedNames = new HashSet<String>(count);
            for(int i = 0; i < count; i++)
                expectedNames.add("sect" + i);

            // Makes sure that we can remove all of the section's sub-sections, and that none remains afterward.
            names = section.sectionNames();
            while(names.hasNext())
                assert expectedNames.remove(names.next());
            assert expectedNames.isEmpty();
        }
    }

    @Test
    public void testSectionNames() {
        ConfigurationSection section;
        ConfigurationSection buffer;

        section = new ConfigurationSection();

        // Create 10 variables.
        buffer = null;
        for(int i = 0; i < 10; i++)
            buffer = section.addSection("sect" + i);
        assertSectionNames(section, 10);

        // Removes the last section by value, makes sure we're in a coherent state.
        assert section.removeSection(buffer);
        assertSectionNames(section, 9);

        // Removes the last section by name, makes sure we're in a coherent state.
        assert section.removeSection("sect8") != null;
        assertSectionNames(section, 8);

        for(int i = 0; i < 8; i++)
            assert section.removeSection("sect" + i) != null;
        assertSectionNames(section, 0);
    }
}
