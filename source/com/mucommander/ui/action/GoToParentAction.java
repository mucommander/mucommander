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

import com.mucommander.file.AbstractFile;
import com.mucommander.ui.event.ActivePanelListener;
import com.mucommander.ui.event.LocationEvent;
import com.mucommander.ui.event.LocationListener;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;

import java.util.Hashtable;

/**
 * This action changes the current folder of the currently active FolderPanel to the current folder's parent.
 * This action only gets enabled when the current folder has a parent.
 *
 * @author Maxence Bernard
 */
public class GoToParentAction extends GoToAction {

    public GoToParentAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }


    /**
     * Enables or disables this action based on the currently active folder's
     * has a parent, this action will be enabled, if not it will be disabled.
     */
    protected void toggleEnabledState() {
        setEnabled(mainFrame.getActiveTable().getFolderPanel().getCurrentFolder().getParentSilently()!=null);
    }

    protected boolean goToParent(FolderPanel panel) {
        AbstractFile parent;

        if((parent = panel.getCurrentFolder().getParentSilently()) != null) {
            panel.tryChangeCurrentFolder(parent);
            return true;
        }
        return false;
    }

    ///////////////////////////////
    // MuAction implementation //
    ///////////////////////////////

    public void performAction() {
        // Changes the current folder to make it the current folder's parent.
        // Does nothing if the current folder doesn't have a parent.
        goToParent(mainFrame.getActiveTable().getFolderPanel());
    }
}
