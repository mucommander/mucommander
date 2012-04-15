/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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

import com.mucommander.ui.action.*;
import com.mucommander.ui.helper.FocusRequester;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.table.FileTable;

import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.util.Hashtable;

/**
 * This action allows to cycle forward through the current FolderPanel's focusable components: file table, folder tree
 * and location field. The action has no effect when the focus is not in the MainFrame this action is tied to.
 *
 * @author Maxence Bernard
 */
public class FocusNextAction extends MuAction {

    public FocusNextAction(MainFrame mainFrame, Hashtable<String,Object> properties) {
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
    
    public static class Factory implements ActionFactory {

		public MuAction createAction(MainFrame mainFrame, Hashtable<String,Object> properties) {
			return new FocusNextAction(mainFrame, properties);
		}
    }
    
    public static class Descriptor extends AbstractActionDescriptor {
    	public static final String ACTION_ID = "FocusNext";
    	
		public String getId() { return ACTION_ID; }

		public ActionCategory getCategory() { return ActionCategories.NAVIGATION; }

		public KeyStroke getDefaultAltKeyStroke() { return null; }

		public KeyStroke getDefaultKeyStroke() { return KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.CTRL_DOWN_MASK); }
    }
}
