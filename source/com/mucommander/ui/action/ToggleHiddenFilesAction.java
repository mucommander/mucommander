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

import com.mucommander.conf.impl.MuConfiguration;
import com.mucommander.conf.ConfigurationListener;
import com.mucommander.conf.ConfigurationEvent;
import com.mucommander.text.Translator;
import com.mucommander.ui.main.CommandBar;
import com.mucommander.ui.main.WindowManager;
import com.mucommander.ui.main.MainFrame;

import java.util.Hashtable;
import java.util.Iterator;

/**
 * A simple action that toggles hidden files visibility on and off.
 * @author Nicolas Rinaudo
 */
public class ToggleHiddenFilesAction extends MuAction implements ConfigurationListener {
    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new <code>ToggleHiddenFilesAction</code>.
     */
    public ToggleHiddenFilesAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties, false);
        MuConfiguration.addConfigurationListener(this);
        updateLabel(MuConfiguration.getVariable(MuConfiguration.SHOW_HIDDEN_FILES, MuConfiguration.DEFAULT_SHOW_HIDDEN_FILES));
    }



    // - State update --------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Updates the action's label depending on whether hidden files are turned on or off.
     * @param bool whether hidden files should be displayed or not.
     */
    public void updateLabel(boolean bool) {
        setLabel(Translator.get(bool ? com.mucommander.ui.action.ToggleHiddenFilesAction.class.getName()+".hide" :
                                com.mucommander.ui.action.ToggleHiddenFilesAction.class.getName()+".show"));
    }

    /**
     * Updates the action's label if necessary.
     * <p>
     * If the event is related to {@link com.mucommander.conf.impl.MuConfiguration#SHOW_HIDDEN_FILES}, we need
     * to update the action's label.
     * </p>
     * @param event described the configuration event.
     */
    public void configurationChanged(ConfigurationEvent event) {
        if(event.getVariable().equals(MuConfiguration.SHOW_HIDDEN_FILES))
            updateLabel(event.getBooleanValue());
    }



    // - Action code ---------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Toggles hidden files display on and off and requests for all file tables to be repainted.
     */
    public void performAction() {
        Iterator  frames;
        MainFrame frame;

        MuConfiguration.setVariable(MuConfiguration.SHOW_HIDDEN_FILES,
                                    !MuConfiguration.getVariable(MuConfiguration.SHOW_HIDDEN_FILES, MuConfiguration.DEFAULT_SHOW_HIDDEN_FILES));

        // Refresh folder panels in a separate thread to show/hide new files
        frames = WindowManager.getMainFrames().iterator();
        while(frames.hasNext()) {
            frame = (MainFrame)frames.next();
            frame.getFolderPanel1().tryRefreshCurrentFolder();
            frame.getFolderPanel2().tryRefreshCurrentFolder();
        }
    }
}
