package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.table.FileTable;

import java.util.Hashtable;

/**
 * This action selects the last row/file in the current FileTable.
 *
 * @author Maxence Bernard
 */
public class SelectLastRowAction extends MucoAction {

    public SelectLastRowAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    public void performAction() {
        FileTable fileTable = mainFrame.getActiveTable();
        fileTable.selectRow(fileTable.getFileTableModel().getRowCount()-1);
    }
}
