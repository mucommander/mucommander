/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (c) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with muCommander; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.mucommander.shell;

import com.mucommander.Debug;
import com.mucommander.xml.parser.ContentHandler;
import com.mucommander.xml.parser.Parser;

import java.io.InputStream;
import java.util.Hashtable;

/**
 * Parses XML shell history files and populates the {@link com.mucommander.shell.ShellHistoryManager}.
 * @author Nicolas Rinaudo
 */
class ShellHistoryReader implements ShellHistoryConstants, ContentHandler {
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
    private StringBuffer command;



    // - Initialisation ------------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Creates a new shell history reader.
     */
    private ShellHistoryReader() {
        command = new StringBuffer();
        status = STATUS_UNKNOWN;
    }



    // - XML interaction -----------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Reads shell history from the specified input stream.
     * @param in where to read the history from.
     */
    public static void read(InputStream in) throws Exception {
        if(Debug.ON) Debug.trace("Starting to load shell history.");
        new Parser().parse(in, new ShellHistoryReader(), "UTF-8");
        if(Debug.ON) Debug.trace("Shell history succesfully loaded.");
    }

    /**
     * Notifies the reader that CDATA has been encountered.
     */
    public void characters(String s) {
        if(status == STATUS_COMMAND)
            command.append(s.trim());
    }

    /**
     * Notifies the reader that a new XML element is starting.
     */
    public void startElement(String uri, String name, Hashtable attValues, Hashtable attURIs) {
        // Root element declaration.
        if(name.equals(ROOT_ELEMENT) && (status == STATUS_UNKNOWN))
            status = STATUS_ROOT;

        // Command element declaration.
        else if(name.equals(COMMAND_ELEMENT) && status == STATUS_ROOT)
            status = STATUS_COMMAND;

        // Unknown element.
        else if(Debug.ON) Debug.trace("Unexpected start of element " + name + ", ignoring.");
    }

    /**
     * Notifies the reader that the current element declaration is over.
     */
    public void endElement(String uri, String name) {
        // Root element finished.
        if(name.equals(ROOT_ELEMENT) && (status == STATUS_ROOT))
            status = STATUS_UNKNOWN;

        // Command element finished.
        else if(name.equals(COMMAND_ELEMENT) && (status == STATUS_COMMAND)) {
            status = STATUS_ROOT;

            // Adds the current command to shell history.
            ShellHistoryManager.add(command.toString());
            command.setLength(0);

        }
        else if(Debug.ON) Debug.trace("Unexpected end of element " + name + ", ignoring.");
    }

    /**
     * Not used.
     */
    public void startDocument() {}

    /**
     * Not used.
     */
    public void endDocument() {}
}
