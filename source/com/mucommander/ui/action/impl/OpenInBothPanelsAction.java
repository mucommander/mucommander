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

import com.mucommander.file.AbstractFile;
import com.mucommander.ui.action.*;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.table.FileTable;
import com.mucommander.ui.main.table.FileTableModel;

import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
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
    // - Initialization ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new <code>OpenInBothPanelsAction</code> with the specified parameters.
     * @param mainFrame  frame to which the action is attached.
     * @param properties action's properties.
     */
    public OpenInBothPanelsAction(MainFrame mainFrame, Hashtable<String,Object> properties) {
        super(mainFrame, properties);

        // Perform this action in a separate thread, to avoid locking the event thread
        setPerformActionInSeparateThread(true);
    }


    /**
     * This method is overridden to enable this action when the parent folder is selected. 
     */
    @Override
    protected boolean getFileTableCondition(FileTable fileTable) {
        AbstractFile selectedFile = fileTable.getSelectedFile(true, true);

        return selectedFile!=null && selectedFile.isBrowsable();
    }


    // - Action code ---------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Opens the current selection and its inactive equivalent.
     */
    @Override
    public void performAction() {
        Thread       openThread;
        AbstractFile selectedFile;
        AbstractFile otherFile = null;

        // Retrieves the current selection, aborts if none (should not normally happen).
        if((selectedFile = mainFrame.getActiveTable().getSelectedFile(true, true)) == null || !selectedFile.isBrowsable())
            return;

        try {
            FileTableModel otherTableModel = mainFrame.getInactiveTable().getFileTableModel();

            if(mainFrame.getActiveTable().isParentFolderSelected()) {
                otherFile = otherTableModel.getParentFolder();
            }
            else {
                // Look for a file in the other table with the same name as the selected one (case insensitive)
                int fileCount = otherTableModel.getFileCount();
                String targetFilename = selectedFile.getName();
                for(int i=otherTableModel.getFirstMarkableRow(); i<fileCount; i++) {
                    otherFile = otherTableModel.getCachedFileAtRow(i);
                    if(otherFile.getName().equalsIgnoreCase(targetFilename))
                        break;

                    if(i==fileCount-1)
                        otherFile = null;
                }
            }
        }
        catch(Exception e) {otherFile = null;}

        // Opens 'file' in the active panel.
        openThread = mainFrame.getActivePanel().tryChangeCurrentFolder(selectedFile);

        // Opens 'otherFile' (if any) in the inactive panel.
        if(otherFile != null) {
            // Waits for the previous folder change to be finished.
            if(openThread != null) {
                while(openThread.isAlive()) {
                    try {openThread.join();}
                    catch(InterruptedException e) {}
                }
            }
            mainFrame.getInactivePanel().tryChangeCurrentFolder(otherFile);
        }
    }
    
    public static class Factory implements ActionFactory {

		public MuAction createAction(MainFrame mainFrame, Hashtable<String,Object> properties) {
			return new OpenInBothPanelsAction(mainFrame, properties);
		}
    }
    
    public static class Descriptor extends AbstractActionDescriptor {
    	public static final String ACTION_ID = "OpenInBothPanels";
    	
		public String getId() { return ACTION_ID; }

		public ActionCategory getCategory() { return ActionCategories.NAVIGATION; }

		public KeyStroke getDefaultAltKeyStroke() { return null; }

		public KeyStroke getDefaultKeyStroke() { return KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK); }
    }
}
