package com.mucommander.bookmark;

import com.mucommander.io.BackupInputStream;
import com.mucommander.xml.parser.ContentHandler;
import com.mucommander.xml.parser.Parser;

import java.io.File;
import java.io.InputStream;
import java.util.Hashtable;


/**
 * This class takes care of parsing the bookmarks XML file and adding parsed {@link Bookmark} instances to {@link BookmarkManager}.
 *
 * @author Maxence Bernard
 */
class BookmarkParser implements ContentHandler, BookmarkConstants {
	
    /** Variable used for XML parsing */
    private String bookmarkName;
    /** Variable used for XML parsing */
    private String bookmarkLocation;
    /** Variable used for XML parsing */
    private String characters;


    /**
     * Creates a new BookmarkParser instance.
     */
    BookmarkParser() {
    }

    /**
     * Parses the given XML bookmarks file. Should only be called by BookmarkManager.
     */
    void parse(File file) throws Exception {
        InputStream fin = new BackupInputStream(file);
        new Parser().parse(fin, this, "UTF-8");
        fin.close();
    }


    /* ------------------------ */
    /*  ContentHandler methods  */
    /* ------------------------ */

    /**
     * Method called when some PCDATA has been found in an XML node.
     */
    public void characters(String s) {
        this.characters = s.trim();
    }

    /**
     * Notifies the parser that a new XML node has been found.
     */
    public void startElement(String uri, String name, Hashtable attValues, Hashtable attURIs) {
        this.characters = null;

        if(name.equals(ELEMENT_BOOKMARK)) {
            // Reset parsing variables
            bookmarkName = null;
            bookmarkLocation = null;
        }
    }

    /**
     * Notifies the parser that an XML node has been closed.
     */
    public void endElement(String uri, String name) {
        if(name.equals(ELEMENT_BOOKMARK)) {
            if(bookmarkName==null || bookmarkLocation==null) {
                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Missing value, bookmark ignored: name="+bookmarkName+" location="+ bookmarkLocation);
                return;
            }

            // Add the new boomark to BookmarkManager's bookmark list
            BookmarkManager.addBookmark(new Bookmark(bookmarkName, bookmarkLocation));
        }
        else if(name.equals(ELEMENT_NAME)) {
            bookmarkName = characters;
        }
        // Note: url element has been deprecated but is still checked against for upward compatibility
        else if(name.equals(ELEMENT_LOCATION) || name.equals(ELEMENT_URL)) {
            bookmarkLocation = characters;
        }
    }

    public void endDocument() {}
    public void startDocument() {}
}
