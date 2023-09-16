/*
 * This file is part of muCommander, http://www.mucommander.com
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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractListModel;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import com.mucommander.commons.file.Credentials;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.connection.ConnectionHandler;
import com.mucommander.commons.file.connection.ConnectionPool;
import com.mucommander.commons.util.ui.dialog.FocusDialog;
import com.mucommander.commons.util.ui.helper.MnemonicHelper;
import com.mucommander.commons.util.ui.layout.XBoxPanel;
import com.mucommander.desktop.ActionType;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.main.MainFrame;

/**
 * This dialog shows a list of server connections and allows the user to close/disconnect them.
 *
 * @author Maxence Bernard
 */
public class ShowServerConnectionsDialog extends FocusDialog implements ActionListener {

    private MainFrame mainFrame;

    private JList<String> connectionList;
    private java.util.List<ConnectionHandler> connections;

    private JButton disconnectButton;
    private JButton goToButton;
    private JButton closeButton;

    // Dialog's size has to be at least 400x300
    private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(400,300);

    // Dialog's size has to be at most 600x400
    private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(600,400);

    
    public ShowServerConnectionsDialog(MainFrame mainFrame) {
        super(mainFrame, ActionProperties.getActionLabel(ActionType.ShowServerConnections), mainFrame);

        this.mainFrame = mainFrame;

        Container contentPane = getContentPane();

        // Add the list of server connections

        connections = ConnectionPool.getConnectionHandlersSnapshot();

        connectionList = new JList<>(new AbstractListModel<String>() {
            @Override
            public int getSize() {
                return connections.size();
            }

            @Override
            public String getElementAt(int i) {
                ConnectionHandler connHandler = connections.get(i);

                // Show login (but not password) in the URL
                // Note: realm returned by ConnectionHandler does not contain credentials
                FileURL clonedRealm = (FileURL)connHandler.getRealm().clone();
                Credentials credentials = connHandler.getCredentials();
                if (credentials != null) {
                    Credentials loginCredentials = new Credentials(credentials.getLogin(), "");
                    clonedRealm.setCredentials(loginCredentials);
                }

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
                final ConnectionHandler connHandler = connections.get(selectedIndex);

                // Close connection in a separate thread as I/O can lock.
                // Todo: Add a confirmation dialog if the connection is active as it will stop whatever the connection is currently doing
                new Thread(){
                    @Override
                    public void run() {
                        connHandler.closeConnection();
                    }
                }.start();

                // Remove connection from the list
                connections.remove(selectedIndex);
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
                mainFrame.getActivePanel().tryChangeCurrentFolder(connections.get(selectedIndex).getRealm());
        }
        // Dispose the dialog
        else if (source==closeButton)  {
            dispose();
        }
    }
}
