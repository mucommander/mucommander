package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;

import java.util.Hashtable;

/**
 * This action selects the first row/file in the current FileTable.
 *
 * @author Maxence Bernard
 */
public class SelectFirstRowAction extends MucoAction {

    public SelectFirstRowAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    public void performAction() {
        mainFrame.getActiveTable().selectRow(0);
    }
}
