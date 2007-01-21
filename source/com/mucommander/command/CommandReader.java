package com.mucommander.command;

import com.mucommander.xml.parser.ContentHandler;
import com.mucommander.xml.parser.Parser;

import java.io.InputStream;
import java.util.Hashtable;

/**
 * @author Nicolas Rinaudo
 */
public class CommandReader implements ContentHandler, CommandsXmlConstants {
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
     * This method assumed <code>in</code> to be <code>UTF-8</code> encoded. To read assocation
     * data from streams using a different encoding, use {@link #read(InputStream,CommandBuilder,String)}.
     * </p>
     * @param  in        where to read association data from.
     * @param  b         where to send building events to.
     * @throws Exception thrown if any error occurs.
     */
    public static void read(InputStream in, CommandBuilder b) throws Exception {read(in, b, "UTF-8");}

    /**
     * Parses the content of the specified input stream.
     * @param  in        where to read association data from.
     * @param  b         where to send building events to.
     * @param  encoding  encoding used by <code>in</code>.
     * @throws Exception thrown if any error occurs.
     */
    public static void read(InputStream in, CommandBuilder b, String encoding) throws Exception {new Parser().parse(in, new CommandReader(b), encoding);}



    // - XML methods ---------------------------------------------------------
    // -----------------------------------------------------------------------
    private static int parseCommandType(String type) {
        if(type == null)
            return Command.NORMAL_COMMAND;
        if(type.equals(VALUE_SYSTEM))
            return Command.SYSTEM_COMMAND;
        if(type.equals(VALUE_INVISIBLE))
           return Command.INVISIBLE_COMMAND;
        return Command.NORMAL_COMMAND;
    }

    /**
     * Notifies the reader that a new XML element is starting.
     */
    public void startElement(String uri, String name, Hashtable attributes, Hashtable attURIs) throws Exception {
        // New custom command declaration.
        if(name.equals(ELEMENT_COMMAND)) {
            String  alias;
            String  command;
            int     type;
            Command buffer;

            // Makes sure the required attributes are there.
            if((alias = (String)attributes.get(ARGUMENT_ALIAS)) == null)
                throw new Exception("Unspecified command alias.");
            if((command = (String)attributes.get(ARGUMENT_VALUE)) == null)
                throw new Exception("Unspecified command value.");
            type = parseCommandType((String)attributes.get(ARGUMENT_TYPE));


            // Creates the command and passes it to the builder.
            builder.addCommand(buffer = CommandParser.getCommand(alias, command, type));
        }
    }

    /**
     * Calls the underlying builder's {@link com.mucommander.command.AssociationBuilder#startBuilding()} method
     */
    public void startDocument() throws Exception {builder.startBuilding();}

    /**
     * Calls the underlying builder's {@link com.mucommander.command.AssociationBuilder#endBuilding()} method
     */
    public void endDocument() throws Exception {builder.endBuilding();}



    // - Unused XML methods --------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Not used.
     */
    public void characters(String s) {}

    /**
     * Not used.
     */
    public void endElement(String uri, String name) throws Exception {}
}
