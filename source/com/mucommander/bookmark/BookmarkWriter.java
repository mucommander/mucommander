package com.mucommander.bookmark;

import com.mucommander.xml.writer.XmlWriter;
import com.mucommander.xml.writer.XmlAttributes;
import com.mucommander.file.FileURL;
import com.mucommander.io.BackupOutputStream;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;


/**
 * This class provides a method to write bookmarks to a file in XML format.
 *
 * @author Maxence Bernard
 */
public class BookmarkWriter implements BookmarkConstants {

    /**
     * Writes the bookmarks XML file in the user's preferences folder.
     */
    public static void write(File file) throws IOException {
        XmlWriter out;
        Iterator  bookmarks;
        Bookmark  bookmark;

        out = null;

        try {
            out = new XmlWriter(new BackupOutputStream(file));

            // Root element, add the encryption method used
            XmlAttributes attributes = new XmlAttributes();
            attributes.add(ATTRIBUTE_ENCRYPTION, WEAK_ENCRYPTION_METHOD);
            out.startElement(ELEMENT_ROOT, attributes);
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

                // Write bookmark's name
                out.startElement(ELEMENT_NAME);
                out.writeCData(bookmark.getName());
                out.endElement(ELEMENT_NAME);

                // Write bookmark's URL
                out.startElement(ELEMENT_URL);
                FileURL url = bookmark.getURL();
                String password = url.getPassword();

                // If URL contains a password, encrypt it in a separate element
                if(password!=null) {
                    // Exclude password from URL
                    url.setPassword(null);
                    // Note: URL may contain a login stored as clear text
                    out.writeCData(url.getStringRep(true));
                    url.setPassword(password);
                    out.endElement(ELEMENT_URL);

                    // Write encrypted password
                    out.startElement(ELEMENT_PASSWORD);
                    out.writeCData(XORCipher.encodeXORBase64(password));
                    out.endElement(ELEMENT_PASSWORD);
                }
                else {
                    // Note: URL may contain a login stored as clear text
                    out.writeCData(url.getStringRep(true));
                    out.endElement(ELEMENT_URL);
                }

                // End bookmark element
                out.endElement(ELEMENT_BOOKMARK);
            }

            // End root element
            out.endElement(ELEMENT_ROOT);

            // Close XMLWriter
            out.close();
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
