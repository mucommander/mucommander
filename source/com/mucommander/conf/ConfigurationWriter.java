package com.mucommander.conf;

import java.io.*;
import java.util.*;

/**
 * Writes the configuration tree to an output stream.
 * <p>
 * This class is pretty straightforward to use: just call the {@link #writeXML(PrintWriter)}
 * method, and everything will be automatically done.
 * </p>
 * @author Nicolas Rinaudo
 */
public class ConfigurationWriter implements ConfigurationTreeBuilder {
    /** Where to print the configuration tree. */
    private PrintWriter out;

    /**
     * Writes the configuration tree's content to the specified output stream.
     * @param out where to write the tree's content.
     */
    public void writeXML(PrintWriter out) {
        this.out = out;
        out.println("<?xml version=\"1.0\" encoding=\"utf-8\" ?>");
        ConfigurationManager.buildConfigurationTree(this);
    }
    /* End ofmethod writeXML(PrintWriter) */

    /**
     * Method called when a new node is opened.
     * @param name node's name.
     */
    public void addNode(String name) {
        // Maxence's patch: otherwise root is added to the xml tree
		if(name.equals("root"))
			return;
		
		out.print('<');
        out.print(name);
        out.println('>');
    }
    /* End of method addNode(String) */

    /**
     * Method called when a node is closed.
     * @param name node's name.
     */
    public void closeNode(String name) {
	    // Maxence's patch: otherwise root is added to the xml tree
	    if(name.equals("root"))
	    	return;

        out.print("</");
        out.print(name);
        out.println('>');
    }
    /* End of method closeNode(String) */

    /**
     * Method called when a new leaf is found in the tree.
     * @param name  leaf's name.
     * @param value leaf's value.
     */
    public void addLeaf(String name, String value) {
        out.print('<');
        out.print(name);
        out.print('>');
        out.print(value);
        out.print("</");
        out.print(name);
        out.println(">");
    }
    /* End of method addLeaf(String, String) */
}
/* End of class ConfigurationWriter */
