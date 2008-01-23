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

import com.mucommander.conf.ConfigurationEvent;
import com.mucommander.conf.impl.MuConfiguration;
import com.mucommander.file.AbstractFile;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.viewer.EditorRegistrar;

import java.util.Hashtable;

/**
 * Opens the current file in edit mode.
 *
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public class EditAction extends AbstractViewerAction {

    public EditAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);

        setUseCustomCommand(MuConfiguration.getVariable(MuConfiguration.USE_CUSTOM_EDITOR, MuConfiguration.DEFAULT_USE_CUSTOM_EDITOR));
        setCustomCommand(MuConfiguration.getVariable(MuConfiguration.CUSTOM_EDITOR));
    }


    /////////////////////////////////////////
    // AbstractViewerAction implementation //
    /////////////////////////////////////////

    /**
     * Opens the internal editor on the specified file.
     * @param file file to edit.
     */
    public void performInternalAction(AbstractFile file) {
        EditorRegistrar.createEditorFrame(mainFrame, file, getIcon().getImage());
    }

    /**
     * Reacts to configuration changed events.
     * @param event describes the configuration change.
     */
    public synchronized void configurationChanged(ConfigurationEvent event) {
        // Sets the custom command.
        if(event.getVariable().equals(MuConfiguration.USE_CUSTOM_EDITOR))
            setUseCustomCommand(event.getBooleanValue());
        // Sets the 'use custom command' flag.
        else if(event.getVariable().equals(MuConfiguration.CUSTOM_EDITOR))
            setCustomCommand(event.getValue());
    }
}
