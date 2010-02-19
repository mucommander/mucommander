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

import com.mucommander.conf.impl.MuConfiguration;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.*;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.commandbar.CommandBar;

import javax.swing.KeyStroke;
import java.util.Hashtable;

/**
 * This action shows/hides the current MainFrame's {@link com.mucommander.ui.main.commandbar.CommandBar} depending on its
 * current visible state: if it is visible, hides it, if not shows it.
 *
 * <p>This action's label will be updated to reflect the current visible state.
 *
 * <p>Each time this action is executed, the new current visible state is stored in the configuration so that
 * new MainFrame windows will use it to determine whether the CommandBar has to be made visible or not.
 *
 * @author Maxence Bernard
 */
public class ToggleCommandBarAction extends MuAction {

    public ToggleCommandBarAction(MainFrame mainFrame, Hashtable<String,Object> properties) {
        super(mainFrame, properties);
        updateLabel(MuConfiguration.getVariable(MuConfiguration.COMMAND_BAR_VISIBLE, MuConfiguration.DEFAULT_COMMAND_BAR_VISIBLE));
    }

    private void updateLabel(boolean visible) {
        setLabel(Translator.get(visible?Descriptor.ACTION_ID+".hide":Descriptor.ACTION_ID+".show"));
    }

    @Override
    public void performAction() {
        CommandBar commandBar = mainFrame.getCommandBar();
        boolean visible = !commandBar.isVisible();
        // Save the last command bar visible state in the configuration, this will become the default for new MainFrame windows.
        MuConfiguration.setVariable(MuConfiguration.COMMAND_BAR_VISIBLE, visible);
        // Change the label to reflect the new command bar state
        updateLabel(visible);
        // Show/hide the command bar
        commandBar.setVisible(visible);
        mainFrame.validate();
    }
    
    public static class Factory implements ActionFactory {

		public MuAction createAction(MainFrame mainFrame, Hashtable<String,Object> properties) {
			return new ToggleCommandBarAction(mainFrame, properties);
		}
    }
    
    public static class Descriptor extends AbstractActionDescriptor {
    	public static final String ACTION_ID = "ToggleCommandBar";
    	
		public String getId() { return ACTION_ID; }

        public ActionCategory getCategory() { return ActionCategories.VIEW; }

		public KeyStroke getDefaultAltKeyStroke() { return null; }

		public KeyStroke getDefaultKeyStroke() { return null; }

        @Override
        public String getLabelKey() { return ACTION_ID+".show"; }
    }
}
