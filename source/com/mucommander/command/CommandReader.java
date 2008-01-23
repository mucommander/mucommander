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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;

/**
 * Class used to parse custom commands XML files.
 * <p>
 * Command file parsing is done through the {@link #read(InputStream,CommandBuilder) read} method, which is
 * the only way to interact with this class.
 * </p>
 * <p>
 * Note that while this class knows how to read the content of an command XML file, its role is not to interpret it. This
 * is done by instances of {@link CommandBuilder}.
 * </p>
 * @see    CommandsXmlConstants
 * @see    CommandBuilder
 * @see    CommandWriter
 * @author Nicolas Rinaudo
 */
public class CommandReader extends DefaultHandler implements CommandsXmlConstants {
    // - Instance variables --------------------------------------------------
    // -----------------------------------------------------------------------
    /** Where to send building messages. */
    private CommandBuilder builder;



    // - Initialisation ------------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Creates a new command reader.
     * @param b where to send custom command events.
     */
    private CommandReader(CommandBuilder b) {builder = b;}



    // - XML interaction -----------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Parses the content of the specified input stream.
     * <p>
     * This method will go through the specified input stream and notify the builder of any new command declaration it
     * encounters. Note that parsing is done in a very lenient fashion, and perfectly invalid XML files might not raise
     * an exception. This is not a flaw in the parser, and both allows muCommander to be error resilient and the commands
     * file format to be extended without having to rewrite most of this code.
     * </p>
     * <p>
     * Note that even if an error occurs, both of the builder's {@link CommandBuilder#startBuilding()} and
     * {@link CommandBuilder#endBuilding()} methods will still be called. Parsing will stop at the first error
     * however, so while the builder is guaranteed to receive correct messages, it might not receive all declared
     * commands.
     * </p>
     * @param  in        where to read command data from.
     * @param  b         where to send building events to.
     * @throws Exception thrown if any error occurs.
     */
    public static void read(InputStream in, CommandBuilder b) throws Exception {
        b.startBuilding();
        try {SAXParserFactory.newInstance().newSAXParser().parse(in, new CommandReader(b));}
        finally {b.endBuilding();}
    }



    // - XML methods ---------------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * This method is public as an implementation side effect and should not be called directly.
     */
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        // New custom command declaration.
        if(qName.equals(ELEMENT_COMMAND)) {
            String  alias;
            String  command;
            int     type;
            String  display;

            // Makes sure the required attributes are there.
            if(((alias = attributes.getValue(ATTRIBUTE_ALIAS)) == null) || ((command = attributes.getValue(ATTRIBUTE_VALUE)) == null))
                return;
            type    = parseCommandType(attributes.getValue(ATTRIBUTE_TYPE));
            display = attributes.getValue(ATTRIBUTE_DISPLAY);


            // Creates the command and passes it to the builder.
            try {builder.addCommand(new Command(alias, command, type, display));}
            catch(CommandException e) {throw new SAXException(e);}
        }
    }



    // - Misc. ---------------------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Returns the integer value of the specified command type.
     * <p>
     * Note that this method is not strict in the arguments it receives:
     * <ul>
     *   <li>If <code>type</code> equals {CommandsXmlConstants#VALUE_SYSTEM}, {@link Command#SYSTEM_COMMAND} will be returned.</li>
     *   <li>If <code>type</code> equals {CommandsXmlConstants#VALUE_INVISIBLE}, {@link Command#INVISIBLE_COMMAND} will be returned.</li>
     *   <li>In any other case, {@link Command.NORMAL_COMMAND} will be returned.</li>
     * </ul>
     * </p>
     * @param  type type to analyse.
     * @return      <code>type</code>'s integer equivalent.
     */
    private static int parseCommandType(String type) {
        if(type == null)
            return Command.NORMAL_COMMAND;
        if(type.equals(VALUE_SYSTEM))
            return Command.SYSTEM_COMMAND;
        if(type.equals(VALUE_INVISIBLE))
            return Command.INVISIBLE_COMMAND;
        return Command.NORMAL_COMMAND;
    }
}
