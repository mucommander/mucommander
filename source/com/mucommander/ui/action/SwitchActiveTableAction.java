package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.table.FileTable;

import java.util.Hashtable;

/**
 * This action switches the currently active FileTable, that is gives focus to the FileTable that currently doesn't
 * have it.
 *
 * @author Maxence Bernard
 */
public class SwitchActiveTableAction extends MucoAction {

    public SwitchActiveTableAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    public void performAction() {
        FileTable activeTable = mainFrame.getActiveTable();
        FileTable table1 = mainFrame.getFolderPanel1().getFileTable();
        FileTable table2 = mainFrame.getFolderPanel2().getFileTable();
        if(activeTable == table1)
            table2.requestFocus();
        else if(activeTable == table2)
            table1.requestFocus();
    }
}
