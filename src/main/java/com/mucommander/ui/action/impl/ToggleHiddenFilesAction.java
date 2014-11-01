/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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

import java.awt.event.KeyEvent;
import java.util.Map;

import javax.swing.KeyStroke;

import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.conf.MuPreferences;
import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategories;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionDescriptor;
import com.mucommander.ui.action.ActionFactory;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.WindowManager;

/**
 * A simple action that toggles hidden files visibility on and off.
 * @author Nicolas Rinaudo
 */
public class ToggleHiddenFilesAction extends MuAction {
    // - Initialization ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new <code>ToggleHiddenFilesAction</code>.
     */
    public ToggleHiddenFilesAction(MainFrame mainFrame, Map<String,Object> properties) {
        super(mainFrame, properties);
    }


    // - Action code ---------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Toggles hidden files display on and off and requests for all file tables to be repainted.
     */
    @Override
    public void performAction() {
    	MuConfigurations.getPreferences().setVariable(MuPreference.SHOW_HIDDEN_FILES,
                                    !MuConfigurations.getPreferences().getVariable(MuPreference.SHOW_HIDDEN_FILES, MuPreferences.DEFAULT_SHOW_HIDDEN_FILES));
        WindowManager.tryRefreshCurrentFolders();
    }

	@Override
	public ActionDescriptor getDescriptor() {
		return new Descriptor();
	}

    public static class Factory implements ActionFactory {

		public MuAction createAction(MainFrame mainFrame, Map<String,Object> properties) {
			return new ToggleHiddenFilesAction(mainFrame, properties);
		}
    }
    
    public static class Descriptor extends AbstractActionDescriptor {
    	public static final String ACTION_ID = "ToggleHiddenFiles";
    	
		public String getId() { return ACTION_ID; }

		public ActionCategory getCategory() { return ActionCategories.VIEW; }

		public KeyStroke getDefaultAltKeyStroke() { return null; }

		public KeyStroke getDefaultKeyStroke() { return KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.ALT_DOWN_MASK); }
    }
}
