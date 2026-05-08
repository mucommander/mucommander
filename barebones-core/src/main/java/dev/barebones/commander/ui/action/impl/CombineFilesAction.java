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
import dev.barebones.commander.commons.file.filter.AttributeFileFilter;
import dev.barebones.commander.commons.file.filter.AttributeFileFilter.FileAttribute;
import dev.barebones.commander.commons.file.filter.FileFilter;
import dev.barebones.commander.commons.file.filter.FileOperationFilter;
import dev.barebones.commander.commons.file.util.FileSet;
import dev.barebones.commander.desktop.ActionType;
import dev.barebones.commander.ui.action.AbstractActionDescriptor;
import dev.barebones.commander.ui.action.ActionCategory;
import dev.barebones.commander.ui.action.ActionDescriptor;
import dev.barebones.commander.ui.action.InvokesDialog;
import dev.barebones.commander.ui.action.NoIcon;
import dev.barebones.commander.ui.dialog.file.CombineFilesDialog;
import dev.barebones.commander.ui.main.MainFrame;

/**
 * This action invokes the merge file dialog which allows to combine file parts into the original file.
 *
 * @author Mariusz Jakubowski
 */
@InvokesDialog
public class CombineFilesAction extends SelectedFilesAction {

    public CombineFilesAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);

        setSelectedFileFilter(new FileOperationFilter(FileOperation.READ_FILE));
    }

    @Override
    public void performAction(FileSet files) {
        // Filter out files that are not regular files
        FileFilter filter = new AttributeFileFilter(FileAttribute.FILE);
        filter.filter(files);

        if (files.size() == 0)
            return;

        AbstractFile destFolder = mainFrame.getInactivePanel().getCurrentFolder();
        new CombineFilesDialog(mainFrame, files, destFolder).showDialog();
    }

    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }

    @NoIcon
    public static class Descriptor extends AbstractActionDescriptor {
        public String getId() {
            return ActionType.CombineFiles.getId();
        }

        public ActionCategory getCategory() {
            return ActionCategory.FILES;
        }
    }
}
