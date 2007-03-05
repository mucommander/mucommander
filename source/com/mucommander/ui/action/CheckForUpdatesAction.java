package com.mucommander.ui.action;

import com.mucommander.ui.CheckVersionDialog;
import com.mucommander.ui.MainFrame;

import java.util.Hashtable;

/**
 * This action checks for a new version of muCommander.
 *
 * @author Maxence Bernard
 */
public class CheckForUpdatesAction extends MucoAction implements InvokesDialog {

    public CheckForUpdatesAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    public void performAction() {
        new CheckVersionDialog(mainFrame, true);
    }
}