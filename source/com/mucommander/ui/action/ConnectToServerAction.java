package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.connect.ServerConnectDialog;

import java.util.Hashtable;

/**
 * This action pops up the 'Connect to Server' dialog that is used to connect to a remote server.
 *
 * @author Maxence Bernard
 */
public class ConnectToServerAction extends MucoAction implements InvokesDialog {

    public ConnectToServerAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    public void performAction() {
        new ServerConnectDialog(mainFrame).showDialog();
    }
}