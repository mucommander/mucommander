package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.table.FileTable;
import com.mucommander.ui.table.FileTableModel;

/**
 * This action unmarks all files in the current file table.
 *
 * @author Maxence Bernard
 */
public class UnmarkAllAction extends MucoAction {

    public UnmarkAllAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction() {
        FileTable fileTable = mainFrame.getLastActiveTable();
        FileTableModel tableModel = (FileTableModel)fileTable.getModel();

        int nbRows = tableModel.getRowCount();
        for(int i=fileTable.getParent()==null?0:1; i<nbRows; i++)
            tableModel.setRowMarked(i, false);
        fileTable.repaint();

        // Update status bar info
        mainFrame.getStatusBar().updateSelectedFilesInfo();
    }
}
