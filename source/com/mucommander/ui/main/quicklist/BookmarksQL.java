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

package com.mucommander.ui.main.quicklist;

import com.mucommander.bookmark.Bookmark;
import com.mucommander.bookmark.BookmarkListener;
import com.mucommander.bookmark.BookmarkManager;
import com.mucommander.file.FileFactory;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.action.impl.ShowBookmarksQLAction;
import com.mucommander.ui.quicklist.QuickListWithIcons;

import javax.swing.Icon;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;

/**
 * This quick list shows existing bookmarks.
 * 
 * @author Arik Hadas
 */
public class BookmarksQL extends QuickListWithIcons implements BookmarkListener {
	protected Bookmark[] sortedBookmarks;

	public BookmarksQL() {
		super(ActionProperties.getActionLabel(ShowBookmarksQLAction.Descriptor.ACTION_ID), Translator.get("bookmarks_menu.no_bookmark"));
		
		bookmarksChanged();
		BookmarkManager.addBookmarkListener(this);
	}

	@Override
    protected void acceptListItem(Object item) {
		folderPanel.tryChangeCurrentFolder(((Bookmark)item).getLocation()); //change with text validate
	}

	@Override
    protected Object[] getData() {
		return sortedBookmarks;
	}
	
	@Override
    protected Icon itemToIcon(Object item) {
		return getIconOfFile(FileFactory.getFile(((Bookmark)item).getLocation()));
	}

	/**
     * Returns a sorted array of bookmarks.
     *
     * @return a sorted array of bookmarks
     */
    private Bookmark[] getSortedBookmarks() {
    	Vector<Bookmark> bookmarks = BookmarkManager.getBookmarks();
        Bookmark[] bookmarkArray = new Bookmark[bookmarks.size()];
        bookmarks.toArray(bookmarkArray);
        Arrays.sort(bookmarkArray, new Comparator<Bookmark>() {
            public int compare(Bookmark b1, Bookmark b2) {
                return String.CASE_INSENSITIVE_ORDER.compare(b1.getName(), b2.getName());
            }
        });

    	return bookmarkArray;
    }
	
	public void bookmarksChanged() {
		sortedBookmarks = getSortedBookmarks();
	}
}
