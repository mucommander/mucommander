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

import java.util.Map;

import dev.barebones.commander.commons.file.AbstractFile;
import dev.barebones.commander.commons.file.FileOperation;
import dev.barebones.commander.commons.file.filter.AndFileFilter;
import dev.barebones.commander.commons.file.filter.FileOperationFilter;
import dev.barebones.commander.commons.file.filter.OrFileFilter;
import dev.barebones.commander.desktop.ActionType;
import dev.barebones.commander.ui.action.AbstractActionDescriptor;
import dev.barebones.commander.ui.action.ActionCategory;
import dev.barebones.commander.ui.action.ActionDescriptor;
import dev.barebones.commander.ui.main.MainFrame;
import dev.barebones.commander.ui.main.table.FileTable;

/**
 * This action triggers in-table renaming of the currently selected file, if no file is marked.
 * If files are marked, it simply invokes 'Move dialog' just like {@link CopyAction}.
 *
 * @author Maxence Bernard
 */
public class RenameAction extends SelectedFileAction {

    public RenameAction(MainFrame mainFrame, Map<String,Object> properties) {
        super(mainFrame, properties);

        setSelectedFileFilter(new OrFileFilter(
            new FileOperationFilter(FileOperation.RENAME),
            new AndFileFilter(
                new FileOperationFilter(FileOperation.READ_FILE),
                new FileOperationFilter(FileOperation.WRITE_FILE)
            )
        ));
    }

    @Override
    public void performAction() {
        FileTable activeTable = mainFrame.getActiveTable();
        AbstractFile selectedFile = activeTable.getSelectedFile(false);

        // Trigger in-table editing only if a file other than parent folder '..' is selected
        if(selectedFile!=null) {
            // Trigger in-table renaming
            activeTable.editCurrentFilename();
        }
    }

	@Override
	public ActionDescriptor getDescriptor() {
		return new Descriptor();
	}

    public static class Descriptor extends AbstractActionDescriptor {
		public String getId() { return ActionType.Rename.getId(); }

		public ActionCategory getCategory() { return ActionCategory.FILES; }
    }
}
