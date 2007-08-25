/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.bookmark;

import com.mucommander.auth.Credentials;
import com.mucommander.auth.CredentialsManager;
import com.mucommander.auth.MappedCredentials;
import com.mucommander.file.FileURL;
import com.mucommander.io.BackupInputStream;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.Locator;
import org.xml.sax.Attributes;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Hashtable;


/**
 * This class takes care of parsing the bookmarks XML file and adding parsed {@link Bookmark} instances to {@link BookmarkManager}.
 *
 * @author Maxence Bernard
 */
class BookmarkParser extends DefaultHandler implements BookmarkConstants {
	
    /** Variable used for XML parsing */
    private String bookmarkName;
    /** Variable used for XML parsing */
    private String bookmarkLocation;
    /** Variable used for XML parsing */
    private String characters;


    /**
     * Creates a new BookmarkParser instance.
     */
    BookmarkParser() {}

    /**
     * Parses the given XML bookmarks file. Should only be called by BookmarkManager.
     */
    void parse(File file) throws Exception {
        InputStream in;

        in = null;
        try {SAXParserFactory.newInstance().newSAXParser().parse(in = new BackupInputStream(file), this);}
        finally {
            if(in != null) {
                try {in.close();}
                catch(Exception e) {}
            }
        }
    }


    /* ------------------------ */
    /*  ContentHandler methods  */
    /* ------------------------ */

    /**
     * Method called when some PCDATA has been found in an XML node.
     */
    public void characters(char[] ch, int start, int length) {
        if(characters == null)
            characters = new String(ch, start, length);
        else
            characters += new String(ch, start, length);
    }

    /**
     * Notifies the parser that a new XML node has been found.
     */
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        this.characters = null;

        if(qName.equals(ELEMENT_BOOKMARK)) {
            // Reset parsing variables
            bookmarkName = null;
            bookmarkLocation = null;
        }
    }

    /**
     * Notifies the parser that an XML node has been closed.
     */
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if(qName.equals(ELEMENT_BOOKMARK)) {
            if(bookmarkName == null || bookmarkLocation == null) {
                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Missing value, bookmark ignored: name=" + bookmarkName + " location=" + bookmarkLocation);
                return;
            }

            // Add the new boomark to BookmarkManager's bookmark list
            BookmarkManager.addBookmark(new Bookmark(bookmarkName, bookmarkLocation));
        }
        else if(qName.equals(ELEMENT_NAME)) {
            bookmarkName = characters.trim();
        }
        else if(qName.equals(ELEMENT_LOCATION)) {
            bookmarkLocation = characters.trim();
        }
        // Note: url element has been deprecated in 0.8 beta3 but is still checked against for upward compatibility.
        else if(qName.equals(ELEMENT_URL)) {
            // Until early 0.8 beta3 nightly builds, credentials were stored directly in the bookmark's url.
            // Now bookmark locations are free of credentials, these are stored in a dedicated credentials file where
            // the password is encrypted.
            try {
                FileURL url = new FileURL(characters.trim());
                Credentials credentials = url.getCredentials();

                // If the URL contains credentials, import them into CredentialsManager and remove credentials
                // from the bookmark's location
                if(credentials!=null) {
                    CredentialsManager.addCredentials(new MappedCredentials(credentials, url, true));
                    bookmarkLocation = url.toString(false);
                }
                else {
                    bookmarkLocation = characters.trim();
                }
            }
            catch(MalformedURLException e) {
                bookmarkLocation = characters.trim();
            }
        }
    }
}
