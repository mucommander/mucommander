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

import java.awt.*;

/**
 * Event triggered when the configuration has been modified.
 * @author Nicolas Rinaudo
 */
public class ConfigurationEvent {
    // - Instance variables --------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Name of the variable that has been modified. */
    private final String name;
    /** Variable's new value. */
    private final String value;



    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Builds a new configuration event initialised on the specified name and value.
     * @param name  name of the variable that was modified.
     * @param value value of the variable that was modified.
     */
    public ConfigurationEvent(String name, String value) {
        this.name  = name;
        this.value = value;
    }



    // - Variable access -----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Returns the name of the variable that was modified.
     * <p>
     * The returned value will be the variable's fully qualified name. If, for example, the
     * modified variable is <code>test.somevar</code>, this is what this method will return,
     * not <code>somevar</code>.
     * </p>
     * @return the name of the variable that was modified.
     */
    public String getVariable() {return name;}

    /**
     * Returns the new value for the modified variable.
     * <p>
     * If the variable has been deleted, this method will return <code>null</code>.
     * </p>
     * @return the new value for the modified variable.
     */
    public String getValue() {return value;}

    /**
     * Returns the new value for the modified variable as an integer.
     * <p>
     * If the variable has been deleted, this method will return 0.
     * </p>
     * @return the new value for the modified variable.
     */
    public int getIntegerValue() {return ConfigurationSection.getIntegerValue(value);}

    /**
     * Returns the new value for the modified variable as a float.
     * <p>
     * If the variable has been deleted, this method will return 0.
     * </p>
     * @return the new value for the modified variable.
     */
    public float getFloatValue() {return ConfigurationSection.getFloatValue(value);}

    /**
     * Returns the new value for the modified variable as a boolean.
     * <p>
     * If the variable has been deleted, this method will return <code>false</code>.
     * </p>
     * @return the new value for the modified variable.
     */
    public boolean getBooleanValue() {return ConfigurationSection.getBooleanValue(value);}

    /**
     * Returns the new value for the modified variable as a long.
     * <p>
     * If the variable has been deleted, this method will return 0.
     * </p>
     * @return the new value for the modified variable.
     */
    public long getLongValue() {return ConfigurationSection.getLongValue(value);}

    /**
     * Returns the new value for the modified variable as a double.
     * <p>
     * If the variable has been deleted, this method will return 0.
     * </p>
     * @return the new value for the modified variable.
     */
    public double getDoubleValue() {return ConfigurationSection.getDoubleValue(value);}
}
