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

import com.mucommander.xml.XmlWriter;

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
     * @param out where to write the XML data.
     */
    public void setOutputStream(OutputStream out) {this.out = new XmlWriter(out);}



    // - Builder methods -------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Starts a new configuration section.
     * @param name name of the new section.
     */
    public void startSection(String name) {
        out.startElement(name);
        out.println();
    }

    /**
     * Ends a configuration section.
     * @param name name of the closed section.
     */
    public void endSection(String name) {out.endElement(name);}

    /**
     * Creates a new variable in the current section.
     * @param name  name of the new variable.
     * @param value value of the new variable.
     */
    public void addVariable(String name, String value) {
        out.startElement(name);
        out.writeCData(value);
        out.endElement(name);
    }

    /**
     * Writes the XML header.
     */
    public void startConfiguration() {
        out.startElement(ROOT_ELEMENT);
        out.println();
    }

    /**
     * Writes the XML footer.
     */
    public void endConfiguration() {out.endElement(ROOT_ELEMENT);}
}
