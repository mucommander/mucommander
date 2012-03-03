/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.commons.conf;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.io.Writer;

/**
 * Implementation of {@link ConfigurationBuilder} used to write XML configuration streams.
 * <p>
 * Information on the XML file format can be found {@link XmlConfigurationReader here}.
 * </p>
 * @author Nicolas Rinaudo
 */
public class XmlConfigurationWriter implements ConfigurationBuilder {
    // - Class constants -----------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /** Factory used to create instances of {@link XmlConfigurationWriter}. */
    public static final ConfigurationWriterFactory<XmlConfigurationWriter> FACTORY;

    // - Instance fields -----------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /** Writer on the destination XML stream. */
    protected final ContentHandler out;
    /** Empty XML attributes (avoids creating a new instance on each <code>startElement</code> call). */
    private   final Attributes     emptyAttributes = new AttributesImpl();
    /** Root element name. */
    protected   final String rootElementName;


    // - Initialization ------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    static {
        FACTORY = new ConfigurationWriterFactory<XmlConfigurationWriter>() {
            public XmlConfigurationWriter getWriterInstance(Writer out) {
                return new XmlConfigurationWriter(out, getRootElementName());
            }
        };
    }

    /**
     * Creates a new instance of XML configuration writer.
     * @param out where to write the configuration data.
     */
    protected XmlConfigurationWriter(Writer out, String rootElementName) {
    	this.rootElementName = rootElementName;
        this.out = createHandler(out);
    }


    // - Writer methods ------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    private static ContentHandler createHandler(Writer out) {
        SAXTransformerFactory factory;
        TransformerHandler    transformer;

        // Initializes the transformer factory.
        factory = (SAXTransformerFactory)SAXTransformerFactory.newInstance();
        factory.setAttribute("indent-number", 4);

        // Creates a new transformer.
        try {transformer = factory.newTransformerHandler();}
        catch(TransformerConfigurationException e) {throw new IllegalStateException(e);}

        // Enables indentation.
        transformer.getTransformer().setOutputProperty(OutputKeys.INDENT, "yes");

        // Sets the standalone property.
        transformer.getTransformer().setOutputProperty(OutputKeys.STANDALONE, "yes");

        // Plugs the transformer into the specified stream.
        transformer.setResult(new StreamResult(out));

        return transformer;
    }



    // - Builder methods -----------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    protected void startElement(String name) throws ConfigurationException {
        try {out.startElement("", name, name, emptyAttributes);}
        catch(SAXException e) {throw new ConfigurationException(e);}
    }

    protected void endElement(String name) throws ConfigurationException {
        try {out.endElement("", name, name);}
        catch(SAXException e) {throw new ConfigurationException(e);}
    }

    /**
     * Starts a new configuration section.
     * @param  name                   name of the new section.
     * @throws ConfigurationException as a wrapper for any <code>IOException</code> that might have occurred.
     */
    public void startSection(String name) throws ConfigurationException {
        startElement(name);
    }

    /**
     * Ends a configuration section.
     * @param  name                   name of the closed section.
     * @throws ConfigurationException as a wrapper for any <code>IOException</code> that might have occurred.
     */
    public void endSection(String name) throws ConfigurationException {
        endElement(name);
    }

    /**
     * Creates a new variable in the current section.
     * @param  name                   name of the new variable.
     * @param  value                  value of the new variable.
     * @throws ConfigurationException as a wrapper for any <code>IOException</code> that might have occurred.
     */
    public void addVariable(String name, String value) throws ConfigurationException {
        char[] data;

        try {
            startElement(name);
            data = value.toCharArray();
            out.characters(data, 0, data.length);
            endElement(name);
        }
        catch(SAXException e) {throw new ConfigurationException(e);}
    }

    /**
     * Writes the XML header.
     * @throws ConfigurationException as a wrapper for any exception that might have occurred.
     */
    public void startConfiguration() throws ConfigurationException {
        try {
            out.startDocument();
            startElement(rootElementName);
        }
        catch(SAXException e) {throw new ConfigurationException(e);}
    }

    /**
     * Writes the XML footer.
     * @throws ConfigurationException as a wrapper for any <code>IOException</code> that might have occurred.
     */
    public void endConfiguration() throws ConfigurationException {
        try {
            endElement(rootElementName);
            out.endDocument();
        }
        catch(SAXException e) {throw new ConfigurationException(e);}
    }
}
