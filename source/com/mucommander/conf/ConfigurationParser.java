/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (c) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with muCommander; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.mucommander.conf;

import com.mucommander.xml.parser.ContentHandler;
import com.mucommander.xml.parser.Parser;

import java.io.InputStream;
import java.util.Hashtable;


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

    /** Name of the currently parsed XML node (can be either a configuration node or leaf). */
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
     * @param     in                    where to read the configuration from. 
     * @exception IllegalStateException thrown if no builder was associated with this parser.
     */
    public void parse(InputStream in) throws Exception {
        Parser parser;

        if(builder == null)
            throw new IllegalStateException("Cannot parse a file without a tree builder.");
        new Parser().parse(in, this, "UTF-8");
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
        if(inNode) {
            builder.closeNode(name);
            buffer = null;
        }
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
