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
     * @param stream where to write the XML data.
     */
    public AssociationWriter(OutputStream stream) {out = new XmlWriter(stream);}



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

    public void startAssociation(String command) {
        XmlAttributes attr;

        attr = new XmlAttributes();
        attr.add(ATTRIBUTE_COMMAND, command);
        out.startElement(ELEMENT_ASSOCIATION, attr);
        out.println();
    }

    public void endAssociation() {out.endElement(ELEMENT_ASSOCIATION);}

    public void setMask(String mask, boolean isCaseSensitive) {
        XmlAttributes attr;

        attr = new XmlAttributes();
        attr.add(ATTRIBUTE_VALUE, mask);
        if(!isCaseSensitive)
            attr.add(ATTRIBUTE_CASE_SENSITIVE, VALUE_FALSE);

        out.writeStandAloneElement(ELEMENT_MASK, attr);
    }

    public void setIsSymlink(boolean isSymlink) {
        XmlAttributes attr;

        attr = new XmlAttributes();
        attr.add(ATTRIBUTE_VALUE, isSymlink ? VALUE_TRUE : VALUE_FALSE);

        out.writeStandAloneElement(ELEMENT_IS_SYMLINK, attr);
    }

    public void setIsHidden(boolean isHidden) {
        XmlAttributes attr;

        attr = new XmlAttributes();
        attr.add(ATTRIBUTE_VALUE, isHidden ? VALUE_TRUE : VALUE_FALSE);

        out.writeStandAloneElement(ELEMENT_IS_HIDDEN, attr);
    }

    public void setIsReadable(boolean isReadable) {
        XmlAttributes attr;

        attr = new XmlAttributes();
        attr.add(ATTRIBUTE_VALUE, isReadable ? VALUE_TRUE : VALUE_FALSE);

        out.writeStandAloneElement(ELEMENT_IS_READABLE, attr);
    }

    public void setIsWritable(boolean isWritable) {
        XmlAttributes attr;

        attr = new XmlAttributes();
        attr.add(ATTRIBUTE_VALUE, isWritable ? VALUE_TRUE : VALUE_FALSE);

        out.writeStandAloneElement(ELEMENT_IS_WRITABLE, attr);
    }

    public void setIsExecutable(boolean isExecutable) {
        XmlAttributes attr;

        attr = new XmlAttributes();
        attr.add(ATTRIBUTE_VALUE, isExecutable ? VALUE_TRUE : VALUE_FALSE);

        out.writeStandAloneElement(ELEMENT_IS_EXECUTABLE, attr);
    }
}
