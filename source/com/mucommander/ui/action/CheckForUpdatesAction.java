package com.mucommander.ui.action;

import com.mucommander.ui.CheckVersionDialog;
import com.mucommander.ui.MainFrame;

/**
 * This action checks for a new version of muCommander.
 *
 * @author Maxence Bernard
 */
public class CheckForUpdatesAction extends MucoAction implements InvokesDialog {

    public CheckForUpdatesAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction() {
        new CheckVersionDialog(mainFrame, true);
    }
}