package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.table.Columns;
import com.mucommander.ui.table.FileTable;
import com.mucommander.text.Translator;

/**
 * Shows/hides the 'Date' column of the currently active FileTable. If the column is currently visible, this will
 * hide it and vice-versa.
 *
 * @author Maxence Bernard
 */
public class ToggleDateColumnAction extends MucoAction {

    public ToggleDateColumnAction(MainFrame mainFrame) {
        super(mainFrame, false);
        setLabel(Translator.get("date"));
    }

    public void performAction() {
        FileTable activeTable = mainFrame.getActiveTable();
        activeTable.setColumnVisible(Columns.DATE, !activeTable.isColumnVisible(Columns.DATE));
    }
}
