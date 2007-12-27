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

import com.mucommander.PlatformManager;
import com.mucommander.file.AbstractFile;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.event.LocationListener;
import com.mucommander.ui.event.LocationEvent;

import java.util.Hashtable;

/**
 * Opens the currently selected file and its equivalent in the inactive folder panel if it exists.
 * <p>
 * This action will analyse the current selection and, if applicable, any file from the inactive
 * panel that bears the same name and:
 * <ul>
 *   <li>
 *     If both the selection and its inactive equivalent are browsable, both will be explored in their
 *     respective panels.
 *   </li>
 *   <li>
 *     If both are non-browsable, both will be opened as defined in {@link OpenAction}.
 *   </li>
 *   <li>
 *     If one is browsable an not the other one, only the current selection will be opened.
 *   </li>
 * </ul>
 * </p>
 * <p>
 * Note that this action's behaviour is strictly equivalent to that of {@link OpenAction} in the
 * active panel. Differences will only occur in the inactive panel, and then again only when possible.
 * </p>
 * <p>
 * This action opens both files synchronously: it will wait for the active panel file to have been
 * opened before opening the inactive panel one.
 * </p>
 * @author Nicolas Rinaudo
 */
public class OpenInBothPanelsAction extends OpenAction implements LocationListener {
    // - Instance variables --------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Whether or not the action is currently waiting on a location event. */
    private boolean isWaiting;
    /** Whether or not the last open action was a success. */
    private boolean status;
    /** Used to synchronize calls to {@link #performAction()}. */
    private Object  lock = new Object();



    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new <code>OpenInBothPanelsAction</code> with the specified parameters.
     * @param mainFrame  frame to which the action is attached.
     * @param properties action's properties.
     */
    public OpenInBothPanelsAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);

        // Listen to location change events
        mainFrame.getFolderPanel1().getLocationManager().addLocationListener(this);
        mainFrame.getFolderPanel2().getLocationManager().addLocationListener(this);
    }



    // - Action code ---------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Opens the current selection and its inactive equivalent.
     */
    public void performAction() {
        synchronized(lock) {
            AbstractFile file;
            AbstractFile otherFile;

            // Retrieves the current selection, aborts if none.
            if((file = mainFrame.getActiveTable().getSelectedFile(true)) == null)
                return;

            // Retrieves the current selection's inactive equivalent, sets it to null
            // if anything wrong occurs or it doesn't have the same 'browsable' status
            // as the current selection.
            try {
                if(mainFrame.getActiveTable().isParentFolderSelected())
                    otherFile = mainFrame.getInactiveTable().getCurrentFolder().getParentSilently();
                else {
                    otherFile = mainFrame.getInactiveTable().getCurrentFolder().getDirectChild(file.getName());
                    if(!(file.isBrowsable() == otherFile.isBrowsable()))
                        otherFile = null;
                }
            }
            catch(Exception e) {otherFile = null;}

            // Opens 'file' in the active panel.
            open(file, mainFrame.getActiveTable().getFolderPanel());

            // If this is not a 'navigate' action, we don't need to wait for the active
            // open to be completed before running the inactive one.
            if(!file.isBrowsable())
                open(otherFile, mainFrame.getInactiveTable().getFolderPanel());

            // If the inactive file exists, wait for the current change directory action
            // to be finished before opening it.
            else if(otherFile != null && otherFile.exists()) {
                isWaiting = true;
                status    = true;
                while(isWaiting) {
                    try {wait();}
                    catch(Exception e) {}
                }
                // Ignores failed open calls.
                if(status)
                    open(otherFile, mainFrame.getInactiveTable().getFolderPanel());
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
            this.status = status;
            isWaiting   = false;
            notifyAll();
        }
    }

    /**
     * Ignored.
     */
    public void locationChanging(LocationEvent e) {}

    /**
     * If necessary, notifies the performAction that it can resume its work.
     */
    public void locationChanged(LocationEvent e) {unlock(true);}

    /**
     * If necessary, notifies the performAction that it can resume its work.
     */
    public void locationCancelled(LocationEvent e) {unlock(false);}

    /**
     * If necessary, notifies the performAction that it can resume its work.
     */
    public void locationFailed(LocationEvent e) {unlock(false);}
}
