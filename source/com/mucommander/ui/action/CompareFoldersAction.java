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

import com.mucommander.file.AbstractFile;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.table.FileTable;
import com.mucommander.ui.main.table.FileTableModel;

import java.util.Hashtable;

/**
 * This action compares the content of the 2 MainFrame's file tables and marks the files that are different.
 *
 * @author Maxence Bernard
 */
public class CompareFoldersAction extends MuAction {

    public CompareFoldersAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    public void performAction() {
        FileTable leftTable = mainFrame.getLeftPanel().getFileTable();
        FileTable rightTable = mainFrame.getRightPanel().getFileTable();

        FileTableModel leftTableModel = leftTable.getFileTableModel();
        FileTableModel rightTableModel = rightTable.getFileTableModel();

        int nbFilesLeft = leftTableModel.getFileCount();
        int nbFilesRight = rightTableModel.getFileCount();
        int fileIndex;
        String tempFileName;
        AbstractFile tempFile;
        for(int i=0; i<nbFilesLeft; i++) {
            tempFile = leftTableModel.getFileAt(i);
            if(tempFile.isDirectory())
                continue;

            tempFileName = tempFile.getName();
            fileIndex = -1;
            for(int j=0; j<nbFilesRight; j++)
                if (rightTableModel.getFileAt(j).getName().equals(tempFileName)) {
                    fileIndex = j;
                    break;
                }
            if (fileIndex==-1 || rightTableModel.getFileAt(fileIndex).getDate()<tempFile.getDate()) {
                leftTableModel.setFileMarked(tempFile, true);
                leftTable.repaint();
            }
        }

        for(int i=0; i<nbFilesRight; i++) {
            tempFile = rightTableModel.getFileAt(i);
            if(tempFile.isDirectory())
                continue;

            tempFileName = tempFile.getName();
            fileIndex = -1;
            for(int j=0; j<nbFilesLeft; j++)
                if (leftTableModel.getFileAt(j).getName().equals(tempFileName)) {
                    fileIndex = j;
                    break;
                }
            if (fileIndex==-1 || leftTableModel.getFileAt(fileIndex).getDate()<tempFile.getDate()) {
                rightTableModel.setFileMarked(tempFile, true);
                rightTable.repaint();
            }
        }

        // Notify registered listeners that currently marked files have changed on the file tables
        leftTable.fireMarkedFilesChangedEvent();
        rightTable.fireMarkedFilesChangedEvent();
    }
}
