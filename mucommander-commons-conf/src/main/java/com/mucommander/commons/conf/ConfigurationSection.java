/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2016 Maxence Bernard
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Represents a section in the configuration tree.
 *
 * @author Nicolas Rinaudo
 */
class ConfigurationSection {
    // - Instance fields -----------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Contains all the variables defined in the section.
     */
    private final Map<String, String> variables;
    /**
     * Contains all the subsections defined the section.
     */
    private final Map<String, ConfigurationSection> sections;


    // - Initialization ------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Creates a new configuration section.
     */
    public ConfigurationSection() {
        variables = new HashMap<String, String>();
        sections = new HashMap<String, ConfigurationSection>();
    }


    // - Variables access ----------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Removes the specified variable from the section.
     *
     * @param name name of the variable to remove.
     * @return the value to which this variable was previously set, <code>null</code> if none.
     */
    public String removeVariable(String name) {
        return variables.remove(name);
    }

    /**
     * Returns the value of the specified variable.
     *
     * @param name name of the variable whose value should be returned.
     * @return the value of the specified variable, or <code>null</code> if it wasn't set.
     */
    public String getVariable(String name) {
        return variables.get(name);
    }

    /**
     * Sets the specified variable to the specified value.
     * <p>
     * If <code>value</code> is either <code>null</code> or an empty string,
     * the call will be equivalent to {@link #removeVariable(String)}.
     * </p>
     *
     * @param name  name of the variable to set.
     * @param value value for the variable.
     * @return <code>true</code> if the variable's value was changed as a result of this call, <code>false</code>
     * otherwise.
     */
    public boolean setVariable(String name, String value) {
        // If the specified value is empty, deletes the variable.
        if (value == null || value.trim().equals("")) {
            // If the variable wasn't set, we haven't changed its value.
            if (getVariable(name) == null)
                return false;

            // Otherwise, deletes it and returns true.
            removeVariable(name);
            return true;
        }

        // Compares the variable's new and old values.
        String buffer;
        buffer = variables.put(name, value);
        return buffer == null || !buffer.equals(value);
    }

    /**
     * Returns an iterator on the names of the variables that are defined in the section.
     * <p>
     * Note that the order in which variable names are returned needs not be that in which they were added to the
     * section. Callers should not rely on the order being consistent over time.
     * </p>
     *
     * @return an iterator on the names of the variables that are defined in the section.
     */
    public Iterator<String> variableNames() {
        return variables.keySet().iterator();
    }

    /**
     * Returns <code>true</code> if the section contains any variable.
     *
     * @return <code>true</code> if the section contains any variable, <code>false</code> otherwise.
     */
    public boolean hasVariables() {
        return !variables.isEmpty();
    }


    // - Value helpers -------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Casts the specified value into an integer.
     * <p>
     * If <code>value</code> is <code>null</code>, this method will return <code>0</code>.
     * </p>
     *
     * @param value value to cast to an integer.
     * @return <code>value</code> as an integer.
     */
    public static int getIntegerValue(String value) {
        return value == null ? 0 : Integer.parseInt(value);
    }

    /**
     * Casts the specified value into a value list.
     * <p>
     * If <code>value</code> is <code>null</code>, this method will return <code>null</code>.
     * </p>
     *
     * @param value     value to cast to a value list.
     * @param separator string used to separate data in tokens.
     * @return <code>value</code> as a value list.
     */
    public static ValueList getListValue(String value, String separator) {
        return value == null ? null : new ValueList(value, separator);
    }

    /**
     * Casts the specified value into an float.
     * <p>
     * If <code>value</code> is <code>null</code>, this method will return <code>0</code>.
     * </p>
     *
     * @param value value to cast to an float.
     * @return <code>value</code> as an float.
     */
    public static float getFloatValue(String value) {
        return value == null ? 0 : Float.parseFloat(value);
    }

    /**
     * Casts the specified value into an boolean.
     * <p>
     * If <code>value</code> is <code>null</code>, this method will return <code>false</code>.
     * </p>
     *
     * @param value value to cast to an boolean.
     * @return <code>value</code> as an boolean.
     */
    public static boolean getBooleanValue(String value) {
        return Boolean.TRUE.toString().equals(value);
    }

    /**
     * Casts the specified value into an long.
     * <p>
     * If <code>value</code> is <code>null</code>, this method will return <code>0</code>.
     * </p>
     *
     * @param value value to cast to an long.
     * @return <code>value</code> as an long.
     */
    public static long getLongValue(String value) {
        return value == null ? 0 : Long.parseLong(value);
    }

    /**
     * Casts the specified value into an double.
     * <p>
     * If <code>value</code> is <code>null</code>, this method will return <code>0</code>.
     * </p>
     *
     * @param value value to cast to an double.
     * @return <code>value</code> as an double.
     */
    public static double getDoubleValue(String value) {
        return value == null ? 0 : Double.parseDouble(value);
    }

    /**
     * Casts the specified value into a string.
     *
     * @param value value to cast as a string.
     * @return <code>value</code> as a string.
     */
    public static String getValue(int value) {
        return Integer.toString(value);
    }

    /**
     * Casts the specified value into a string.
     *
     * @param value     value to cast as a string.
     * @param separator string to use as a separator.
     * @return <code>value</code> as a string.
     */
    public static String getValue(List<String> value, String separator) {
        return ValueList.toString(value, separator);
    }

    /**
     * Casts the specified value into a string.
     *
     * @param value value to cast as a string.
     * @return <code>value</code> as a string.
     */
    public static String getValue(float value) {
        return Float.toString(value);
    }

    /**
     * Casts the specified value into a string.
     *
     * @param value value to cast as a string.
     * @return <code>value</code> as a string.
     */
    public static String getValue(boolean value) {
        return Boolean.toString(value);
    }

    /**
     * Casts the specified value into a string.
     *
     * @param value value to cast as a string.
     * @return <code>value</code> as a string.
     */
    public static String getValue(long value) {
        return Long.toString(value);
    }

    /**
     * Casts the specified value into a string.
     *
     * @param value value to cast as a string.
     * @return <code>value</code> as a string.
     */
    public static String getValue(double value) {
        return Double.toString(value);
    }


    // - Section access ------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Creates a subsection wit the specified name in the section.
     * <p>
     * If a subsection with the specified name already exists, it will be returned.
     * </p>
     *
     * @param name name of the new section.
     * @return the subsection with the specified name.
     */
    public ConfigurationSection addSection(String name) {
        ConfigurationSection section;

        // The section already exists, returns it.
        if ((section = getSection(name)) != null)
            return section;

        // Creates the new section.
        sections.put(name, section = new ConfigurationSection());
        return section;
    }

    /**
     * Deletes the specified section.
     *
     * @param name name of the section to delete.
     * @return the section that was deleted if any, <code>null</code> otherwise.
     */
    public ConfigurationSection removeSection(String name) {
        return sections.remove(name);
    }

    /**
     * Deletes the specified section.
     * <p>
     * Note that this method is very inefficient and should only be called when strictly necessary.
     * </p>
     *
     * @param section section to remove.
     * @return <code>true</code> if the specified section was removed, <code>false</code> if it didn't exist.
     */
    public boolean removeSection(ConfigurationSection section) {
        String name;
        Iterator<String> sectionNames;

        sectionNames = sectionNames();

        // Goes through each key / value pair and checks whether we've found the sectioon
        // we were looking for.
        while (sectionNames.hasNext()) {
            name = sectionNames.next();

            // If we have, remove it and break.
            if (getSection(name).equals(section)) {
                removeSection(name);
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the subsection with the specified name.
     *
     * @param name name of the section to retrieve.
     * @return the requested section if found, <code>null</code> otherwise.
     */
    public ConfigurationSection getSection(String name) {
        return sections.get(name);
    }

    /**
     * Returns an enumeration on all of this section's subsections' names.
     * <p>
     * Note that the order in which section names are returned needs not be that in which they were added to the
     * section. Callers should not rely on the order being consistent over time.
     * </p>
     *
     * @return an enumeration on all of this section's subsections' names.
     */
    public Iterator<String> sectionNames() {
        return sections.keySet().iterator();
    }

    /**
     * Returns <code>true</code> if this section has subsections.
     *
     * @return <code>true</code> if this section has subsections, <code>false</code> otherwise.
     */
    public boolean hasSections() {
        return !sections.isEmpty();
    }


    // - Misc. ---------------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Returns <code>true</code> if the section doesn't contain either variables or sub-sections.
     * <p>
     * This method is meant for {@link Configuration} instances to prune dead branches.
     * </p>
     *
     * @return <code>true</code> if the section doesn't contain either variables or sub-sections, <code>false</code>
     * otherwise.
     */
    public boolean isEmpty() {
        return !hasSections() && !hasVariables();
    }

    /**
     * Remove all variables and sub-sections of the section
     */
    public void clear() {
        variables.clear();
        sections.clear();
    }
}
