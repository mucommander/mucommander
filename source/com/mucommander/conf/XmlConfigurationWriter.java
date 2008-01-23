/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
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

import com.mucommander.xml.XmlWriter;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Implementation of {@link ConfigurationWriter} used to write XML configuration streams.
 * <p>
 * Informations on the XML file format can be found {@link XmlConfigurationReader here}.
 * </p>
 * @author Nicolas Rinaudo
 */
public class XmlConfigurationWriter implements ConfigurationWriter {
    // - Class constants -------------------------------------------------------
    // -------------------------------------------------------------------------
    /** Root element name. */
    private static final String ROOT_ELEMENT = "prefs";



    // - Instance fields -------------------------------------------------------
    // -------------------------------------------------------------------------
    /** Writer on the destination XML stream. */
    protected XmlWriter out;



    // - Initialisation --------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Creates a new instance of XML configuration writer.
     */
    public XmlConfigurationWriter() {}



    // - Writer methods --------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Sets the output stream in which to write the XML data.
     * @param  out                    where to write the XML data.
     * @throws ConfigurationException as a wrapper for any <code>IOException</code> that might have occured. 
     */
    public void setOutputStream(OutputStream out) throws ConfigurationException {
        try {this.out = new XmlWriter(out);}
        catch(IOException e) {throw new ConfigurationException(e);}
    }



    // - Builder methods -------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Starts a new configuration section.
     * @param  name                   name of the new section.
     * @throws ConfigurationException as a wrapper for any <code>IOException</code> that might have occured. 
     */
    public void startSection(String name) throws ConfigurationException {
        try {
            out.startElement(name);
            out.println();
        }
        catch(IOException e) {throw new ConfigurationException(e);}
    }

    /**
     * Ends a configuration section.
     * @param  name                   name of the closed section.
     * @throws ConfigurationException as a wrapper for any <code>IOException</code> that might have occured. 
     */
    public void endSection(String name) throws ConfigurationException {
        try {out.endElement(name);}
        catch(IOException e) {throw new ConfigurationException(e);}
    }

    /**
     * Creates a new variable in the current section.
     * @param  name                   name of the new variable.
     * @param  value                  value of the new variable.
     * @throws ConfigurationException as a wrapper for any <code>IOException</code> that might have occured. 
     */
    public void addVariable(String name, String value) throws ConfigurationException {
        try {
            out.startElement(name);
            out.writeCData(value);
            out.endElement(name);
        }
        catch(IOException e) {throw new ConfigurationException(e);}
    }

    /**
     * Writes the XML header.
     * @throws ConfigurationException as a wrapper for any <code>IOException</code> that might have occured. 
     */
    public void startConfiguration() throws ConfigurationException {
        try {
            out.startElement(ROOT_ELEMENT);
            out.println();
        }
        catch(IOException e) {throw new ConfigurationException(e);}
    }

    /**
     * Writes the XML footer.
     * @throws ConfigurationException as a wrapper for any <code>IOException</code> that might have occured. 
     */
    public void endConfiguration() throws ConfigurationException {
        try {out.endElement(ROOT_ELEMENT);}
        catch(IOException e) {throw new ConfigurationException(e);}
    }
}
