/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
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
import com.mucommander.file.FileProtocols;
import com.mucommander.file.FileURL;
import com.mucommander.file.impl.ftp.FTPFile;
import com.mucommander.text.Translator;
import com.mucommander.ui.combobox.EncodingComboBox;
import com.mucommander.ui.combobox.SaneComboBox;
import com.mucommander.ui.main.MainFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;


/**
 * 
 *
 * @author Maxence Bernard
 */
class FTPPanel extends ServerPanel implements ActionListener {

    private JTextField serverField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField initialDirField;
    private JTextField portField;
    private SaneComboBox encodingComboBox;
    private JCheckBox passiveCheckBox;
    private JCheckBox anonymousCheckBox;
	
    private static String lastServer = "";
    private static String lastUsername = "";
    private static String lastInitialDir = "/";
    private static int lastPort = 21;
    private static String lastEncoding = FTPFile.DEFAULT_ENCODING;
    // Not static so that it is not remembered (for security reasons)
    private String lastPassword = "";
    private String lastAnonymousPassword = "";

    /** Passive mode is enabled by default because of firewall restrictions */
    private static boolean passiveMode = true;
    private static boolean anonymousUser;
	
	
    FTPPanel(ServerConnectDialog dialog, MainFrame mainFrame) {
        super(dialog, mainFrame);

        // Server field, initialized to last server entered
        serverField = new JTextField(lastServer);
        serverField.selectAll();
        addTextFieldListeners(serverField, true);
        addRow(Translator.get("server_connect_dialog.server"), serverField, 15);

        // Username field, initialized to last username entered or 'anonymous' if anonymous user was previously selected
        usernameField = new JTextField(anonymousUser?"anonymous":lastUsername);
        usernameField.selectAll();
        usernameField.setEditable(!anonymousUser);
        addTextFieldListeners(usernameField, false);
        addRow(Translator.get("server_connect_dialog.username"), usernameField, 5);

        // Password field, initialized to ""
        passwordField = new JPasswordField();
        addTextFieldListeners(passwordField, false);
        addRow(Translator.get("password"), passwordField, 15);

        // Initial directory field, initialized to "/"
        initialDirField = new JTextField(lastInitialDir);
        initialDirField.selectAll();
        addTextFieldListeners(initialDirField, true);
        addRow(Translator.get("server_connect_dialog.initial_dir"), initialDirField, 5);
	
        // Port field, initialized to last port (default is 21)
        portField = new JTextField(""+lastPort, 5);
        portField.selectAll();
        addTextFieldListeners(portField, true);
        addRow(Translator.get("server_connect_dialog.port"), portField, 5);

        // Encoding combo box
        encodingComboBox = new EncodingComboBox();
        encodingComboBox.setSelectedItem(lastEncoding);
        encodingComboBox.addActionListener(this);
        addRow(Translator.get("encoding"), encodingComboBox, 15);

        // Anonymous user checkbox
        anonymousCheckBox = new JCheckBox(Translator.get("ftp_connect.anonymous_user"), anonymousUser);
        anonymousCheckBox.addActionListener(this);
        addRow(anonymousCheckBox, 5);

        // Passive mode checkbox
        passiveCheckBox = new JCheckBox(Translator.get("ftp_connect.passive_mode"), passiveMode);
        passiveCheckBox.addActionListener(this);
        addRow(passiveCheckBox, 0);
    }

	
    private void updateValues() {
        lastServer = serverField.getText();
        if(!anonymousUser) {
            lastUsername = usernameField.getText();
            lastPassword = new String(passwordField.getPassword());
        }

        lastInitialDir = initialDirField.getText();
		
        lastPort = 21;
        try {
            lastPort = Integer.parseInt(portField.getText());
        }
        catch(NumberFormatException e) {
            // Port is a malformed number
        }
    }
	
	
    ////////////////////////////////
    // ServerPanel implementation //
    ////////////////////////////////
	
    FileURL getServerURL() throws MalformedURLException {
        updateValues();
        if(!lastInitialDir.startsWith("/"))
            lastInitialDir = "/"+lastInitialDir;
			
        FileURL url = new FileURL(FileProtocols.FTP+"://"+lastServer+lastInitialDir);

        if(anonymousUser)
            url.setCredentials(new Credentials("anonymous", new String(passwordField.getPassword())));
        else
            url.setCredentials(new Credentials(lastUsername, lastPassword));

        // Set port
        if(lastPort!=21 && (lastPort>0 && lastPort<65536))
            url.setPort(lastPort);

        // Set passiveMode property to true (default) or false
        url.setProperty(FTPFile.PASSIVE_MODE_PROPERTY_NAME, ""+passiveMode);

        // Set FTP encoding property
        url.setProperty(FTPFile.ENCODING_PROPERTY_NAME, (String)encodingComboBox.getSelectedItem());

        return url;
    }

    boolean usesCredentials() {
        return true;
    }

    public void dispose() {
        updateValues();
    }


    ////////////////////////////
    // ActionListener methods //
    ////////////////////////////
	
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
		
        if(source == passiveCheckBox) {
            passiveMode = passiveCheckBox.isSelected();
        }
        else if (source == anonymousCheckBox) {
            updateValues();
            this.anonymousUser = anonymousCheckBox.isSelected();
            if(anonymousUser) {
                usernameField.setEditable(false);
                usernameField.setText("anonymous");
                passwordField.setText(lastAnonymousPassword);
            }
            else {
                lastAnonymousPassword = new String(passwordField.getPassword());
                usernameField.setEditable(true);
                usernameField.setText(lastUsername);
                passwordField.setText(lastPassword);
            }
        }
        else if (source == encodingComboBox) {
            lastEncoding = (String)encodingComboBox.getSelectedItem();
        }
    }
	
}
