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

import com.mucommander.file.filter.FileFilter;
import com.mucommander.ui.event.ActivePanelListener;
import com.mucommander.ui.event.TableSelectionListener;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.table.FileTable;

import java.util.Hashtable;

/**
 * FileAction is an abstract action that operates on the currently active FileTable. It is enabled only when
 * the table condition as tested by {@link #getFileTableCondition(FileTable) getFileTableCondition()}
 * method is satisfied.
 *
 * <p>Those tests are performed when:
 * <ul>
 * <li>the selected file on the currently active FileTable has changed
 * <li>the marked files on the currently active FileTable has changed
 * <li>the currently active FileTable has changed
 * </ul>
 *
 * @author Maxence Bernard
 */
public abstract class FileAction extends MuAction implements TableSelectionListener, ActivePanelListener {

    /** Filter that restricts the enabled condition to files that match it (can be null) */
    protected FileFilter filter;


    public FileAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
        init(mainFrame);
    }

    
    private void init(MainFrame mainFrame) {
        mainFrame.addActivePanelListener(this);
        mainFrame.getFolderPanel1().getFileTable().addTableSelectionListener(this);
        mainFrame.getFolderPanel2().getFileTable().addTableSelectionListener(this);

        // Set initial enabled state
        updateEnabledState(mainFrame.getActiveTable());
    }


    /**
     * Enables/disables this action if both of the {@link #getFileTableCondition(FileTable)} and file filter
     * (if there is one) tests are satisfied.
     *
     * <p>This method is called each time:
     * <ul>
     * <li>the selected file on the currently active FileTable has changed
     * <li>the marked files on the currently active FileTable has changed
     * <li>the currently active FileTable has changed
     * </ul>
     *
     * @param fileTable the currently active FileTable
     */
    protected void updateEnabledState(FileTable fileTable) {
        // Note: AbstractAction checks if enabled value has changed before firing an event
        setEnabled(getFileTableCondition(fileTable));
    }


    /**
     * This method is called to determine if the current FileTable state allows this action to be enabled.
     * If <code>false</code> is returned, the action will be disabled.
     * If <code>true</code> is returned, the action will be enabled if the file filter (if there is one) matches the
     * selected file.
     *
     * @param fileTable currently active FileTable
     */
    protected abstract boolean getFileTableCondition(FileTable fileTable);


    ///////////////////////////////////////////
    // TableSelectionListener implementation //
    ///////////////////////////////////////////

    /**
     * Updates this action's enabled status based on the new currently selected file.
     */
    public void selectedFileChanged(FileTable source) {
        // No need to update state if the originating FileTable is not the currently active one 
        if(source==mainFrame.getActiveTable())
            updateEnabledState(source);
    }

    /**
     * Updates this action's enabled status based on the new currently marked files.
     */
    public void markedFilesChanged(FileTable source) {
        // No need to update state if the originating FileTable is not the currently active one
        if(source==mainFrame.getActiveTable())
            updateEnabledState(source);
    }

    ////////////////////////////////////////
    // ActivePanelListener implementation //
    ////////////////////////////////////////

    public void activePanelChanged(FolderPanel folderPanel) {
        updateEnabledState(folderPanel.getFileTable());
    }
}
