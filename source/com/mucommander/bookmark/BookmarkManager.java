
package com.mucommander.bookmark;

import com.mucommander.Debug;
import com.mucommander.PlatformManager;
import com.mucommander.io.BackupOutputStream;
import com.mucommander.util.AlteredVector;
import com.mucommander.util.VectorChangeListener;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.WeakHashMap;

/**
 * This class manages the boomark list and its parsing and storage as an XML file.
 *
 * <p>It monitors any changes made to the bookmarks and when changes are made, fires change events to registered
 * listeners.
 *
 * @author Maxence Bernard
 */
public class BookmarkManager implements VectorChangeListener {
    private static File bookmarksFile;

    /** Bookmark file name */
    private static final String BOOKMARKS_FILENAME = "bookmarks.xml";

    /** Bookmark instances */
    private static AlteredVector bookmarks = new AlteredVector();

    /** Contains all registered bookmark listeners, stored as weak references */
    private static WeakHashMap listeners = new WeakHashMap();

    /** Specifies whether bookmark events should be fired when a change to the bookmarks is detected */
    private static boolean fireEvents = true;

    /** True when changes were made after the bookmarks file was last saved */
    private static boolean saveNeeded;

    /** Last bookmark change timestamp */
    private static long lastBookmarkChangeTime;

    /** Last event pause timestamp */
    private static long lastEventPauseTime;

    /** Create a singleton instance, needs to be referenced so that it's not garbage collected (AlteredVector
     * stores VectorChangeListener as weak references) */
    private static BookmarkManager singleton = new BookmarkManager();


    static {
        // Listen to changes made to the bookmarks vector
        bookmarks.addVectorChangeListener(singleton);
    }


    private BookmarkManager() {
    }


    /**
     * Return a java.io.File instance that points to the bookmarks file location.
     */
    private static File getBookmarksFile() {
        if(bookmarksFile == null)
            return new File(PlatformManager.getPreferencesFolder(), BOOKMARKS_FILENAME);
        else
            return bookmarksFile;
    }

    /**
     * Sets the path to the bookmarks file.
     *
     * @param path the path to the bookmarks file
     */
    public static void setBookmarksFile(String path) {
        bookmarksFile = new File(path);
    }

	
    /**
     * Tries to load bookmarks from the bookmarks file if it exists, and reports any error that occur during parsing
     * to the standard output. Does nothing if the bookmarks file doesn't exist.
     */
    public static void loadBookmarks() {
        File bookmarksFile = getBookmarksFile();
        try {
            if(bookmarksFile.exists()) {
                if(Debug.ON) Debug.trace("Found bookmarks file: "+bookmarksFile.getAbsolutePath());
                // Parse the bookmarks file
                new BookmarkParser().parse(bookmarksFile);
                if(Debug.ON) Debug.trace("Bookmarks file loaded.");
            }
            else {
                if(Debug.ON) Debug.trace("No bookmarks file found at "+bookmarksFile.getAbsolutePath());
            }
        }
        catch(Exception e) {
            // Report on the standard output that something went wrong while parsing the bookmarks file
            // as this shouldn't normally happen
            System.out.println("An error occurred while loading bookmarks file "+bookmarksFile.getAbsolutePath()+": "+e);			
        }
    }
	
    /**
     * Tries to write the bookmarks file. Unless the 'forceWrite' is set to true, the bookmarks file will be written
     * only if changes were made to bookmarks since last write.
     *
     * @param forceWrite if false, the bookmarks file will be written only if changes were made to bookmarks since
     * last write, if true the file will always be written
     */
    public static void writeBookmarks(boolean forceWrite) {
        // Write bookmarks file only if changes were made to the bookmarks since last write, or if write is forced 
        if(!(forceWrite || saveNeeded))
            return;

        BackupOutputStream out = null;
        try {
            bookmarksFile = getBookmarksFile();
            BookmarkWriter.write(out = new BackupOutputStream(bookmarksFile));
            if(Debug.ON) Debug.trace("Bookmarks file saved successfully.");
            out.close();

            saveNeeded = false;
        }
        catch(IOException e) {
            if(out != null) {
                try {out.close(false);}
                catch(Exception e2) {}
            }

            // Notify user that something went wrong while writing the bookmarks file
            System.out.println("An error occurred while writing bookmarks file "+bookmarksFile.getAbsolutePath()+": "+e);			
        }
    }


    /**
     * Returns an {@link AlteredVector} that contains all bookmarks.
     * 
     * <p>Important: the returned Vector should not directly be used to
     * add or remove bookmarks, doing so won't trigger any event to registered bookmark listeners.
     * However, it is safe to modify bookmarks individually, events will be properly fired.</p>
     */
    public static AlteredVector getBookmarks() {
        return bookmarks;
    }


    /**
     * Convenience method that looks for a Bookmark with the given name (case ignored) and returns it,
     * or null if none was found. If several bookmarks have the given name, the first one is returned.
     *
     * @param name the bookmark's name
     * @return a Bookmark instance with the given name, null if none was found
     */
    public static Bookmark getBookmark(String name) {
        int nbBookmarks = bookmarks.size();
        Bookmark b;
        for(int i=0; i<nbBookmarks; i++) {
            b = (Bookmark)bookmarks.elementAt(i);
            if(b.getName().equalsIgnoreCase(name))
                return b;
        }

        return null;
    }


    /**
     * Convenience method that adds a bookmark to the bookmark list.
     *
     * @param b the Bookmark instance to add to the bookmark list.
     */
    public static void addBookmark(Bookmark b) {
        bookmarks.add(b);

//        // Notify registered listeners of the change
//        fireBookmarkChanged(b);
    }
//
//
//    /**
//     * Removes a bookmark.
//     *
//     * @param b the Bookmark instance to remove.
//     */
//    public static void removeBookmark(Bookmark b) {
//        bookmarks.remove(b);
//
//        // Notify registered listeners of the change
//        fireBookmarkChanged(b);
//    }
	
	
    /**
     * Adds the specified BookmarkListener to the list of registered listeners.
     *
     * <p>Listeners are stored as weak references so {@link #removeBookmarkListener(BookmarkListener) removeBookmarkListener()}
     * doesn't need to be called for listeners to be garbage collected when they're not used anymore.</p>
     *
     * @param listener the BookmarkListener to add to the list of registered listeners.
     */
    public static void addBookmarkListener(BookmarkListener listener) {
        listeners.put(listener, null);
    }

    /**
     * Removes the specified BookmarkListener from the list of registered listeners.
     *
     * @param listener the BookmarkListener to remove from the list of registered listeners.
     */
    public static void removeBookmarkListener(BookmarkListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notifies all the registered bookmark listeners of a bookmark change. This can be :
     * <ul>
     * <li>A new bookmark which has just been added
     * <li>An existing bookmark which has been modified
     * <li>An existing bookmark which has been removed
     * </ul>
     */
    public static void fireBookmarksChanged() {
        // Bookmarks file will need to be saved
        saveNeeded = true;

        lastBookmarkChangeTime = System.currentTimeMillis();

        // Do not fire event if events are currently disabled
        if(!fireEvents)
            return;

        if(Debug.ON) Debug.trace("firing an event to registered listeners");

        // Iterate on all listeners
        Iterator iterator = listeners.keySet().iterator();
        while(iterator.hasNext())
            ((BookmarkListener)iterator.next()).bookmarksChanged();
    }


    /**
     * Specifies whether bookmark events should be fired when a change in the bookmarks is detected. This allows
     * to temporarily suspend events firing when a lot of them are made, for example when editing the bookmarks list.
     *
     * <p>If true is speicified, any subsequent calls to fireBookmarksChanged will be ignored, until this method is
     * called again with false.
     */
    public static void setFireEvents(boolean b) {
        if(b) {
            // Fire a bookmarks changed event if bookmarks were modified during event pause
            if(!fireEvents && lastBookmarkChangeTime >= lastEventPauseTime) {
                fireEvents = true;
                fireBookmarksChanged();
            }
        }
        else {
            // Remember pause start time
            if(fireEvents) {
                fireEvents = false;
                lastEventPauseTime = System.currentTimeMillis();
            }
        }
    }

    /////////////////////////////////////////
    // VectorChangeListener implementation //
    /////////////////////////////////////////

    public void elementsAdded(int startIndex, int nbAdded) {
        fireBookmarksChanged();
    }

    public void elementsRemoved(int startIndex, int nbRemoved) {
        fireBookmarksChanged();
    }

    public void elementChanged(int index) {
        fireBookmarksChanged();
    }
}
