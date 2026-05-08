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
import dev.barebones.commander.job.impl.TransferFileJob;
import dev.barebones.commander.job.impl.UnpackJob;
import dev.barebones.commander.text.Translator;
import dev.barebones.commander.ui.action.ActionProperties;
import dev.barebones.commander.ui.main.MainFrame;


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
     */
    public UnpackDialog(MainFrame mainFrame, FileSet files) {
        super(mainFrame, files,
        	  ActionProperties.getActionLabel(ActionType.Unpack),
              Translator.get("unpack_dialog.destination"),
              Translator.get("unpack"),
              Translator.get("unpack_dialog.error_title"),
              true);
    }

    
    //////////////////////////////////////////////
    // TransferDestinationDialog implementation //
    //////////////////////////////////////////////

    @Override
    protected PathFieldContent computeInitialPath(FileSet files) {
        return new PathFieldContent(mainFrame.getInactivePanel().getCurrentFolder().getAbsolutePath(true));
    }

    @Override
    protected TransferFileJob createTransferFileJob(ProgressDialog progressDialog, PathUtils.ResolvedDestination resolvedDest, FileCollisionDialog.FileCollisionAction defaultFileExistsAction) {
        DestinationType destinationType = resolvedDest.getDestinationType();
        if(destinationType==DestinationType.EXISTING_FILE) {
            showErrorDialog(Translator.get("invalid_path", resolvedDest.getDestinationFile().getAbsolutePath()));
            return null;
        }

        return new UnpackJob(
                progressDialog,
                mainFrame,
                files,
                destinationType==DestinationType.NEW_FILE?resolvedDest.getDestinationFile():resolvedDest.getDestinationFolder(),
                defaultFileExistsAction);
    }

    @Override
    protected String getProgressDialogTitle() {
        return Translator.get("unpack_dialog.unpacking");
    }
}
