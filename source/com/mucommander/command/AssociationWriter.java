package com.mucommander.command;

import com.mucommander.xml.writer.*;

import java.io.*;

/**
 * Class used to write custom command associations to an XML stream.
 * @author Nicolas Rinaudo
 */
public class AssociationWriter implements XmlConstants, AssociationBuilder {
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
     * Writes the specified command's XML description.
     * @param command command that should be written.
     */
    public void addCommand(Command command) {
        XmlAttributes attributes;

        // Builds the XML description of the command.
        attributes = new XmlAttributes();
        attributes.add(ARGUMENT_COMMAND_ALIAS, command.getAlias());
        attributes.add(ARGUMENT_COMMAND_VALUE, command.getCommand());

        // Writes the XML description.
        out.writeStandAloneElement(ELEMENT_COMMAND, attributes);
    }

    /**
     * Writes the specified association's XML description.
     * @param mask    file name mask to use in the association.
     * @param command alias of the command to use in the associations.
     */
    public void addAssociation(String mask, String command) {
        XmlAttributes attributes;

        // Builds the XML description of the association.
        attributes = new XmlAttributes();
        attributes.add(ARGUMENT_ASSOCIATION_MASK, mask);
        attributes.add(ARGUMENT_ASSOCIATION_COMMAND, command);

        // Writes the XML description.
        out.writeStandAloneElement(ELEMENT_ASSOCIATION, attributes);
    }
}
