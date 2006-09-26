package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.table.FileTable;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * This action sorts the currently active FileTable by size.
 * If the table is already sorted by size, the sort order will be reversed.
 *
 * @author Maxence Bernard
 */
public class SortBySizeAction extends MucoAction {

    public SortBySizeAction(MainFrame mainFrame) {
        super(mainFrame, KeyStroke.getKeyStroke(KeyEvent.VK_F5, KeyEvent.CTRL_MASK));
    }

    public void performAction(MainFrame mainFrame) {
        mainFrame.getLastActiveTable().sortBy(FileTable.SIZE);
    }
}
