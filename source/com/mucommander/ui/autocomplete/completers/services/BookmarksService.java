/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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
	private String[] sortedBookmarkNames;
	private String[] sortedBookmarkLocations;
	
	public BookmarksService() {
		fetchBookmarks();
		
		// Register as a bookmark-listener, in order to be up-to-date with existing bookmarks.
        BookmarkManager.addBookmarkListener(this);
	}
	
	public Vector<String> getPossibleCompletions(String path) {
		Vector<String> result = new Vector<String>();
		PrefixFilter filter = PrefixFilter.createPrefixFilter(path);
		result.addAll(filter.filter(sortedBookmarkNames));
		result.addAll(filter.filter(sortedBookmarkLocations)); 
		return result;
	}
	
	public String complete(String selectedCompletion) {
		String result = null;
		int nbBookmarks = sortedBookmarkNames.length;
		int i;
		for (i = 0; i < nbBookmarks; i++)
			if (sortedBookmarkNames[i].equalsIgnoreCase(selectedCompletion)) {
				result = sortedBookmarkNames[i];
				break;
			}

		for (i = 0; i < nbBookmarks; i++)
			if (sortedBookmarkLocations[i].equalsIgnoreCase(selectedCompletion)) {
				result = sortedBookmarkLocations[i];
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
    	Vector<Bookmark> bookmarks = BookmarkManager.getBookmarks();
        int nbBookmarks = bookmarks.size();
    	String[] result = new String[nbBookmarks];
    	for (int i=0; i<nbBookmarks; i++)
    		result[i] = bookmarks.elementAt(i).getName();
    	Arrays.sort(result, String.CASE_INSENSITIVE_ORDER);
    	return result;
    }
    
    /**
     * Returns array of bookmarks locations - the item at index i in the returned array, 
     * 	is the location of the bookmark at index i in sortedBookmarkNames array.
     *
     * @return array of bookmarks locations.
     */
    private String[] getLocationsOfBookmarks() {    	
        int nbBookmarks = sortedBookmarkNames.length;
    	String[] result = new String[nbBookmarks];
    	for (int i=0; i<nbBookmarks; i++)
    		result[i] = BookmarkManager.getBookmark(sortedBookmarkNames[i]).getLocation();
        Arrays.sort(result, String.CASE_INSENSITIVE_ORDER);
    	return result;
    }
    
    protected void fetchBookmarks() {
    	// get bookmarks names.
    	sortedBookmarkNames = getSortedBookmarkNames();
    	// get bookmarks locations.
    	sortedBookmarkLocations = getLocationsOfBookmarks();
    }
    
    // Bookmarks listening:
	public void bookmarksChanged() {
		fetchBookmarks();
	}
}
