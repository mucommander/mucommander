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

import com.mucommander.Debug;
import com.mucommander.PlatformManager;
import com.mucommander.io.BackupOutputStream;
import com.mucommander.io.BackupInputStream;
import com.mucommander.util.AlteredVector;
import com.mucommander.util.VectorChangeListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.WeakHashMap;

/**
 * This class manages the boomark list and its parsing and storage as an XML file.
 * <p>
 * It monitors any changes made to the bookmarks and when changes are made, fires change events to registered
 * listeners.
 * </p>
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public class BookmarkManager implements VectorChangeListener {
    /** Whether we're currently loading the bookmarks or not. */
    private static boolean isLoading = false;

    /** Bookmarks file location */
    private static File bookmarksFile;

    /** Default bookmarks file name */
    private static final String DEFAULT_BOOKMARKS_FILE_NAME = "bookmarks.xml";

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



    // - Initialisation --------------------------------------------------------
    // -------------------------------------------------------------------------
    static {
        // Listen to changes made to the bookmarks vector
        bookmarks.addVectorChangeListener(singleton);
    }

    /**
     * Prevents instanciation of <code>BookmarkManager</code>.
     */
    private BookmarkManager() {}



    // - Bookmark building -----------------------------------------------------
    // -------------------------------------------------------------------------
    public static synchronized void buildBookmarks(BookmarkBuilder builder) throws BookmarkException {
        Iterator iterator;
        Bookmark bookmark;

        builder.startBookmarks();
        iterator = bookmarks.iterator();
        while(iterator.hasNext()) {
            bookmark = (Bookmark)iterator.next();
            builder.addBookmark(bookmark.getName(), bookmark.getLocation());
        }
        builder.endBookmarks();
    }



    // - Bookmark file access --------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Returns the path to the bookmark file.
     * <p>
     * If it hasn't been changed through a call to {@link #setBookmarksFile(String)},
     * this method will return the default, system dependant bookmarks file.
     * </p>
     * @return the path to the bookmark file.
     * @see    #setBookmarksFile(String)
     */
    public static synchronized File getBookmarksFile() {
        if(bookmarksFile == null)
            return new File(PlatformManager.getPreferencesFolder(), DEFAULT_BOOKMARKS_FILE_NAME);
        return bookmarksFile;
    }

    /**
     * Sets the path to the bookmarks file.
     * @param     path                  path to the bookmarks file
     * @exception FileNotFoundException if <code>path</code> is not accessible.
     * @see       #getBookmarksFile()
     */
    public static synchronized void setBookmarksFile(String path) throws FileNotFoundException {
        File tempFile;

        // Makes sure the file is accessible.
        tempFile = new File(path);
        if(!(tempFile.exists() && tempFile.isFile() && tempFile.canRead()))
            throw new FileNotFoundException("Not a valid file: " + path);

        bookmarksFile = tempFile;
    }



    // - Bookmarks loading -----------------------------------------------------
    // -------------------------------------------------------------------------
    public static synchronized void loadBookmarks() throws Exception {
        InputStream in;

        // Parse the bookmarks file
        in = null;
        isLoading = true;
        try {readBookmarks(in = new BackupInputStream(getBookmarksFile()), new Loader());}
        finally {
            if(in != null) {
                try {in.close();}
                catch(Exception e) {}
            }
            isLoading = false;
        }
    }

    public static synchronized void readBookmarks(InputStream in, BookmarkBuilder builder) throws Exception {new BookmarkParser().parse(in, builder);}



    // - Bookmarks writing -----------------------------------------------------
    // -------------------------------------------------------------------------
    public static BookmarkBuilder getBookmarkWriter(OutputStream out) throws IOException {return new BookmarkWriter(out);}

    /**
     * Writes all known bookmarks to the bookmark {@link #getBookmarksFile() file}.
     * @param forceWrite if false, the bookmarks file will be written only if changes were made to bookmarks since
     * last write, if true the file will always be written
     * @throws IOException if an I/O error occurs.
     * @throws BookmarkException if an error occurs.
     */
    public static synchronized void writeBookmarks(boolean forceWrite) throws IOException, BookmarkException {
        OutputStream out;

        // Write bookmarks file only if changes were made to the bookmarks since last write, or if write is forced.
        if(!(forceWrite || saveNeeded))
            return;
        out = null;
        try {
            buildBookmarks(getBookmarkWriter(out = new BackupOutputStream(getBookmarksFile())));
            saveNeeded = false;
        }
        finally {
            if(out != null) {
                try {out.close();}
                catch(Exception e) {}
            }
        }
    }



    // - Bookmarks access ------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Returns an {@link AlteredVector} that contains all bookmarks.
     * 
     * <p>Important: the returned Vector should not directly be used to
     * add or remove bookmarks, doing so won't trigger any event to registered bookmark listeners.
     * However, it is safe to modify bookmarks individually, events will be properly fired.
     */
    public static synchronized AlteredVector getBookmarks() {
        return bookmarks;
    }

    /**
     * Deletes the specified bookmark.
     * @param bookmark bookmark to delete from the list.
     */
    public static synchronized void removeBookmark(Bookmark bookmark) {bookmarks.remove(bookmark);}

    /**
     * Convenience method that looks for a Bookmark with the given name (case ignored) and returns it,
     * or null if none was found. If several bookmarks have the given name, the first one is returned.
     *
     * @param name the bookmark's name
     * @return a Bookmark instance with the given name, null if none was found
     */
    public static synchronized Bookmark getBookmark(String name) {
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
    public static synchronized void addBookmark(Bookmark b) {bookmarks.add(b);}



    // - Listeners -------------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Adds the specified BookmarkListener to the list of registered listeners.
     *
     * <p>Listeners are stored as weak references so {@link #removeBookmarkListener(BookmarkListener)}
     * doesn't need to be called for listeners to be garbage collected when they're not used anymore.
     *
     * @param listener the BookmarkListener to add to the list of registered listeners.
     */
    public static void addBookmarkListener(BookmarkListener listener) {synchronized(listeners) {listeners.put(listener, null);}}

    /**
     * Removes the specified BookmarkListener from the list of registered listeners.
     *
     * @param listener the BookmarkListener to remove from the list of registered listeners.
     */
    public static void removeBookmarkListener(BookmarkListener listener) {synchronized(listeners) {listeners.remove(listener);}}

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
        if(!isLoading)
            saveNeeded = true;

        lastBookmarkChangeTime = System.currentTimeMillis();

        // Do not fire event if events are currently disabled
        if(!fireEvents)
            return;

        if(Debug.ON) Debug.trace("firing an event to registered listeners");

        synchronized(listeners) {
            // Iterate on all listeners
            Iterator iterator = listeners.keySet().iterator();
            while(iterator.hasNext())
                ((BookmarkListener)iterator.next()).bookmarksChanged();
        }
    }

    /**
     * Specifies whether bookmark events should be fired when a change in the bookmarks is detected. This allows
     * to temporarily suspend events firing when a lot of them are made, for example when editing the bookmarks list.
     *
     * <p>If true is speicified, any subsequent calls to fireBookmarksChanged will be ignored, until this method is
     * called again with false.
     */
    public static synchronized void setFireEvents(boolean b) {
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



    // - Bookmark loading ------------------------------------------------------
    // -------------------------------------------------------------------------
    private static class Loader implements BookmarkBuilder {
        public void startBookmarks() {}
        public void endBookmarks() {}
        public void addBookmark(String name, String location) {BookmarkManager.addBookmark(new Bookmark(name, location));}
    }
}
