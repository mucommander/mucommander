/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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

package com.mucommander.ui.action.impl;

import com.mucommander.command.Command;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileOperation;
import com.mucommander.file.filter.AndFileFilter;
import com.mucommander.file.filter.AttributeFileFilter;
import com.mucommander.file.filter.FileOperationFilter;
import com.mucommander.file.impl.local.LocalFile;
import com.mucommander.job.TempOpenWithJob;
import com.mucommander.process.ProcessRunner;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.InformationDialog;
import com.mucommander.ui.dialog.file.ProgressDialog;
import com.mucommander.ui.main.MainFrame;

import java.util.Hashtable;

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
    public AbstractViewerAction(MainFrame mainFrame, Hashtable<String,Object> properties) {
        super(mainFrame, properties);

        // Enable this action only if the currently selected file is not a directory and can be read.
        setSelectedFileFilter(new AndFileFilter(
            new FileOperationFilter(FileOperation.READ_FILE),
            new AttributeFileFilter(AttributeFileFilter.DIRECTORY, true)
        ));
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
        if(file != null) {
            customCommand = getCustomCommand();


            // If we're using a custom command...
            if(customCommand != null) {
                // If it's local, run the custom editor on it.
                if(file.hasAncestor(LocalFile.class)) {
                    try {ProcessRunner.execute(customCommand.getTokens(file), file);}
                    catch(Exception e) {
                        InformationDialog.showErrorDialog(mainFrame);}
                }
                
                // If it's distant, copies it locally before running the custom editor on it.
                else {
                    ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("copy_dialog.copying"));
                    TempOpenWithJob job = new TempOpenWithJob(progressDialog, mainFrame, file, customCommand);
                    progressDialog.start(job);
                }
            }
            // If we're not using a custom editor, this action behaves exactly like its parent.
            else
                performInternalAction(file);
        }
    }



    // - Abstract methods ----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Opens the specified file without a custom command.
     * @param file file to open.
     */
    protected abstract void performInternalAction(AbstractFile file);

    protected abstract Command getCustomCommand();
}
