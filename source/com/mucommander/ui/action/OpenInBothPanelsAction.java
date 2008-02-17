/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
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
import com.mucommander.ui.main.MainFrame;
import com.mucommander.file.filter.AttributeFileFilter;

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
public class OpenInBothPanelsAction extends SelectedFileAction {
    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new <code>OpenInBothPanelsAction</code> with the specified parameters.
     * @param mainFrame  frame to which the action is attached.
     * @param properties action's properties.
     */
    public OpenInBothPanelsAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
        setSelectedFileFilter(new AttributeFileFilter(AttributeFileFilter.BROWSABLE));
    }



    // - Action code ---------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Opens the current selection and its inactive equivalent.
     */
    public void performAction() {
        Thread       openThread;
        AbstractFile file;
        AbstractFile otherFile;

        // Retrieves the current selection, aborts if none.
        if((file = mainFrame.getActiveTable().getSelectedFile(true)) == null || !file.isBrowsable())
            return;

        try {
            if(mainFrame.getActiveTable().isParentFolderSelected())
                otherFile = mainFrame.getInactiveTable().getCurrentFolder().getParentSilently();
            else {
                otherFile = mainFrame.getInactiveTable().getCurrentFolder().getDirectChild(file.getName());
                if(!otherFile.exists() || !otherFile.isBrowsable())
                    otherFile = null;
            }
        }
        catch(Exception e) {otherFile = null;}

        // Opens 'file' in the active panel.
        openThread = mainFrame.getActiveTable().getFolderPanel().tryChangeCurrentFolder(file);

        // Opens 'otherFIle' in the inactive panel if necessary.
        if(otherFile != null) {
            // Waits for the previous operation to be finished.
            if(openThread != null) {
                while(openThread.isAlive()) {
                    try {openThread.join();}
                    catch(InterruptedException e) {}
                }
            }
            mainFrame.getInactiveTable().getFolderPanel().tryChangeCurrentFolder(otherFile);
        }
    }
}
