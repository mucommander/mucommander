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

import javax.swing.ImageIcon;

import dev.barebones.commander.command.Command;
import dev.barebones.commander.commons.file.AbstractFile;
import dev.barebones.commander.commons.file.FileOperation;
import dev.barebones.commander.commons.file.filter.AndFileFilter;
import dev.barebones.commander.commons.file.filter.FileOperationFilter;
import dev.barebones.commander.commons.file.util.FileSet;
import dev.barebones.commander.desktop.ActionType;
import dev.barebones.commander.ui.action.AbstractActionDescriptor;
import dev.barebones.commander.ui.action.ActionCategory;
import dev.barebones.commander.ui.action.ActionDescriptor;
import dev.barebones.commander.ui.dialog.file.ChangePermissionsDialog;
import dev.barebones.commander.ui.main.MainFrame;
import dev.barebones.commander.ui.viewer.EditorRegistrar;

/**
 * Opens the current file in edit mode.
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public class InternalEditAction extends AbstractViewerAction {
    // - Initialization ------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Creates a new instance of <code>EditAction</code>.
     * @param mainFrame  frame to which the action is attached.
     * @param properties action's properties.
     */
    public InternalEditAction(MainFrame mainFrame, Map<String,Object> properties) {
        super(mainFrame, properties);

        // Edit requires being able to write the file (in addition to view requirements)
        setSelectedFileFilter(new AndFileFilter(
            new FileOperationFilter(FileOperation.WRITE_FILE),
            getSelectedFileFilter()
        ));

        ImageIcon icon;
        if((icon = getStandardIcon(EditAction.class)) != null)
            setIcon(icon);
    }

    // - AbstractViewerAction implementation ---------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    @Override
    protected void performInternalAction(AbstractFile file, boolean fromSearchWithContent) {
        if (file.isDirectory()) {
            FileSet fileSet = new FileSet();
            fileSet.add(file);
            new ChangePermissionsDialog(mainFrame, fileSet).showDialog();
        } else {
            EditorRegistrar.getInstance().createOpenFileFrame(mainFrame, file, fromSearchWithContent,
                    getIcon().getImage());
        }
    }

    @Override
    protected Command getCustomCommand() {
        return null;
    }

	@Override
	public ActionDescriptor getDescriptor() {
		return new Descriptor();
	}

    public static class Descriptor extends AbstractActionDescriptor {
		public String getId() { return ActionType.InternalEdit.getId(); }

		public ActionCategory getCategory() { return ActionCategory.FILES; }
    }
}
