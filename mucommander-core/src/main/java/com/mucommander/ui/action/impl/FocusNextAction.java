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

import java.awt.Component;
import java.util.Map;

import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;

import com.mucommander.commons.util.ui.helper.FocusRequester;
import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionDescriptor;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.table.FileTable;

/**
 * This action allows to cycle forward through the current FolderPanel's focusable components: file table, folder tree
 * and location field. The action has no effect when the focus is not in the MainFrame this action is tied to.
 *
 * @author Maxence Bernard
 */
public class FocusNextAction extends MuAction {

    public FocusNextAction(MainFrame mainFrame, Map<String,Object> properties) {
        super(mainFrame, properties);

        // Perform the action also when in 'no events' mode
        setHonourNoEventsMode(false);
    }

    @Override
    public void performAction() {
        Component focusOwner = mainFrame.getFocusOwner();

        // Abort if the focus is not in the MainFrame this action is tied to
        if(focusOwner==null)
            return;

        FolderPanel folderPanel = mainFrame.getActivePanel();
        FileTable fileTable = folderPanel.getFileTable();
        JTextField locationField = folderPanel.getLocationTextField();
        JTree tree = folderPanel.getFoldersTreePanel().getTree();

        // Request focus on the 'next' component, the cycle order being from left to right, top to bottom.
        Component nextComponent;
        if(focusOwner==locationField)
            nextComponent = folderPanel.isTreeVisible()?tree:fileTable;
        else if(focusOwner==tree)
            nextComponent = fileTable;
        else if(focusOwner==fileTable)
            nextComponent = locationField;
        else
            return;

        FocusRequester.requestFocusInWindow(nextComponent);
    }

	@Override
	public ActionDescriptor getDescriptor() {
		return new Descriptor();
	}

    public static class Descriptor extends AbstractActionDescriptor {
    	public static final String ACTION_ID = "FocusNext";
    	
		public String getId() { return ACTION_ID; }

		public ActionCategory getCategory() { return ActionCategory.NAVIGATION; }

		public KeyStroke getDefaultAltKeyStroke() { return null; }

		public KeyStroke getDefaultKeyStroke() { return null; }
    }
}
