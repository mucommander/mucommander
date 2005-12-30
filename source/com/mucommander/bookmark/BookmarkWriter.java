package com.mucommander.bookmark;

import java.io.*;
import java.util.Vector;


/**
 * This class provides a method to write bookmarks to a file in XML format.
 *
 * @author Maxence Bernard
 */
public class BookmarkWriter {

	private final static String[] XML_ENTITIES_CHAR = new String[]{"&", "\"" , "'", "<", ">"};		// & must be first, otherwise it will recurse on converted character entities!
	private final static String[] XML_ENTITIES_REP = new String[]{"&amp;", "&quot;", "&apos;", "&lt;", "&gt;"};
	

	/**
	 * Writes the bookmarks XML file in the user's preferences folder.
	 */
	static void write(File file) throws IOException {
		// Use UTF-8 encoding
		PrintStream ps = new PrintStream(new FileOutputStream(file), false, "UTF-8");
		
		// Write XML header
		ps.println("<?xml version=\"1.0\" encoding=\"utf-8\" ?>");

		// Start root element
		ps.println("<bookmarks>");

		// Write muCommander version
		ps.println("\t<version>"+encodeEntities(com.mucommander.Launcher.MUCOMMANDER_VERSION)+"</version>");		

		// Write bookmarks
		Vector bookmarks = BookmarkManager.getBookmarks();
		int nbBookmarks = bookmarks.size();
		for(int i=0; i<nbBookmarks; i++) {
			Bookmark bookmark = (Bookmark)bookmarks.elementAt(i);
			ps.println("\t<bookmark>");
			ps.println("\t\t<name>"+encodeEntities(bookmark.getName())+"</name>");
			ps.println("\t\t<url>"+encodeEntities(bookmark.getURL().getStringRep(true))+"</url>");
			ps.println("\t</bookmark>");
		}

		// End root element
		ps.println("</bookmarks>");
	}
	
	
	/**
	 * Properly encodes XML character entities and returns the encoded string.
	 */
	private static String encodeEntities(String s) {
		for(int i=0; i<XML_ENTITIES_CHAR.length; i++) {
			int pos = 0;
			while((pos=s.indexOf(XML_ENTITIES_CHAR[i], pos))!=-1) {
				s = s.substring(0, pos)+XML_ENTITIES_REP[i]+(pos==s.length()-1?"":s.substring(pos+1, s.length()));
				pos = pos + XML_ENTITIES_REP[i].length();
			}
		}
		return s;
	}
}