/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.ui.dialog.server;

import com.mucommander.auth.Credentials;
import com.mucommander.file.FileURL;
import com.mucommander.file.connection.ConnectionHandler;
import com.mucommander.file.connection.ConnectionPool;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.FocusDialog;
import com.mucommander.ui.helper.MnemonicHelper;
import com.mucommander.ui.layout.XBoxPanel;
import com.mucommander.ui.main.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

/**
 * This dialog shows a list of server connections and allows the user to close/disconnect them.
 *
 * @author Maxence Bernard
 */
public class ShowServerConnectionsDialog extends FocusDialog implements ActionListener {

    private MainFrame mainFrame;

    private JList connectionList;
    private Vector connections;

    private JButton disconnectButton;
    private JButton goToButton;
    private JButton closeButton;

    // Dialog's size has to be at least 400x300
    private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(400,300);

    // Dialog's size has to be at most 600x400
    private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(600,400);

    
    public ShowServerConnectionsDialog(MainFrame mainFrame) {
        super(mainFrame, Translator.get(com.mucommander.ui.action.ShowServerConnectionsAction.class.getName()+".label"), mainFrame);

        this.mainFrame = mainFrame;

        Container contentPane = getContentPane();

        // Add the list of server connections

        connections = ConnectionPool.getConnectionHandlersSnapshot();

        connectionList = new JList(new AbstractListModel() {
            public int getSize() {
                return connections.size();
            }

            public Object getElementAt(int i) {
                ConnectionHandler connHandler = ((ConnectionHandler)connections.elementAt(i));

                // Show login (but not password) in the URL
                // Note: realm returned by ConnectionHandler does not contain credentials
                FileURL clonedRealm = (FileURL)connHandler.getRealm().clone();
                Credentials loginCredentials = new Credentials(connHandler.getCredentials().getLogin(), "");
                clonedRealm.setCredentials(loginCredentials);

                return clonedRealm.toString(true)
                        +" ("+Translator.get(connHandler.isLocked()?"server_connections_dialog.connection_busy":"server_connections_dialog.connection_idle")+")";
            }
        });

        // Only one list index can be selected at a time
        connectionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Select the first connection in the list
        boolean hasConnections = connections.size()>0;
        if(hasConnections)
            connectionList.setSelectedIndex(0);

        contentPane.add(
                new JScrollPane(connectionList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),
                BorderLayout.CENTER);

        // Add buttons

        XBoxPanel buttonsPanel = new XBoxPanel();
        JPanel buttonGroupPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        MnemonicHelper mnemonicHelper = new MnemonicHelper();

        // Disconnect button
        disconnectButton = new JButton(Translator.get("server_connections_dialog.disconnect"));
        disconnectButton.setMnemonic(mnemonicHelper.getMnemonic(disconnectButton));
        disconnectButton.setEnabled(hasConnections);
        if(hasConnections)
            disconnectButton.addActionListener(this);

        buttonGroupPanel.add(disconnectButton);

        // Go to button
        goToButton = new JButton(Translator.get("go_to"));
        goToButton.setMnemonic(mnemonicHelper.getMnemonic(goToButton));
        goToButton.setEnabled(hasConnections);
        if(hasConnections)
            goToButton.addActionListener(this);
        buttonGroupPanel.add(goToButton);

        buttonsPanel.add(buttonGroupPanel);

        // Button that closes the window
        closeButton = new JButton(Translator.get("close"));
        closeButton.setMnemonic(mnemonicHelper.getMnemonic(closeButton));
        closeButton.addActionListener(this);

        buttonsPanel.add(Box.createHorizontalGlue());
        buttonsPanel.add(closeButton);

        contentPane.add(buttonsPanel, BorderLayout.SOUTH);

        // Connections list will receive initial focus
        setInitialFocusComponent(connectionList);

        // Selects 'Done' button when enter is pressed
        getRootPane().setDefaultButton(closeButton);

        // Packs dialog
        setMinimumSize(MINIMUM_DIALOG_DIMENSION);
        setMaximumSize(MAXIMUM_DIALOG_DIMENSION);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        showDialog();
    }


    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        // Disconnects the selected connection
        if(source==disconnectButton) {
            int selectedIndex = connectionList.getSelectedIndex();

            if(selectedIndex>=0 && selectedIndex<connections.size()) {
                final ConnectionHandler connHandler = (ConnectionHandler)connections.elementAt(selectedIndex);

                // Close connection in a separate thread as I/O can lock.
                // Todo: Add a confirmation dialog if the connection is active as it will stop whatever the connection is currently doing
                new Thread(){
                    public void run() {
                        connHandler.closeConnection();
                    }
                }.start();

                // Remove connection from the list
                connections.removeElementAt(selectedIndex);
                connectionList.setSelectedIndex(Math.min(selectedIndex, connections.size()));
                connectionList.repaint();

                // Disable contextual butons if there are no more connections
                if(connections.size()==0) {
                    disconnectButton.setEnabled(false);
                    goToButton.setEnabled(false);
                }
            }
        }
        // Goes to the selected connection
        else if (source==goToButton)  {
            // Dispose the dialog first
            dispose();

            int selectedIndex = connectionList.getSelectedIndex();
            if(selectedIndex>=0 && selectedIndex<connections.size())
                mainFrame.getActiveTable().getFolderPanel().tryChangeCurrentFolder(((ConnectionHandler)connections.elementAt(selectedIndex)).getRealm());
        }
        // Dispose the dialog
        else if (source==closeButton)  {
            dispose();
        }
    }
}
