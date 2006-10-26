package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.table.FileTable;

/**
 * This action sorts the currently active FileTable by size.
 * If the table is already sorted by size, the sort order will be reversed.
 *
 * @author Maxence Bernard
 */
public class SortBySizeAction extends MucoAction {

    public SortBySizeAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction() {
        mainFrame.getActiveTable().sortBy(FileTable.SIZE);
    }
}
