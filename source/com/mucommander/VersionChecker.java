
package com.mucommander;

import java.net.URL;
import java.io.*;
import java.util.Hashtable;
import com.muxml.*;

/**
 * Checks latest version by parsing an XML document located at some remote URL.
 */
public class VersionChecker implements ContentHandler {

    private final static String VERSION_DOCUMENT_URL = "http://127.0.0.1/~maxence/version.xml";

    private String lastVersion;
    private String elementName;

    public VersionChecker() {
    }

    /**
     * Parses a remote XML document and returns the latest version string.
     *
     * @throw Exception if the document could not be parsed (connection down, 404...) or if the XML
     * document did not contain any version information.
     */
    public String getLatestVersion() throws Exception {
        Parser parser = new Parser();
        parser.parse(new URL(VERSION_DOCUMENT_URL).openStream(), this);
    
        if(lastVersion==null || lastVersion.equals(""))
            throw new Exception();
        
        return lastVersion;
    }

    public void characters(String s) {
        if(elementName.equals("last_version"))
            lastVersion = s;
    }

    public void startElement(String uri, String name, Hashtable attValues, Hashtable attURIs) {
        elementName = name;
    }

    public void endElement(String uri, String name) {}
    public void endDocument() {}
    public void startDocument() {}
}
