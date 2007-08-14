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

import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.table.FileTable;

import java.util.Hashtable;

/**
 * Marks/unmarks rows in the active FileTable, from the currently selected row to the next page's row (inclusive).
 * The row immediately after the last marked/unmarked row will become the currently selected row.
 *
 * @author Maxence Bernard
 */
public class MarkPageDownAction extends MuAction {

    public MarkPageDownAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    public void performAction() {
        FileTable fileTable = mainFrame.getActiveTable();

        int currentRow = fileTable.getSelectedRow();
        int lastRow = fileTable.getRowCount()-1;
        int endRow = Math.min(lastRow, currentRow + fileTable.getPageRowIncrement());

        fileTable.setRangeMarked(currentRow, endRow, !fileTable.getFileTableModel().isRowMarked(currentRow));
        fileTable.selectRow(Math.min(lastRow, endRow+1));
    }
}
