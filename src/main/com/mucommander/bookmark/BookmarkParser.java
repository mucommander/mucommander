/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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

import com.mucommander.auth.CredentialsManager;
import com.mucommander.auth.CredentialsMapping;
import com.mucommander.commons.file.Credentials;
import com.mucommander.commons.file.FileURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.net.MalformedURLException;


/**
 * This class takes care of parsing the bookmarks XML file and adding parsed {@link Bookmark} instances to {@link BookmarkManager}.
 *
 * @author Maxence Bernard
 */
class BookmarkParser extends DefaultHandler implements BookmarkConstants {
	private static final Logger LOGGER = LoggerFactory.getLogger(BookmarkParser.class);
	
    /** Variable used for XML parsing */
    private String bookmarkName;
    /** Variable used for XML parsing */
    private String bookmarkLocation;
    /** Variable used for XML parsing */
    private StringBuilder characters;
    /** Receives bookmarks events. */
    private BookmarkBuilder builder;
    /** muCommander version that was used to write the bookmarks file */
    private String version;


    /**
     * Creates a new BookmarkParser instance.
     */
    public BookmarkParser() {}

    /**
     * Parses the given XML bookmarks file. Should only be called by BookmarkManager.
     */
    void parse(InputStream in, BookmarkBuilder builder) throws Exception {
        this.builder = builder;
        characters   = new StringBuilder();
        SAXParserFactory.newInstance().newSAXParser().parse(in, this);
    }

    /**
     * Returns the muCommander version that was used to write the bookmarks file, <code>null</code> if it is unknown.
     * <p>
     * Note: the version attribute was introduced in muCommander 0.8.4.
     * </p>
     *
     * @return the muCommander version that was used to write the bookmarks file, <code>null</code> if it is unknown.
     */
    public String getVersion() {
        return version;
    }


    /* ------------------------ */
    /*  ContentHandler methods  */
    /* ------------------------ */

    @Override
    public void startDocument() throws SAXException {
        try {builder.startBookmarks();}
        catch(BookmarkException e) {throw new SAXException(e);}
    }

    @Override
    public void endDocument() throws SAXException {
        try {builder.endBookmarks();}
        catch(BookmarkException e) {throw new SAXException(e);}
    }

    /**
     * Method called when some PCDATA has been found in an XML node.
     */
    @Override
    public void characters(char[] ch, int start, int length) {characters.append(ch, start, length);}

    /**
     * Notifies the parser that a new XML node has been found.
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        characters.setLength(0);

        if(qName.equals(ELEMENT_ROOT)) {
            version = attributes.getValue(ATTRIBUTE_VERSION);
        }
        else if(qName.equals(ELEMENT_BOOKMARK)) {
            // Reset parsing variables
            bookmarkName = null;
            bookmarkLocation = null;
        }
    }

    /**
     * Notifies the parser that an XML node has been closed.
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if(qName.equals(ELEMENT_BOOKMARK)) {
            if(bookmarkName == null || bookmarkLocation == null) {
                LOGGER.info("Missing value, bookmark ignored: name=" + bookmarkName + " location=" + bookmarkLocation);
                return;
            }

            try {builder.addBookmark(bookmarkName, bookmarkLocation);}
            catch(BookmarkException e) {throw new SAXException(e);}
        }
        else if(qName.equals(ELEMENT_NAME)) {
            bookmarkName = characters.toString().trim();
        }
        else if(qName.equals(ELEMENT_LOCATION)) {
            bookmarkLocation = characters.toString().trim();
        }
        // Note: url element has been deprecated in 0.8 beta3 but is still checked against for upward compatibility.
        else if(qName.equals(ELEMENT_URL)) {
            // Until early 0.8 beta3 nightly builds, credentials were stored directly in the bookmark's url.
            // Now bookmark locations are free of credentials, these are stored in a dedicated credentials file where
            // the password is encrypted.
            try {
                FileURL url = FileURL.getFileURL(characters.toString().trim());
                Credentials credentials = url.getCredentials();

                // If the URL contains credentials, import them into CredentialsManager and remove credentials
                // from the bookmark's location
                if(credentials!=null) {
                    CredentialsManager.addCredentials(new CredentialsMapping(credentials, url, true));
                    bookmarkLocation = url.toString(false);
                }
                else {
                    bookmarkLocation = characters.toString().trim();
                }
            }
            catch(MalformedURLException e) {
                bookmarkLocation = characters.toString().trim();
            }
        }
    }
}
