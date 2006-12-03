package com.mucommander.bookmark;

import com.mucommander.io.BackupInputStream;
import com.mucommander.xml.parser.ContentHandler;
import com.mucommander.xml.parser.Parser;
import com.mucommander.file.FileURL;
import com.mucommander.auth.Credentials;
import com.mucommander.auth.CredentialsManager;
import com.mucommander.auth.MappedCredentials;

import java.io.File;
import java.io.InputStream;
import java.util.Hashtable;
import java.net.MalformedURLException;


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
        else if(name.equals(ELEMENT_LOCATION)) {
            bookmarkLocation = characters;
        }
        // Note: url element has been deprecated in 0.8 beta3 but is still checked against for upward compatibility.
        else if(name.equals(ELEMENT_URL)) {
            // Until early 0.8 beta3 nightly builds, credentials were stored directly in the bookmark's url.
            // Now bookmark locations are free of credentials, these are stored in a dedicated credentials file where
            // the password is encrypted.
            try {
                FileURL url = new FileURL(characters);
                Credentials credentials = url.getCredentials();

                // If the URL contains credentials, import them into CredentialsManager and remove credentials
                // from the bookmark's location
                if(credentials!=null) {
                    CredentialsManager.addCredentials(new MappedCredentials(credentials, url, true));
                    bookmarkLocation = url.getStringRep(false);
                }
                else {
                    bookmarkLocation = characters;
                }
            }
            catch(MalformedURLException e) {
                bookmarkLocation = characters;
            }
        }
    }

    public void endDocument() {}
    public void startDocument() {}
}
