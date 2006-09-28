package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;

/**
 * This action selects the first row/file in the current FileTable.
 *
 * @author Maxence Bernard
 */
public class SelectFirstRowAction extends MucoAction {

    public SelectFirstRowAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction(MainFrame mainFrame) {
        mainFrame.getLastActiveTable().selectRow(0);
    }
}
