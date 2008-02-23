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

import com.mucommander.ui.event.ActivePanelListener;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.table.FileTable;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import java.util.Hashtable;

/**
 * This action sorts the currently active {@link com.mucommander.ui.main.table.FileTable} by a specified criterion.
 * If the table is already sorted by this particular criterion when the action is performed, the sort order will be
 * reversed.
 *
 * <p>This action is enabled only if the corresponding column is currently visible. This prevents this action from being
 * performed when the column is not visible, which is an unsupported operation</p>.
 *
 * @author Maxence Bernard
 */
public abstract class SortByAction extends MuAction implements ActivePanelListener, TableColumnModelListener {

    /** Index of the FileTable column this action operates on */ 
    protected int columnIndex;

    public SortByAction(MainFrame mainFrame, Hashtable properties, int columnIndex) {
        super(mainFrame, properties);

        this.columnIndex = columnIndex;

        mainFrame.addActivePanelListener(this);
        mainFrame.getLeftPanel().getFileTable().getColumnModel().addColumnModelListener(this);
        mainFrame.getRightPanel().getFileTable().getColumnModel().addColumnModelListener(this);

        updateState(mainFrame.getActiveTable());
    }

    /**
     * Updates this action's enable state, enabling this action if the corresponding column is visible and vice-versa.
     *
     * @param fileTable the FileTable this action currently operates on
     */
    private void updateState(FileTable fileTable) {
        setEnabled(fileTable.isColumnVisible(columnIndex));
    }


    /////////////////////////////
    // MuAction implementation //
    /////////////////////////////

    public void performAction() {
        mainFrame.getActiveTable().sortBy(columnIndex);
    }


    ////////////////////////////////////////
    // ActivePanelListener implementation //
    ////////////////////////////////////////

    public void activePanelChanged(FolderPanel folderPanel) {
        // Update this action's enabled state when the active panel has changed
        updateState(folderPanel.getFileTable());
    }


    /////////////////////////////////////////////
    // TableColumnModelListener implementation //
    /////////////////////////////////////////////

    public void columnAdded(TableColumnModelEvent event) {
        // Enable this action when the corresponding column has been made visible
        if(event.getFromIndex()==columnIndex)
            setEnabled(true);
    }

    public void columnRemoved(TableColumnModelEvent event) {
        // Disable this action when the corresponding column has been made invisible
        if(event.getFromIndex()==columnIndex)
            setEnabled(false);
    }

    public void columnMoved(TableColumnModelEvent event) {
    }

    public void columnMarginChanged(ChangeEvent event) {
    }

    public void columnSelectionChanged(ListSelectionEvent event) {
    }
}
