package com.mucommander.bookmark;

import com.mucommander.xml.writer.XmlWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;


/**
 * This class provides a method to write bookmarks to an XML file.
 *
 * @author Maxence Bernard
 */
public class BookmarkWriter implements BookmarkConstants {

    /**
     * Writes the bookmarks XML file in the user's preferences folder.
     * This method should only be called by {@link BookmarkManager}.
     */
    static void write(OutputStream stream) throws IOException {
        XmlWriter out;
        Iterator  bookmarks;
        Bookmark  bookmark;

        out = new XmlWriter(stream);

        // Root element
        out.startElement(ELEMENT_ROOT);
        out.println();
        
        // Add muCommander version
        out.startElement(ELEMENT_VERSION);
        out.writeCData(com.mucommander.RuntimeConstants.VERSION);
        out.endElement(ELEMENT_VERSION);

        bookmarks = BookmarkManager.getBookmarks().iterator();
        while(bookmarks.hasNext()) {
            bookmark = (Bookmark)bookmarks.next();

            // Start bookmark element
            out.startElement(ELEMENT_BOOKMARK);
            out.println();

            // Write the bookmark's name
            out.startElement(ELEMENT_NAME);
            out.writeCData(bookmark.getName());
            out.endElement(ELEMENT_NAME);

            // Write the bookmark's location
            out.startElement(ELEMENT_LOCATION);
            out.writeCData(bookmark.getLocation());
            out.endElement(ELEMENT_LOCATION);

            // End bookmark element
            out.endElement(ELEMENT_BOOKMARK);
        }

        // End root element
        out.endElement(ELEMENT_ROOT);
    }
}
