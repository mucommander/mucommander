package com.mucommander.ui.action;

import com.mucommander.text.Translator;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.table.Columns;
import com.mucommander.ui.table.FileTable;

import java.util.Hashtable;

/**
 * Shows/hides the 'Permissions' column of the currently active FileTable. If the column is currently visible, this will
 * hide it and vice-versa.
 *
 * @author Maxence Bernard
 */
public class TogglePermissionsColumnAction extends MucoAction {

    public TogglePermissionsColumnAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties, false);
        setLabel(Translator.get("permissions"));
    }

    public void performAction() {
        FileTable activeTable = mainFrame.getActiveTable();
        activeTable.setColumnVisible(Columns.PERMISSIONS, !activeTable.isColumnVisible(Columns.PERMISSIONS));
    }
}
