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

import com.mucommander.file.AbstractFile;
import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategories;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.action.ActionFactory;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.table.FileTable;

import java.util.Hashtable;

import javax.swing.KeyStroke;

/**
 * Opens browsable files in the inactive panel.
 * <p>
 * This action is only enabled if the current selection is browsable as defined by
 * {@link com.mucommander.file.AbstractFile#isBrowsable()}.
 * </p>
 * @author Nicolas Rinaudo
 */
public class OpenInOtherPanelAction extends SelectedFileAction {
    /**
     * Creates a new <code>OpenInOtherPanelAction</code> with the specified parameters.
     * @param mainFrame  frame to which the action is attached.
     * @param properties action's properties.
     */
    public OpenInOtherPanelAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    /**
     * This method is overridden to enable this action when the parent folder is selected.
     */
    protected boolean getFileTableCondition(FileTable fileTable) {
        AbstractFile selectedFile = fileTable.getSelectedFile(true, true);

        return selectedFile!=null && selectedFile.isBrowsable();
    }

    /**
     * Opens the currently selected file in the inactive folder panel.
     */
    public void performAction() {
        AbstractFile file;

        // Retrieves the currently selected file, aborts if none (should not normally happen).
        if((file = mainFrame.getActiveTable().getSelectedFile(true, true)) == null || !file.isBrowsable())
            return;

        // Opens the currently selected file in the inactive panel.
        mainFrame.getInactivePanel().tryChangeCurrentFolder(file);
    }
    
    public static class Factory implements ActionFactory {

		public MuAction createAction(MainFrame mainFrame, Hashtable properties) {
			return new OpenInOtherPanelAction(mainFrame, properties);
		}
    }
    
    public static class Descriptor extends AbstractActionDescriptor {
    	public static final String ACTION_ID = "OpenInOtherPanel";
    	
		public String getId() { return ACTION_ID; }

		public ActionCategory getCategory() { return ActionCategories.NAVIGATION; }

		public KeyStroke getDefaultAltKeyStroke() { return null; }

		public KeyStroke getDefaultKeyStroke() { return KeyStroke.getKeyStroke("control ENTERS"); }
    }
}
