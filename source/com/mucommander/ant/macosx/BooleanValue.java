package com.mucommander.ant.macosx;

import com.mucommander.xml.XmlWriter;
import org.apache.tools.ant.BuildException;

/**
 *
 */
public class BooleanValue implements InfoElement {
    public static final String ELEMENT_TRUE      = "true";
    public static final String ELEMENT_FALSE     = "false";
    private boolean value;

    public BooleanValue() {}
    public BooleanValue(boolean b) {setValue(b);}
    public void setValue(boolean b) {value = b;}

    public void write(XmlWriter out) throws BuildException {
        if(value)
            out.writeStandAloneElement(ELEMENT_TRUE);
        else
            out.writeStandAloneElement(ELEMENT_FALSE);
    }
}
