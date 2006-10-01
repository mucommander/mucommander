package com.mucommander.shell;

import java.io.*;
import java.util.*;
import com.mucommander.xml.writer.*;

/**
 * Used to save the content of the {@link com.mucommander.shell.ShellHistoryManager} to a file.
 * @author Nicolas Rinaudo
 */
class ShellHistoryWriter implements ShellHistoryConstants {
    /**
     * Writes the content of the {@link com.mucommander.shell.ShellHistoryManager} to the specified file.
     * @param file where to save the shell history.
     */
    public static void write(File file) {
        Iterator  history;
        XmlWriter out;

        // Initialises writing.
        out     = null;
        history = ShellHistoryManager.getHistoryIterator();

        try {
            // Opens the file for writing.
            out = new XmlWriter(file);

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

        // Cleans up.
        finally {
            if(out != null) {
                try {out.close();}
                catch(Exception e) {}
            }
        }
    }
}
