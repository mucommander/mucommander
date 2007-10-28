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


package com.mucommander.ui.dialog.file;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.file.util.FileSet;
import com.mucommander.job.CopyJob;
import com.mucommander.text.Translator;
import com.mucommander.ui.main.MainFrame;


/**
 * Dialog invoked when the user wants to copy currently selected files.
 *
 * @see com.mucommander.ui.action.CopyAction
 * @author Maxence Bernard
 */
public class CopyDialog extends DestinationDialog {

    /**
     * Creates and displays a new CopyDialog.
     *
     * @param mainFrame the main frame this dialog is attached to.
     * @param localCopy true if shift key was pressed when invoking this dialog.
     */
    public CopyDialog(MainFrame mainFrame, FileSet files, boolean localCopy) {
        super(mainFrame, files,
              Translator.get("copy_dialog.copy"),
              Translator.get("copy_dialog.destination"),
              Translator.get("copy_dialog.copy"),
              Translator.get("copy_dialog.error_title"));

        String fieldText;     // Text to display in the destination field.
        int    startPosition; // Index of the first selected character in the destination field.
        int    endPosition;   // Index of the last selected character in the destination field.
        int    nbFiles = files.size();

        AbstractFile destFolder = mainFrame.getInactiveTable().getCurrentFolder();

        // Local copy: fill text field with the sole file's name
        if(localCopy) {
            fieldText     = ((AbstractFile)files.elementAt(0)).getName();
            startPosition = 0;
            endPosition   = fieldText.indexOf('.');

            // If the file doesn't have an extension, selection extends to the end of its name.
            if(endPosition <= 0)
                endPosition = fieldText.length();
        }
        // Fill text field with absolute path, and if there is only one file, 
        // append file's name
        else {
            fieldText = destFolder.getAbsolutePath(true);
            // Append filename to destination path if there is only one file to copy
            // and if the file is not a directory that already exists in destination
            // (otherwise folder would be copied into the destination folder)
            if(nbFiles==1) {
                AbstractFile file = ((AbstractFile)files.elementAt(0));
                AbstractFile destFile;

                // TODO: move those I/O bound calls to another thread as they can lock the main thread
                startPosition  = fieldText.length();

                if(!(file.isDirectory() && (destFile=FileFactory.getFile(fieldText+file.getName()))!=null && destFile.exists() && destFile.isDirectory())) {
                    endPosition = file.getName().lastIndexOf('.');
                    if(endPosition > 0)
                        endPosition += startPosition;
                    else
                        endPosition = startPosition + file.getName().length();
                    fieldText += file.getName();
                }
                else
                    endPosition = fieldText.length();
            }
            else {
                endPosition   = fieldText.length();
                startPosition = 0;
            }

        }

        setTextField(fieldText, startPosition, endPosition);
    }


    /**
     * Starts a CopyJob. This method is trigged by the 'OK' button or return key.
     */
    protected void startJob(AbstractFile destFolder, String newName, int defaultFileExistsAction) {
        // Starts copying files
        ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("copy_dialog.copying"));
        CopyJob job = new CopyJob(progressDialog, mainFrame, files, destFolder, newName, CopyJob.COPY_MODE, defaultFileExistsAction);
        progressDialog.start(job);
    }

}
