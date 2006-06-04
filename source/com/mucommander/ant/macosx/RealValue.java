package com.mucommander.ant.macosx;

import com.mucommander.xml.XmlWriter;
import org.apache.tools.ant.BuildException;

/**
 *
 */
public class RealValue implements InfoElement {
    private static final String ELEMENT_REAL    = "real";
    private float value;

    public RealValue() {}
    public RealValue(float f) {setValue(f);}

    public void setValue(float f) {value = f;}

    public void write(XmlWriter out) throws BuildException {
        out.startElement(ELEMENT_REAL);
        out.writeCData(Float.toString(value));
        out.endElement(ELEMENT_REAL);
    }
}
