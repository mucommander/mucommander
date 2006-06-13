package com.mucommander.ant.macosx;

import com.mucommander.xml.writer.XmlWriter;
import org.apache.tools.ant.BuildException;

public class DataValue implements InfoElement {
    private static final String DATA_ELEMENT = "data";

    private StringBuffer data;

    public DataValue() {data = new StringBuffer();}

    public void addText(String txt) {data.append(txt);}

    public void write(XmlWriter out) throws BuildException {
        out.startElement(DATA_ELEMENT);
        out.writeCData(data.toString());
        out.endElement(DATA_ELEMENT);
    }
}
