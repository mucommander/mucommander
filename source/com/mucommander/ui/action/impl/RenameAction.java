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

import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileOperation;
import com.mucommander.file.filter.AndFileFilter;
import com.mucommander.file.filter.FileOperationFilter;
import com.mucommander.file.filter.OrFileFilter;
import com.mucommander.ui.action.*;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.table.FileTable;

import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import java.util.Hashtable;

/**
 * This action triggers in-table renaming of the currently selected file, if no file is marked.
 * If files are marked, it simply invokes 'Move dialog' just like {@link CopyAction}.
 *
 * @author Maxence Bernard
 */
public class RenameAction extends SelectedFileAction {

    public RenameAction(MainFrame mainFrame, Hashtable<String,Object> properties) {
        super(mainFrame, properties);

        setSelectedFileFilter(new OrFileFilter(
            new FileOperationFilter(FileOperation.RENAME),
            new AndFileFilter(
                new FileOperationFilter(FileOperation.READ_FILE),
                new FileOperationFilter(FileOperation.WRITE_FILE)
            )
        ));
    }

    @Override
    public void performAction() {
        FileTable activeTable = mainFrame.getActiveTable();
        AbstractFile selectedFile = activeTable.getSelectedFile(false);

        // Trigger in-table editing only if a file other than parent folder '..' is selected
        if(selectedFile!=null) {
            // Trigger in-table renaming
            activeTable.editCurrentFilename();
        }
    }
    
    public static class Factory implements ActionFactory {

		public MuAction createAction(MainFrame mainFrame, Hashtable<String,Object> properties) {
			return new RenameAction(mainFrame, properties);
		}
    }
    
    public static class Descriptor extends AbstractActionDescriptor {
    	public static final String ACTION_ID = "Rename";
    	
		public String getId() { return ACTION_ID; }

		public ActionCategory getCategory() { return ActionCategories.FILES; }

		public KeyStroke getDefaultAltKeyStroke() { return null; }

		public KeyStroke getDefaultKeyStroke() { return KeyStroke.getKeyStroke(KeyEvent.VK_F6, KeyEvent.SHIFT_DOWN_MASK); }
    }
}
