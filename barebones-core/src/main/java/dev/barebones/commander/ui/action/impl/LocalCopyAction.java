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
import dev.barebones.commander.commons.file.util.FileSet;
import dev.barebones.commander.desktop.ActionType;
import dev.barebones.commander.ui.action.AbstractActionDescriptor;
import dev.barebones.commander.ui.action.ActionCategory;
import dev.barebones.commander.ui.action.ActionDescriptor;
import dev.barebones.commander.ui.dialog.file.LocalCopyDialog;
import dev.barebones.commander.ui.main.FolderPanel;
import dev.barebones.commander.ui.main.MainFrame;

/**
 * This action invokes the 'Copy dialog' which allows to copy the currently selected/marked files to a specified destination.
 * The only difference with {@link dev.barebones.commander.ui.action.impl.CopyAction} is that if a single file is selected,
 * the destination will be preset to the selected file's name so that it can easily be copied to a similar filename in
 * the current directory.
 *
 * @author Maxence Bernard
 */
public class LocalCopyAction extends SelectedFileAction {

    public LocalCopyAction(MainFrame mainFrame, Map<String,Object> properties) {
        super(mainFrame, properties);

        setSelectedFileFilter(new AndFileFilter(
            new FileOperationFilter(FileOperation.READ_FILE),
            new FileOperationFilter(FileOperation.WRITE_FILE)
        ));
    }

    @Override
    public void performAction() {
        FolderPanel activePanel = mainFrame.getActivePanel();
        AbstractFile selectedFile = activePanel.getFileTable().getSelectedFile(false, true);

        // Display local copy dialog only if a file other than '..' is currently selected
        if(selectedFile!=null) {
            new LocalCopyDialog(mainFrame, new FileSet(activePanel.getCurrentFolder(), selectedFile)).showDialog();
        }
    }

	@Override
	public ActionDescriptor getDescriptor() {
		return new Descriptor();
	}

    public static class Descriptor extends AbstractActionDescriptor {
		public String getId() { return ActionType.LocalCopy.getId(); }

		public ActionCategory getCategory() { return null; }
    }
}
