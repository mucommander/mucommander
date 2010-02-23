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
import com.mucommander.file.filter.AndFileFilter;
import com.mucommander.file.filter.AttributeFileFilter;
import com.mucommander.file.filter.FileOperationFilter;
import com.mucommander.ui.action.*;
import com.mucommander.ui.dialog.file.SplitFileDialog;
import com.mucommander.ui.main.MainFrame;

import javax.swing.KeyStroke;
import java.util.Hashtable;

/**
 * This action invokes the split file dialog which allows to split the selected file into several parts.
 *
 * @author Mariusz Jakubowski
 */
public class SplitFileAction extends SelectedFileAction implements InvokesDialog {

    public SplitFileAction(MainFrame mainFrame, Hashtable<String,Object> properties) {
        super(mainFrame, properties);

        setSelectedFileFilter(new AndFileFilter(
            new AttributeFileFilter(AttributeFileFilter.DIRECTORY, true),
            new FileOperationFilter(FileOperation.READ_FILE)
        ));
    }

    @Override
    public void performAction() {
        new SplitFileDialog(mainFrame,
                mainFrame.getActiveTable().getSelectedFile(),
                mainFrame.getInactivePanel().getCurrentFolder()
        ).showDialog();
    }

    public static class Factory implements ActionFactory {

		public MuAction createAction(MainFrame mainFrame, Hashtable<String,Object> properties) {
			return new SplitFileAction(mainFrame, properties);
		}
    }
    
    public static class Descriptor extends AbstractActionDescriptor {
    	public static final String ACTION_ID = "SplitFile";
    	
		public String getId() { return ACTION_ID; }

		public ActionCategory getCategory() { return null; }

		public KeyStroke getDefaultAltKeyStroke() { return null; }

		public KeyStroke getDefaultKeyStroke() { return null; }
    }
}
