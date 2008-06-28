/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
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

package com.mucommander.ui.autocomplete.completers.services;

import com.mucommander.bookmark.Bookmark;
import com.mucommander.bookmark.BookmarkListener;
import com.mucommander.bookmark.BookmarkManager;

import java.util.Arrays;
import java.util.Vector;

/**
 * This <code>CompletionService</code> handles bookmarks completion.
 * 
 * @author Arik Hadas
 */

public class BookmarksService implements CompletionService, BookmarkListener {
	private String[] bookmarkNames;
	
	public BookmarksService() {
		bookmarkNames = getSortedBookmarkNames();
		
		// Register as a bookmark-listener, in order to be up-to-date with the existing bookmarks.
        BookmarkManager.addBookmarkListener(this);
	}
	
	public Vector getPossibleCompletions(String path) {
		return PrefixFilter.createPrefixFilter(path).filter(bookmarkNames);
	}
	
	public String complete(String selectedCompletion) {
		String result = null;
		int nbBookmarks = bookmarkNames.length;
		for (int i = 0; i < nbBookmarks; i++)
			if (bookmarkNames[i].equalsIgnoreCase(selectedCompletion)) {
				result = bookmarkNames[i];
				break;
			}
		return result;
	}    
	
    /**
     * Returns a sorted array of bookmarks names.
     *
     * @return a sorted array of bookmarks names
     */
    private String[] getSortedBookmarkNames() {
    	Vector bookmarks = BookmarkManager.getBookmarks();
        int nbBookmarks = bookmarks.size();
    	String[] result = new String[nbBookmarks];
    	for (int i=0; i<nbBookmarks; i++)
    		result[i] = ((Bookmark) bookmarks.elementAt(i)).getName();
    	Arrays.sort(result, String.CASE_INSENSITIVE_ORDER);
    	return result;
    }
    
    // Bookmarks listening:
	public void bookmarksChanged() {
		bookmarkNames = getSortedBookmarkNames();
	}
}
