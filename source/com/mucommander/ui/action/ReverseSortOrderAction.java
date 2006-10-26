package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;


/**
 * This action reverses the sort order of the currently active FileTable.
 *
 * @author Maxence Bernard
 */
public class ReverseSortOrderAction extends MucoAction {

    public ReverseSortOrderAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction() {
        mainFrame.getActiveTable().reverseSortOrder();
    }
}
