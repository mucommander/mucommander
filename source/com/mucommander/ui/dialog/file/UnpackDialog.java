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


package com.mucommander.ui.dialog.file;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.util.FileSet;
import com.mucommander.file.util.PathUtils;
import com.mucommander.job.UnpackJob;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.action.impl.UnpackAction;
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
        	  ActionProperties.getActionLabel(UnpackAction.Descriptor.ACTION_ID),
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

    protected void startJob(PathUtils.ResolvedDestination resolvedDest, int defaultFileExistsAction, boolean verifyIntegrity) {
        ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("unpack_dialog.unpacking"));

        int destinationType = resolvedDest.getDestinationType();
        if(destinationType==PathUtils.ResolvedDestination.EXISTING_FILE) {
            showErrorDialog(Translator.get("invalid_path", resolvedDest.getDestinationFile().getAbsolutePath()));
            return;
        }

        UnpackJob job = new UnpackJob(
                progressDialog,
                mainFrame,
                files,
                destinationType==PathUtils.ResolvedDestination.NEW_FILE?resolvedDest.getDestinationFile():resolvedDest.getDestinationFolder(),
                defaultFileExistsAction);

        job.setIntegrityCheckEnabled(verifyIntegrity);

        progressDialog.start(job);
    }

}
