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

import com.mucommander.core.LocationChanger.ChangeFolderThread;
import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategories;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionDescriptor;
import com.mucommander.ui.action.ActionFactory;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.event.LocationEvent;
import com.mucommander.ui.event.LocationListener;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;

/**
 * This action is invoked to stop a running location change.
 *
 * @author Maxence Bernard
 */
public class StopAction extends MuAction implements LocationListener {

    public StopAction(MainFrame mainFrame, Map<String,Object> properties) {
        super(mainFrame, properties);

        // This action is initially disabled and enabled only during a folder change
        setEnabled(false);

        // Listen to location change events
        mainFrame.getLeftPanel().getLocationManager().addLocationListener(this);
        mainFrame.getRightPanel().getLocationManager().addLocationListener(this);

        // This action must be available while in 'no events mode', that's the whole point 
        setHonourNoEventsMode(false);
    }

    @Override
    public void performAction() {
        FolderPanel folderPanel = mainFrame.getActivePanel();
        ChangeFolderThread changeFolderThread = folderPanel.getChangeFolderThread();

        if(changeFolderThread!=null)
            changeFolderThread.tryKill();
    }

	@Override
	public ActionDescriptor getDescriptor() {
		return new Descriptor();
	}


    //////////////////////////////
    // LocationListener methods //
    //////////////////////////////

    public void locationChanged(LocationEvent e) {
        setEnabled(false);
    }

    public void locationChanging(LocationEvent e) {
        setEnabled(true);
    }

    public void locationCancelled(LocationEvent e) {
        setEnabled(false);
    }

    public void locationFailed(LocationEvent e) {
        setEnabled(false);
    }
    
    public static class Factory implements ActionFactory {

		public MuAction createAction(MainFrame mainFrame, Map<String,Object> properties) {
			return new StopAction(mainFrame, properties);
		}
    }
    
    public static class Descriptor extends AbstractActionDescriptor {
    	public static final String ACTION_ID = "Stop";
    	
		public String getId() { return ACTION_ID; }

		public ActionCategory getCategory() { return ActionCategories.NAVIGATION; }

		public KeyStroke getDefaultAltKeyStroke() { return null; }

		public KeyStroke getDefaultKeyStroke() { return KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0); }
    }
}
