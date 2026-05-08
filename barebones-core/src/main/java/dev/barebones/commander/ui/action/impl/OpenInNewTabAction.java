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

package dev.barebones.commander.ui.action.impl;

import java.net.MalformedURLException;
import java.util.Map;

import dev.barebones.commander.bookmark.BookmarkManager;
import dev.barebones.commander.commons.file.AbstractFile;
import dev.barebones.commander.commons.file.FileURL;
import dev.barebones.commander.desktop.ActionType;
import dev.barebones.commander.ui.action.AbstractActionDescriptor;
import dev.barebones.commander.ui.action.ActionCategory;
import dev.barebones.commander.ui.action.ActionDescriptor;
import dev.barebones.commander.ui.action.NoIcon;
import dev.barebones.commander.ui.main.MainFrame;
import dev.barebones.commander.ui.main.table.FileTable;

/**
 * Opens browsable file in a new tab.
 * <p>
 * This action is only enabled if the current selection is browsable as defined by
 * {@link dev.barebones.commander.commons.file.AbstractFile#isBrowsable()}.
 * </p>
 * 
 * @author Arik Hadas
 */
public class OpenInNewTabAction extends SelectedFileAction {

    public OpenInNewTabAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);
    }

    /**
     * This method is overridden to enable this action when the parent folder is selected.
     */
    @Override
    protected boolean getFileTableCondition(FileTable fileTable) {
        AbstractFile selectedFile = fileTable.getSelectedFile(true, true);

        return selectedFile != null && selectedFile.isBrowsable();
    }

    @Override
    public void performAction() {
        AbstractFile file = mainFrame.getActiveTable().getSelectedFile(true, true);

        // Retrieves the currently selected file, aborts if none (should not normally happen).
        if (file == null || !file.isBrowsable())
            return;

        FileURL fileURL = file.getURL();

        if (BookmarkManager.isBookmark(fileURL)) {
            String bookmarkLocation = BookmarkManager.getBookmark(file.getName()).getLocation();
            try {
                fileURL = FileURL.getFileURL(bookmarkLocation);
            } catch (MalformedURLException e) {
                LOGGER.error("Failed to resolve bookmark's location: " + bookmarkLocation);
                return;
            }
        }

        // Opens the currently selected file in a new tab
        mainFrame.getActivePanel().getTabs().add(fileURL);
    }

    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }

    @NoIcon
    public static class Descriptor extends AbstractActionDescriptor {
        public String getId() {
            return ActionType.OpenInNewTab.getId();
        }

        public ActionCategory getCategory() {
            return ActionCategory.NAVIGATION;
        }
    }
}
