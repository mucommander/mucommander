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
import com.mucommander.text.Translator;
import com.mucommander.ui.main.MainFrame;

import javax.swing.*;
import java.net.MalformedURLException;
import java.text.ParseException;


/**
 * This ServerPanel helps initiate HTTP connections. 
 *
 * @author Maxence Bernard
 */
public class HTTPPanel extends ServerPanel {

    private final static int STANDARD_PORT = FileURL.getRegisteredHandler(FileProtocols.HTTP).getStandardPort();

    private JTextField urlField;
    private JSpinner portSpinner;
    private JTextField usernameField;
    private JPasswordField passwordField;

    private static String lastURL = "http://";
    private static int lastPort = STANDARD_PORT;
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
        portSpinner = createPortSpinner(lastPort);
        addRow(Translator.get("server_connect_dialog.port"), portSpinner, 20);
        
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

        lastPort = ((Integer)portSpinner.getValue()).intValue();
    }


    ////////////////////////////////
    // ServerPanel implementation //
    ////////////////////////////////
	
    FileURL getServerURL() throws MalformedURLException {
        updateValues();
        
        if(!lastURL.toLowerCase().startsWith(FileProtocols.HTTP+"://"))
            lastURL = FileProtocols.HTTP+"://"+lastURL;

        FileURL fileURL = FileURL.getFileURL(lastURL);

        fileURL.setPort(lastPort);
        fileURL.setCredentials(new Credentials(lastUsername, lastPassword));

        return fileURL;
    }
	
    boolean usesCredentials() {
        return true;
    }

    public void dialogValidated() {
        // Commits the current spinner value in case it was being edited and 'enter' was pressed
        // (the spinner value would otherwise not be committed)
        try { portSpinner.commitEdit(); }
        catch(ParseException e) { }

        updateValues();
    }

}
