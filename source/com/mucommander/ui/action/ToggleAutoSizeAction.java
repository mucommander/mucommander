package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.table.FileTable;
import com.mucommander.conf.ConfigurationManager;

/**
 * This action toggles the 'auto-size columns' option on the currently active FileTable, which automatically resizes
 * columns to fit the table's width.
 *
 * @author Maxence Bernard
 */
public class ToggleAutoSizeAction extends MucoAction {

    public ToggleAutoSizeAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction(MainFrame mainFrame) {
        FileTable activeTable = mainFrame.getLastActiveTable();
        boolean autoSizeEnabled = !activeTable.isAutoSizeColumnsEnabled();
        activeTable.setAutoSizeColumnsEnabled(autoSizeEnabled);
        ConfigurationManager.setVariableBoolean("prefs.file_table.auto_size_columns", autoSizeEnabled);
    }
}

