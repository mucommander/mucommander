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

import com.mucommander.xml.writer.XmlWriter;

import java.io.OutputStream;

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
