package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;

import java.util.Hashtable;


/**
 * This action reverses the sort order of the currently active FileTable.
 *
 * @author Maxence Bernard
 */
public class ReverseSortOrderAction extends MucoAction {

    public ReverseSortOrderAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    public void performAction() {
        mainFrame.getActiveTable().reverseSortOrder();
    }
}
