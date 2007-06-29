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
import com.mucommander.job.CopyJob;
import com.mucommander.text.Translator;


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
            // (otherwise folder would be copied inside the destination folder)
            if(nbFiles==1) {
                AbstractFile file = ((AbstractFile)files.elementAt(0));
                AbstractFile testFile;

                // TODO: find a way to remove this AbstractFile.getFile() which can lock the main thread if the file is on a remote filesystem
                startPosition  = fieldText.length();
                if(!(file.isDirectory() && (testFile= FileFactory.getFile(fieldText+file.getName()))!=null && testFile.exists() && testFile.isDirectory())) {
                    endPosition = file.getName().indexOf('.');
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

        //        showDialog();
    }


    /**
     * Starts a CopyJob. This method is trigged by the 'OK' button or return key.
     */
    protected void startJob(AbstractFile destFolder, String newName, int defaultFileExistsAction) {
        /*
        // Makes sure the source file and destination files are not the same.
        if (newName==null || files.getBaseFolder().equals(destFolder)) {
            showErrorDialog(Translator.get("same_source_destination"));
            return;
        }
        */

        // Starts copying files
        ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("copy_dialog.copying"));
        CopyJob job = new CopyJob(progressDialog, mainFrame, files, destFolder, newName, CopyJob.COPY_MODE, defaultFileExistsAction);
        progressDialog.start(job);
    }

}
