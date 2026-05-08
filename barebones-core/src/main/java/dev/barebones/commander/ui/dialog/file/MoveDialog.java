/*
 * This file is part of muCommander, http://www.mucommander.com
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



package dev.barebones.commander.ui.dialog.file;

import dev.barebones.commander.commons.file.util.DestinationType;
import dev.barebones.commander.commons.file.util.FileSet;
import dev.barebones.commander.commons.file.util.PathUtils;
import dev.barebones.commander.desktop.ActionType;
import dev.barebones.commander.job.impl.MoveJob;
import dev.barebones.commander.job.impl.TransferFileJob;
import dev.barebones.commander.text.Translator;
import dev.barebones.commander.ui.action.ActionProperties;
import dev.barebones.commander.ui.action.impl.MoveAction;
import dev.barebones.commander.ui.main.MainFrame;


/**
 * Dialog invoked when the user wants to move or rename currently selected files.
 *
 * @see dev.barebones.commander.ui.action.impl.MoveAction
 * @see dev.barebones.commander.ui.action.impl.RenameAction
 * @author Maxence Bernard
 */
public class MoveDialog extends AbstractCopyDialog {

    public MoveDialog(MainFrame mainFrame, FileSet files) {
        super(mainFrame, files,
                ActionProperties.getActionLabel(ActionType.Move),
                Translator.get("move_dialog.move_description"),
                Translator.get("move"),
                Translator.get("move_dialog.error_title"));
    }


    //////////////////////////////////////////////
    // TransferDestinationDialog implementation //
    //////////////////////////////////////////////

    @Override
    protected TransferFileJob createTransferFileJob(ProgressDialog progressDialog, PathUtils.ResolvedDestination resolvedDest, FileCollisionDialog.FileCollisionAction defaultFileExistsAction) {
        return new MoveJob(
                progressDialog,
                mainFrame,
                files,
                resolvedDest.getDestinationFolder(),
                resolvedDest.getDestinationType() == DestinationType.EXISTING_FOLDER ? null : resolvedDest.getDestinationFile().getName(),
                defaultFileExistsAction,
                false);
    }

    @Override
    protected String getProgressDialogTitle() {
        return Translator.get("move_dialog.moving");
    }
}
