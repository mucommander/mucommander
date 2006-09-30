package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.table.FileTable;
import com.mucommander.ui.table.FileTableModel;

/**
 * This action marks all files in the current file table.
 *
 * @author Maxence Bernard
 */
public class MarkAllAction extends MucoAction {

    public MarkAllAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction() {
        FileTable fileTable = mainFrame.getLastActiveTable();
        FileTableModel tableModel = (FileTableModel)fileTable.getModel();

        int nbRows = tableModel.getRowCount();
        for(int i=fileTable.getParent()==null?0:1; i<nbRows; i++)
            tableModel.setRowMarked(i, true);
        fileTable.repaint();

        // Update status bar info
        mainFrame.getStatusBar().updateSelectedFilesInfo();
    }
}