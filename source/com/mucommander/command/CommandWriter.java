/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.command;

import com.mucommander.xml.writer.XmlAttributes;
import com.mucommander.xml.writer.XmlWriter;

import java.io.OutputStream;

/**
 * Class used to write custom commands XML files.
 * <p>
 * <code>CommandWriter</code> is a {@link CommandBuilder} that will send
 * all build messages it receives into an XML stream (as defined in {@link CommandsXmlConstants}).
 * </p>
 * @author Nicolas Rinaudo
 */
public class CommandWriter implements CommandsXmlConstants, CommandBuilder {
    // - Instance variables --------------------------------------------------
    // -----------------------------------------------------------------------
    /** Where to write the custom command associations to. */
    private XmlWriter out;



    // - Initialisation ------------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Builds a new writer that will send data to the specified output stream.
     * @param stream where to write the XML data.
     */
    public CommandWriter(OutputStream stream) {out = new XmlWriter(stream);}



    // - Builder methods ------------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Opens the root XML element.
     */
    public void startBuilding() {
        out.startElement(ELEMENT_ROOT);
        out.println();
    }

    /**
     * Closes the root XML element.
     */
    public void endBuilding() {out.endElement(ELEMENT_ROOT);}

    /**
     * Writes the specified command's XML description.
     * @param command command that should be written.
     */
    public void addCommand(Command command) {
        XmlAttributes attributes;

        // Builds the XML description of the command.
        attributes = new XmlAttributes();
        attributes.add(ATTRIBUTE_ALIAS, command.getAlias());
        attributes.add(ATTRIBUTE_VALUE, command.getCommand());
        if(command.getType() == Command.SYSTEM_COMMAND)
            attributes.add(ATTRIBUTE_TYPE, VALUE_SYSTEM);
        else if(command.getType() == Command.INVISIBLE_COMMAND)
            attributes.add(ATTRIBUTE_TYPE, VALUE_INVISIBLE);
        if(command.isDisplayNameSet())
            attributes.add(ATTRIBUTE_DISPLAY, command.getDisplayName());

        // Writes the XML description.
        out.writeStandAloneElement(ELEMENT_COMMAND, attributes);
    }
}
