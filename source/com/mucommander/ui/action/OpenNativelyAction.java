/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
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

package com.mucommander.ui.action;

import com.mucommander.PlatformManager;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.ArchiveEntryFile;
import com.mucommander.file.FileProtocols;
import com.mucommander.job.TempExecJob;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.file.ProgressDialog;
import com.mucommander.ui.main.MainFrame;

import java.util.Hashtable;
import java.io.IOException;

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
        AbstractFile selectedFile = mainFrame.getActiveTable().getSelectedFile(true);

        if(selectedFile==null)
            return;

        // Copy file to a temporary local file and execute it with native file associations if
        // file is not on a local filesystem or file is an archive entry
        if(!FileProtocols.FILE.equals(selectedFile.getURL().getProtocol()) || selectedFile.hasAncestor(ArchiveEntryFile.class)) {
            ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("copy_dialog.copying"));
            TempExecJob job = new TempExecJob(progressDialog, mainFrame, selectedFile);
            progressDialog.start(job);
        }
        else {
            // Tries to execute file with native file associations
            try {PlatformManager.open(selectedFile);}
            // Ignores errors here are there really isn't anything we can do.
            catch(IOException e) {}
        }
    }
}
