package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.CheckVersionDialog;

/**
 * This action checks for a new version of muCommander.
 *
 * @author Maxence Bernard
 */
public class CheckForUpdatesAction extends MucoAction {

    public CheckForUpdatesAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction(MainFrame mainFrame) {
        new CheckVersionDialog(mainFrame, true);
    }
}