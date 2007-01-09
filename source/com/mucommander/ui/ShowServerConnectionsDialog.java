package com.mucommander.ui;

import com.mucommander.auth.Credentials;
import com.mucommander.file.FileURL;
import com.mucommander.file.connection.ConnectionHandler;
import com.mucommander.file.connection.ConnectionPool;
import com.mucommander.text.Translator;
import com.mucommander.ui.comp.MnemonicHelper;
import com.mucommander.ui.comp.dialog.FocusDialog;
import com.mucommander.ui.comp.dialog.XBoxPanel;

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

    private JList connectionList;
    private Vector connections;

    private JButton disconnectButton;
    private JButton doneButton;

    // Dialog's size has to be at least 400x300
    private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(400,300);

    // Dialog's size has to be at most 600x400
    private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(600,400);

    
    public ShowServerConnectionsDialog(MainFrame mainFrame) {
        super(mainFrame, Translator.get("com.mucommander.ui.action.ShowServerConnectionsAction.label"), mainFrame);

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

                return clonedRealm.getStringRep(true)
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
        MnemonicHelper mnemonicHelper = new MnemonicHelper();

        // Remove button
        disconnectButton = new JButton(Translator.get("server_connections_dialog.disconnect"));
        disconnectButton.setMnemonic(mnemonicHelper.getMnemonic(disconnectButton));
        disconnectButton.setEnabled(hasConnections);
        if(hasConnections)
            disconnectButton.addActionListener(this);

        buttonsPanel.add(disconnectButton);

        // 'Done' button that closes the window
        doneButton = new JButton(Translator.get("done"));
        doneButton.setMnemonic(mnemonicHelper.getMnemonic(doneButton));
        doneButton.addActionListener(this);

        buttonsPanel.add(Box.createHorizontalGlue());
        buttonsPanel.add(doneButton);

        contentPane.add(buttonsPanel, BorderLayout.SOUTH);

        // Connections list will receive initial focus
        setInitialFocusComponent(connectionList);

        // Selects 'Done' button when enter is pressed
        getRootPane().setDefaultButton(doneButton);

        // Packs dialog
        setMinimumSize(MINIMUM_DIALOG_DIMENSION);
        setMaximumSize(MAXIMUM_DIALOG_DIMENSION);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        showDialog();
    }


    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        // Dispose the dialog
        if (source==doneButton)  {
            dispose();
        }
        else if(source==disconnectButton) {
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

                if(connections.size()==0)
                    disconnectButton.setEnabled(false);
            }
        }
    }
}
