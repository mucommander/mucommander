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
import java.util.Map;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.desktop.ActionType;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionDescriptor;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.dialog.InformationDialog;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.menu.TablePopupMenu;
import com.mucommander.ui.main.table.Column;
import com.mucommander.ui.main.table.FileTable;
import com.mucommander.ui.main.table.FileTableModel;


/**
 * This action shows the popup menu for currently selected file.
 * Exactly the same popup menu appears also when right-clicking on file/folder.
 */
public class ShowFilePopupMenuAction extends SelectedFileAction {

    public ShowFilePopupMenuAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);
    }

    @Override
    public void performAction() {
        try {
            AbstractFile selectedFile = mainFrame.getActiveTable().getSelectedFile();
            FileTable fileTable = mainFrame.getActiveTable();
            FileTableModel tableModel = (FileTableModel) fileTable.getModel();
            int selectedRow = fileTable.getSelectedRow();
            Rectangle rect = fileTable.getCellRect(selectedRow,
                    fileTable.convertColumnIndexToView(Column.NAME.ordinal()), true);
            boolean parentFolderSelected = selectedRow == 0 && tableModel.hasParentFolder();

            new TablePopupMenu(mainFrame,
                    fileTable.getFolderPanel().getCurrentFolder(),
                    parentFolderSelected ? null : selectedFile,
                    parentFolderSelected,
                    tableModel.getMarkedFiles()).show(fileTable, rect.x + rect.width, rect.y);
        } catch (Exception e) {
            InformationDialog.showErrorDialog(mainFrame);
        }
    }

    protected void updateEnabledState(FileTable fileTable) {
        setEnabled(true);
    }

    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }

    public static final class Descriptor extends AbstractActionDescriptor {
        public String getId() {
            return ActionType.ShowFilePopupMenu.getId();
        }

        public ActionCategory getCategory() {
            return ActionCategory.NAVIGATION;
        }

        @Override
        public String getLabel() {
            return Translator.get(ActionProperties.getActionLabelKey(ActionType.ShowFilePopupMenu));
        }
    }
}