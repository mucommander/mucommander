package com.mucommander.bookmark;

import java.util.Hashtable;

import java.io.File;
import java.io.FileInputStream;

import com.muxml.ContentHandler;
import com.muxml.Parser;


/**
 * This class takes care of parsing the bookmarks XML file and feeding them to the BookmarkManager.
 *
 * @author Maxence Bernard
 */
class BookmarkParser implements ContentHandler {
	
	private String bookmarkName;
	private String bookmarkURL;
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
        new Parser().parse(new FileInputStream(file), this);
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

		if(name.equals("bookmark")) {
			bookmarkName = null;
			bookmarkURL = null;
		}
    }

    /**
     * Notifies the parser that an XML node has been closed.
     */
    public void endElement(String uri, String name) {
		if(name.equals("bookmark")) {
			if(com.mucommander.Debug.ON && (bookmarkName==null || bookmarkName.equals("") || bookmarkURL==null || bookmarkURL.equals("")))
				com.mucommander.Debug.trace("Missing value, bookmark ignored: name="+bookmarkName+" url="+bookmarkURL);
			else
				try {
					BookmarkManager.addBookmark(new Bookmark(bookmarkName, new com.mucommander.file.FileURL(bookmarkURL)));
				}
				catch(java.net.MalformedURLException e) {
					com.mucommander.Debug.trace("Invalid bookmark URL: "+bookmarkURL+", "+e);
				}
		}	
		else if(name.equals("name")) {
			bookmarkName = characters;
		}
		else if(name.equals("url")) {
			bookmarkURL = characters;
		}
    }

    public void endDocument() {}
    public void startDocument() {}


	/**
	 * Test method
	 */
	public static void main(String args[]) throws Exception {
		new BookmarkParser().parse(new File("/Users/maxence/Projects/mucommander/bookmarks.xml"));

		System.out.println("1- "+BookmarkManager.getBookmarks());
		
		BookmarkWriter.write(new File("/Users/maxence/Projects/mucommander/bookmarks2.xml"));
		new BookmarkParser().parse(new File("/Users/maxence/Projects/mucommander/bookmarks2.xml"));

		System.out.println("2- "+BookmarkManager.getBookmarks());
	}

}