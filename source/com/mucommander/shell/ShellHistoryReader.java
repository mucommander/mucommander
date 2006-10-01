package com.mucommander.shell;

import com.mucommander.xml.parser.*;
import java.util.*;
import java.io.*;

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
     * Reads shell history from the specified file.
     * @param file where to read the history from.
     */
    public static void read(File file) {
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(file);
            new Parser().parse(fin, new ShellHistoryReader(), "UTF-8");
        }
        catch(Exception e) {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Failed to load shell history: " + e);
        }
        finally {
            if(fin!=null)
                try { fin.close(); }
                catch(IOException e) {}
        }
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
        if(name.equals(ROOT_ELEMENT)) {
            if(status != STATUS_UNKNOWN)
                throw new IllegalStateException("Illegal declaration of element " + name);
            status = STATUS_ROOT;
        }

        // Command element declaration.
        else if(name.equals(COMMAND_ELEMENT)) {
            if(status != STATUS_ROOT)
                throw new IllegalStateException("Illegal declaration of element " + name);
            status = STATUS_COMMAND;
        }

        // Unknown element.
        else
            throw new IllegalArgumentException("Unknown XML element: " + name);
    }

    /**
     * Notifies the reader that the current element declaration is over.
     */
    public void endElement(String uri, String name) {
        // Root element finished.
        if(name.equals(ROOT_ELEMENT)) {
            if(status != STATUS_ROOT)
                throw new IllegalStateException("Illegal end of element " + name);
            status = STATUS_UNKNOWN;
        }

        // Command element finished.
        else if(name.equals(COMMAND_ELEMENT)) {
            if(status != STATUS_COMMAND)
                throw new IllegalStateException("Illegal end of element " + name);
            status = STATUS_ROOT;

            // Adds the current command to shell history.
            ShellHistoryManager.add(command.toString());
            command.setLength(0);

        }
        else
            throw new IllegalArgumentException("Unknown XML element: " + name);
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
