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
import com.mucommander.conf.impl.MuConfiguration;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.file.FileProtocols;
import com.mucommander.file.impl.local.LocalFile;
import com.mucommander.file.util.ResourceLoader;
import com.mucommander.job.TempOpenWithJob;
import com.mucommander.process.ProcessRunner;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.file.ProgressDialog;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.main.MainFrame;

import java.util.Hashtable;

/**
 * Customisable version of {@link InternalViewAction}.
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public class ViewAction extends InternalViewAction implements ConfigurationListener {
    // - Instance variables --------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Custom viewer defined in the configuration. */
    private Command customViewer;
    /** Whether or not to use the custom viewer. */
    private boolean useCustomViewer;



    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new <code>ViewAction</code>.
     */
    public ViewAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);

        // Initialises the icon
        String iconPath;
        iconPath = getIconPath(InternalViewAction.class);
        if(ResourceLoader.getResource(iconPath)!=null)
            setIcon(IconManager.getIcon(iconPath));


        // Initialises configuration.
        useCustomViewer = MuConfiguration.getVariable(MuConfiguration.USE_CUSTOM_VIEWER, MuConfiguration.DEFAULT_USE_CUSTOM_VIEWER);
        setCustomViewer(MuConfiguration.getVariable(MuConfiguration.CUSTOM_VIEWER));
    }



    // - Action execution ----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Views the currently selected file.
     */
    public synchronized void performAction() {
        // If we're using a custom viewer...
        if(useCustomViewer) {
            AbstractFile file;

            file = mainFrame.getActiveTable().getSelectedFile();
            // If the file is viewable...
            if(file != null && !(file.isDirectory() || file.isSymlink())) {
                // If it's local, run the custom viewer on it.
                if(file.getURL().getProtocol().equals(FileProtocols.FILE) && (file instanceof LocalFile)) {
                    try {ProcessRunner.execute(customViewer.getTokens(file), file);}
                    catch(Exception e) {}
                }
                // If it's distant, copies it locally before running the custom viewer on it.
                else {
                    ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("copy_dialog.copying"));
                    TempOpenWithJob job = new TempOpenWithJob(progressDialog, mainFrame, file, FileFactory.getTemporaryFile(file.getName(), true), customViewer);
                    progressDialog.start(job);
                }
            }
        }
        // If we're not using a custom viewer, this action behaves exactly like its parent.
        else
            super.performAction();
    }



    // - Configuration management --------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Reacts to configuration changed events.
     * @param event describes the configuration change.
     */
    public synchronized void configurationChanged(ConfigurationEvent event) {
        // Updates useCustomViewer.
        if(event.getVariable().equals(MuConfiguration.USE_CUSTOM_VIEWER))
            useCustomViewer = event.getBooleanValue();
        // Updates customViewer.
        else if(event.getVariable().equals(MuConfiguration.CUSTOM_VIEWER))
            setCustomViewer(event.getValue());
    }

    /**
     * Sets the custom viewer to the specified command.
     * @param command command to use as a custom viewer.
     */
    private void setCustomViewer(String command) {
        if(command == null)
            customViewer = null;
        else
            customViewer = CommandParser.getCommand("view", command);
    }
}
