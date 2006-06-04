package com.mucommander.ant.macosx;

import com.mucommander.xml.*;
import org.apache.tools.ant.BuildException;

/**
 *
 */
public class RootKey extends DictValue {
    private static final String ELEMENT_PLIST = "plist";
    public static final String ATTRIBUTE_VERSION = "version";
    public static final String URL_PLIST_DTD     = "file://localhost/System/Library/DTDs/PropertyList.dtd";

    private String version;

    public void setVersion(String s) {version = s;}

    public void write(XmlWriter out) throws BuildException {
        XmlAttributes attr;

        out.writeDocType(ELEMENT_PLIST, XmlWriter.AVAILABILITY_SYSTEM, null, URL_PLIST_DTD);
        attr = new XmlAttributes();

        if(version != null)
            attr.add(ATTRIBUTE_VERSION, version);

        out.startElement(ELEMENT_PLIST, attr);
        out.println();

        super.write(out);

        out.endElement(ELEMENT_PLIST);
    }
}
