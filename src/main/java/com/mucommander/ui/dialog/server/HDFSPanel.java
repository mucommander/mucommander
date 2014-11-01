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
import com.mucommander.commons.file.impl.hadoop.HDFSFile;
import com.mucommander.text.Translator;
import com.mucommander.ui.main.MainFrame;

import javax.swing.*;
import java.net.MalformedURLException;
import java.text.ParseException;


/**
 * This ServerPanel helps initiate HDFS connections.
 *
 * @author Maxence Bernard
 */
public class HDFSPanel extends ServerPanel {

    private JTextField serverField;
    private JTextField usernameField;
//    private JTextField groupField;
    private JTextField initialDirField;
    private JSpinner portSpinner;

    private static String lastServer = "";
    private static String lastUsername = HDFSFile.getDefaultUsername();
//    private static String lastGroup = HDFSFile.getDefaultGroup();
    private static String lastInitialDir = "/";
    private static int lastPort = FileURL.getRegisteredHandler(FileProtocols.HDFS).getStandardPort();


    HDFSPanel(ServerConnectDialog dialog, final MainFrame mainFrame) {
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
        addRow(Translator.get("server_connect_dialog.username"), usernameField, 15);

//        // Password field, initialized to ""
//        groupField = new JTextField(lastGroup);
//        groupField.selectAll();
//        addTextFieldListeners(groupField, false);
//        addRow(Translator.get("server_connect_dialog.group"), groupField, 15);

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
//        lastGroup = groupField.getText();
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

        FileURL url = FileURL.getFileURL(FileProtocols.HDFS+"://"+lastServer+lastInitialDir);

        // Set user and group
        url.setCredentials(new Credentials(lastUsername, ""));
//        url.setProperty(HDFSFile.GROUP_PROPERTY_NAME, lastGroup);

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
