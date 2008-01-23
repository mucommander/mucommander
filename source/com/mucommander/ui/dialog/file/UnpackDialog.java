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
import com.mucommander.file.util.FileSet;
import com.mucommander.job.CopyJob;
import com.mucommander.text.Translator;
import com.mucommander.ui.main.MainFrame;


/**
 * Dialog that allows the user to choose the destination to unpack files to.
 *
 * @author Maxence Bernard
 */
public class UnpackDialog extends TransferDestinationDialog {

    /**
     * Creates and displays a new UnpackDialog.
     *
     * @param mainFrame the main frame this dialog is attached to
     * @param files the set of files to unpack
     * @param isShiftDown true if shift key was pressed when invoking this dialog
     */
    public UnpackDialog(MainFrame mainFrame, FileSet files, boolean isShiftDown) {
        super(mainFrame, files,
              Translator.get(com.mucommander.ui.action.UnpackAction.class.getName()+".label"),
              Translator.get("unpack_dialog.destination"),
              Translator.get("unpack_dialog.unpack"),
              Translator.get("unpack_dialog.error_title"));
	    
        AbstractFile destFolder = mainFrame.getInactiveTable().getCurrentFolder();
        String fieldText;
        if(isShiftDown)
            fieldText = ".";
        else
            fieldText = destFolder.getAbsolutePath(true);
		
        setTextField(fieldText);
		
        showDialog();
    }

    protected void startJob(AbstractFile destFolder, String newName, int defaultFileExistsAction, boolean verifyIntegrity) {
        ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("unpack_dialog.unpacking"));

        CopyJob job = new CopyJob(progressDialog, mainFrame, files, destFolder, newName, CopyJob.UNPACK_MODE, defaultFileExistsAction);
        job.setIntegrityCheckEnabled(verifyIntegrity);

        progressDialog.start(job);
    }

}
