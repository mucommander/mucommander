package com.mucommander.bookmark;

import java.util.Vector;

import com.muxml.ContentHandler;
import com.muxml.Parser;


/**
 * This class takes care of parsing the bookmarks XML file and returning a Vector of loaded bookmarks.
 *
 * @author Maxence Bernard
 */
class BookmarkParser implements ContentHandler {
	
	private Vector bookmarks;

	BookmarkParser(AbstractFile ) {
		boookmarks = new Vector();
	}
	
	Vector getBookmarks() {
		return bookmarks;
	}

    /* ---------------- */
    /*  Parsing methods */
    /* ---------------- */

    public void parse(String file) throws IOException {
        new Parser.parse(new FileInputStream(file), this);
    }

    /**
     * Method called when some PCDATA has been found in an XML node.
     */
    public void characters(String s) {
        String value;

        value = s.trim();
        if(!value.equals("")) {
            inNode = false;
            if(buffer != null)
                builder.addLeaf(buffer, value);
            buffer = null;
        }
    }

    /**
     * Notifies the parser that a new XML node has been found.
     */
    public void startElement(String uri, String name, Hashtable attValues, Hashtable attURIs) {
        inNode = true;
        if(buffer != null) {
            node = buffer;
            builder.addNode(buffer);
        }
        buffer = name;
    }

    /**
     * Notifies the parser that an XML node has been closed.
     */
    public void endElement(String uri, String name) {
        if(inNode)
            builder.closeNode(name);
        else {
            //System.out.println("defineNode(" + node + ")");
            node = buffer;
            inNode = true;
        }
    }

    public void endDocument() {}
    public void startDocument() {}
}