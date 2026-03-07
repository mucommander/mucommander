/*
 * This file is part of muCommander, http://www.mucommander.com
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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;

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
    private final AssociationBuilder builder;
    private       boolean            isInAssociation;



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
     * @param  in          where to read association data from.
     * @param  b           where to send building events to.
     * @throws IOException if any IO error occurs.
     * @see                #read(InputStream,AssociationBuilder)
     */
    public static void read(InputStream in, AssociationBuilder b) throws IOException, CommandException {
        b.startBuilding();
        try {SAXParserFactory.newInstance().newSAXParser().parse(in, new AssociationReader(b));}
        catch(ParserConfigurationException e) {throw new CommandException(e);}
        catch(SAXException e) {throw new CommandException(e);}
        finally {b.endBuilding();}
    }



    // - XML methods ---------------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * This method is public as an implementation side effect and should not be called directly.
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        String buffer;

        try {
            if(!isInAssociation) {
                if(ELEMENT_ASSOCIATION.equals(qName)) {
                    // Makes sure the required attributes are present.
                    if((buffer = attributes.getValue(ATTRIBUTE_COMMAND)) == null)
                        return;

                    isInAssociation = true;
                    builder.startAssociation(buffer);
                }
            }
            else if(ELEMENT_MASK.equals(qName)) {
                String caseSensitive;

                if((buffer = attributes.getValue(ATTRIBUTE_VALUE)) == null)
                    return;
                if((caseSensitive = attributes.getValue(ATTRIBUTE_CASE_SENSITIVE)) != null)
                    builder.setMask(buffer, VALUE_TRUE.equals(caseSensitive));
                else
                    builder.setMask(buffer, true);
            }
            else if(ELEMENT_IS_HIDDEN.equals(qName)) {
                if((buffer = attributes.getValue(ATTRIBUTE_VALUE)) == null)
                    return;
                builder.setIsHidden(VALUE_TRUE.equals(buffer));
            }
            else if(ELEMENT_IS_SYMLINK.equals(qName)) {
                if((buffer = attributes.getValue(ATTRIBUTE_VALUE)) == null)
                    return;
                builder.setIsSymlink(VALUE_TRUE.equals(buffer));
            }
            else if(ELEMENT_IS_READABLE.equals(qName)) {
                if((buffer = attributes.getValue(ATTRIBUTE_VALUE)) == null)
                    return;
                builder.setIsReadable(VALUE_TRUE.equals(buffer));
            }
            else if(ELEMENT_IS_WRITABLE.equals(qName)) {
                if((buffer = attributes.getValue(ATTRIBUTE_VALUE)) == null)
                    return;
                builder.setIsWritable(VALUE_TRUE.equals(buffer));
            }
            else if(ELEMENT_IS_EXECUTABLE.equals(qName)) {
                if((buffer = attributes.getValue(ATTRIBUTE_VALUE)) == null)
                    return;
                builder.setIsExecutable(VALUE_TRUE.equals(buffer));
            }
        }
        catch(CommandException e) {throw new SAXException(e);}
    }

    /**
     * This method is public as an implementation side effect, but should not be called directly.
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if(ELEMENT_ASSOCIATION.equals(qName) && isInAssociation) {
            try {builder.endAssociation();}
            catch(CommandException e) {throw new SAXException(e);}
            isInAssociation = false;
        }
    }
}
