package com.mucommander.ant.macosx;

import com.mucommander.xml.writer.XmlWriter;
import org.apache.tools.ant.BuildException;

/**
 *
 */
class StringValue implements InfoElement {
    private static final String ELEMENT_STRING    = "string";
    private String value;

    public StringValue() {}
    public StringValue(String s) {setValue(s);}

    public void setValue(String s) {value = s;}

    public void write(XmlWriter out) throws BuildException {
        if(value == null)
            throw new BuildException("Uninitialised string key.");
        out.startElement(ELEMENT_STRING);
        out.writeCData(value);
        out.endElement(ELEMENT_STRING);
    }
}
