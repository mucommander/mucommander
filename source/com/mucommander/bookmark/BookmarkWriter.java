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
    private static final String TAG_ROOT     = "bookmarks";
    private static final String TAG_VERSION  = "version";
    private static final String TAG_BOOKMARK = "bookmark";
    private static final String TAG_NAME     = "name";
    private static final String TAG_URL      = "url";


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
            out.openTag(TAG_ROOT);
            out.println();

            // muCommander version.
            out.openTag(TAG_VERSION);
            out.writeCData(com.mucommander.RuntimeConstants.VERSION);
            out.closeTag(TAG_VERSION);

            bookmarks = BookmarkManager.getBookmarks().iterator();
            while(bookmarks.hasNext()) {
                bookmark = (Bookmark)bookmarks.next();

                out.openTag(TAG_BOOKMARK);
                out.println();
                out.openTag(TAG_NAME);
                out.writeCData(bookmark.getName());
                out.closeTag(TAG_NAME);
                out.openTag(TAG_URL);
                out.writeCData(bookmark.getURL().getStringRep(true));
                out.closeTag(TAG_URL);
                out.closeTag(TAG_BOOKMARK);
            }

            // End root element
            out.closeTag(TAG_ROOT);
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
