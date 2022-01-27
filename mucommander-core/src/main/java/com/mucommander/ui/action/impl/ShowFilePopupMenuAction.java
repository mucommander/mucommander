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

package com.mucommander.ui.action.impl;

import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.Map;

import javax.swing.KeyStroke;

import com.mucommander.commons.file.util.FileSet;
import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionDescriptor;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.menu.TablePopupMenu;
import com.mucommander.ui.main.table.FileTable;


/**
 * This action shows the popup menu for currently selected file
 *
 * @author Miroslav Oujesky
 */
public class ShowFilePopupMenuAction extends SelectedFilesAction {

    public ShowFilePopupMenuAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);
    }

    @Override
    public void performAction(FileSet files) {
        FileTable table = mainFrame.getActiveTable();

        int currentRow = table.getSelectedRow();

        // Does the row correspond to the parent '..' folder ?
        boolean parentFolderClicked = currentRow == 0 && table.getFileTableModel().hasParentFolder();

        TablePopupMenu popupMenu = new TablePopupMenu(
            mainFrame,
            table.getFolderPanel().getCurrentFolder(),
            parentFolderClicked ? null : table.getSelectedFile(),
            parentFolderClicked,
            files
        );

        // find coordinates of current row and show popup menu bellow it
        Rectangle rect = table.getCellRect(currentRow, table.getSelectedColumn(), true);
        popupMenu.show(table, 0, rect.y + rect.height);
    }

    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }

    public static final class Descriptor extends AbstractActionDescriptor {
        public static final String ACTION_ID = "ShowFilePopupMenu";

        public String getId() { return ACTION_ID; }

        public ActionCategory getCategory() { return ActionCategory.FILES; }

        public KeyStroke getDefaultAltKeyStroke() { return null; }

        public KeyStroke getDefaultKeyStroke() { return KeyStroke.getKeyStroke(KeyEvent.VK_CONTEXT_MENU, 0); }
    }
}
