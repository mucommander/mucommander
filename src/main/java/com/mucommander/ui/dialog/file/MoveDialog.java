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

import com.mucommander.commons.file.util.FileSet;
import com.mucommander.commons.file.util.PathUtils;
import com.mucommander.job.MoveJob;
import com.mucommander.job.TransferFileJob;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.action.impl.MoveAction;
import com.mucommander.ui.main.MainFrame;


/**
 * Dialog invoked when the user wants to move or rename currently selected files.
 *
 * @see com.mucommander.ui.action.impl.MoveAction
 * @see com.mucommander.ui.action.impl.RenameAction
 * @author Maxence Bernard
 */
public class MoveDialog extends AbstractCopyDialog {

    public MoveDialog(MainFrame mainFrame, FileSet files) {
        super(mainFrame, files,
                ActionProperties.getActionLabel(MoveAction.Descriptor.ACTION_ID),
                Translator.get("move_dialog.move_description"),
                Translator.get("move"),
                Translator.get("move_dialog.error_title"));
    }


    //////////////////////////////////////////////
    // TransferDestinationDialog implementation //
    //////////////////////////////////////////////

    @Override
    protected TransferFileJob createTransferFileJob(ProgressDialog progressDialog, PathUtils.ResolvedDestination resolvedDest, int defaultFileExistsAction) {
        return new MoveJob(
                progressDialog,
                mainFrame,
                files,
                resolvedDest.getDestinationFolder(),
                resolvedDest.getDestinationType()==PathUtils.ResolvedDestination.EXISTING_FOLDER?null:resolvedDest.getDestinationFile().getName(),
                defaultFileExistsAction,
                false);
    }

    @Override
    protected String getProgressDialogTitle() {
        return Translator.get("move_dialog.moving");
    }
}
