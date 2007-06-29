/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (c) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with muCommander; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.mucommander.ui.action;

import com.mucommander.Debug;
import com.mucommander.command.Command;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.file.FileProtocols;
import com.mucommander.file.impl.local.LocalFile;
import com.mucommander.job.TempCommandJob;
import com.mucommander.process.ProcessRunner;
import com.mucommander.text.Translator;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.ProgressDialog;
import com.mucommander.ui.table.FileTable;

import java.util.Hashtable;

/**
 * @author Nicolas Rinaudo
 */
public class CommandAction extends MucoAction {
    private Command command;

    public CommandAction(MainFrame mainFrame, Hashtable properties, Command command) {
        super(mainFrame, properties, false);
        this.command = command;
        setLabel(command.getDisplayName());
    }


    public void performAction() {
        FileTable fileTable = mainFrame.getActiveTable();
        AbstractFile selectedFile = fileTable.getSelectedFile(true);

        if(selectedFile == null)
            return;

        if(selectedFile.getURL().getProtocol().equals(FileProtocols.FILE) && (selectedFile instanceof LocalFile)) {
            try {ProcessRunner.execute(command.getTokens(selectedFile), selectedFile);}
            catch(Exception e) {
                if(Debug.ON) {
                    Debug.trace("Failed to execute command: " + command.getCommand());
                    Debug.trace(e);
                }
            }
        }
        else {
            ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("copy_dialog.copying"));
            TempCommandJob job = new TempCommandJob(progressDialog, mainFrame, selectedFile, FileFactory.getTemporaryFile(selectedFile.getName(), true), command);
            progressDialog.start(job);
        }
    }
}
