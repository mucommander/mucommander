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
import com.mucommander.auth.MappedCredentials;
import com.mucommander.file.FileURL;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.DialogToolkit;
import com.mucommander.ui.dialog.FocusDialog;
import com.mucommander.ui.helper.FocusRequester;
import com.mucommander.ui.layout.XBoxPanel;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.main.MainFrame;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Vector;


/**
 * Dialog that facilitate the connection to a server. It contains tabs and associated panels for each of the protocols
 * supported.   
 *
 * @author Maxence Bernard
 */
public class ServerConnectDialog extends FocusDialog implements ActionListener, ChangeListener, DocumentListener {

    private MainFrame mainFrame;
	
    private JButton cancelButton;
    private ServerPanel currentServerPanel;

    private JTabbedPane tabbedPane;
    private Vector serverPanels;

    private JLabel urlLabel;
    private JCheckBox saveCredentialsCheckBox;

    // Dialog's width has to be at least 320
    private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(360,0);	
	
    public final static int SMB_INDEX = 0;
    public final static int FTP_INDEX = 1;
    public final static int SFTP_INDEX = 2;
    public final static int HTTP_INDEX = 3;
    public final static int NFS_INDEX = 4;

    private static int lastPanelIndex = SMB_INDEX;
	
	
    public ServerConnectDialog(MainFrame mainFrame) {
        this(mainFrame, lastPanelIndex);
    }
	
		
    public ServerConnectDialog(MainFrame mainFrame, int panelIndex) {
        super(mainFrame, Translator.get(com.mucommander.ui.action.ConnectToServerAction.class.getName()+".label"), mainFrame);
        this.mainFrame = mainFrame;
        this.lastPanelIndex = panelIndex;

        Container contentPane = getContentPane();
		
        this.tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        this.serverPanels = new Vector();

//        Enumeration registeredProtocols = FileFactory.getRegisteredFileProtocols();
//        if(registeredProtocols.contains(FileProtocols.SMB))
            addTab("SMB", new SMBPanel(this, mainFrame));
//        if(registeredProtocols.contains(FileProtocols.FTP))
            addTab("FTP", new FTPPanel(this, mainFrame));
//        if(registeredProtocols.contains(FileProtocols.SFTP))
            addTab("SFTP", new SFTPPanel(this, mainFrame));
//        if(registeredProtocols.contains(FileProtocols.HTTP))
            addTab("HTTP", new HTTPPanel(this, mainFrame));
        addTab("NFS", new NFSPanel(this, mainFrame));

        tabbedPane.setSelectedIndex(panelIndex);
        currentServerPanel = (ServerPanel)serverPanels.elementAt(panelIndex);

        // Listen to tab change events
        tabbedPane.addChangeListener(this);
        contentPane.add(tabbedPane, BorderLayout.CENTER);
		
        YBoxPanel yPanel = new YBoxPanel();
        XBoxPanel xPanel = new XBoxPanel();
        xPanel.add(new JLabel(Translator.get("server_connect_dialog.server_url")+":"));
        xPanel.addSpace(5);
        urlLabel = new JLabel("");
        updateURLLabel();
        xPanel.add(urlLabel);
        yPanel.add(xPanel);

        yPanel.addSpace(10);

        this.saveCredentialsCheckBox = new JCheckBox(Translator.get("auth_dialog.store_credentials"), false);
        // Enables 'save credentials' checkbox only if server panel/protocol uses credentials
        saveCredentialsCheckBox.setEnabled(currentServerPanel.usesCredentials());
        yPanel.add(saveCredentialsCheckBox);

        JButton okButton = new JButton(Translator.get("server_connect_dialog.connect"));
        cancelButton = new JButton(Translator.get("cancel"));
        yPanel.add(DialogToolkit.createOKCancelPanel(okButton, cancelButton, this));

        contentPane.add(yPanel, BorderLayout.SOUTH);
		
        // initial focus
        setInitialFocusComponent(currentServerPanel);		
		
        // Selects OK when enter is pressed
        getRootPane().setDefaultButton(okButton);

        setMinimumSize(MINIMUM_DIALOG_DIMENSION);
    }


    public void addTab(String title, ServerPanel serverPanel) {
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(serverPanel, BorderLayout.NORTH);
        tabbedPane.addTab(title, northPanel);
        serverPanels.add(serverPanel);
    }


    private void updateURLLabel() {
        try {
            FileURL url = currentServerPanel.getServerURL();
            urlLabel.setText(url==null?" ":url.toString(false));
        }
        catch(MalformedURLException ex) {
            urlLabel.setText(" ");
        }
    }
	
	
    ////////////////////////////
    // ActionListener methods //
    ////////////////////////////
	
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source==cancelButton) {
            dispose();
            return;
        }

        try {
            FileURL serverURL = currentServerPanel.getServerURL();	// Can thrown a MalformedURLException

            // Create MappedCedentials instance and use it in the URL so that it's added to CredentialsManager once the
            // location has been properly opened
            Credentials credentials = serverURL.getCredentials();
            if(credentials!=null)
                serverURL.setCredentials(new MappedCredentials(credentials, serverURL, saveCredentialsCheckBox.isSelected()));

            currentServerPanel.dispose();
            dispose();

            // Change current folder
            mainFrame.getActiveTable().getFolderPanel().tryChangeCurrentFolder(serverURL);
        }
        catch(IOException ex) {
            JOptionPane.showMessageDialog(this, Translator.get("folder_does_not_exist"), Translator.get("table.folder_access_error_title"), JOptionPane.ERROR_MESSAGE);
            return;
        }
    }
	
	
    ///////////////////////////
    // ChangeListener method //
    ///////////////////////////
	
    public void stateChanged(ChangeEvent e) {
        lastPanelIndex = tabbedPane.getSelectedIndex();
        currentServerPanel = (ServerPanel)serverPanels.elementAt(lastPanelIndex);

        // Enables 'save credentials' checkbox only if server panel/protocol uses credentials
        saveCredentialsCheckBox.setEnabled(currentServerPanel.usesCredentials());

        updateURLLabel();
        FocusRequester.requestFocus(currentServerPanel);
    }

	
    //////////////////////////////
    // DocumentListener methods //
    //////////////////////////////
	
    public void changedUpdate(DocumentEvent e) {
        updateURLLabel();
    }
	
    public void insertUpdate(DocumentEvent e) {
        updateURLLabel();
    }

    public void removeUpdate(DocumentEvent e) {
        updateURLLabel();
    }
}
