package com.mucommander.bookmark;

import com.mucommander.io.BackupInputStream;
import com.mucommander.xml.parser.ContentHandler;
import com.mucommander.xml.parser.Parser;

import java.io.File;
import java.io.InputStream;
import java.util.Hashtable;


/**
 * This class takes care of parsing the bookmarks XML file and passing Bookmark instance to BookmarkManager.
 *
 * @author Maxence Bernard
 */
class BookmarkParser implements ContentHandler, BookmarkConstants {
	
    /** Variable used for XML parsing */
    private String bookmarkName;
    /** Variable used for XML parsing */
    private String bookmarkLocation;
//    /** Variable used for XML parsing */
//    private String bookmarkPassword;
    /** Variable used for XML parsing */
    private String characters;

//    private String encryptionMethod;


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
            bookmarkName = null;
            bookmarkLocation = null;
        }
//        // Root element, specifies which encoding is used for passwords
//        else if(name.equals(ELEMENT_ROOT)) {
//            encryptionMethod = (String)attValues.get("encryption");
//        }
    }

    /**
     * Notifies the parser that an XML node has been closed.
     */
    public void endElement(String uri, String name) {
        if(name.equals(ELEMENT_BOOKMARK)) {
            if(bookmarkName==null || bookmarkName.equals("") || bookmarkLocation ==null || bookmarkLocation.equals("")) {
                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Missing value, bookmark ignored: name="+bookmarkName+" url="+ bookmarkLocation);
            }
            else {
//                try {
//                    FileURL url = new com.mucommander.file.FileURL(bookmarkURL);
//                    if(bookmarkPassword!=null)
//                        url.setPassword(bookmarkPassword);
//
//                    BookmarkManager.addBookmark(new Bookmark(bookmarkName, url));
//                }
//                catch(java.net.MalformedURLException e) {
//                    if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Invalid bookmark URL: "+bookmarkURL+", "+e);
//                }

                BookmarkManager.addBookmark(new Bookmark(bookmarkName, bookmarkLocation));
            }
        }	
        else if(name.equals(ELEMENT_NAME)) {
            bookmarkName = characters;
        }
        else if(name.equals(ELEMENT_LOCATION) || name.equals(ELEMENT_URL)) {
            bookmarkLocation = characters;
        }
//        else if(name.equals(ELEMENT_PASSWORD)) {
//            bookmarkPassword = characters;
//            if(BookmarkWriter.WEAK_ENCRYPTION_METHOD.equals(encryptionMethod)) {
//                try {
//                    bookmarkPassword = XORCipher.decodeXORBase64(bookmarkPassword);
//                }
//                catch(IOException e) {
//                    if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Error while decoding password: "+bookmarkPassword+", "+e);
//                }
//            }
//        }
    }

    public void endDocument() {}
    public void startDocument() {}
}
