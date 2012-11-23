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

import java.util.Map;

import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.event.ActivePanelListener;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.tabs.ActiveTabListener;

/**
 * This class is an abstract {@link MuAction} that operates on the current tab. It monitors changes in the active
 * tab's properties and calls {@link #toggleEnabledState()} when the properties have changed, or when the active 
 * tab itself has changed, in order to enable or disable this action.
 * 
 * @author Arik Hadas
 */
public abstract class ActiveTabAction extends MuAction implements ActivePanelListener, ActiveTabListener {

	public ActiveTabAction(MainFrame mainFrame, Map<String,Object> properties) {
		super(mainFrame, properties);

		// Listen to active table change events
		mainFrame.addActivePanelListener(this);

		// Listen to active tab change events
		mainFrame.getLeftPanel().getTabs().addActiveTabListener(this);
		mainFrame.getRightPanel().getTabs().addActiveTabListener(this);

		toggleEnabledState();
	}


	//////////////////////
	// Abstract methods //
	//////////////////////

	/**
	 * Enables or disables this action based on the location of the currently active {@link FolderPanel}.
	 * This method is called once by the constructor to set the initial state. Then it is called every time the location
	 * of the currently active <code>FolderPanel</code> has changed, and when the currently active <code>FolderPanel</code>
	 * has changed.
	 */
	protected abstract void toggleEnabledState();


	/////////////////////////////////
	// ActivePanelListener methods //
	/////////////////////////////////

	public void activePanelChanged(FolderPanel folderPanel) {
		toggleEnabledState();
	}

	/////////////////////////////////
	// ActivePanelListener methods //
	/////////////////////////////////

	public void activeTabChanged() {
		toggleEnabledState();
	}
}
