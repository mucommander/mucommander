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

import com.mucommander.commons.file.util.FileSet;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.table.FileTable;

import java.util.Map;

/**
 * SelectedFilesAction is an abstract action that operates on the currently active FileTable, and is enabled only
 * when at least one file is marked, or when a file other than the parent folder file '..' is selected.
 * When none of those conditions is satisfied, this action is disabled.
 *
 * <p>Optionally, a FileFilter can be specified using {@link #setSelectedFileFilter(com.mucommander.commons.file.filter.FileFilter) setSelectedFileFilter}
 * to further restrict the enabled condition to files that match the filter.</p>
 *
 * @author Maxence Bernard
 */
public abstract class SelectedFilesAction extends SelectedFileAction {

    public SelectedFilesAction(MainFrame mainFrame, Map<String,Object> properties) {
        super(mainFrame, properties);
    }

    @Override
    protected boolean getFileTableCondition(FileTable fileTable) {
        return fileTable.getFileTableModel().getNbMarkedFiles()>0 || super.getFileTableCondition(fileTable);
    }


    /////////////////////////////
    // MuAction implementation //
    /////////////////////////////

    @Override
    public final void performAction() {
        FileSet files = mainFrame.getActiveTable().getSelectedFiles();
        // Perform the action only if at least one file is selected/marked
        if(files.size()>0)
            performAction(files);
    }


    //////////////////////
    // Abstract methods //
    //////////////////////
    
    /**
     * Performs the action on the files that were selected/marked by the user in the currently active table.
     *
     * @param files files that were selected/marked by the user in the currently active table
     */
    public abstract void performAction(FileSet files);
}
