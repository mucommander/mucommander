/*
 * This file is part of muCommander, http://www.mucommander.com
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

package dev.barebones.commander.ui.main.quicklist;

import javax.swing.Icon;

import dev.barebones.commander.bookmark.Bookmark;
import dev.barebones.commander.bookmark.BookmarkListener;
import dev.barebones.commander.bookmark.BookmarkManager;
import dev.barebones.commander.commons.collections.AlteredVector;
import dev.barebones.commander.commons.file.FileFactory;
import dev.barebones.commander.desktop.ActionType;
import dev.barebones.commander.text.Translator;
import dev.barebones.commander.ui.action.ActionProperties;
import dev.barebones.commander.ui.main.FolderPanel;
import dev.barebones.commander.ui.quicklist.QuickListWithIcons;

/**
 * This quick list shows existing bookmarks.
 * 
 * @author Arik Hadas
 */
public class BookmarksQL extends QuickListWithIcons<Bookmark> implements BookmarkListener {
	private Bookmark[] cachedBookmarks;
	private FolderPanel folderPanel;

	public BookmarksQL(FolderPanel folderPanel) {
		super(folderPanel, ActionProperties.getActionLabel(ActionType.ShowBookmarksQL), Translator.get("bookmarks_menu.no_bookmark"));
		
		this.folderPanel = folderPanel;
		
		bookmarksChanged();
		BookmarkManager.addBookmarkListener(this);
	}

	@Override
    protected void acceptListItem(Bookmark item) {
		folderPanel.tryChangeCurrentFolder(item.getLocation()); //change with text validate
	}

	@Override
    protected Bookmark[] getData() {
		return cachedBookmarks;
	}
	
	@Override
    protected Icon itemToIcon(Bookmark item) {
		return getIconOfFile(FileFactory.getFile(item.getLocation()));
	}

	public void bookmarksChanged() {
		AlteredVector<Bookmark> bookmarks = BookmarkManager.getBookmarks();
		cachedBookmarks = new Bookmark[bookmarks.size()];
		bookmarks.toArray(cachedBookmarks);
	}
}
