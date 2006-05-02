
package com.mucommander.bookmark;


/**
 * Interface to be implemented by classes that wish to be notified of bookmark changes.
 * Those classes need to be registered to receive those events, this can be done by calling
 * {@link BookmarkManager#addLocationListener(BookmarkListener) BookmarkManager.addLocationListener()}.
 *
 * @author Maxence Bernard
 */
public interface BookmarkListener {
	
    /**
     * This method is invoked when a bookmark has been added, edited or removed.
     *
     * @param b the bookmark which has been added, edited or removed.
     */
    public void bookmarkChanged(Bookmark b);

}
