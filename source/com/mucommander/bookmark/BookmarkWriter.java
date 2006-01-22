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
		PrintWriter pw = null;
		try {
			// Use UTF-8 encoding
	//		PrintStream ps = new PrintStream(new OutputStreamWriter(new FileOutputStream(file), false, "UTF-8"));
			pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
			
			// Write XML header
			pw.println("<?xml version=\"1.0\" encoding=\"utf-8\" ?>");

			// Start root element
			pw.println("<bookmarks>");

			// Write muCommander version
			pw.println("\t<version>"+encodeEntities(com.mucommander.Launcher.MUCOMMANDER_VERSION)+"</version>");		

			// Write bookmarks
			Vector bookmarks = BookmarkManager.getBookmarks();
			int nbBookmarks = bookmarks.size();
			for(int i=0; i<nbBookmarks; i++) {
				Bookmark bookmark = (Bookmark)bookmarks.elementAt(i);
				pw.println("\t<bookmark>");
				pw.println("\t\t<name>"+encodeEntities(bookmark.getName())+"</name>");
				pw.println("\t\t<url>"+encodeEntities(bookmark.getURL().getStringRep(true))+"</url>");
				pw.println("\t</bookmark>");
			}

			// End root element
			pw.println("</bookmarks>");
		}
		catch(IOException e) {
			// Rethrow exception
			throw e;
		}
		finally {
			// Close stream, IOException is thrown under Java 1.3 but no longer under 1.4 and up,
			// so we catch Exception instead of IOException to let javac compile without bitching
			// about the exception never being thrown
			if(pw!=null)
				try { pw.close(); } catch(Exception e2) {}
		}
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