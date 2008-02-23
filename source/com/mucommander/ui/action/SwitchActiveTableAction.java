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

import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.table.FileTable;

import java.util.Hashtable;

/**
 * This action switches the currently active FileTable, that is gives focus to the FileTable that currently doesn't
 * have it.
 *
 * @author Maxence Bernard
 */
public class SwitchActiveTableAction extends MuAction {

    public SwitchActiveTableAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    public void performAction() {
        FileTable activeTable = mainFrame.getActiveTable();
        FileTable leftTable = mainFrame.getLeftPanel().getFileTable();
        FileTable rightTable = mainFrame.getRightPanel().getFileTable();
        if(activeTable == leftTable)
            rightTable.requestFocus();
        else if(activeTable == rightTable)
            leftTable.requestFocus();
    }
}
