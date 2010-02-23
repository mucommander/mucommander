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

import com.mucommander.file.FileOperation;
import com.mucommander.file.filter.FileOperationFilter;
import com.mucommander.file.util.FileSet;
import com.mucommander.ui.action.*;
import com.mucommander.ui.dialog.file.DeleteDialog;
import com.mucommander.ui.main.MainFrame;

import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import java.util.Hashtable;

/**
 * This action invokes a Delete confirmation dialog to delete currently the selected / marked files
 * in the currently active folder. Files are moved to the system trash when possible, i.e. if there is a trash available
 * on the current OS environment, and if the selected files are on a filesystem that allows it (usually only local files
 * can be moved to the trash).
 *
 * @see com.mucommander.ui.action.impl.PermanentDeleteAction
 * @author Maxence Bernard
 */
public class DeleteAction extends SelectedFilesAction {

    public DeleteAction(MainFrame mainFrame, Hashtable<String,Object> properties) {
        super(mainFrame, properties);

        setSelectedFileFilter(new FileOperationFilter(FileOperation.DELETE));
    }

    @Override
    public void performAction() {
        FileSet files = mainFrame.getActiveTable().getSelectedFiles();
        // Invoke confirmation dialog only if at least one file is selected/marked
        if(files.size()>0)
            new DeleteDialog(mainFrame, files, false).showDialog();
    }

    public static class Factory implements ActionFactory {

		public MuAction createAction(MainFrame mainFrame, Hashtable<String,Object> properties) {
			return new DeleteAction(mainFrame, properties);
		}
    }
    
    public static class Descriptor extends AbstractActionDescriptor {
    	public static final String ACTION_ID = "Delete";
    	
		public String getId() { return ACTION_ID; }

		public ActionCategory getCategory() { return ActionCategories.FILES; }

		public KeyStroke getDefaultAltKeyStroke() { return KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0); }

		public KeyStroke getDefaultKeyStroke() { return KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0); }
    }
}
