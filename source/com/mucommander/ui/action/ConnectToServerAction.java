package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.connect.ServerConnectDialog;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * This action pops up the 'Connect to Server' dialog that is used to connect to a remote server.
 *
 * @author Maxence Bernard
 */
public class ConnectToServerAction extends MucoAction {

    public ConnectToServerAction(MainFrame mainFrame) {
        super(mainFrame, KeyStroke.getKeyStroke(KeyEvent.VK_K, KeyEvent.CTRL_MASK));
    }

    public void performAction(MainFrame mainFrame) {
        new ServerConnectDialog(mainFrame).showDialog();
    }
}