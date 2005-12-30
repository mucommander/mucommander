package com.mucommander.conf;

import java.util.*;
import java.io.*;
import com.muxml.ContentHandler;
import com.muxml.Parser;


/**
 * Parses configuration files.
 * <p>
 * This class will parse a muCommander configuration file and pass all the
 * configuration items it finds to the specified {@link com.mucommander.conf.ConfigurationTreeBuilder}.
 *
 * /!\ /!\ /!\ This parser is known to bug when it encounters empty elements (containing
 *  only whitespace characters) /!\ /!\ /!\
 * </p>
 * @author Nicolas Rinaudo
 */
public class ConfigurationParser implements ContentHandler {

    /** True if the parser is in a node, false otherwise. */
    private boolean inNode = false;

    /** Name of the currently parsed node. */
    private String node;

    /** Name of the currently parsed XML node(can be either a configuration node or leaf). */
    private String buffer;

    /** Class notified whenever a new configuration item is found. */
    private ConfigurationTreeBuilder builder;

    /* ------------------------ */
    /*      Initialisation      */
    /* ------------------------ */
    /**
     * Builds an empty ConfigurationParser.
     * <p>
     * Note that you'll need to call the {@link #setBuilder(ConfigurationTreeBuilder)}
     * method before being able to actually perform the configuration file.
     * </p>
     */
    public ConfigurationParser() {}

    /**
     * Builds a new ConfigurationParser with the specified tree builder.
     * @param builder object that will be notified whenever a new configuration item is found.
     */
    public ConfigurationParser(ConfigurationTreeBuilder builder) {setBuilder(builder);}

    /* ------------------------ */
    /*      Builder access      */
    /* ------------------------ */
    /**
     * Returns the associated tree builder.
     * @return the associated tree builder.
     */
    public ConfigurationTreeBuilder getBuilder() {return builder;}

    /**
     * Sets the builder to use while parsing the configuration file.
     * @param builder builder to use while parsing the configuration file.
     */
    public void setBuilder(ConfigurationTreeBuilder builder) {this.builder = builder;}

    /* ------------------------ */
    /*          Parsing         */
    /* ------------------------ */
    /**
     * Parses the specified file.
     * @param file name of the file to parse.
     * @exception IllegalStateException thrown if no builder was associated with this parser.
     */
    public void parse(String file) throws Exception {
        Parser parser;

        if(builder == null)
            throw new IllegalStateException("Cannot parse a file without a tree builder.");

        parser = new Parser();
		// Use UTF-8 encoding
        parser.parse(new FileInputStream(file), this, "UTF-8");
    }

    /**
     * Method called when some PCDATA has been found in an XML node.
     */
    public void characters(String s) {
        String value;

        value = s.trim();
        if(!value.equals("")) {
            inNode = false;
            if(buffer != null)
                builder.addLeaf(buffer, value);
            buffer = null;
        }
    }

    /**
     * Notifies the parser that a new XML node has been found.
     */
    public void startElement(String uri, String name, Hashtable attValues, Hashtable attURIs) {
        inNode = true;
        if(buffer != null) {
            node = buffer;
            builder.addNode(buffer);
        }
        buffer = name;
    }

    /**
     * Notifies the parser that an XML node has been closed.
     */
    public void endElement(String uri, String name) {
        if(inNode)
            builder.closeNode(name);
        else {
            node = buffer;
            inNode = true;
        }
    }

    /* ------------------------ */
    /*      Unused methods      */
    /* ------------------------ */
    public void endDocument() {}
    public void startDocument() {}
}
