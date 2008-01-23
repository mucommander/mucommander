/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
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

import com.mucommander.xml.XmlAttributes;
import com.mucommander.xml.XmlWriter;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Class used to write custom associations XML files.
 * <p>
 * <code>AssociationWriter</code> is an {@link AssociationBuilder} that will send
 * all build messages it receives into an XML stream (as defined in {@link AssociationsXmlConstants}).
 * </p>
 * @author Nicolas Rinaudo
 */
public class AssociationWriter implements AssociationsXmlConstants, AssociationBuilder {
    // - Instance variables --------------------------------------------------
    // -----------------------------------------------------------------------
    /** Where to write the custom command associations to. */
    private XmlWriter out;



    // - Initialisation ------------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Builds a new writer that will send data to the specified output stream.
     * @param  stream      where to write the XML data.
     * @throws IOException if an I/O error occurs.
     */
    public AssociationWriter(OutputStream stream) throws IOException {out = new XmlWriter(stream);}



    // - Builder methods ------------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Opens the root XML element.
     */
    public void startBuilding() throws CommandException {
        try {
            out.startElement(ELEMENT_ROOT);
            out.println();
        }
        catch(IOException e) {throw new CommandException(e);}
    }

    /**
     * Closes the root XML element.
     */
    public void endBuilding() throws CommandException {
        try {out.endElement(ELEMENT_ROOT);}
        catch(IOException e) {throw new CommandException(e);}
    }

    public void startAssociation(String command) throws CommandException {
        XmlAttributes attr;

        attr = new XmlAttributes();
        attr.add(ATTRIBUTE_COMMAND, command);

        try {
            out.startElement(ELEMENT_ASSOCIATION, attr);
            out.println();
        }
        catch(IOException e) {throw new CommandException(e);}
    }

    public void endAssociation() throws CommandException {
        try {out.endElement(ELEMENT_ASSOCIATION);}
        catch(IOException e) {throw new CommandException(e);}
    }

    public void setMask(String mask, boolean isCaseSensitive) throws CommandException {
        XmlAttributes attr;

        attr = new XmlAttributes();
        attr.add(ATTRIBUTE_VALUE, mask);
        if(!isCaseSensitive)
            attr.add(ATTRIBUTE_CASE_SENSITIVE, VALUE_FALSE);

        try {out.writeStandAloneElement(ELEMENT_MASK, attr);}
        catch(IOException e) {throw new CommandException(e);}
    }

    public void setIsSymlink(boolean isSymlink) throws CommandException {
        XmlAttributes attr;

        attr = new XmlAttributes();
        attr.add(ATTRIBUTE_VALUE, isSymlink ? VALUE_TRUE : VALUE_FALSE);

        try {out.writeStandAloneElement(ELEMENT_IS_SYMLINK, attr);}
        catch(IOException e) {throw new CommandException(e);}
    }

    public void setIsHidden(boolean isHidden) throws CommandException {
        XmlAttributes attr;

        attr = new XmlAttributes();
        attr.add(ATTRIBUTE_VALUE, isHidden ? VALUE_TRUE : VALUE_FALSE);

        try {out.writeStandAloneElement(ELEMENT_IS_HIDDEN, attr);}
        catch(IOException e) {throw new CommandException(e);}
    }

    public void setIsReadable(boolean isReadable) throws CommandException {
        XmlAttributes attr;

        attr = new XmlAttributes();
        attr.add(ATTRIBUTE_VALUE, isReadable ? VALUE_TRUE : VALUE_FALSE);

        try {out.writeStandAloneElement(ELEMENT_IS_READABLE, attr);}
        catch(IOException e) {throw new CommandException(e);}
    }

    public void setIsWritable(boolean isWritable) throws CommandException {
        XmlAttributes attr;

        attr = new XmlAttributes();
        attr.add(ATTRIBUTE_VALUE, isWritable ? VALUE_TRUE : VALUE_FALSE);

        try {out.writeStandAloneElement(ELEMENT_IS_WRITABLE, attr);}
        catch(IOException e) {throw new CommandException(e);}
    }

    public void setIsExecutable(boolean isExecutable) throws CommandException {
        XmlAttributes attr;

        attr = new XmlAttributes();
        attr.add(ATTRIBUTE_VALUE, isExecutable ? VALUE_TRUE : VALUE_FALSE);

        try {out.writeStandAloneElement(ELEMENT_IS_EXECUTABLE, attr);}
        catch(IOException e) {throw new CommandException(e);}
    }
}
