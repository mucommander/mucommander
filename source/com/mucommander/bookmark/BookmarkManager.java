
package com.mucommander.bookmark;

import java.util.Vector;

import java.io.File;
import java.io.IOException;

/**
 * This class manages boomarks and provides add/get/remove methods.
 * Bookmarks are initially loaded from a file, and automatically saved when a bookmark has been added or removed.
 *
 * @author Maxence Bernard
 */
public class BookmarkManager {

    /** Name of the bookmarks file */
    private static final String BOOKMARKS_FILE = "bookmarks.xml";

	private static Vector bookmarks = new Vector();


	private static File getBookmarksFile() {
		return new File(com.mucommander.PlatformManager.getPreferencesFolder(), BOOKMARKS_FILE);
	}

	public static void loadBookmarks() {
		File bookmarksFile = getBookmarksFile();
		try {
			if(bookmarksFile.exists()) {
				if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Found bookmarks file: "+bookmarksFile.getAbsolutePath());
				new BookmarkParser().parse(bookmarksFile);
				if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Bookmarks file loaded.");
			}
			else {
				if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("No bookmarks file found at "+bookmarksFile.getAbsolutePath());			
			}
		}
		catch(Exception e) {
			// Notify user that something went wrong while parsing the bookmarks file
			System.out.println("An error occurred while loading bookmarks file "+bookmarksFile.getAbsolutePath()+": "+e);			
		}
	}
	
	public static void writeBookmarks() {
		File bookmarksFile = getBookmarksFile();
		try {
			BookmarkWriter.write(bookmarksFile);
			if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Bookmarks file saved successfully.");
		}
		catch(IOException e) {
			// Notify user that something went wrong while writing the bookmarks file
			System.out.println("An error occurred while writing bookmarks file "+bookmarksFile.getAbsolutePath()+": "+e);			
		}
	}


	/**
	 * Returns a Vector of bookmarks.
	 */
	public static Vector getBookmarks() {
		return bookmarks;
	}

	
	/**
	 * Adds a bookmark.
	 */
	public static void addBookmark(Bookmark bm) {
		bookmarks.add(bm);
	}


	/**
	 * Removes a bookmark.
	 */
	public static void removeBookmark(Bookmark bm) {
		bookmarks.remove(bm);
	}
}