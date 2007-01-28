package com.mucommander.command;

import com.mucommander.xml.writer.*;

import java.io.*;

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

    /**
     * Writes the specified association's XML description.
     * @param mask    file name mask to use in the association.
     * @param command alias of the command to use in the associations.
     */
    public void addAssociation(String mask, String command) {
        addAssociation(mask, CommandAssociation.UNFILTERED, CommandAssociation.UNFILTERED, CommandAssociation.UNFILTERED, command);
    }

    public void addAssociation(String mask, int read, int write, int execute, String command) {
        XmlAttributes attributes;

        // Builds the XML description of the association.
        attributes = new XmlAttributes();
        if(mask != null)
            attributes.add(ARGUMENT_MASK, mask);
        if(read == CommandAssociation.YES)
            attributes.add(ARGUMENT_READABLE, VALUE_YES);
        else if(read == CommandAssociation.NO)
            attributes.add(ARGUMENT_READABLE, VALUE_NO);

        if(write == CommandAssociation.YES)
            attributes.add(ARGUMENT_WRITABLE, VALUE_YES);
        else if(write == CommandAssociation.NO)
            attributes.add(ARGUMENT_WRITABLE, VALUE_NO);

        if(execute == CommandAssociation.YES)
            attributes.add(ARGUMENT_EXECUTABLE, VALUE_YES);
        else if(execute == CommandAssociation.NO)
            attributes.add(ARGUMENT_EXECUTABLE, VALUE_NO);

        attributes.add(ARGUMENT_COMMAND, command);

        // Writes the XML description.
        out.writeStandAloneElement(ELEMENT_ASSOCIATION, attributes);
    }
}
