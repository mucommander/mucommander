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

import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.table.FileTable;

import java.util.Map;

/**
 * Moves the current {@link FileTable}'s selection forward, {@link #getRowIncrement} rows at a time.
 *
 * @author Maxence Bernard
 */
public abstract class SelectForwardAction extends MuAction {

    public SelectForwardAction(MainFrame mainFrame, Map<String,Object> properties) {
        super(mainFrame, properties);
    }


    /////////////////////////////
    // MuAction implementation //
    /////////////////////////////

    @Override
    public void performAction() {
        FileTable activeTable = mainFrame.getActiveTable();

        activeTable.selectRow(Math.min(activeTable.getSelectedRow()+getRowIncrement(), activeTable.getRowCount()-1));
    }


    //////////////////////
    // Abstract methods //
    //////////////////////

    /**
     * Returns the number of rows to increment the current selection.
     *
     * @return the number of rows to increment the current selection.
     */
    protected abstract int getRowIncrement();
}
