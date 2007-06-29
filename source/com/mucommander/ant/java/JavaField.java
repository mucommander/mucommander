/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (c) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with muCommander; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.mucommander.ant.java;

import org.apache.tools.ant.BuildException;

/**
 * @author Nicolas Rinaudo
 * @ant.type name="field" category="util"
 */
public class JavaField {
    // - Instance fields -------------------------------------------------
    // -------------------------------------------------------------------
    /** Name of the Java field. */
    private String name;
    /** Value of the Java field. */
    private String value;
    /** Type of the Java field. */
    private int    type;


    // - Initialisation --------------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Creates a new Java field.
     */
    public JavaField() {
        name  = null;
        value = null;
        type  = JavaWriter.TYPE_UNKNOWN;
    }


    // - Ant interaction -------------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Sets the java field's name.
     * @ant.required
     */
    public void setName(String str) throws BuildException {
        // Makes sure the name is not only composed of whitespaces.
        str = str.trim();
        if(str.length() == 0)
            throw new BuildException("Empty Java field name. Fill in the 'name' attribute.");

        name = str;
    }

    /**
     * Sets the java field's value.
     * @ant.required
     */
    public void setValue(String str) throws BuildException {
        // Makes sure the value is not only whitespaces.
        str = str.trim();
        if(str.length() == 0)
            throw new BuildException("Empty Java field value. Fill in the 'value' attribute.");
        value = str;
    }

    /**
     * Sets the java field's type.
     * @ant.not-required Default is value dependant.
     */
    public void setType(String str) throws BuildException {
        if((type = JavaWriter.getTypeForLabel(str)) == JavaWriter.TYPE_UNKNOWN)
            throw new BuildException("Unknown field type '" + str + "'. Check the 'type' attribute.");
    }


    // - Misc. -----------------------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Prints this Java field to the specified java writer.
     * @param out where to print the java field.
     */
    void print(JavaWriter out) {
        out.printField(name, type, value, JavaWriter.MODIFIER_PUBLIC | JavaWriter.MODIFIER_STATIC | JavaWriter.MODIFIER_FINAL);
    }

    /**
     * Checks wether this Java field was properly initialized.
     * @exception BuildException thrown if the field was not properly initialized.
     */
    public void check() throws BuildException {
        // Checks wether the field name was specified.
        if(name == null)
            throw new BuildException("Missing name for a Java field. Fill in the 'name' attribute.");

        // Checks wether the field value was specified.
        if(value == null)
            throw new BuildException("Missing value for a Java field. Fill in the 'value' attribute.");

        // If the type is not known, tries to analyze the proper type.
        if(type == JavaWriter.TYPE_UNKNOWN) {
            try {
                Integer.parseInt(value);
                type = JavaWriter.TYPE_INT;
            }
            catch(Exception e) {type = JavaWriter.TYPE_STRING;}
        }
    }
}
