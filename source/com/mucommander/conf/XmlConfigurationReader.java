/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
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

package com.mucommander.conf;

import java.io.InputStream;
import java.io.IOException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.Locator;
import org.xml.sax.Attributes;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Implementation of {@link ConfigurationReader} used to read XML configuration streams.
 * <p>
 * The format of XML files parsed by instances of <code>XmlConfigurationReader</code> is fairly simple:
 * <ul>
 *   <li>
 *     Any element that doesn't contain other elements is considered to be a variable. Its value will be
 *     the CDATA contained by the element.
 *   </li>
 *   <li>
 *     Any element that contains other elements is considered to be a section. Any CDATA it might contain
 *     will be ignored.
 *   </li>
 *   <li>
 *     The XML file's first element is traditionally called <code>prefs</code>, but this isn't enforced. It
 *     will be excluded from section names.
 *   </li>
 * </ul>
 * </p>
 * <p>
 * For example:
 * <pre>
 * &lt;prefs&gt;
 *   &lt;some&gt;
 *     Random CDATA
 *     &lt;section&gt;
 *       &lt;var1&gt;value1&lt;/var1&gt;
 *       &lt;var2&gt;value2&lt;/var2&gt;
 *     &lt;/section&gt;
 *   &lt;/some&gt;
 * &lt;/prefs&gt;
 * </pre>
 * This will be interpreted as follows:
 * <ul>
 *   <li><code>Random CDATA</code> will be ignored.</li>
 *   <li>A variable called <code>some.section.var1</code> will be created with a value of <code>value1</code>.</li>
 *   <li>A variable called <code>some.section.var2</code> will be created with a value of <code>value2</code>.</li>
 * </ul>
 * </p>
 * @author Nicolas Rinaudo
 * @see    XmlConfigurationReaderFactory
 * @see    XmlConfigurationWriter
 */
public class XmlConfigurationReader extends DefaultHandler implements ConfigurationReader {
    // - Instance variables ----------------------------------------------------
    // -------------------------------------------------------------------------
    /** Current depth in the configuration tree. */
    private   int                  depth;
    /** Buffer for each element's CDATA. */
    private   StringBuffer         buffer;
    /** Name of the item being parsed. */
    private   String               itemName;
    /** Class notified whenever a new configuration item is found. */
    protected ConfigurationBuilder builder;
    /** Whether the current element is a variable. */
    private   boolean              isVariable;
    /** Used to track the parser's position in the XML file. */
    private   Locator              locator;



    // - Initialisation --------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Creates a new instance of XML configuration reader.
     */
    public XmlConfigurationReader() {buffer = new StringBuffer();}



    // - Reader methods --------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Reads the content of <code>in</code> an passes build messages to <code>builder</code>.
     * @param in input stream from which to read the configuration data.
     * @param  builder                      object to notify of build events.
     * @throws IOException                  if an I/O error occurs.
     * @throws ConfigurationFormatException if a configuration file format occurs.
     * @throws ConfigurationException       if a non-specific error occurs.
     */
    public void read(InputStream in, ConfigurationBuilder builder) throws IOException, ConfigurationException, ConfigurationFormatException {
        this.builder = builder;
        locator      = null;
        try {SAXParserFactory.newInstance().newSAXParser().parse(in, this);}
        catch(ParserConfigurationException e) {throw new ConfigurationException("Failed to create a SAX parser", e);}
        catch(SAXParseException e) {throw new ConfigurationFormatException(e.getMessage(), e.getLineNumber(), e.getColumnNumber());}
        catch(SAXException e) {throw new ConfigurationFormatException(e.getException() == null ? e : e.getException());}
    }



    // - XML handling ----------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * This method is public as an implementation side effect and should never be called directlry.
     */
    public void characters(char[] ch, int start, int length) {buffer.append(ch, start, length);}

    /**
     * This method is public as an implementation side effect and should never be called directlry.
     */
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        depth++;
        if(depth == 1)
            return;

        if(itemName != null) {
            try {builder.startSection(itemName);}
            catch(Exception e) {throw new SAXParseException(e.getMessage(), locator, e);}
        }
        buffer.setLength(0);
        itemName   = qName;
        isVariable = true;
    }

    /**
     * This method is public as an implementation side effect and should never be called directlry.
     */
    public void endElement(String uri, String localName, String qName) throws SAXException {
        depth--;
        if(depth == 0)
            return;

        // If the current element doesn't have subsections, considers it to be a variable.
        if(isVariable) {
            String value;

            value = buffer.toString().trim();

            // Ignores empty values, otherwise notifies the builder of a new variable.
            if(!value.equals("")) {
                try {builder.addVariable(qName, value);}
                catch(Exception e) {throw new SAXParseException(e.getMessage(), locator, e);}
            }
        }

        // The current element is a container, closes it.
        else {
            try {builder.endSection(qName);}
            catch(Exception e) {throw new SAXParseException(e.getMessage(), locator, e);}
        }

        isVariable = false;
        itemName   = null;
    }

    /**
     * This method is public as an implementation side effect and should never be called directlry.
     */
    public void startDocument() throws SAXException {
        try {builder.startConfiguration();}
        catch(Exception e) {throw new SAXParseException(e.getMessage(), locator, e);}
    }

    /**
     * This method is public as an implementation side effect and should never be called directlry.
     */
    public void endDocument() throws SAXException {
        try {builder.endConfiguration();}
        catch(Exception e) {throw new SAXParseException(e.getMessage(), locator, e);}
    }

    /**
     * This method is public as an implementation side effect and should never be called directlry.
     */
    public void setDocumentLocator(Locator locator) {this.locator = locator;}
}
