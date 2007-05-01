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
        if(Debug.ON) Debug.trace("Starting to load command associations.");
        b.startBuilding();
        try {new Parser().parse(in, new AssociationReader(b), encoding);}
        finally {b.endBuilding();}
        if(Debug.ON) Debug.trace("Command associations succesfully loaded.");
    }



    // - XML methods ---------------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * This method is public as an implementation side effect and should not be called directly.
     */
    public void startElement(String uri, String name, Hashtable attributes, Hashtable attURIs) throws Exception {
        String buffer;

        if(!isInAssociation) {
            if(name.equals(ELEMENT_ASSOCIATION)) {
                // Makes sure the required attributes are present.
                if((buffer = (String)attributes.get(ATTRIBUTE_COMMAND)) == null) {
                    if(Debug.ON) Debug.trace("Missing command attribute in association declaration. Ignoring association.");
                    return;
                }

                isInAssociation = true;
                builder.startAssociation(buffer);
            }
            else if(Debug.ON) Debug.trace("Unexpected start of element " + name + ", ignoring.");
        }
        else {
            if(name.equals(ELEMENT_MASK)) {
                String caseSensitive;

                if((buffer = (String)attributes.get(ATTRIBUTE_VALUE)) == null) {
                    if(Debug.ON) Debug.trace("Missing value in file mask declaration. Ignoring mask.");
                    return;
                }
                if((caseSensitive = (String)attributes.get(ATTRIBUTE_CASE_SENSITIVE)) != null)
                    builder.setMask(buffer, caseSensitive.equals(VALUE_TRUE));
                else
                    builder.setMask(buffer, true);
            }
            else if(name.equals(ELEMENT_IS_HIDDEN)) {
                if((buffer = (String)attributes.get(ATTRIBUTE_VALUE)) == null) {
                    if(Debug.ON) Debug.trace("Missing value in is_hidden declaration. Ignoring filter.");
                    return;
                }
                builder.setIsHidden(buffer.equals(VALUE_TRUE));
            }
            else if(name.equals(ELEMENT_IS_SYMLINK)) {
                if((buffer = (String)attributes.get(ATTRIBUTE_VALUE)) == null) {
                    if(Debug.ON) Debug.trace("Missing value in is_symlink declaration. Ignoring filter.");
                    return;
                }
                builder.setIsSymlink(buffer.equals(VALUE_TRUE));
            }
            else if(name.equals(ELEMENT_IS_READABLE)) {
                if((buffer = (String)attributes.get(ATTRIBUTE_VALUE)) == null) {
                    if(Debug.ON) Debug.trace("Missing value in is_readable declaration. Ignoring filter.");
                    return;
                }
                builder.setIsReadable(buffer.equals(VALUE_TRUE));
            }
            else if(name.equals(ELEMENT_IS_WRITABLE)) {
                if((buffer = (String)attributes.get(ATTRIBUTE_VALUE)) == null) {
                    if(Debug.ON) Debug.trace("Missing value in is_writable declaration. Ignoring filter.");
                    return;
                }
                builder.setIsWritable(buffer.equals(VALUE_TRUE));
            }
            else if(name.equals(ELEMENT_IS_EXECUTABLE)) {
                if((buffer = (String)attributes.get(ATTRIBUTE_VALUE)) == null) {
                    if(Debug.ON) Debug.trace("Missing value in is_executable declaration. Ignoring filter.");
                    return;
                }
                builder.setIsExecutable(buffer.equals(VALUE_TRUE));
            }
            else if(Debug.ON) Debug.trace("Unexpected start of element " + name + ", ignoring.");
        }
    }

    /**
     * This method is public as an implementation side effect, but should not be called directly.
     */
    public void endElement(String uri, String name) throws Exception {
        if(name.equals(ELEMENT_ASSOCIATION) && isInAssociation) {
            builder.endAssociation();
            isInAssociation = false;
        }
        else if(Debug.ON) Debug.trace("Unexpected end of element " + name + ", ignoring.");
    }



    // - Unused XML methods --------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * This method is public as an implementation side effect, but should not be called directly.
     */
    public void startDocument() throws Exception {isInAssociation = false;}

    /**
     * This method is public as an implementation side effect, but should not be called directly.
     */
    public void endDocument() throws Exception {}

    /**
     * This method is public as an implementation side effect, but should not be called.
     */
    public void characters(String s) {}
}
