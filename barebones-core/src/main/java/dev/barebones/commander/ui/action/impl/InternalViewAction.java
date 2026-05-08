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
import dev.barebones.commander.desktop.ActionType;
import dev.barebones.commander.ui.action.AbstractActionDescriptor;
import dev.barebones.commander.ui.action.ActionCategory;
import dev.barebones.commander.ui.action.ActionDescriptor;
import dev.barebones.commander.ui.main.MainFrame;
import dev.barebones.commander.ui.main.table.FileTable;
import dev.barebones.commander.ui.main.table.FileTableModel;
import dev.barebones.commander.ui.viewer.ViewerRegistrar;

/**
 * Opens the current file in view mode.
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public class InternalViewAction extends AbstractViewerAction {
    // - Initialization ------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Creates a new instance of <code>InternalViewAction</code>.
     * @param mainFrame  frame to which the action is attached.
     * @param properties action's properties.
     */
    public InternalViewAction(MainFrame mainFrame, Map<String,Object> properties) {
        super(mainFrame, properties);

        ImageIcon icon;
        if((icon = getStandardIcon(ViewAction.class)) != null)
            setIcon(icon);
    }



    // - AbstractViewerAction implementation ---------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    @Override
    protected void performInternalAction(AbstractFile file, boolean fromSearchWithContent) {
        if (file.isDirectory()) {
            FileTable activeTable = mainFrame.getActiveTable();
            FileTableModel fileTableModel = (FileTableModel)activeTable.getModel();
            fileTableModel.startDirectorySizeCalculation(activeTable, file);
        } else {
            ViewerRegistrar.getInstance().createOpenFileFrame(mainFrame, file, fromSearchWithContent,
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
		public String getId() { return ActionType.InternalView.getId(); }

		public ActionCategory getCategory() { return ActionCategory.FILES; }
    }
}
