/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2009 Maxence Bernard
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

import com.mucommander.file.FileOperation;
import com.mucommander.ui.action.*;
import com.mucommander.ui.dialog.file.MkdirDialog;
import com.mucommander.ui.event.ActivePanelListener;
import com.mucommander.ui.event.LocationEvent;
import com.mucommander.ui.event.LocationListener;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;

import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import java.util.Hashtable;

/**
 * This action brings up the 'Make directory' dialog which allows to create a new directory in the currently active folder.
 *
 * @author Maxence Bernard
 */
public class MkdirAction extends MuAction implements ActivePanelListener, LocationListener {

    public MkdirAction(MainFrame mainFrame, Hashtable<String,Object> properties) {
        super(mainFrame, properties);

        // Listen to active table change events
        mainFrame.addActivePanelListener(this);

        // Listen to location change events
        mainFrame.getLeftPanel().getLocationManager().addLocationListener(this);
        mainFrame.getRightPanel().getLocationManager().addLocationListener(this);
    }

    private void toggleEnabledState() {
        setEnabled(mainFrame.getActiveTable().getCurrentFolder().isFileOperationSupported(FileOperation.CREATE_DIRECTORY));
    }

    @Override
    public void performAction() {
        new MkdirDialog(mainFrame, false).showDialog();
    }


    /////////////////////////////////
    // ActivePanelListener methods //
    /////////////////////////////////

    public void activePanelChanged(FolderPanel folderPanel) {
        toggleEnabledState();
    }


    //////////////////////////////
    // LocationListener methods //
    //////////////////////////////

    public void locationChanged(LocationEvent e) {
        toggleEnabledState();
    }

    public void locationChanging(LocationEvent e) {
    }

    public void locationCancelled(LocationEvent e) {
    }

    public void locationFailed(LocationEvent e) {
    }


    ///////////////////
    // Inner classes //
    ///////////////////

    public static class Factory implements ActionFactory {

		public MuAction createAction(MainFrame mainFrame, Hashtable<String,Object> properties) {
			return new MkdirAction(mainFrame, properties);
		}
    }
    
    public static class Descriptor extends AbstractActionDescriptor {
    	public static final String ACTION_ID = "Mkdir";
    	
		public String getId() { return ACTION_ID; }

		public ActionCategory getCategory() { return ActionCategories.FILES; }

		public KeyStroke getDefaultAltKeyStroke() { return null; }

		public KeyStroke getDefaultKeyStroke() { return KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0); }
    }
}
