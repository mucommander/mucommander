/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2009 Maxence Bernard
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

import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.action.ActionFactory;
import com.mucommander.ui.helper.FocusRequester;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.table.FileTable;

import javax.swing.*;

import java.awt.*;
import java.util.Hashtable;

/**
 * This action allows to cycle backward through the current FolderPanel's focusable components: file table, folder tree
 * and location field. The action has no effect when the focus is not in the MainFrame this action is tied to.
 *
 * @author Maxence Bernard
 */
public class CycleBackwardThruFolderPanelAction extends MuAction {

    public CycleBackwardThruFolderPanelAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);

        // Perform the action also when in 'no events' mode
        setHonourNoEventsMode(false);
    }

    public void performAction() {
        Component focusOwner = mainFrame.getFocusOwner();

        // Abort if the focus is not in the MainFrame this action is tied to
        if(focusOwner==null)
            return;

        FolderPanel folderPanel = mainFrame.getActivePanel();
        FileTable fileTable = folderPanel.getFileTable();
        JTextField locationField = folderPanel.getLocationTextField();
        JTree tree = folderPanel.getFoldersTreePanel().getTree();

        // Request focus on the 'previous' component, the cycle order being from right to left, bottom to top.
        Component previousComponent;
        if(focusOwner==fileTable)
            previousComponent = folderPanel.isTreeVisible()?(JComponent)tree:locationField;
        else if(focusOwner==tree)
            previousComponent = locationField;
        else if(focusOwner==locationField)
            previousComponent = fileTable;
        else
            return;

        FocusRequester.requestFocusInWindow(previousComponent);
    }
    
    public static class Factory implements ActionFactory {

		public MuAction createAction(MainFrame mainFrame, Hashtable properties) {
			return new CycleBackwardThruFolderPanelAction(mainFrame, properties);
		}
    }
    
    public static class Descriptor extends AbstractActionDescriptor {
    	public static final String ACTION_ID = "CycleBackwardThruFolderPanel";
    	
		public String getId() { return ACTION_ID; }

		public ActionCategory getCategory() { return null; }

		public KeyStroke getDefaultAltKeyStroke() { return null; }

		public KeyStroke getDefaultKeyStroke() { return KeyStroke.getKeyStroke("shift control TAB"); }
    }
}
