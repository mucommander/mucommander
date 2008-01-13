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
import com.mucommander.ui.main.table.Columns;

import java.util.Hashtable;

/**
 * Shows/hides a specified column of the currently active FileTable. If the column is currently visible, this action
 * will hide it and vice-versa.
 *
 * @author Maxence Bernard
 */
public abstract class ToggleColumnAction extends MuAction {

    /** Index of the FileTable column this action operates on */
    protected int columnIndex;

    public ToggleColumnAction(MainFrame mainFrame, Hashtable properties, int columnIndex) {
        super(mainFrame, properties, false);
        setLabel(Columns.getColumnLabel(columnIndex));

        this.columnIndex = columnIndex;
    }

    public void performAction() {
        mainFrame.getActiveTable().setColumnVisible(columnIndex, !mainFrame.getActiveTable().isColumnVisible(columnIndex));
    }
}
