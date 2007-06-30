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

import java.awt.*;

/**
 * Event used to notify registered listeners that a configuration variable has been modified.
 *
 * @author Nicolas Rinaudo, Maxence Bernard
 */
public class ConfigurationEvent {

    /** Name of the variable that has been modified. */
    private String variable;
    /** New value of the variable that has been modified. */
    private String value;

    /* ------------------------ */
    /*      Initialisation      */
    /* ------------------------ */
    /**
     * Builds a new configuration event with the specified variable name and value.
     * @param variable name of the variable that has been modified.
     * @param value    new value of the variable that has been modified.
     */
    ConfigurationEvent(String variable, String value) {
        setVariable(variable);
        setValue(value);
    }

    /* ------------------------ */
    /*        Name access       */
    /* ------------------------ */
    /**
     * Sets the name of the variable that has been modified.
     * @param variable name of the variable that has been modified.
     */
    void setVariable(String variable) { 
        this.variable = variable;
    }

    /**
     * Returns the name of the variable that has been modified.
     * @return the name of the variable that has been modified.
     */
    public String getVariable() {
        return variable;
    }

    /* ------------------------ */
    /*       Value access       */
    /* ------------------------ */
    /**
     * Sets the new value of the variable that has been modified.
     * @param value new value of the variable that has been modified.
     */
    void setValue(String value) {
        this.value = value;
    }

    /**
     * Returns the new value of the variable that has been modified.
     * <p>
     * If the returned value is <i>null</i>, it means that the configuration variable
     * has been destroyed.
     * </p>
     * @return the new value of the variable that has been modified.
     */
    public String getValue() {
        return value;
    }


    /**
     * Returns the new value of the variable that has been modified, parsed as an int.
     * <p>
     * <b>Warning: </b>this method will return <code>-1</code> if the variable has been destroyed. 
     * Use {@link #getValue getValue} and test the value against <code>null</code> to know if it has been destroyed.
     * </p>
     * @return the int value of the variable that has been modified, or -1 if the variable has been destroyed or
     * the value could not be parsed as an int.
     */
    public int getIntValue() {
        if(value==null)
            return -1;
		
        try {
            return Integer.parseInt(value);
        }
        catch(NumberFormatException e) {
            return -1;
        }
    }


    /**
     * Returns the new value of the variable that has been modified, parsed as a float.
     * <p>
     * <b>Warning: </b>this method will return <code>-1</code> if the variable has been destroyed. 
     * Use {@link #getValue getValue} and test the value against <code>null</code> to know if it has been destroyed.
     * </p>
     * @return the float value of the variable that has been modified, or -1 if the variable has been destroyed or
     * the value could not be parsed as a float.
     */
    public float getFloatValue() {
        if(value==null)
            return -1;
		
        try {
            return Float.parseFloat(value);
        }
        catch(NumberFormatException e) {
            return -1;
        }
    }


    /**
     * Returns the new value of the variable that has been modified, parsed as a boolean.
     * <p>
     * <b>Warning: </b>this method will return <code>false</code> if the value has been destroyed. 
     * Use {@link #getValue getValue} and test the value against <code>null</code> to know if it has been destroyed.
     * </p>
     * @return the boolean value of the variable that has been modified.
     */
    public boolean getBooleanValue() {
        return value==null?false:value.equals("true");
    }


    /**
     * Returns the new value of the variable that has been modified, parsed as a color represented in hexadecimal RGB format.
     * 
     * @return the Color value of the variable that has been modified, <code>null</code> if it has been destroyed.
     */
    public Color getColorValue() {
        return value==null?null:new Color(Integer.parseInt(value, 16));
    }
}
