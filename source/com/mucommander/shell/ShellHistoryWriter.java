package com.mucommander.shell;

import com.mucommander.xml.writer.XmlWriter;

import java.io.OutputStream;
import java.util.Iterator;

/**
 * Used to save the content of the {@link com.mucommander.shell.ShellHistoryManager} to a file.
 * @author Nicolas Rinaudo
 */
class ShellHistoryWriter implements ShellHistoryConstants {
    /**
     * Writes the content of the {@link com.mucommander.shell.ShellHistoryManager} to the specified output stream.
     * @param stream where to save the shell history.
     */
    public static void write(OutputStream stream) {
        Iterator  history; // Iterator on the shell history.
        XmlWriter out;     // Where to write the shell history to.

        // Initialises writing.
        history = ShellHistoryManager.getHistoryIterator();

        try {
            // Opens the file for writing.
            out = new XmlWriter(stream);

            // Writes the content of the shell history.
            out.startElement(ROOT_ELEMENT);
            out.println();
            while(history.hasNext()) {
                out.startElement(COMMAND_ELEMENT);
                out.writeCData(history.next().toString());
                out.endElement(COMMAND_ELEMENT);
            }
            out.endElement(ROOT_ELEMENT);
        }
        catch(Exception e) {if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Failed to write shell history: " + e);}
    }
}
