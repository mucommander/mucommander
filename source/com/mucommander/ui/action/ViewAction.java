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

import com.mucommander.conf.ConfigurationEvent;
import com.mucommander.conf.ConfigurationListener;
import com.mucommander.conf.impl.MuConfiguration;
import com.mucommander.file.AbstractFile;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.viewer.ViewerRegistrar;

import java.util.Hashtable;

/**
 * Opens the current file in view mode.
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public class ViewAction extends AbstractViewerAction {
    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new <code>ViewAction</code>.
     */
    public ViewAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);

        // Initialises configuration.
        setUseCustomCommand(MuConfiguration.getVariable(MuConfiguration.USE_CUSTOM_VIEWER, MuConfiguration.DEFAULT_USE_CUSTOM_VIEWER));
        setCustomCommand(MuConfiguration.getVariable(MuConfiguration.CUSTOM_VIEWER));
    }



    // - Abstract methods ----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Opens the internal viewer on the specified file.
     * @param file file to view.
     */
    public void performInternalAction(AbstractFile file) {ViewerRegistrar.createViewerFrame(mainFrame, file, getIcon().getImage());}

    /**
     * Reacts to configuration changed events.
     * @param event describes the configuration change.
     */
    public synchronized void configurationChanged(ConfigurationEvent event) {
        // Sets the custom command.
        if(event.getVariable().equals(MuConfiguration.USE_CUSTOM_VIEWER))
            setUseCustomCommand(event.getBooleanValue());
        // Sets the 'use custom command' flag.
        else if(event.getVariable().equals(MuConfiguration.CUSTOM_VIEWER))
            setCustomCommand(event.getValue());
    }
}
