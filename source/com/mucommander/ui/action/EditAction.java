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
import com.mucommander.command.CommandParser;
import com.mucommander.conf.ConfigurationEvent;
import com.mucommander.conf.ConfigurationListener;
import com.mucommander.conf.ConfigurationManager;
import com.mucommander.conf.ConfigurationVariables;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.file.FileProtocols;
import com.mucommander.file.impl.local.LocalFile;
import com.mucommander.file.util.ResourceLoader;
import com.mucommander.job.TempOpenWithJob;
import com.mucommander.process.ProcessRunner;
import com.mucommander.text.Translator;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.ProgressDialog;
import com.mucommander.ui.icon.IconManager;

import java.util.Hashtable;

/**
 * Customisable version of {@link InternalEditAction}.
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public class EditAction extends InternalEditAction implements ConfigurationListener {
    // - Instance variables --------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Custom editor defined in the configuration. */
    private Command customEditor;
    /** Whether or not to use the custom editor. */
    private boolean useCustomEditor;



    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new <code>EditAction</code>.
     */
    public EditAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);

        // Initialises the icon
        String iconPath;
        iconPath = getIconPath(InternalEditAction.class);
        if(ResourceLoader.getResource(iconPath)!=null)
            setIcon(IconManager.getIcon(iconPath));

        // Initialises configuration.
        useCustomEditor = ConfigurationManager.getVariableBoolean(ConfigurationVariables.USE_CUSTOM_EDITOR, ConfigurationVariables.DEFAULT_USE_CUSTOM_EDITOR);
        setCustomEditor(ConfigurationManager.getVariable(ConfigurationVariables.CUSTOM_EDITOR));
    }



    // - Action execution ----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Edits the currently selected file.
     */
    public synchronized void performAction() {
        // If we're using a custom editor...
        if(useCustomEditor) {
            AbstractFile file;

            file = mainFrame.getActiveTable().getSelectedFile();
            // If the file is editable...
            if(file != null && !(file.isDirectory() || file.isSymlink())) {
                // If it's local, run the custom editor on it.
                if(file.getURL().getProtocol().equals(FileProtocols.FILE) && (file instanceof LocalFile)) {
                    try {ProcessRunner.execute(customEditor.getTokens(file), file);}
                    catch(Exception e) {}
                }
                // If it's distant, copies it locally before running the custom editor on it.
                else {
                    ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("copy_dialog.copying"));
                    TempOpenWithJob job = new TempOpenWithJob(progressDialog, mainFrame, file, FileFactory.getTemporaryFile(file.getName(), true), customEditor);
                    progressDialog.start(job);
                }
            }
        }
        // If we're not using a custom editor, this action behaves exactly like its parent.
        else
            super.performAction();
    }



    // - Configuration management --------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Reacts to configuration changed events.
     * @param event describes the configuration change.
     */
    public synchronized boolean configurationChanged(ConfigurationEvent event) {
        // Updates useCustomEditor.
        if(event.getVariable().equals(ConfigurationVariables.USE_CUSTOM_EDITOR))
            useCustomEditor = event.getBooleanValue();
        // Updates customEditor.
        else if(event.getVariable().equals(ConfigurationVariables.CUSTOM_EDITOR))
            setCustomEditor(event.getValue());
        return true;
    }

    /**
     * Sets the custom editor to the specified command.
     * @param command command to use as a custom editor.
     */
    private void setCustomEditor(String command) {
        if(command == null)
            customEditor = null;
        else
            customEditor = CommandParser.getCommand("edit", command);
    }
}
