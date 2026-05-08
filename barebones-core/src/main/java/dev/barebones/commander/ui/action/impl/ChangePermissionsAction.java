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
import dev.barebones.commander.ui.action.InvokesDialog;
import dev.barebones.commander.ui.action.NoIcon;
import dev.barebones.commander.ui.dialog.file.ChangePermissionsDialog;
import dev.barebones.commander.ui.main.MainFrame;

/**
 * Brings up a dialog that allows the user to change the file permissions the currently selected/marked files.
 * 
 * @author Maxence Bernard
 */
@InvokesDialog
public class ChangePermissionsAction extends SelectedFilesAction {

    public ChangePermissionsAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);

        setSelectedFileFilter(new FileOperationFilter(FileOperation.CHANGE_PERMISSION));
    }

    @Override
    public void performAction(FileSet files) {
        new ChangePermissionsDialog(mainFrame, files).showDialog();
    }

    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }

    @NoIcon
    public static class Descriptor extends AbstractActionDescriptor {
        public String getId() {
            return ActionType.ChangePermissions.getId();
        }

        public ActionCategory getCategory() {
            return ActionCategory.FILES;
        }
    }
}
