package com.mucommander.command;

import com.mucommander.Debug;
import com.mucommander.xml.parser.ContentHandler;
import com.mucommander.xml.parser.Parser;
import com.mucommander.file.filter.PermissionsFileFilter;

import java.io.InputStream;
import java.util.Hashtable;

/**
 * Class used to parse custom associations XML files.
 * <p>
 * Association file parsing is done through the {@link #read(InputStream,AssociationBuilder,String) read} method, which is
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
public class AssociationReader implements ContentHandler, AssociationsXmlConstants {
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
     * This is a convenience method, and is equivalent to calling <code>AssociationReader.read(in, b, "UTF-8")</code>.
     * </p>
     * @param  in        where to read association data from.
     * @param  b         where to send building events to.
     * @throws Exception thrown if any error occurs.
     * @see    #read(InputStream,AssociationBuilder,String)
     */
    public static void read(InputStream in, AssociationBuilder b) throws Exception {read(in, b, "UTF-8");}

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
     * @param  encoding  encoding used by <code>in</code>.
     * @throws Exception thrown if any error occurs.
     * @see    #read(InputStream,AssociationBuilder)
     */
    public static void read(InputStream in, AssociationBuilder b, String encoding) throws Exception {
        b.startBuilding();
        try {new Parser().parse(in, new AssociationReader(b), encoding);}
        finally {b.endBuilding();}
    }



    // - XML methods ---------------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * This method is public as an implementation side effect and should not be called directly.
     */
    public void startElement(String uri, String name, Hashtable attributes, Hashtable attURIs) throws Exception {
        // New custom association definition. Any other element is ignored, in an attempt to make
        // parsing as error resistant as possible.
        if(name.equals(ELEMENT_ASSOCIATION)) {
            String mask;    // Association's mask.
            String command; // Association's command.
         
            // Makes sure the required attributes are present.
            if((command = (String)attributes.get(ARGUMENT_COMMAND)) == null)
                throw new Exception("Unspecified association command.");
            if((mask = (String)attributes.get(ARGUMENT_MASK)) == null)
                throw new Exception("Unspecified association mask.");

            // Notifies the builder that a new association has been found.
            builder.addAssociation(mask, getConstantForValue((String)attributes.get(ARGUMENT_READABLE)),
                                   getConstantForValue((String)attributes.get(ARGUMENT_WRITABLE)),
                                   getConstantForValue((String)attributes.get(ARGUMENT_EXECUTABLE)), command);
        }
    }



    // - Unused XML methods --------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * This method is public as an implementation side effect, but should not be called directly.
     */
    public void startDocument() throws Exception {}

    /**
     * This method is public as an implementation side effect, but should not be called directly.
     */
    public void endDocument() throws Exception {}

    /**
     * This method is public as an implementation side effect, but should not be called.
     */
    public void characters(String s) {}

    /**
     * This method is public as an implementation side effect, but should not be called.
     */
    public void endElement(String uri, String name) throws Exception {}



    // - Misc. methods -------------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Analyses the specified value and returns its integer constant equivalent.
     * <p>
     * Note that this method is not strict about its argument: if <code>value</code> is <code>null</code>
     * or not a known value, it will be taken to mean {@link CommandAssociation@UNFILTERED}.
     * </p>
     * @param  value value to analyse.
     * @return       <code>value</code>'s integer equivalent.
     */
    private static int getConstantForValue(String value) {
        if(value != null) {
            if(value.equals(VALUE_YES))
                return CommandAssociation.YES;
            else if(value.equals(VALUE_NO))
                return CommandAssociation.NO;
            // If value is neither null nor known, we're in a bit of a dodgy case
            // and should at least notify the user.
            else if(Debug.ON)
                Debug.trace("Warning: unknown command type '" + value + "'.");
        }
        return CommandAssociation.UNFILTERED;
    }

}
