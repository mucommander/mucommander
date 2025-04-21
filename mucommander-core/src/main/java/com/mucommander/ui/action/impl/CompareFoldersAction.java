/*
 * This file is part of muCommander, http://www.mucommander.com
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

import java.util.Map;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.protocol.FileProtocols;
import com.mucommander.desktop.ActionType;
import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionDescriptor;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.action.NoIcon;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.table.FileTable;
import com.mucommander.ui.main.table.FileTableModel;

/**
 * This action compares the content of the 2 MainFrame's file tables and marks the files that are different.
 *
 * @author Maxence Bernard
 */
public class CompareFoldersAction extends MuAction {

    public CompareFoldersAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);
    }

    @Override
    public void performAction() {
        FileTable leftTable = mainFrame.getLeftPanel().getFileTable();
        FileTable rightTable = mainFrame.getRightPanel().getFileTable();

        FileTableModel leftTableModel = leftTable.getFileTableModel();
        FileTableModel rightTableModel = rightTable.getFileTableModel();

        boolean ignoreMs =
                leftTableModel.getCurrentFolder().getURL().getScheme().equals(FileProtocols.SFTP) ||
                rightTableModel.getCurrentFolder().getURL().getScheme().equals(FileProtocols.SFTP);

        int nbFilesLeft = leftTableModel.getFileCount();
        int nbFilesRight = rightTableModel.getFileCount();
        int fileIndex;
        String tempFileName;
        AbstractFile tempFile;
        for (int i = 0; i < nbFilesLeft; i++) {
            tempFile = leftTableModel.getFileAt(i);
            if (tempFile.isDirectory())
                continue;

            tempFileName = tempFile.getName();
            fileIndex = -1;
            for (int j = 0; j < nbFilesRight; j++)
                if (rightTableModel.getFileAt(j).getName().equals(tempFileName)) {
                    fileIndex = j;
                    break;
                }
            if (fileIndex == -1 || isLessThanDate(rightTableModel.getFileAt(fileIndex).getDate(), tempFile.getDate(), ignoreMs)) {
                leftTableModel.setFileMarked(tempFile, true);
                leftTable.repaint();
            }
        }

        for (int i = 0; i < nbFilesRight; i++) {
            tempFile = rightTableModel.getFileAt(i);
            if (tempFile.isDirectory())
                continue;

            tempFileName = tempFile.getName();
            fileIndex = -1;
            for (int j = 0; j < nbFilesLeft; j++)
                if (leftTableModel.getFileAt(j).getName().equals(tempFileName)) {
                    fileIndex = j;
                    break;
                }
            if (fileIndex == -1 || isLessThanDate(leftTableModel.getFileAt(fileIndex).getDate(), tempFile.getDate(), ignoreMs)) {
                rightTableModel.setFileMarked(tempFile, true);
                rightTable.repaint();
            }
        }

        // Notify registered listeners that currently marked files have changed on the file tables
        leftTable.fireMarkedFilesChangedEvent();
        rightTable.fireMarkedFilesChangedEvent();
    }

    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }

    private boolean isLessThanDate(long a, long b, boolean ignoreMs) {
        if (ignoreMs) {
            a = (a / 1000) * 1000;
            b = (b / 1000) * 1000;
        }
        return a < b;
    }

    @NoIcon
    public static class Descriptor extends AbstractActionDescriptor {
        public String getId() {
            return ActionType.CompareFolders.getId();
        }

        public ActionCategory getCategory() {
            return ActionCategory.SELECTION;
        }
    }
}
