package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.table.Columns;
import com.mucommander.ui.table.FileTable;
import com.mucommander.text.Translator;

/**
 * Shows/hides the 'Extension' column of the currently active FileTable. If the column is currently visible, this will
 * hide it and vice-versa.
 *
 * @author Maxence Bernard
 */
public class ToggleExtensionColumnAction extends MucoAction {

    public ToggleExtensionColumnAction(MainFrame mainFrame) {
        super(mainFrame, false);
        setLabel(Translator.get("extension"));
    }

    public void performAction() {
        FileTable activeTable = mainFrame.getActiveTable();
        activeTable.setColumnVisible(Columns.EXTENSION, !activeTable.isColumnVisible(Columns.EXTENSION));
    }
}
