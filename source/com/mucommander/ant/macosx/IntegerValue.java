package com.mucommander.ant.macosx;

import com.mucommander.xml.XmlWriter;
import org.apache.tools.ant.BuildException;

/**
 * Represents the value part of an integer property.
 * @author Nicolas Rinaudo
 */
public class IntegerValue implements InfoElement {
    // - Fields ----------------------------------------------------------
    // -------------------------------------------------------------------
    /** Label of the 'integer' XML element. */
    private static final String ELEMENT_INTEGER = "integer";
    /** Value of the integer. */
    private              int    value;



    // - Initialisation --------------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Creates an empty integer value.
     */
    public IntegerValue() {}

    /**
     * Creates an integer with the specified value.
     * @param i integer's value.
     */
    public IntegerValue(int i) {setValue(i);}



    // - Ant interaction -------------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Allows Ant to set the integer's value.
     * @param i integer's value.
     */
    public void setValue(int i) {value = i;}



    // - XML output ------------------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Writes the XML representation of this integer value.
     * @param     out            where to write the integer's value to.
     * @exception BuildException thrown if anything wrong happens.
     */
    public void write(XmlWriter out) throws BuildException {
        out.startElement(ELEMENT_INTEGER);
        out.writeCData(Integer.toString(value));
        out.endElement(ELEMENT_INTEGER);
    }
}
