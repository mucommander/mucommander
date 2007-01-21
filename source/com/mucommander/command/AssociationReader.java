package com.mucommander.command;

import com.mucommander.xml.parser.ContentHandler;
import com.mucommander.xml.parser.Parser;
import com.mucommander.file.filter.PermissionsFileFilter;

import java.io.InputStream;
import java.util.Hashtable;

/**
 * Class used to parse custom associations XML files.
 * <p>
 * This class knows how to read and analyse the content of a custom associations XML file
 * (as defined in {@link AssociationsXmlConstants}), but not what to do with it. For this,
 * it needs an {@link AssociationBuilder}.
 * </p>
 * <p>
 * Instances of <code>AssociationReader</code> are not retrievable. The only way to interact with
 * it is through the {@link #read(InputStream,AssociationBuilder,String) read} methods.
 * </p>
 * @see AssociationsXmlConstants
 * @see AssociationBuilder
 * @see AssociationWriter
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
     * This method assumes <code>in</code> to be <code>UTF-8</code> encoded. To read assocation
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
     * XML parsing method.
     * <p>
     * This method is public as an implementation side effect, but should not be called directly.
     * </p>
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

    /**
     * XML parsing method.
     * <p>
     * This method is public as an implementation side effect, but should not be called directly.
     * </p>
     */
    public void startDocument() throws Exception {builder.startBuilding();}

    /**
     * XML parsing method.
     * <p>
     * This method is public as an implementation side effect, but should not be called directly.
     * </p>
     */
    public void endDocument() throws Exception {builder.endBuilding();}



    // - Unused XML methods --------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Not used.
     * <p>
     * This method is public as an implementation side effect, but should not be called.
     * </p>
     */
    public void characters(String s) {}

    /**
     * Not used.
     * <p>
     * This method is public as an implementation side effect, but should not be called.
     * </p>
     */
    public void endElement(String uri, String name) throws Exception {}



    // - Misc. methods -------------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Analyses the specified value and returns its integer constant equivalent.
     * <p>
     * Note that this method is error resilient: if <code>value</code> is <code>null</code>
     * or not a known value, it will be taken to mean {@link CommandAssociation@UNFILTERED}.
     * </p>
     * @param value value to analyze.
     * @return <code>value</code>'s integer equivalent.
     */
    private static int getConstantForValue(String value) {
        if(value != null) {
            if(value.equals(VALUE_YES))
                return CommandAssociation.YES;
            else if(value.equals(VALUE_NO))
                return CommandAssociation.NO;
        }
        return CommandAssociation.UNFILTERED;
    }

}
