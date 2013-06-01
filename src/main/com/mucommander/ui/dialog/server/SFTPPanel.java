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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.text.ParseException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;

import com.mucommander.commons.file.Credentials;
import com.mucommander.commons.file.FileProtocols;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.impl.sftp.SFTPFile;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.text.Translator;
import com.mucommander.ui.main.MainFrame;


/**
 * This ServerPanel helps initiate SFTP connections.
 *
 * @author Maxence Bernard, Vassil Dichev
 */
public class SFTPPanel extends ServerPanel {

    private final static int STANDARD_PORT = FileURL.getRegisteredHandler(FileProtocols.SFTP).getStandardPort();

    private JTextField serverField;
    private JTextField privateKeyPathField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField initialDirField;
    private JSpinner portSpinner;

    private static String lastServer = "";
    private static String lastKeyPath = "";
    private static String lastUsername = "";
    // Not static so that it is not saved (for security reasons)
    private String lastPassword = "";
    private static String lastInitialDir = "/";
    private static int lastPort = STANDARD_PORT;


    SFTPPanel(ServerConnectDialog dialog, final MainFrame mainFrame) {
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
        addRow(Translator.get("password")+"/"+Translator.get("server_connect_dialog.passphrase"), passwordField, 15);

        // Key file field, initialized to last file
        JPanel privateKeyChooser = new JPanel(new BorderLayout());

        privateKeyPathField = new JTextField(lastKeyPath);
        privateKeyPathField.selectAll();
        addTextFieldListeners(privateKeyPathField, false);
        privateKeyChooser.add(privateKeyPathField, BorderLayout.CENTER);

        JButton chooseFileButton = new JButton("...");
        // Mac OS X: small component size
        if(OsFamily.MAC_OS_X.isCurrent())
            chooseFileButton.putClientProperty("JComponent.sizeVariant", "small");

        chooseFileButton.addActionListener(new ActionListener() {
                JFileChooser fc = new JFileChooser(System.getProperty("user.home") + System.getProperty("file.separator") + ".ssh");
                public void actionPerformed(ActionEvent e) {
                    int returnVal = fc.showOpenDialog(mainFrame);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        privateKeyPathField.setText(fc.getSelectedFile().getAbsolutePath());
                    }
                }
            }
        );
        privateKeyChooser.add(chooseFileButton, BorderLayout.EAST);

        addRow(Translator.get("server_connect_dialog.private_key"), privateKeyChooser, 15);

        // Initial directory field, initialized to "/"
        initialDirField = new JTextField(lastInitialDir);
        initialDirField.selectAll();
        addTextFieldListeners(initialDirField, true);
        addRow(Translator.get("server_connect_dialog.initial_dir"), initialDirField, 5);

        // Port field, initialized to last port (default is 22)
        portSpinner = createPortSpinner(lastPort);
        addRow(Translator.get("server_connect_dialog.port"), portSpinner, 15);
    }


    private void updateValues() {
        lastKeyPath = privateKeyPathField.getText();
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

        FileURL url = FileURL.getFileURL(FileProtocols.SFTP+"://"+lastServer+lastInitialDir);

        // Set credentials
        url.setCredentials(new Credentials(lastUsername, lastPassword));
        if(!"".equals(lastKeyPath.trim()))
            url.setProperty(SFTPFile.PRIVATE_KEY_PATH_PROPERTY_NAME, lastKeyPath);

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
