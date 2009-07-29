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

import com.mucommander.desktop.DesktopManager;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.ArchiveEntryFile;
import com.mucommander.file.FileProtocols;
import com.mucommander.job.TempExecJob;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategories;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.action.ActionFactory;
import com.mucommander.ui.dialog.ErrorDialog;
import com.mucommander.ui.dialog.file.ProgressDialog;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.quicklist.RecentExecutedFilesQL;

import java.io.IOException;
import java.util.Hashtable;

import javax.swing.KeyStroke;

/**
 * This action opens the currently selected file or folder with native file associations.
 *
 * @author Maxence Bernard
 */
public class OpenNativelyAction extends MuAction {

    public OpenNativelyAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    public void performAction() {
        AbstractFile selectedFile = mainFrame.getActiveTable().getSelectedFile(true, true);

        if(selectedFile==null)
            return;

        // Copy file to a temporary local file and execute it with native file associations if
        // file is not on a local filesystem or file is an archive entry
        if(!FileProtocols.FILE.equals(selectedFile.getURL().getScheme()) || selectedFile.hasAncestor(ArchiveEntryFile.class)) {
            ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("copy_dialog.copying"));
            TempExecJob job = new TempExecJob(progressDialog, mainFrame, selectedFile);
            progressDialog.start(job);
        }
        else {
            // Tries to execute file with native file associations
            try {
            	DesktopManager.open(selectedFile);
            	RecentExecutedFilesQL.addFile(selectedFile);
        	}
            catch(IOException e) {
                ErrorDialog.showErrorDialog(mainFrame);
            }
        }
    }
    
    public static class Factory implements ActionFactory {

		public MuAction createAction(MainFrame mainFrame, Hashtable properties) {
			return new OpenNativelyAction(mainFrame, properties);
		}
    }
    
    public static class Descriptor extends AbstractActionDescriptor {
    	public static final String ACTION_ID = "OpenNatively";
    	
		public String getId() { return ACTION_ID; }

		public ActionCategory getCategory() { return ActionCategories.NEVIGATION; }

		public KeyStroke getDefaultAltKeyStroke() { return null; }

		public KeyStroke getDefaultKeyStroke() { return KeyStroke.getKeyStroke("shift ENTER"); }
    }
}
