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

package com.mucommander.res;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.Locator;
import org.xml.sax.Attributes;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Vector;

/**
 * @author Nicolas Rinaudo
 */
public class ResourceListReader extends DefaultHandler implements XmlConstants {
    // - Parser states -------------------------------------------------------
    // -----------------------------------------------------------------------
    /** Parsing hasn't started. */
    private static final int STATE_UNKNOWN = 0;
    /** Parsing the root element. */
    private static final int STATE_ROOT    = 1;



    // - Instance variables --------------------------------------------------
    // -----------------------------------------------------------------------
    /** Resources contained in the stream being parsed. */
    private Vector content;
    /** Current XML parser state. */
    private int    state;



    // - Initialisation ------------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Creates a new instance of resource list reader.
     */
    public ResourceListReader() {}



    // - XML interaction -----------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Reads the content of the specified stream and returns the result
     * @param  in        where to read the data from.
     * @return           the different resource file paths described by the list.
     * @throws Exception thrown if an error occurs.
     */
    public Vector read(InputStream in) throws Exception {
        // Initialises parsing.
        state   = STATE_UNKNOWN;
        content = new Vector();

        SAXParserFactory.newInstance().newSAXParser().parse(in, this);

        return content;
    }

    /**
     * Notifies the reader that a new XML element is starting.
     */
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        String path;

        // Root element.
        if(qName.equals(ROOT_ELEMENT)) {
            if(state != STATE_UNKNOWN)
                throw new SAXException("Illegal start of element: " + qName);
            state = STATE_ROOT;
        }

        // File element.
        else if(qName.equals(FILE_ELEMENT)) {
            if(state != STATE_ROOT)
                throw new SAXException("Illegal start of element: " + qName);
            if((path = attributes.getValue(PATH_ATTRIBUTE)) != null)
                content.add(path);
        }

        // Unknown element.
        else
            throw new SAXException("Unknown XML element: " + qName);
    }
    
    /**
     * Notifies the reader that an XML element declaration is finished,
     */
    public void endElement(String uri, String localName, String qName) throws SAXException {
        // Root element.
        if(qName.equals(ROOT_ELEMENT)) {
            if(state != STATE_ROOT)
                throw new SAXException("Illegal end of element: " + qName);
            state = STATE_UNKNOWN;
        }

        // Unknown element.
        else if(!qName.equals(FILE_ELEMENT))
            throw new SAXException("Unknown XML element: " + qName);
    }
}
