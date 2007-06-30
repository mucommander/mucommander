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

import com.mucommander.file.AbstractFile;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.table.FileTable;
import com.mucommander.ui.table.FileTableModel;

import java.util.Hashtable;

/**
 * This action .
 *
 * @author Maxence Bernard
 */
public class InvertSelectionAction extends MucoAction {

    public InvertSelectionAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    public void performAction() {
        FileTable fileTable = mainFrame.getActiveTable();
        FileTableModel tableModel = fileTable.getFileTableModel();

        // Starts at 1 if current folder is not root so that '..' is not marked
        AbstractFile file;
        int nbRows = tableModel.getRowCount();
        for(int i=fileTable.getParent()==null?0:1; i<nbRows; i++) {
            file = tableModel.getFileAtRow(i);
            if(!file.isDirectory())
                tableModel.setRowMarked(i, !tableModel.isRowMarked(i));
        }
        fileTable.repaint();

        // Notify registered listeners that currently marked files have changed on the FileTable
        fileTable.fireMarkedFilesChangedEvent();
    }
}
