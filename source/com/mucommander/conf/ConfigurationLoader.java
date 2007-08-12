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

import java.util.Stack;
import java.util.EmptyStackException;

/**
 * {@link com.mucommander.conf.ConfigurationBuilder} implementation used to load the configuration.
 * <p>
 * The sole purpose of this class is to be used by the {@link com.mucommander.conf.ConfigurationManager}
 * when loading configuration files.
 * </p>
 * @author Nicolas Rinaudo
 */
class ConfigurationLoader implements ConfigurationBuilder {
    // - Instance variables --------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Parents of {@link #currentSection}. */
    private Stack                sections;
    /** Fully qualified names of {@link #currentSection}. */
    private Stack                sectionNames;
    /** Section that we're currently building. */
    private ConfigurationSection currentSection;



    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new configuration loader.
     * @param root where to create the configuration in.
     */
    public ConfigurationLoader(ConfigurationSection root) {currentSection = root;}



    // - Building ------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Initialises the configuration bulding.
     */
    public void startConfiguration() {
        sections     = new Stack();
        sectionNames = new Stack();
    }

    /**
     * Ends the configuration building.
     * @throws ConfigurationException if not all opened sections have been closed.
     */
    public void endConfiguration() throws ConfigurationException {
        // Makes sure currentSection is the root section.
        if(!sections.empty())
            throw new ConfigurationException("Not all sections have been closed.");
        sections     = null;
        sectionNames = null;
    }

    /**
     * Creates a new sub-section to the current section.
     * @param name name of the new section.
     */
    public void startSection(String name) throws ConfigurationException {
        ConfigurationSection buffer;

        buffer = currentSection.addSection(name);
        sections.push(currentSection);
        if(sectionNames.empty())
            sectionNames.push(name + '.');
        else
            sectionNames.push(((String)sectionNames.peek()) + name + '.');
        currentSection = buffer;
    }

    /**
     * Ends the current section.
     * @param  name                   name of the section that's being closed.
     * @throws ConfigurationException if we're not closing a legal section.
     */
    public void endSection(String name) throws ConfigurationException {
        ConfigurationSection buffer;

        // Makes sure there is a section to close.
        try {
            buffer = (ConfigurationSection)sections.pop();
            sectionNames.pop();
        }
        catch(EmptyStackException e) {throw new ConfigurationException("Section " + name + " was already closed.");}

        // Makes sure we're closing the right section.
        if(buffer.getSection(name) != currentSection)
            throw new ConfigurationException("Section " + name + " is not the currently opened section.");
        currentSection = buffer;
    }

    /**
     * Adds the specified variable to the current section.
     * @param name  name of the variable.
     * @param value value of the variable.
     */
    public void addVariable(String name, String value) {
        // If the variable's value was modified, trigger an event.
        if(currentSection.setVariable(name, value)) {
            if(sectionNames.empty())
                ConfigurationEvent.triggerEvent(new ConfigurationEvent(name, value));
            else
                ConfigurationEvent.triggerEvent(new ConfigurationEvent(((String)sectionNames.peek()) + name, value));
        }
    }
}
