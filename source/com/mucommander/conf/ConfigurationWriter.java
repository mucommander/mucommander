package com.mucommander.conf;

import java.io.*;
import java.util.*;

/**
 * Writes the configuration tree to an output stream.
 * <p>
 * This class is pretty straightforward to use: just call the {@link #writeXML(PrintWriter)}
 * method, and everything will be automatically done.
 * </p>
 * @author Nicolas Rinaudo, Maxence Bernard
 */
public class ConfigurationWriter implements ConfigurationTreeBuilder {

    /** Where to print the configuration tree. */
    private PrintWriter out;

	/** Current depth of the XML tree */
	private int depth = 0;

    /**
     * Writes the configuration tree's content to the specified output stream.
     * @param out where to write the tree's content.
     */
    public void writeXML(PrintWriter out) {
        this.out = out;
        out.println("<?xml version=\"1.0\" encoding=\"utf-8\" ?>");
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
		
		// Indent statement
		indent();
		// Increase depth
		depth++;
		
		out.println("<"+name+">");
    }

    /**
     * Method called when a node is closed.
     * @param name node's name.
     */
    public void closeNode(String name) {
	    // Remove 'root' element from the xml tree
	    if(name.equals("root"))
	    	return;

		// Decrease depth
		depth--;
		// Indent statement
		indent();

        out.println("</"+name+">");
    }

    /**
     * Method called when a new leaf is found in the tree.
     * @param name  leaf's name.
     * @param value leaf's value.
     */
    public void addLeaf(String name, String value) {
		// Indent element
		indent();
        out.println("<"+name+">"+value+"</"+name+">");
    }
	
	
	/**
	 * Adds tab characters to the stream based on the current XML tree's depth.
	 */
	private void indent() {
		for(int i=0; i<depth; i++)
			out.print("\t");
	}
}
