package com.mucommander.bookmark;

import java.io.*;
import java.util.*;
import com.mucommander.xml.XmlWriter;


/**
 * This class provides a method to write bookmarks to a file in XML format.
 *
 * @author Maxence Bernard
 */
public class BookmarkWriter {
    private static final String ELEMENT_ROOT     = "bookmarks";
    private static final String ELEMENT_VERSION  = "version";
    private static final String ELEMENT_BOOKMARK = "bookmark";
    private static final String ELEMENT_NAME     = "name";
    private static final String ELEMENT_URL      = "url";


    /**
     * Writes the bookmarks XML file in the user's preferences folder.
     */
    public static void write(File file) throws IOException {
        XmlWriter out;
        Iterator  bookmarks;
        Bookmark  bookmark;

        out = null;

        try {
            out = new XmlWriter(file);

            // Root element.
            out.startElement(ELEMENT_ROOT);
            out.println();

            // muCommander version.
            out.startElement(ELEMENT_VERSION);
            out.writeCData(com.mucommander.RuntimeConstants.VERSION);
            out.endElement(ELEMENT_VERSION);

            bookmarks = BookmarkManager.getBookmarks().iterator();
            while(bookmarks.hasNext()) {
                bookmark = (Bookmark)bookmarks.next();

                out.startElement(ELEMENT_BOOKMARK);
                out.println();
                out.startElement(ELEMENT_NAME);
                out.writeCData(bookmark.getName());
                out.endElement(ELEMENT_NAME);
                out.startElement(ELEMENT_URL);
                out.writeCData(bookmark.getURL().getStringRep(true));
                out.endElement(ELEMENT_URL);
                out.endElement(ELEMENT_BOOKMARK);
            }

            // End root element
            out.endElement(ELEMENT_ROOT);
        }
        finally {
            // Close stream, IOException is thrown under Java 1.3 but no longer under 1.4 and up,
            // so we catch Exception instead of IOException to let javac compile without bitching
            // about the exception never being thrown
            if(out != null) {
                try {out.close();}
                catch(Exception e) {}
            }
        }
    }
}
