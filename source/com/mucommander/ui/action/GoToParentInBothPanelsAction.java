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

import com.mucommander.ui.event.LocationEvent;
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
    // - Instance variables --------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Whether or not the action is currently waiting on a location event. */
    private boolean isWaiting;
    /** Whether or not the last open action was a success. */
    private boolean status;
    /** Used to synchronize calls to {@link #performAction()}. */
    private Object lock = new Object();



    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new <code>GoToParentInBothPanelsAction</code> instance with the specified parameters.
     * @param mainFrame  frame to which the action is attached.
     * @param properties action's properties.
     */
    public GoToParentInBothPanelsAction(MainFrame mainFrame, Hashtable properties) {super(mainFrame, properties);}



    // - Action code ---------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Opens both the active and inactive folder panel's parent directories.
     */
    public void performAction() {
        synchronized(lock) {

            if(goToParent(mainFrame.getActiveTable().getFolderPanel())) {
                isWaiting = true;
                status    = false;
                while(isWaiting) {
                    try {wait();}
                    catch(Exception e) {}
                }
                if(status)
                    goToParent(mainFrame.getInactiveTable().getFolderPanel());
            }
        }
    }



    // - Synchronisation code ------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * If necessary, notifies the performAction that it can resume its work.
     */
    private synchronized void unlock(boolean status) {
        if(isWaiting) {
            isWaiting   = false;
            this.status = status;
            notifyAll();
        }
    }

    /**
     * If necessary, notifies the performAction that it can resume its work.
     */
    public void locationChanged(LocationEvent e) {
        super.locationChanged(e);
        unlock(true);
    }

    /**
     * If necessary, notifies the performAction that it can resume its work.
     */
    public void locationCancelled(LocationEvent e) {
        super.locationCancelled(e);
        unlock(false);
    }

    /**
     * If necessary, notifies the performAction that it can resume its work.
     */
    public void locationFailed(LocationEvent e) {
        super.locationFailed(e);
        unlock(false);
    }
}
