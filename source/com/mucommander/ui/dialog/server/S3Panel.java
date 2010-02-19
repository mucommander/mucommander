/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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

import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import java.net.MalformedURLException;
import java.text.ParseException;


/**
 * This ServerPanel helps initiate S3 connections.
 *
 * @author Maxence Bernard
 */
public class S3Panel extends ServerPanel {

    private JTextField serverField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField initialDirField;
    private JSpinner portSpinner;

    private static String lastServer = "s3.amazonaws.com";
    private static String lastUsername = "";
    // Not static so that it is not saved (for security reasons)
    private String lastPassword = "";
    private static String lastInitialDir = "/";
    private static int lastPort = FileURL.getRegisteredHandler(FileProtocols.S3).getStandardPort();


    S3Panel(ServerConnectDialog dialog, final MainFrame mainFrame) {
        super(dialog, mainFrame);

        // Server field, initialized to last server entered (s3.amazonaws.com by default)
        serverField = new JTextField(lastServer);
        serverField.selectAll();
        addTextFieldListeners(serverField, true);
        addRow(Translator.get("server_connect_dialog.server"), serverField, 15);

        // Username field, initialized to last username
        usernameField = new JTextField(lastUsername);
        usernameField.selectAll();
        addTextFieldListeners(usernameField, false);
        // Not localized on purpose
        addRow("Access ID Key", usernameField, 5);

        // Password field, initialized to ""
        passwordField = new JPasswordField();
        addTextFieldListeners(passwordField, false);
        // Not localized on purpose
        addRow("Secret Access Key", passwordField, 15);

        // Initial directory field, initialized to "/"
        initialDirField = new JTextField(lastInitialDir);
        initialDirField.selectAll();
        addTextFieldListeners(initialDirField, true);
        addRow(Translator.get("server_connect_dialog.initial_dir"), initialDirField, 5);

        // Port field, initialized to last port
        portSpinner = createPortSpinner(lastPort);
        addRow(Translator.get("server_connect_dialog.port"), portSpinner, 15);
    }


    private void updateValues() {
        lastServer = serverField.getText();
        lastUsername = usernameField.getText();
        lastPassword = new String(passwordField.getPassword());
        lastInitialDir = initialDirField.getText();
        lastPort = (Integer) portSpinner.getValue();
    }


    ////////////////////////////////
    // ServerPanel implementation //
    ////////////////////////////////

    @Override
    FileURL getServerURL() throws MalformedURLException {
        updateValues();
        if(!lastInitialDir.startsWith("/"))
            lastInitialDir = "/"+lastInitialDir;

        FileURL url = FileURL.getFileURL(FileProtocols.S3+"://"+lastServer+lastInitialDir);

        // Set credentials
        url.setCredentials(new Credentials(lastUsername, lastPassword));

        // Set port
        url.setPort(lastPort);

        return url;
    }

    @Override
    boolean usesCredentials() {
        return true;
    }

    @Override
    public void dialogValidated() {
        // Commits the current spinner value in case it was being edited and 'enter' was pressed
        // (the spinner value would otherwise not be committed)
        try { portSpinner.commitEdit(); }
        catch(ParseException e) { }

        updateValues();
    }
}
