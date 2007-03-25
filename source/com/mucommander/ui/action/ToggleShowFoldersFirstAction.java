package com.mucommander.ui.action;

import com.mucommander.conf.ConfigurationManager;
import com.mucommander.conf.ConfigurationVariables;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.table.FileTable;

import java.util.Hashtable;

/**
 * This action toggles the 'Show folders first' option, which controls whether folders are displayed first in the
 * FileTable or mixed with regular files.
 *
 * @author Maxence Bernard
 */
public class ToggleShowFoldersFirstAction extends MucoAction {

    public ToggleShowFoldersFirstAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    public void performAction() {
        FileTable activeTable = mainFrame.getActiveTable();
        boolean showFoldersFirst = !activeTable.isShowFoldersFirstEnabled();
        activeTable.setShowFoldersFirstEnabled(showFoldersFirst);
        ConfigurationManager.setVariableBoolean(ConfigurationVariables.SHOW_FOLDERS_FIRST, showFoldersFirst);
    }
}
