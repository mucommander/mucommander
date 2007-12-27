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
import com.mucommander.ui.event.LocationEvent;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;

import java.util.Hashtable;

/**
 * Changes the current directory to its parent and tries to do the same in the inactive panel.
 * <p>
 * When possible, this action will open the active panel's current folder's parent. Additionally,
 * if the inactive panel's current folder has a parent, it will open that one as well.
 * </p>
 * <p>
 * Note that this action's behaviour is strictly equivalent to that of {@link OpenAction} in the
 * active panel. Differences will only occur in the inactive panel, and then again only when possible.
 * </p>
 * <p>
 * This action opens both files synchronously: it will wait for the active panel location change confirmation
 * before performing the inactive one.
 * </p>
 * @author Nicolas Rinaudo
 */
public class GoToParentInBothPanelsAction extends GoToParentAction {
    /** Whether or not we should react to location changed events. */
    private boolean goToParent;

    /**
     * Creates a new <code>GoToParentInBothPanelsAction</code> instance with the specified parameters.
     * @param mainFrame  frame to which the action is attached.
     * @param properties action's properties.
     */
    public GoToParentInBothPanelsAction(MainFrame mainFrame, Hashtable properties) {super(mainFrame, properties);}

    /**
     * Opens both the active and inactive folder panel's parent directories.
     */
    public synchronized void performAction() {goToParent = goToParent(mainFrame.getActiveTable().getFolderPanel());}

    /**
     * If necessary, triggers a 'GoToParentAction' on the inactive panel.
     */
    public synchronized void locationChanged(LocationEvent e) {
        super.locationChanged(e);
        if((e.getFolderPanel() == mainFrame.getActiveTable().getFolderPanel()) && goToParent)
            goToParent(mainFrame.getInactiveTable().getFolderPanel());
        goToParent = false;
    }

    /**
     * Cancels the 'GoToParentAction' on the inactive panel.
     */
    public synchronized void locationCancelled(LocationEvent e) {
        super.locationCancelled(e);
        goToParent = false;
    }

    /**
     * Cancels the 'GoToParentAction' on the inactive panel.
     */
    public synchronized void locationFailed(LocationEvent e) {
        super.locationFailed(e);
        goToParent = false;
    }
}
