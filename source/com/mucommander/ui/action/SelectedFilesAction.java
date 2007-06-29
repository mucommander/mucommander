/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (c) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with muCommander; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.table.FileTable;

import java.util.Hashtable;

/**
 * SelectedFilesAction is an abstract action that operates on the currently active FileTable, and is enabled only
 * when at least one file is marked, or when a file other than the parent folder file '..' is selected.
 * When none of those conditions is satisfied, this action is disabled.
 *
 * @author Maxence Bernard
 */
public abstract class SelectedFilesAction extends SelectedFileAction {

    public SelectedFilesAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    protected boolean getFileTableCondition(FileTable fileTable) {
        return fileTable.getFileTableModel().getNbMarkedFiles()>0 || super.getFileTableCondition(fileTable);
    }
}
