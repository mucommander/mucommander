package com.mucommander.command;

import com.mucommander.xml.writer.*;

import java.io.*;

/**
 * @author Nicolas Rinaudo
 */
public class CommandWriter implements CommandsXmlConstants, CommandBuilder {
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
    public CommandWriter(OutputStream stream) {out = new XmlWriter(stream);}



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
        attributes.add(ARGUMENT_ALIAS, command.getAlias());
        attributes.add(ARGUMENT_VALUE, command.getCommand());
        if(command.getType() == Command.SYSTEM_COMMAND)
            attributes.add(ARGUMENT_TYPE, VALUE_SYSTEM);
        else if(command.getType() == Command.INVISIBLE_COMMAND)
            attributes.add(ARGUMENT_TYPE, VALUE_INVISIBLE);

        // Writes the XML description.
        out.writeStandAloneElement(ELEMENT_COMMAND, attributes);
    }
}
