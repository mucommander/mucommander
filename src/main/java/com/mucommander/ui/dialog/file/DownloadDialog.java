/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.util.FileSet;
import com.mucommander.commons.file.util.PathUtils;
import com.mucommander.job.CopyJob;
import com.mucommander.job.TransferFileJob;
import com.mucommander.text.Translator;
import com.mucommander.ui.main.MainFrame;


/**
 * Dialog invoked when the user wants to download a file.
 *
 * @author Maxence Bernard
 */
public class DownloadDialog extends TransferDestinationDialog {

    public DownloadDialog(MainFrame mainFrame, FileSet files) {
        super(mainFrame, files,
              Translator.get("download_dialog.download"),
              Translator.get("download_dialog.description"),
              Translator.get("download_dialog.download"),
              Translator.get("download_dialog.error_title"),
              true);
    }

    
    //////////////////////////////////////////////
    // TransferDestinationDialog implementation //
    //////////////////////////////////////////////

    @Override
    protected PathFieldContent computeInitialPath(FileSet files) {
        AbstractFile file = files.elementAt(0);

        //		AbstractFile activeFolder = mainFrame.getActiveTable().getCurrentFolder();
        AbstractFile unactiveFolder = mainFrame.getInactivePanel().getCurrentFolder();
        // Fill text field with current folder's absolute path and file name
        return new PathFieldContent(unactiveFolder.getAbsolutePath(true)+file.getName());
    }

    @Override
    protected TransferFileJob createTransferFileJob(ProgressDialog progressDialog, PathUtils.ResolvedDestination resolvedDest, int defaultFileExistsAction) {
        return new CopyJob(
                progressDialog,
                mainFrame,
                files,
                resolvedDest.getDestinationFolder(),
                resolvedDest.getDestinationType()==PathUtils.ResolvedDestination.EXISTING_FOLDER?null:resolvedDest.getDestinationFile().getName(),
                CopyJob.DOWNLOAD_MODE,
                defaultFileExistsAction);
    }

    @Override
    protected String getProgressDialogTitle() {
        return Translator.get("download_dialog.downloading");
    }
}
