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

import com.mucommander.file.util.FileSet;
import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategories;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.InvokesDialog;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.action.ActionFactory;
import com.mucommander.ui.dialog.file.CalculateChecksumDialog;
import com.mucommander.ui.main.MainFrame;

import java.util.Hashtable;

import javax.swing.KeyStroke;

/**
 * This action invokes the {@link com.mucommander.ui.dialog.file.CalculateChecksumDialog} which allows to calculate
 * the checksum of the selected files and store the results in a pseudo-standard checksum file. 
 *
 * @author Maxence Bernard
 */
public class CalculateChecksumAction extends SelectedFilesAction implements InvokesDialog  {

    public CalculateChecksumAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    public void performAction() {
        FileSet files = mainFrame.getActiveTable().getSelectedFiles();

        // Invoke job dialog only if at least one file is selected/marked
        if(files.size()>0)
            new CalculateChecksumDialog(mainFrame, files);
    }
    
    public static class Factory implements ActionFactory {

		public MuAction createAction(MainFrame mainFrame, Hashtable properties) {
			return new CalculateChecksumAction(mainFrame, properties);
		}
    }
    
    public static class Descriptor extends AbstractActionDescriptor {
    	public static final String ACTION_ID = "CalculateChecksum";
    	
		public String getId() { return ACTION_ID; }

		public ActionCategory getCategory() { return ActionCategories.FILES; }

		public KeyStroke getDefaultAltKeyStroke() { return null; }

		public KeyStroke getDefaultKeyStroke() { return KeyStroke.getKeyStroke("shift alt K"); }
    }
}
