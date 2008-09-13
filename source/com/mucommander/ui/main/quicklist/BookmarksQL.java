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

package com.mucommander.ui.main.quicklist;

import java.util.Arrays;
import java.util.Vector;

import javax.swing.ImageIcon;

import com.mucommander.bookmark.Bookmark;
import com.mucommander.bookmark.BookmarkListener;
import com.mucommander.bookmark.BookmarkManager;
import com.mucommander.text.Translator;
import com.mucommander.ui.quicklist.QuickListWithIcons;

/**
 * This quick list shows existing bookmarks.
 * 
 * @author Arik Hadas
 */
public class BookmarksQL extends QuickListWithIcons implements BookmarkListener {
	protected String[] sortedBookmarkNames; 

	public BookmarksQL() {
		super(Translator.get("bookmarks_quick_list.title"), Translator.get("bookmarks_quick_list.empty_message"));
		
		bookmarksChanged();
		BookmarkManager.addBookmarkListener(this);
	}

	protected void acceptListItem(String item) {
		folderPanel.tryChangeCurrentFolder(BookmarkManager.getBookmark(item).getLocation()); //change with text validate
	}

	protected Object[] getData() {
		return sortedBookmarkNames;
	}
	
	protected ImageIcon getImageIcon(String value) {
		return super.getImageIcon(BookmarkManager.getBookmark(value).getLocation());
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
	
	public void bookmarksChanged() {
		sortedBookmarkNames = getSortedBookmarkNames();
	}
}
