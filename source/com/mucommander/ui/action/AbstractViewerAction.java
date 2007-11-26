/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
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
import com.mucommander.conf.ConfigurationListener;
import com.mucommander.conf.impl.MuConfiguration;
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
 *
 * @author Maxence Bernard, Nicolas Rinaudo
 */
abstract class AbstractViewerAction extends SelectedFileAction implements ConfigurationListener {

    /** Custom command defined in the configuration. */
    private Command customCommand;
    /** Whether or not to use the custom command. */
    private boolean useCustomCommand;


    public AbstractViewerAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);

        // Enable this action only when the currently selected file is not a directory.
        setSelectedFileFilter(new AttributeFileFilter(AttributeFileFilter.DIRECTORY, true));

        // Listens to configuration.
        MuConfiguration.addConfigurationListener(this);
    }


    /**
     * Edits the currently selected file.
     */
    public synchronized void performAction() {
        AbstractFile file;

        file = mainFrame.getActiveTable().getSelectedFile();
        // If the file is editable...
        if(file != null && !(file.isDirectory() || file.isSymlink())) {
            // If we're using a custom editor...
            if(useCustomCommand && customCommand != null) {
                // If it's local, run the custom editor on it.
                if(file.getURL().getProtocol().equals(FileProtocols.FILE) && (file instanceof LocalFile)) {
                    try {ProcessRunner.execute(customCommand.getTokens(file), file);}
                    catch(Exception e) {}
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

    /**
     * Sets the custom editor to the specified command.
     * @param command command to use as a custom editor.
     */
    protected void setCustomCommand(String command) {
        if(command == null)
            customCommand = null;
        else
            customCommand = new Command(getClass().getName(), command);
    }

    protected void setUseCustomCommand(boolean use) {
        useCustomCommand = use;
    }


    public abstract void performInternalAction(AbstractFile file);
}
