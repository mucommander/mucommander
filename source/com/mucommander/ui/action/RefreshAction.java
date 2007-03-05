package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;

import java.util.Hashtable;

/**
 * This action refreshes the currently active FolderPanel (refreshes the content of the folder).
 *
 * @author Maxence Bernard
 */
public class RefreshAction extends MucoAction {

    public RefreshAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    public void performAction() {
        // Refresh current folder in a separate thread
        mainFrame.getActiveTable().getFolderPanel().tryRefreshCurrentFolder();
    }
}
