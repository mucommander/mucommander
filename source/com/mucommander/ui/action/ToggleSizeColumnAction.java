package com.mucommander.ui.action;

import com.mucommander.text.Translator;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.table.Columns;
import com.mucommander.ui.table.FileTable;

import java.util.Hashtable;

/**
 * Shows/hides the 'Size' column of the currently active FileTable. If the column is currently visible, this will
 * hide it and vice-versa.
 *
 * @author Maxence Bernard
 */
public class ToggleSizeColumnAction extends MucoAction {

    public ToggleSizeColumnAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties, false);
        setLabel(Translator.get("size"));
    }

    public void performAction() {
        FileTable activeTable = mainFrame.getActiveTable();
        activeTable.setColumnVisible(Columns.SIZE, !activeTable.isColumnVisible(Columns.SIZE));
    }
}
