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

package dev.barebones.commander.ui.action.impl;

import java.util.Map;

import dev.barebones.commander.command.Command;
import dev.barebones.commander.commons.file.AbstractFile;
import dev.barebones.commander.commons.file.FileOperation;
import dev.barebones.commander.commons.file.protocol.local.LocalFile;
import dev.barebones.commander.commons.file.filter.FileOperationFilter;
import dev.barebones.commander.commons.file.protocol.search.SearchFile;
import dev.barebones.commander.commons.util.StringUtils;
import dev.barebones.commander.job.impl.TempOpenWithJob;
import dev.barebones.commander.process.ProcessRunner;
import dev.barebones.commander.search.SearchProperty;
import dev.barebones.commander.text.Translator;
import dev.barebones.commander.ui.dialog.InformationDialog;
import dev.barebones.commander.ui.dialog.file.ProgressDialog;
import dev.barebones.commander.ui.main.MainFrame;

/**
 * Provides a common base for viewer and editor actions.
 * @author Maxence Bernard, Nicolas Rinaudo
 */
abstract class AbstractViewerAction extends SelectedFileAction {

    // - Initialization ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new instance of <code>AbstractViewerAction</code>.
     * @param mainFrame  frame to which the action is attached.
     * @param properties action's properties.
     */
    public AbstractViewerAction(MainFrame mainFrame, Map<String,Object> properties) {
        super(mainFrame, properties);

        // Enable this action only if the currently selected file is not a directory and can be read.
        setSelectedFileFilter(new FileOperationFilter(FileOperation.READ_FILE));
    }



    // - AbstractAction implementation ---------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Edits the currently selected file.
     */
    @Override
    public synchronized void performAction() {
        AbstractFile file;
        Command      customCommand;

        file = mainFrame.getActiveTable().getSelectedFile(false, true);

        // At this stage, no assumption should be made on the type of file that is allowed to be viewed/edited:
        // viewer/editor implementations will decide whether they allow a particular file or not.
        if (file != null) {
            customCommand = getCustomCommand();

            // If we're using a custom command...
            if (customCommand != null) {
                // If it's local, run the custom editor on it.
                if (file.hasAncestor(LocalFile.class)) {
                    try {
                        InformationDialog.showErrorDialogIfNeeded(getMainFrame().getJFrame(), ProcessRunner.executeAsync(customCommand.getTokens(file), file));
                    }
                    catch(Exception e) {
                        InformationDialog.showErrorDialog(mainFrame.getJFrame());
                    }
                } else {
                    // If it's distant, copies it locally before running the custom editor on it.
                    ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("copy_dialog.copying"));
                    TempOpenWithJob job = new TempOpenWithJob(progressDialog, mainFrame, file, customCommand);
                    progressDialog.start(job);
                }
            } else {
                // If we're not using a custom editor, this action behaves exactly like its parent.

                boolean fromSearchWithContent = mainFrame.getActivePanel().getCurrentFolder().getURL().getScheme().equals(SearchFile.SCHEMA) &&
                        !StringUtils.isNullOrEmpty(SearchProperty.SEARCH_TEXT.getValue());
                performInternalAction(file, fromSearchWithContent);
            }
        }
    }

    // - Abstract methods ----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Opens the specified file without a custom command.
     * @param file file to open.
     * @param fromSearchWithContent whether file is opened from File Search with Content
     */
    protected abstract void performInternalAction(AbstractFile file, boolean fromSearchWithContent);

    protected abstract Command getCustomCommand();
}
