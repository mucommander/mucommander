package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.table.FileTable;

/**
 * This action selects the last row/file in the current FileTable.
 *
 * @author Maxence Bernard
 */
public class SelectLastRowAction extends MucoAction {

    public SelectLastRowAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction() {
        FileTable fileTable = mainFrame.getLastActiveTable();
        fileTable.selectRow(fileTable.getFileTableModel().getRowCount()-1);
    }
}
