package com.mucommander.conf;

import java.io.*;
import java.util.*;
import com.mucommander.xml.*;

/**
 * Writes the configuration tree to an output stream.
 * <p>
 * This class is pretty straightforward to use: just call the {@link #writeXML(OutputStream)}
 * method, and everything will be automatically done.
 * </p>
 * @author Nicolas Rinaudo, Maxence Bernard
 */
public class ConfigurationWriter implements ConfigurationTreeBuilder {
    private XmlWriter out;

    /**
     * Writes the configuration tree's content to the specified output stream.
     * @param stream where to write the tree's content.
     */
    public void writeXML(OutputStream stream) {
        out = new XmlWriter(stream);
        ConfigurationManager.buildConfigurationTree(this);
    }

    /**
     * Method called when a new node is opened.
     * @param name node's name.
     */
    public void addNode(String name) {
        // Remove 'root' element from the xml tree
        if(name.equals("root"))
            return;
        out.startElement(name);
        out.println();
    }

    /**
     * Method called when a node is closed.
     * @param name node's name.
     */
    public void closeNode(String name) {
        // Remove 'root' element from the xml tree
        if(name.equals("root"))
            return;
        out.endElement(name);
    }

    /**
     * Method called when a new leaf is found in the tree.
     * @param name  leaf's name.
     * @param value leaf's value.
     */
    public void addLeaf(String name, String value) {
        out.startElement(name);
        out.writeCData(value);
        out.endElement(name);
    }
}
