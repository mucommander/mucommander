package com.mucommander.ui.action;

import com.mucommander.conf.*;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.table.FileTable;

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

    public void performAction() {
        FileTable activeTable = mainFrame.getActiveTable();
        boolean autoSizeEnabled = !activeTable.isAutoSizeColumnsEnabled();
        activeTable.setAutoSizeColumnsEnabled(autoSizeEnabled);
        ConfigurationManager.setVariableBoolean(ConfigurationVariables.AUTO_SIZE_COLUMNS, autoSizeEnabled);
    }
}
