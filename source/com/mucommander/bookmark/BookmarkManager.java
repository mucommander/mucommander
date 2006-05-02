
package com.mucommander.bookmark;

import java.util.Vector;

import java.io.File;
import java.io.IOException;

import java.util.WeakHashMap;
import java.util.Iterator;

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

    /** Contains all registered bookmark listeners, stored as weak references */
    private static WeakHashMap listeners = new WeakHashMap();


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
     * 
     * <p>Important: the returned Vector should not directly be used to
     * add or remove bookmarks, doing so won't trigger any event to registered bookmark listeners.
     * However, it is safe to modify bookmarks individually, events will be properly fired.</p>
     */
    public static Vector getBookmarks() {
        return bookmarks;
    }

	
    /**
     * Adds a bookmark.
     *
     * @param bm the Bookmark instance to add.
     */
    public static void addBookmark(Bookmark b) {
        bookmarks.add(b);

        // Notify registered listeners of the change
        fireBookmarkChanged(b);
    }


    /**
     * Removes a bookmark.
     *
     * @param bm the Bookmark instance to remove.
     */
    public static void removeBookmark(Bookmark b) {
        bookmarks.remove(b);

        // Notify registered listeners of the change
        fireBookmarkChanged(b);
    }
	
	
    /**
     * Adds the specified BookmarkListener to the list of registered listeners.
     *
     * <p>Listeners are stored as weak references so {@link #removeBookmarkListener(BookmarkListener) removeBookmarkListener()}
     * doesn't need to be called for listeners to be garbage collected when they're not used anymore.</p>
     *
     * @param listener the BookmarkListener to add to the list of registered listeners.
     */
    public static synchronized void addBookmarkListener(BookmarkListener listener) {
        if(listener==null)
            return;
		
        listeners.put(listener, null);
    }

    /**
     * Removes the specified BookmarkListener from the list of registered listeners.
     *
     * @param listener the BookmarkListener to remove from the list of registered listeners.
     */
    public static synchronized void removeBookmarkListener(BookmarkListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notifies all the registered bookmark listeners of a bookmark change. This can be :
     * <ul>
     * <li>A new bookmark which has just been added
     * <li>An existing bookmark which has been modified
     * <li>An existing bookmark which has been removed
     * </ul>
     *
     * @param b the bookmark that has been added/edited/removed
     */
    public static synchronized void fireBookmarkChanged(Bookmark b) {
        Iterator iterator = listeners.keySet().iterator();
        while(iterator.hasNext())
            ((BookmarkListener)iterator.next()).bookmarkChanged(b);
    }
}
