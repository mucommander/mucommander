
package com.mucommander.bookmark;

import java.util.Vector;

import java.io.File;
import java.io.IOException;

import com.mucommander.PlatformManager;

/**
 * This class manages boomarks and provides add/get/remove methods.
 *
 * <p>Bookmarks are initially loaded from a file on startup, and automatically saved when a bookmark has been added or removed.
 *
 * @author Maxence Bernard
 */
public class BookmarkManager {

	/** Bookmark file name */
    private static final String BOOKMARKS_FILENAME = "bookmarks.xml";

	/** Bookmark instances */
	private static Vector bookmarks = new Vector();

	/**
	 * Return a java.io.File instance that points to the bookmarks file location.
	 */
	private static File getBookmarksFile() {
		return new File(PlatformManager.getPreferencesFolder(), BOOKMARKS_FILENAME);
	}

	
	/**
	 * Tries to load bookmarks from the bookmarks file if it exists, and reports any error that occur during parsing
	 * to the standard output. Does nothing if the bookmarks file doesn't exist.
	 */
	public static void loadBookmarks() {
		File bookmarksFile = getBookmarksFile();
		try {
			if(bookmarksFile.exists()) {
				if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Found bookmarks file: "+bookmarksFile.getAbsolutePath());
				// Parse the bookmarks file
				new BookmarkParser().parse(bookmarksFile);
				if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Bookmarks file loaded.");
			}
			else {
				if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("No bookmarks file found at "+bookmarksFile.getAbsolutePath());			
			}
		}
		catch(Exception e) {
			// Report on the standard output that something went wrong while parsing the bookmarks file
			// as this shouldn't normally happen
			System.out.println("An error occurred while loading bookmarks file "+bookmarksFile.getAbsolutePath()+": "+e);			
		}
	}
	
	/**
	 * Tries to write the bookmarks file.
	 */
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
	 * Returns a Vector filled with all bookmarks.
	 */
	public static Vector getBookmarks() {
		return bookmarks;
	}

	
	/**
	 * Adds a bookmark.
	 *
	 * @param bm the Bookmark instance to add.
	 */
	public static void addBookmark(Bookmark bm) {
		bookmarks.add(bm);
	}


	/**
	 * Removes a bookmark.
	 *
	 * @param bm the Bookmark instance to remove.
	 */
	public static void removeBookmark(Bookmark bm) {
		bookmarks.remove(bm);
	}
}