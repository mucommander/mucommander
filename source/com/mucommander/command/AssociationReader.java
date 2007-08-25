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

import com.mucommander.Debug;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.Locator;
import org.xml.sax.Attributes;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.InputStream;
import java.util.Hashtable;

/**
 * Class used to parse custom associations XML files.
 * <p>
 * Association file parsing is done through the {@link #read(InputStream,AssociationBuilder) read} method, which is
 * the only way to interact with this class.
 * </p>
 * <p>
 * Note that while this class knows how to read the content of an association XML file, its role is not to interpret it. This
 * is done by instances of {@link AssociationBuilder}.
 * </p>
 * @see    AssociationsXmlConstants
 * @see    AssociationBuilder
 * @see    AssociationWriter
 * @author Nicolas Rinaudo
 */
public class AssociationReader extends DefaultHandler implements AssociationsXmlConstants {
    // - Instance variables --------------------------------------------------
    // -----------------------------------------------------------------------
    /** Where to send building messages. */
    private AssociationBuilder builder;
    private boolean            isInAssociation;



    // - Initialisation ------------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Creates a new command reader.
     * @param b where to send custom command events.
     */
    private AssociationReader(AssociationBuilder b) {builder = b;}



    // - XML interaction -----------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Parses the content of the specified input stream.
     * <p>
     * This method will go through the specified input stream and notify the builder of any new association declaration it
     * encounters. Note that parsing is done in a very lenient fashion, and perfectly invalid XML files might not raise
     * an exception. This is not a flaw in the parser, and both allows muCommander to be error resilient and the associations
     * file format to be extended without having to rewrite most of this code.
     * </p>
     * <p>
     * Note that even if an error occurs, both of the builder's {@link AssociationBuilder#startBuilding()} and
     * {@link AssociationBuilder#endBuilding()} methods will still be called. Parsing will stop at the first error
     * however, so while the builder is guaranteed to receive correct messages, it might not receive all declared
     * associations.
     * </p>
     * @param  in        where to read association data from.
     * @param  b         where to send building events to.
     * @throws Exception thrown if any error occurs.
     * @see    #read(InputStream,AssociationBuilder)
     */
    public static void read(InputStream in, AssociationBuilder b) throws Exception {
        b.startBuilding();
        try {SAXParserFactory.newInstance().newSAXParser().parse(in, new AssociationReader(b));}
        finally {b.endBuilding();}
    }



    // - XML methods ---------------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * This method is public as an implementation side effect and should not be called directly.
     */
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        String buffer;

        try {
            if(!isInAssociation) {
                if(qName.equals(ELEMENT_ASSOCIATION)) {
                    // Makes sure the required attributes are present.
                    if((buffer = attributes.getValue(ATTRIBUTE_COMMAND)) == null)
                        return;

                    isInAssociation = true;
                    builder.startAssociation(buffer);
                }
            }
            else {
                if(qName.equals(ELEMENT_MASK)) {
                    String caseSensitive;

                    if((buffer = attributes.getValue(ATTRIBUTE_VALUE)) == null)
                        return;
                    if((caseSensitive = attributes.getValue(ATTRIBUTE_CASE_SENSITIVE)) != null)
                        builder.setMask(buffer, caseSensitive.equals(VALUE_TRUE));
                    else
                        builder.setMask(buffer, true);
                }
                else if(qName.equals(ELEMENT_IS_HIDDEN)) {
                    if((buffer = attributes.getValue(ATTRIBUTE_VALUE)) == null)
                        return;
                    builder.setIsHidden(buffer.equals(VALUE_TRUE));
                }
                else if(qName.equals(ELEMENT_IS_SYMLINK)) {
                    if((buffer = attributes.getValue(ATTRIBUTE_VALUE)) == null)
                        return;
                    builder.setIsSymlink(buffer.equals(VALUE_TRUE));
                }
                else if(qName.equals(ELEMENT_IS_READABLE)) {
                    if((buffer = attributes.getValue(ATTRIBUTE_VALUE)) == null)
                        return;
                    builder.setIsReadable(buffer.equals(VALUE_TRUE));
                }
                else if(qName.equals(ELEMENT_IS_WRITABLE)) {
                    if((buffer = attributes.getValue(ATTRIBUTE_VALUE)) == null)
                        return;
                    builder.setIsWritable(buffer.equals(VALUE_TRUE));
                }
                else if(qName.equals(ELEMENT_IS_EXECUTABLE)) {
                    if((buffer = attributes.getValue(ATTRIBUTE_VALUE)) == null)
                        return;
                    builder.setIsExecutable(buffer.equals(VALUE_TRUE));
                }
            }
        }
        catch(CommandException e) {throw new SAXException(e);}
    }

    /**
     * This method is public as an implementation side effect, but should not be called directly.
     */
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if(qName.equals(ELEMENT_ASSOCIATION) && isInAssociation) {
            try {builder.endAssociation();}
            catch(CommandException e) {throw new SAXException(e);}
            isInAssociation = false;
        }
    }
}
