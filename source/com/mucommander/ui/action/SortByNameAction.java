package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.table.FileTable;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * This action sorts the currently active FileTable by name.
 * If the table is already sorted by name, the sort order will be reversed.
 *
 * @author Maxence Bernard
 */
public class SortByNameAction extends MucoAction {

    public SortByNameAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction(MainFrame mainFrame) {
        mainFrame.getLastActiveTable().sortBy(FileTable.NAME);
    }
}
