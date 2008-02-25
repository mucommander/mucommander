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

package com.mucommander.ui.action;

import com.mucommander.command.Command;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileProtocols;
import com.mucommander.file.filter.AttributeFileFilter;
import com.mucommander.file.impl.local.LocalFile;
import com.mucommander.job.TempOpenWithJob;
import com.mucommander.process.ProcessRunner;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.file.ProgressDialog;
import com.mucommander.ui.main.MainFrame;

import java.util.Hashtable;

/**
 * Provides a common base for viewer and editor actions.
 * @author Maxence Bernard, Nicolas Rinaudo
 */
abstract class AbstractViewerAction extends SelectedFileAction {
    // - Property names ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Key that controls whether or not to use the custom command. */
    public static final String USE_CUSTOM_COMMAND_PROPERTY_KEY = "use_custom";
    /** Key that controls the command that will be used to open files. */
    public static final String CUSTOM_COMMAND_PROPERTY_KEY     = "custom_command";



    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new instance of <code>AbstractViewerAction</code>.
     * @param mainFrame  frame to which the action is attached.
     * @param properties action's properties.
     */
    public AbstractViewerAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);

        // Enable this action only when the currently selected file is not a directory.
        setSelectedFileFilter(new AttributeFileFilter(AttributeFileFilter.DIRECTORY, true));
    }



    // - Property retrieval --------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Returns <code>true</code> if this action should use custom command.
     * @return <code>true</code> if this action should use custom command, <code>false</code> otherwise.
     */
    protected boolean useCustomCommand() {
        Object o;

        if((o = getValue(USE_CUSTOM_COMMAND_PROPERTY_KEY)) == null)
            return false;
        if(o instanceof Boolean)
            return ((Boolean)o).booleanValue();
        return false;
    }

    /**
     * Returns the command that should be used to open files.
     * @return the command that should be used to open files, or <code>false</code> if none.
     */
    protected Command getCustomCommand() {
        Object o;

        if((o = getValue(CUSTOM_COMMAND_PROPERTY_KEY)) == null)
            return null;
        if(o instanceof Command)
            return (Command)o;
        return null;
    }

    /**
     * Sets the command to use to open files.
     * @param command command that will be used to open files.
     */
    protected void setCustomCommand(String command) {putValue(CUSTOM_COMMAND_PROPERTY_KEY, command == null ? null : new Command(getClass().getName(), command));}

    /**
     * Sets whether or not to use custom commands to open files.
     * @param use whether or not to use custom commands.
     */
    protected void setUseCustomCommand(boolean use) {putValue(USE_CUSTOM_COMMAND_PROPERTY_KEY, new Boolean(use));}



    // - AbstractAction implementation ---------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Edits the currently selected file.
     */
    public synchronized void performAction() {
        AbstractFile file;
        boolean      useCustomCommand;
        Command      customCommand;

        file = mainFrame.getActiveTable().getSelectedFile();

        // At this stage, no assumption should be made on the type of file that is allowed to be viewed/edited:
        // viewer/editor implementations will decide whether they allow a particular file or not.
        if(file!=null) {
            useCustomCommand = useCustomCommand();
            customCommand    = getCustomCommand();

            // If we're using a custom editor...
            if(useCustomCommand && customCommand != null) {
                // If it's local, run the custom editor on it.
                if(file.getURL().getProtocol().equals(FileProtocols.FILE) && (file instanceof LocalFile)) {
                    try {ProcessRunner.execute(customCommand.getTokens(file), file);}
                    catch(Exception e) {reportGenericError();}
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
    public abstract void performInternalAction(AbstractFile file);
}
