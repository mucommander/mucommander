package com.mucommander.bookmark;

import com.mucommander.xml.writer.XmlWriter;

import java.io.IOException;
import java.io.OutputStream;
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
    public static void write(OutputStream stream) throws IOException {
        XmlWriter out;
        Iterator  bookmarks;
        Bookmark  bookmark;

        out = new XmlWriter(stream);

//        // Root element, add the encryption method used
//        XmlAttributes attributes = new XmlAttributes();
//        attributes.add(ATTRIBUTE_ENCRYPTION, WEAK_ENCRYPTION_METHOD);
//        out.startElement(ELEMENT_ROOT, attributes);
//        out.println();

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

            // Write bookmark's name
            out.startElement(ELEMENT_NAME);
            out.writeCData(bookmark.getName());
            out.endElement(ELEMENT_NAME);

            // Write bookmark's URL
            out.startElement(ELEMENT_LOCATION);

////                FileURL url = bookmark.getURL();
//            // Try to resolve the location as a FileURL
//            FileURL url = bookmark.getLocationAsURL();
//
//            // The bookmark's location designates a valid URL
//            if(url!=null) {
//                String password = url.getPassword();
//
//                // If URL contains a password, encrypt it in a separate element
//                if(password!=null) {
//                    // Exclude password from URL
//                    url.setPassword(null);
//                    // Note: URL may also contain a login which will be stored as clear text
//                    out.writeCData(url.getStringRep(true));
//                    url.setPassword(password);
//                    out.endElement(ELEMENT_LOCATION);
//
//                    // Write encrypted password
//                    out.startElement(ELEMENT_PASSWORD);
//                    out.writeCData(XORCipher.encodeXORBase64(password));
//                    out.endElement(ELEMENT_PASSWORD);
//                }
//                // URL does not contain a password, but may contain a login which will be stored as clear text
//                else {
//                    out.writeCData(url.getStringRep(true));
//                    out.endElement(ELEMENT_LOCATION);
//                }
//            }
//            // The bookmark's location does not designate a URL but a path (or anything else as it's user-entered)
//            else {
                out.writeCData(bookmark.getLocation());
                out.endElement(ELEMENT_LOCATION);
//            }

            // End bookmark element
            out.endElement(ELEMENT_BOOKMARK);
        }

        // End root element
        out.endElement(ELEMENT_ROOT);
    }
}
