package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.table.FileTable;

/**
 * This action sorts the currently active FileTable by permissions.
 * If the table is already sorted by permissions, the sort order will be reversed.
 *
 * @author Maxence Bernard
 */
public class SortByPermissionsAction extends MucoAction {

    public SortByPermissionsAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction() {
        mainFrame.getActiveTable().sortBy(FileTable.PERMISSIONS);
    }
}
