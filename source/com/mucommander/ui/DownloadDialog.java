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
import com.mucommander.file.util.FileSet;
import com.mucommander.job.CopyJob;
import com.mucommander.text.Translator;


/**
 * Dialog invoked when the user wants to download a file.
 *
 * @author Maxence Bernard
 */
public class DownloadDialog extends DestinationDialog {

    private FileSet files;
	
	
    public DownloadDialog(MainFrame mainFrame, FileSet files) {
        super(mainFrame, files,
              Translator.get("download_dialog.download"),
              Translator.get("download_dialog.description"),
              Translator.get("download_dialog.download"),
              Translator.get("download_dialog.error_title"));

        this.files = files;
        AbstractFile file = (AbstractFile)files.elementAt(0);
		
        //		AbstractFile activeFolder = mainFrame.getActiveTable().getCurrentFolder();
        AbstractFile unactiveFolder = mainFrame.getInactiveTable().getCurrentFolder();
        // Fill text field with current folder's absolute path and file name
        setTextField(unactiveFolder.getAbsolutePath(true)+file.getName());
        showDialog();
    }

	
    protected void startJob(AbstractFile destFolder, String newName, int defaultFileExistsAction) {
        // Starts moving files
        ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("download_dialog.downloading"));
        CopyJob downloadJob = new CopyJob(progressDialog, mainFrame, files, destFolder, newName, CopyJob.DOWNLOAD_MODE, defaultFileExistsAction);
        progressDialog.start(downloadJob);
    }
	
}
