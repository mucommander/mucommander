package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.ShowServerConnectionsDialog;

/**
 * Displays a dialog which shows a list of open server connections and allows the user to close them.
 *
 * @author Maxence Bernard
 */
public class ShowServerConnectionsAction extends MucoAction implements InvokesDialog {

    public ShowServerConnectionsAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction() {
        new ShowServerConnectionsDialog(mainFrame);
    }
}
