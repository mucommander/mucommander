package com.mucommander.ant.java;

import org.apache.tools.ant.BuildException;

/*
 * - Class documentation -
 *
 * TODO: automatic type detection is very basic, and should be improved on.
 */

/**
 * @author Nicolas Rinaudo
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
     * @param     str            name of the java field.
     * @exception BuildException thrown if <code>str</code> is empty.
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
     * @param     str            value of the java field.
     * @exception BuildException thrown if <code>str</code> is empty.
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
     * @param str java field's type, as defined in {@link com.mucommander.ant.java.JavaWriter}.
     * @exception BuildException thrown if <code>str</code> is not a recognized type.
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
