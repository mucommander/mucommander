/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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

package com.mucommander.shell;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;

/**
 * Parses XML shell history files and populates the {@link com.mucommander.shell.ShellHistoryManager}.
 * @author Nicolas Rinaudo
 */
class ShellHistoryReader extends DefaultHandler implements ShellHistoryConstants {
    // - Reader statuses -----------------------------------------------------
    // -----------------------------------------------------------------------
    /** Parsing hasn't started. */
    private static final int STATUS_UNKNOWN = 0;
    /** Currently parsing the root tag. */
    private static final int STATUS_ROOT    = 1;
    /** Currently parsing a command tag. */
    private static final int STATUS_COMMAND = 2;



    // - Instance fields -----------------------------------------------------
    // -----------------------------------------------------------------------
    /** Reader's current status. */
    private int          status;
    /** Buffer for the current command. */
    private StringBuilder command;
    /** muCommander version that was used to write the shell history file */
    private String version;


    // - Initialisation ------------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Creates a new shell history reader.
     */
    private ShellHistoryReader() {
        command = new StringBuilder();
        status = STATUS_UNKNOWN;
    }

    /**
     * Returns the muCommander version that was used to write the shell history file, <code>null</code> if it is unknown.
     * <p>
     * Note: the version attribute was introduced in muCommander 0.8.4.
     * </p>
     *
     * @return the muCommander version that was used to write the shell history file, <code>null</code> if it is unknown.
     */
    public String getVersion() {
        return version;
    }


    // - XML interaction -----------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Reads shell history from the specified input stream.
     * @param in where to read the history from.
     */
    public static void read(InputStream in) throws Exception {SAXParserFactory.newInstance().newSAXParser().parse(in, new ShellHistoryReader());}

    /**
     * Notifies the reader that CDATA has been encountered.
     */
    @Override
    public void characters(char[] ch, int start, int length) {
        if(status == STATUS_COMMAND)
            command.append(ch, start, length);
    }

    /**
     * Notifies the reader that a new XML element is starting.
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        // Root element declaration.
        if(qName.equals(ROOT_ELEMENT) && (status == STATUS_UNKNOWN)) {
            status = STATUS_ROOT;
            version = attributes.getValue(ATTRIBUTE_VERSION);
        }

        // Command element declaration.
        else if(qName.equals(COMMAND_ELEMENT) && status == STATUS_ROOT)
            status = STATUS_COMMAND;
    }

    /**
     * Notifies the reader that the current element declaration is over.
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        // Root element finished.
        if(qName.equals(ROOT_ELEMENT) && (status == STATUS_ROOT))
            status = STATUS_UNKNOWN;

        // Command element finished.
        else if(qName.equals(COMMAND_ELEMENT) && (status == STATUS_COMMAND)) {
            status = STATUS_ROOT;

            // Adds the current command to shell history.
            ShellHistoryManager.add(command.toString().trim());
            command.setLength(0);

        }
    }
}
