/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (c) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with muCommander; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.mucommander.ui.action;

import com.mucommander.file.*;
import com.mucommander.file.impl.local.LocalFile;
import com.mucommander.ui.ProgressDialog;
import com.mucommander.text.Translator;
import com.mucommander.job.TempOpenWithJob;
import com.mucommander.ui.MainFrame;
import com.mucommander.conf.*;
import com.mucommander.command.*;
import com.mucommander.process.*;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.file.util.ResourceLoader;

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
