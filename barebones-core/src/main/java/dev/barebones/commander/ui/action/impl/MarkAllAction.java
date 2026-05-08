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

package dev.barebones.commander.ui.action.impl;

import java.util.Map;

import dev.barebones.commander.desktop.ActionType;
import dev.barebones.commander.ui.action.AbstractActionDescriptor;
import dev.barebones.commander.ui.action.ActionCategory;
import dev.barebones.commander.ui.action.ActionDescriptor;
import dev.barebones.commander.ui.action.MuAction;
import dev.barebones.commander.ui.action.NoIcon;
import dev.barebones.commander.ui.main.MainFrame;
import dev.barebones.commander.ui.main.table.FileTable;
import dev.barebones.commander.ui.main.table.FileTableModel;

/**
 * This action marks all files in the current file table.
 *
 * @author Maxence Bernard
 */
public class MarkAllAction extends MuAction {
    private boolean mark;

    protected MarkAllAction(MainFrame mainFrame, Map<String, Object> properties, boolean mark) {
        super(mainFrame, properties);
        this.mark = mark;
    }

    public MarkAllAction(MainFrame mainFrame, Map<String, Object> properties) {
        this(mainFrame, properties, true);
    }

    @Override
    public void performAction() {
        FileTable fileTable = mainFrame.getActiveTable();
        FileTableModel tableModel = fileTable.getFileTableModel();

        int nbRows = tableModel.getRowCount();
        for (int i = tableModel.getFirstMarkableRow(); i < nbRows; i++)
            tableModel.setRowMarked(i, mark);
        fileTable.repaint();

        // Notify registered listeners that currently marked files have changed on the FileTable
        fileTable.fireMarkedFilesChangedEvent();
    }

    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }

    @NoIcon
    public static class Descriptor extends AbstractActionDescriptor {
        public String getId() {
            return ActionType.MarkAll.getId();
        }

        public ActionCategory getCategory() {
            return ActionCategory.SELECTION;
        }
    }
}
