/*
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

/**
 * Helper class meant for instances of {@link Configuration} to explore their own configuration tree.
 * @author Nicolas Rinaudo
 */
class ConfigurationExplorer {
    // - Instance variables --------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Current section. */
    private ConfigurationSection section;



    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new explorer on the specified section.
     * @param root section from which to start exploring.
     */
    public ConfigurationExplorer(ConfigurationSection root) {section = root;}



    // - Exploration methods -------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Returns the current section.
     * @return the current section.
     */
    public ConfigurationSection getSection() {return section;}

    /**
     * Move to the specified section.
     * @param  name   name of the current section's subsection in which to move.
     * @param  create if <code>true</code> and <code>name</code> doesn't exist, it will be created.
     * @return        <code>true</code> if we could move to <code>name</code>, <code>false</code> otherwise.
     */
    public boolean moveTo(String name, boolean create) {
        ConfigurationSection buffer; // Buffer for the subsection.

        // Checks whether the requested subsection exists.
        if((buffer = section.getSection(name)) == null) {
            // If it doesn't exist, either return false or create it depending on
            // parameters.
            if(create) {
                section = section.addSection(name);
                return true;
            }
            return false;
        }

        section = buffer;
        return true;
    }
}
