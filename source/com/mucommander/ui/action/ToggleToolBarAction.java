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
import com.mucommander.text.Translator;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.ToolBar;

import java.util.Hashtable;

/**
 * This action shows/hides the current MainFrame's {@link com.mucommander.ui.main.ToolBar} depending on its
 * current visible state: if it is visible, hides it, if not shows it.
 *
 * <p>This action's label will be updated to reflect the current visible state.
 *
 * <p>Each time this action is executed, the new current visible state is stored in the configuration so that
 * new MainFrame windows will use it to determine whether the ToolBar has to be made visible or not.
 *
 * @author Maxence Bernard
 */
public class ToggleToolBarAction extends MuAction {

    public ToggleToolBarAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties, false);
        setLabel(Translator.get(MuConfiguration.getVariable(MuConfiguration.TOOLBAR_VISIBLE,
                                                                 MuConfiguration.DEFAULT_TOOLBAR_VISIBLE)
                                ? com.mucommander.ui.action.ToggleToolBarAction.class.getName()+".hide":com.mucommander.ui.action.ToggleToolBarAction.class.getName()+".show"));
    }

    public void performAction() {
        ToolBar toolBar = mainFrame.getToolBar();
        boolean visible = !toolBar.isVisible();
        // Save the last toolbar visible state in the configuration, this will become the default for new MainFrame windows.
        MuConfiguration.setVariable(MuConfiguration.TOOLBAR_VISIBLE, visible);
        // Change the label to reflect the new toolbar state
        setLabel(Translator.get(visible?com.mucommander.ui.action.ToggleToolBarAction.class.getName()+".hide":com.mucommander.ui.action.ToggleToolBarAction.class.getName()+".show"));
        // Show/hide the toolbar
        toolBar.setVisible(visible);
        mainFrame.validate();
    }
}
