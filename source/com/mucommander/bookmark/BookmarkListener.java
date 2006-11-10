
package com.mucommander.bookmark;


/**
 * Interface to be implemented by classes that wish to be notified when changes are made to the bookmarks list.
 * Those classes need to be registered to receive those events, this can be done by calling
 * {@link BookmarkManager#addBookmarkListener(BookmarkListener)}.
 *
 * @author Maxence Bernard
 */
public interface BookmarkListener {
	
    /**
     * This method is invoked when a bookmark has been added, removed or modified.
     */
    public void bookmarksChanged();

}
