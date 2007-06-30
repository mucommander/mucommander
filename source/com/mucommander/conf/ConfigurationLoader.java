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

/**
 * Class used to load configuration data.
 * <p>
 * This class should in no case be used by anyone but the ConfigurationManager.
 * </p>
 * @author Nicolas Rinaudo
 */
class ConfigurationLoader implements ConfigurationTreeBuilder {

    /** Buffer for the configuration path. */
    private String variable = null;

    /**
     * Adds a new entry to the configuration path.
     * @param name name of the entry to add.
     */
    public void addNode(String name) {
        if(variable == null)
            variable = name;
        else
            variable += '.' + name;
    }

    /**
     * Removes the last entry in the path.
     * @param name name of the entry that was closed.
     */
    public void closeNode(String name) {
        int index;
		
        index = variable.lastIndexOf('.');
        if(index > 0)
            variable = variable.substring(0, index);
    }

    /**
     * Defines a new variable with the specified name and value.
     * @param name  variable's name.
     * @param value variable's value.
     */
    public void addLeaf(String name, String value) {
        ConfigurationManager.setVariable(variable + '.' + name, value);
    }
}

