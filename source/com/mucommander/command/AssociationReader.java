package com.mucommander.command;

import com.mucommander.xml.parser.ContentHandler;
import com.mucommander.xml.parser.Parser;

import java.io.InputStream;
import java.util.Hashtable;

/**
 * Class used to parse command association XML files.
 * <p>
 * This class works with {@link com.mucommander.command.AssociationBuilder builders} in order to
 * parse the content of XML association description files. This is achieved with the
 * {@link #read(InputStream,AssociationBuilder)} method.
 * </p>
 * @author Nicolas Rinaudo
 */
public class AssociationReader implements ContentHandler, XmlConstants {
    // - Instance variables --------------------------------------------------
    // -----------------------------------------------------------------------
    /** Where to send building messages. */
    private AssociationBuilder builder;



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
     * This method assumed <code>in</code> to be <code>UTF-8</code> encoded. To read assocation
     * data from streams using a different encoding, use {@link #read(InputStream,AssociationBuilder,String)}.
     * </p>
     * @param  in        where to read association data from.
     * @param  b         where to send building events to.
     * @throws Exception thrown if any error occurs.
     */
    public static void read(InputStream in, AssociationBuilder b) throws Exception {read(in, b, "UTF-8");}

    /**
     * Parses the content of the specified input stream.
     * @param  in        where to read association data from.
     * @param  b         where to send building events to.
     * @param  encoding  encoding used by <code>in</code>.
     * @throws Exception thrown if any error occurs.
     */
    public static void read(InputStream in, AssociationBuilder b, String encoding) throws Exception {new Parser().parse(in, new AssociationReader(b), encoding);}



    // - XML methods ---------------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Notifies the reader that a new XML element is starting.
     */
    public void startElement(String uri, String name, Hashtable attributes, Hashtable attURIs) throws Exception {
        // New custom command declaration.
        if(name.equals(ELEMENT_COMMAND)) {
            String  alias;
            String  command;
            Command buffer;

            // Makes sure the required attributes are there.
            if((alias = (String)attributes.get(ARGUMENT_COMMAND_ALIAS)) == null)
                throw new Exception("Unspecified command alias.");
            if((command = (String)attributes.get(ARGUMENT_COMMAND_VALUE)) == null)
                throw new Exception("Unspecified command value.");


            // Creates the command and passes it to the builder.
            builder.addCommand(buffer = CommandParser.getCommand(alias, command));

            // Sets the command's system flag.
            if((command = (String)attributes.get(ARGUMENT_COMMAND_SYSTEM)) != null)
                if(command.equals("true"))
                    buffer.setSystem(true);

            // Sets the command's visible flag if it's not a system command (system
            // commands are always invisible).
            if(!buffer.isSystem()) {
                if((command = (String)attributes.get(ARGUMENT_COMMAND_VISIBLE)) != null)
                    if(!command.equals("true"))
                        buffer.setVisible(false);
            }
        }

        // New custom association definition.
        else if(name.equals(ELEMENT_ASSOCIATION)) {
            String mask;
            String command;

            // Makes sure the required attributes are there.
            if((mask = (String)attributes.get(ARGUMENT_ASSOCIATION_MASK)) == null)
                throw new Exception("Unspecified association mask.");
            if((command = (String)attributes.get(ARGUMENT_ASSOCIATION_COMMAND)) == null)
                throw new Exception("Unspecified association command.");

            // Passes the association to the builder.
            builder.addAssociation(mask, command);
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
