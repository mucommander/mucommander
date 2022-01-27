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

import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import com.mucommander.command.Command;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionDescriptor;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.table.FileTable;
import com.mucommander.ui.main.table.FileTableModel;
import com.mucommander.ui.viewer.ViewerRegistrar;

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
    protected void performInternalAction(AbstractFile file) {
        if (file.isDirectory()) {
            FileTable activeTable = mainFrame.getActiveTable();
            FileTableModel fileTableModel = (FileTableModel)activeTable.getModel();
            fileTableModel.startDirectorySizeCalculation(activeTable, file);
        } else {
            ViewerRegistrar.createViewerFrame(mainFrame, file, getIcon().getImage());
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
    	public static final String ACTION_ID = "InternalView";

		public String getId() { return ACTION_ID; }

		public ActionCategory getCategory() { return ActionCategory.FILES; }

		public KeyStroke getDefaultAltKeyStroke() { return null; }

		public KeyStroke getDefaultKeyStroke() { return null; }
    }
}
