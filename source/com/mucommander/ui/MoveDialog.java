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



package com.mucommander.ui;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.file.util.FileSet;
import com.mucommander.job.MoveJob;
import com.mucommander.text.Translator;


/**
 * Dialog invoked when the user wants to move or rename currently selected files.
 *
 * @see com.mucommander.ui.action.MoveAction
 * @see com.mucommander.ui.action.RenameAction
 * @author Maxence Bernard
 */
public class MoveDialog extends DestinationDialog {

	
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
        // Append filename to destination path if there is only one file to move
        // and if the file is not a directory that already exists in destination
        // (otherwise folder would be moved inside the destination folder)
        int nbFiles = files.size();
        if(nbFiles==1) {
            AbstractFile file = ((AbstractFile)files.elementAt(0));
            AbstractFile testFile;
            // TODO: find a way to remove this AbstractFile.getFile() which can lock the main thread if the file is on a remote filesystem
            startPosition = fieldText.length();
            if(!(file.isDirectory() && (testFile=FileFactory.getFile(fieldText+file.getName()))!=null && testFile.exists() && testFile.isDirectory())) {
                endPosition = file.getName().indexOf('.');
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



    /**
     * Starts a MoveJob. This method is trigged by the 'OK' button or return key.
     */
    protected void startJob(AbstractFile destFolder, String newName, int defaultFileExistsAction) {
        /*
        // Makes sure the source and destination files are not the same.
        if (newName==null || files.getBaseFolder().equals(destFolder) || files.contains(destFolder)) {
            showErrorDialog(Translator.get("same_source_destination"));
            return;
        }
        */
		
        // Starts moving files
        ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("move_dialog.moving"));
        MoveJob moveJob = new MoveJob(progressDialog, mainFrame, files, destFolder, newName, defaultFileExistsAction, false);
        progressDialog.start(moveJob);
    }
	
}
