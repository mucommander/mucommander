/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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

import com.mucommander.commons.file.Credentials;
import com.mucommander.commons.file.FileProtocols;
import com.mucommander.commons.file.FileURL;
import com.mucommander.text.Translator;
import com.mucommander.ui.main.MainFrame;

import javax.swing.*;
import java.net.MalformedURLException;


/**
 * This ServerPanel helps initiate SMB connections.
 *
 * @author Maxence Bernard
 */
public class SMBPanel extends ServerPanel {

    private JTextField domainField;
    private JTextField serverField;
    private JTextField shareField;
    private JTextField usernameField;
    private JPasswordField passwordField;

    private static String lastDomain = "";
    private static String lastServer = "";
    private static String lastShare = "";
    private static String lastUsername = "";
    // Not static so that it is not saved (for security reasons)
    private String lastPassword = "";

	
    SMBPanel(ServerConnectDialog dialog, MainFrame mainFrame) {
        super(dialog, mainFrame);

        // Server field
        serverField = new JTextField(lastServer);
        serverField.selectAll();
        addTextFieldListeners(serverField, true);
        addRow(Translator.get("server_connect_dialog.server"), serverField, 5);

        // Share field
        shareField = new JTextField(lastShare);
        shareField.selectAll();
        addTextFieldListeners(shareField, true);
        addRow(Translator.get("server_connect_dialog.share"), shareField, 15);

        // Domain field
        domainField = new JTextField(lastDomain);
        domainField.selectAll();
        addTextFieldListeners(domainField, true);
        addRow(Translator.get("server_connect_dialog.domain"), domainField, 15);

        // Username field
        usernameField = new JTextField(lastUsername);
        usernameField.selectAll();
        addTextFieldListeners(usernameField, false);
        addRow(Translator.get("server_connect_dialog.username"), usernameField, 5);

        // Password field
        passwordField = new JPasswordField();
        addTextFieldListeners(passwordField, false);
        addRow(Translator.get("password"), passwordField, 0);
    }

	
    private void updateValues() {
        lastServer = serverField.getText();
        lastShare = shareField.getText();
        lastDomain = domainField.getText();
        lastUsername = usernameField.getText();
        lastPassword = new String(passwordField.getPassword());
    }
	
	
    ////////////////////////////////
    // ServerPanel implementation //
    ////////////////////////////////
	
    @Override
    FileURL getServerURL() throws MalformedURLException {
        updateValues();
        FileURL url = FileURL.getFileURL(FileProtocols.SMB+"://"+lastServer+(lastShare.startsWith("/")?"":"/")+lastShare);

        // Insert the domain (if any) before the username, separated by a semicolon
        String userInfo = lastUsername;
        if(!lastDomain.equals(""))
            userInfo = lastDomain+";"+userInfo;

        url.setCredentials(new Credentials(userInfo, lastPassword));

        return url;
    }

    @Override
    boolean usesCredentials() {
        return true;
    }
    @Override
    public void dialogValidated() {
        updateValues();
    }
}
