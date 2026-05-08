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

import dev.barebones.commander.commons.file.FileOperation;
import dev.barebones.commander.commons.file.filter.FileOperationFilter;
import dev.barebones.commander.commons.file.util.FileSet;
import dev.barebones.commander.desktop.ActionType;
import dev.barebones.commander.ui.action.AbstractActionDescriptor;
import dev.barebones.commander.ui.action.ActionCategory;
import dev.barebones.commander.ui.action.ActionDescriptor;
import dev.barebones.commander.ui.dialog.file.DeleteDialog;
import dev.barebones.commander.ui.main.MainFrame;

/**
 * This action invokes a Delete confirmation dialog to delete currently the selected / marked files
 * in the currently active folder. Files are moved to the system trash when possible, i.e. if there is a trash available
 * on the current OS environment, and if the selected files are on a filesystem that allows it (usually only local files
 * can be moved to the trash).
 *
 * @see dev.barebones.commander.ui.action.impl.PermanentDeleteAction
 * @author Maxence Bernard
 */
public class DeleteAction extends SelectedFilesAction {

    public DeleteAction(MainFrame mainFrame, Map<String,Object> properties) {
        super(mainFrame, properties);

        setSelectedFileFilter(new FileOperationFilter(FileOperation.DELETE));
    }

    @Override
    public void performAction(FileSet files) {
        new DeleteDialog(mainFrame, files, false).showDialog();
    }

	@Override
	public ActionDescriptor getDescriptor() {
		return new Descriptor();
	}

    public static class Descriptor extends AbstractActionDescriptor {
		public String getId() { return ActionType.Delete.getId(); }

		public ActionCategory getCategory() { return ActionCategory.FILES; }
    }
}
