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
import com.mucommander.file.FileProtocols;
import com.mucommander.file.FileURL;
import com.mucommander.text.Translator;
import com.mucommander.ui.main.MainFrame;

import javax.swing.*;
import java.net.MalformedURLException;


class SFTPPanel extends ServerPanel {

    private JTextField serverField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField initialDirField;
    private JTextField portField;
	
    private static String lastServer = "";
    private static String lastUsername = "";
    // Not static so that it is not saved (for security reasons)
    private String lastPassword = "";
    private static String lastInitialDir = "/";
    private static int lastPort = 22;
	
	
    SFTPPanel(ServerConnectDialog dialog, MainFrame mainFrame) {
        super(dialog, mainFrame);

        // Server field, initialized to last server entered
        serverField = new JTextField(lastServer);
        serverField.selectAll();
        addTextFieldListeners(serverField, true);
        addRow(Translator.get("server_connect_dialog.server"), serverField, 15);

        // Username field, initialized to last username
        usernameField = new JTextField(lastUsername);
        usernameField.selectAll();
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
	
        // Port field, initialized to last port (default is 22)
        portField = new JTextField(""+lastPort, 5);
        portField.selectAll();
        addTextFieldListeners(portField, true);
        addRow(Translator.get("server_connect_dialog.port"), portField, 15);
    }

	
    private void updateValues() {
        lastServer = serverField.getText();
        lastUsername = usernameField.getText();
        lastPassword = new String(passwordField.getPassword());
        lastInitialDir = initialDirField.getText();
        lastPort = 22;

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
			
        FileURL url = new FileURL(FileProtocols.SFTP+"://"+lastServer+lastInitialDir);

        url.setCredentials(new Credentials(lastUsername, lastPassword));

        // Set port
        if(lastPort>0 && lastPort!=22)
            url.setPort(lastPort);

        return url;
    }

    boolean usesCredentials() {
        return true;
    }

    public void dispose() {
        updateValues();
    }
}
