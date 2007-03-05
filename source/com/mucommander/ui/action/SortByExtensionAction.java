package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.table.FileTable;

import java.util.Hashtable;

/**
 * This action sorts the currently active FileTable by extension.
 * If the table is already sorted by extension, the sort order will be reversed.
 *
 * @author Maxence Bernard
 */
public class SortByExtensionAction extends MucoAction {

    public SortByExtensionAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    public void performAction() {
        mainFrame.getActiveTable().sortBy(FileTable.EXTENSION);
    }
}
