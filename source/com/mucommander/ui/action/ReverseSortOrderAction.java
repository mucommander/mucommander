package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;


/**
 * This action reverses the sort order of the currently active FileTable.
 *
 * @author Maxence Bernard
 */
public class ReverseSortOrderAction extends MucoAction {

    public ReverseSortOrderAction(MainFrame mainFrame) {
        super(mainFrame, "view_menu.reverse_order");
    }

    public void performAction(MainFrame mainFrame) {
        mainFrame.getLastActiveTable().reverseSortOrder();
    }
}
