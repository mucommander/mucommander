/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
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



package com.mucommander.ui.dialog.file;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.file.util.FileSet;
import com.mucommander.job.MoveJob;
import com.mucommander.text.Translator;
import com.mucommander.ui.main.MainFrame;


/**
 * Dialog invoked when the user wants to move or rename currently selected files.
 *
 * @see com.mucommander.ui.action.MoveAction
 * @see com.mucommander.ui.action.RenameAction
 * @author Maxence Bernard
 */
public class MoveDialog extends TransferDestinationDialog {

    public MoveDialog(MainFrame mainFrame, FileSet files) {
        super(mainFrame, files);
		
        init(Translator.get("move_dialog.move"),
             Translator.get("move_dialog.move_description"),
             Translator.get("move_dialog.move"),
             Translator.get("move_dialog.error_title"));
        
        String       fieldText;
        int          startPosition;
        int          endPosition;
        AbstractFile destFolder = mainFrame.getInactiveTable().getCurrentFolder();
        fieldText = destFolder.getAbsolutePath(true);
        // Append filename to destination path if there is only one file to copy
        // and if the file is not a directory that already exists in destination
        // (otherwise folder would be copied into the destination folder)
        int nbFiles = files.size();
        if(nbFiles==1) {
            AbstractFile file = ((AbstractFile)files.elementAt(0));
            AbstractFile destFile;
            // TODO: move those I/O bound calls to another thread as they can lock the main thread
            startPosition = fieldText.length();
            if(!(file.isDirectory() && (destFile=FileFactory.getFile(fieldText+file.getName()))!=null && destFile.exists() && destFile.isDirectory())) {
                endPosition = file.getName().lastIndexOf('.');
                if(endPosition > 0)
                    endPosition += startPosition;
                else
                    endPosition  = startPosition + file.getName().length();
                fieldText += file.getName();
            }
            else
                endPosition = fieldText.length();
        }
        else {
            startPosition = 0;
            endPosition   = fieldText.length();
        }

        setTextField(fieldText, startPosition, endPosition);

        showDialog();
    }

    protected void startJob(AbstractFile destFolder, String newName, int defaultFileExistsAction, boolean verifyIntegrity) {
        ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("move_dialog.moving"));

        MoveJob moveJob = new MoveJob(progressDialog, mainFrame, files, destFolder, newName, defaultFileExistsAction, false);
        moveJob.setIntegrityCheckEnabled(verifyIntegrity);

        progressDialog.start(moveJob);
    }
	
}
