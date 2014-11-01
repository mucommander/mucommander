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

import com.mucommander.commons.file.AbstractArchiveEntryFile;
import com.mucommander.commons.file.AbstractArchiveFile;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.ArchiveEntry;
import com.mucommander.commons.file.util.FileSet;
import com.mucommander.commons.file.util.PathUtils;
import com.mucommander.job.CopyJob;
import com.mucommander.job.TransferFileJob;
import com.mucommander.job.UnpackJob;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.action.impl.CopyAction;
import com.mucommander.ui.main.MainFrame;

import java.util.List;
import java.util.Vector;


/**
 * Dialog invoked when the user wants to copy currently selected files. The destination field is pre-filled with
 * the 'other' panel's path and, if there is only one file to copy, with the source file's name.
 *
 * @see com.mucommander.ui.action.impl.CopyAction
 * @author Maxence Bernard
 */
public class CopyDialog extends AbstractCopyDialog {

    /**
     * Creates a new <code>CopyDialog</code>.
     *
     * @param mainFrame the main frame that spawned this dialog.
     * @param files files to be copied
     */
    public CopyDialog(MainFrame mainFrame, FileSet files) {
        super(mainFrame, files,
              ActionProperties.getActionLabel(CopyAction.Descriptor.ACTION_ID),
              Translator.get("copy_dialog.destination"),
              Translator.get("copy"),
              Translator.get("copy_dialog.error_title"));
    }


    //////////////////////////////////////////////
    // TransferDestinationDialog implementation //
    //////////////////////////////////////////////

    @Override
    protected TransferFileJob createTransferFileJob(ProgressDialog progressDialog, PathUtils.ResolvedDestination resolvedDest, int defaultFileExistsAction) {
        AbstractFile baseFolder = files.getBaseFolder();
        AbstractArchiveFile parentArchiveFile = baseFolder.getParentArchive();
        TransferFileJob job;
        String newName = resolvedDest.getDestinationType()==PathUtils.ResolvedDestination.EXISTING_FOLDER?null:resolvedDest.getDestinationFile().getName();

        // If the source files are located inside an archive, use UnpackJob instead of CopyJob to unpack archives in
        // their natural order (more efficient)
        if(parentArchiveFile!=null) {
            // Add all selected archive entries to a vector
            int nbFiles = files.size();
            List<ArchiveEntry> selectedEntries = new Vector<ArchiveEntry>();
            for(int i=0; i<nbFiles; i++) {
                selectedEntries.add((ArchiveEntry)files.elementAt(i).getAncestor(AbstractArchiveEntryFile.class).getUnderlyingFileObject());
            }

            job = new UnpackJob(
                progressDialog,
                mainFrame,
                parentArchiveFile,
                PathUtils.getDepth(baseFolder.getAbsolutePath(), baseFolder.getSeparator()) - PathUtils.getDepth(parentArchiveFile.getAbsolutePath(), parentArchiveFile.getSeparator()),
                resolvedDest.getDestinationFolder(),
                newName,
                defaultFileExistsAction,
                selectedEntries
            );
        }
        else {
            job = new CopyJob(
                progressDialog,
                mainFrame,
                files,
                resolvedDest.getDestinationFolder(),
                newName,
                CopyJob.COPY_MODE,
                defaultFileExistsAction);
        }

        return job;
    }

    @Override
    protected String getProgressDialogTitle() {
        return Translator.get("copy_dialog.copying");
    }
}
