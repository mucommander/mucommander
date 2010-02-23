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
import com.mucommander.ui.dialog.file.EmailFilesDialog;
import com.mucommander.ui.main.MainFrame;

import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import java.util.Hashtable;

/**
 * This action pops up the 'Email files' dialog that allows to email the currently marked files as attachment.
 *
 * @author Maxence Bernard
 */
public class EmailAction extends SelectedFilesAction implements InvokesDialog {

    public EmailAction(MainFrame mainFrame, Hashtable<String,Object> properties) {
        super(mainFrame, properties);

        setSelectedFileFilter(new FileOperationFilter(FileOperation.READ_FILE));
    }

    @Override
    public void performAction() {
        FileSet files = mainFrame.getActiveTable().getSelectedFiles();
        if(files.size()>0)
            new EmailFilesDialog(mainFrame, files).showDialog();
    }
    
    public static class Factory implements ActionFactory {

		public MuAction createAction(MainFrame mainFrame, Hashtable<String,Object> properties) {
			return new EmailAction(mainFrame, properties);
		}
    }
    
    public static class Descriptor extends AbstractActionDescriptor {
    	public static final String ACTION_ID = "Email";
    	
		public String getId() { return ACTION_ID; }

		public ActionCategory getCategory() { return ActionCategories.FILES; }

		public KeyStroke getDefaultAltKeyStroke() { return null; }

		public KeyStroke getDefaultKeyStroke() { return KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK); }
    }
}