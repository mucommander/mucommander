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


/**
 * 
 *
 * @author Maxence Bernard
 */
class HTTPPanel extends ServerPanel {

    private JTextField urlField;
    private JTextField portField;
    private JTextField usernameField;
    private JPasswordField passwordField;

    private static String lastURL = "http://";
    private static int lastPort = 80;
    private static String lastUsername = "";
    // Not static so that it is not saved (for security reasons)
    private String lastPassword = "";

	
    HTTPPanel(ServerConnectDialog dialog, MainFrame mainFrame) {
        super(dialog, mainFrame);

        // Webserver (URL) field
        urlField = new JTextField(lastURL);
        urlField.selectAll();
        addTextFieldListeners(urlField, true);
        addRow(Translator.get("server_connect_dialog.http_url"), urlField, 5);

        // Port field, initialized to last port (default is 80)
        portField = new JTextField(""+lastPort, 5);
        portField.selectAll();
        addTextFieldListeners(portField, true);
        addRow(Translator.get("server_connect_dialog.port"), portField, 20);
        
        // HTTP Basic authentication fields
        addRow(new JLabel(Translator.get("http_connect.basic_authentication")), 10);

        // Username field
        usernameField = new JTextField(lastUsername);
        usernameField.selectAll();
        addTextFieldListeners(usernameField, false);
        addRow(Translator.get("server_connect_dialog.username"), usernameField, 5);

        // Password field
        passwordField = new JPasswordField(lastPassword);
        addTextFieldListeners(passwordField, false);
        addRow(Translator.get("password"), passwordField, 0);
    }


    private void updateValues() {
        lastURL = urlField.getText();
        lastUsername = usernameField.getText();
        lastPassword = new String(passwordField.getPassword());

        lastPort = 80;
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
        
        if(!lastURL.toLowerCase().startsWith(FileProtocols.HTTP+"://"))
            lastURL = FileProtocols.HTTP+"://"+lastURL;

        FileURL fileURL = new FileURL(lastURL);

        // Set port
        if(lastPort!=80 && (lastPort>0 && lastPort<65536))
            fileURL.setPort(lastPort);

        fileURL.setCredentials(new Credentials(lastUsername, lastPassword));

        return fileURL;
    }
	
    boolean usesCredentials() {
        return true;
    }

    public void dispose() {
        updateValues();
    }

}
