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
import com.mucommander.commons.file.impl.ftp.FTPFile;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.DialogOwner;
import com.mucommander.ui.encoding.EncodingListener;
import com.mucommander.ui.encoding.EncodingSelectBox;
import com.mucommander.ui.main.MainFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.text.ParseException;


/**
 * This ServerPanel helps initiate FTP connections.
 *
 * @author Maxence Bernard
 */
public class FTPPanel extends ServerPanel implements ActionListener, EncodingListener {

    private final static int STANDARD_PORT = FileURL.getRegisteredHandler(FileProtocols.FTP).getStandardPort();
    private static Credentials ANONYMOUS_CREDENTIALS = FileURL.getRegisteredHandler(FileProtocols.FTP).getGuestCredentials();

    private JTextField serverField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField initialDirField;
    private JSpinner portSpinner;
    private JSpinner nbRetriesSpinner;
    private JSpinner retryDelaySpinner;
    private EncodingSelectBox encodingSelectBox;
    private JCheckBox passiveCheckBox;
    private JCheckBox anonymousCheckBox;
	
    private static String lastServer = "";
    private static String lastUsername = "";
    private static String lastInitialDir = "/";
    private static int lastPort = STANDARD_PORT;
    private static String lastEncoding = FTPFile.DEFAULT_ENCODING;
    // Not static so that it is not remembered (for security reasons)
    private String lastPassword = "";

    /** Passive mode is enabled by default because of firewall restrictions */
    private static boolean passiveMode = true;
    private static boolean anonymousUser;


    FTPPanel(final ServerConnectDialog dialog, MainFrame mainFrame) {
        super(dialog, mainFrame);

        // Server field, initialized to last server entered
        serverField = new JTextField(lastServer);
        serverField.selectAll();
        addTextFieldListeners(serverField, true);
        addRow(Translator.get("server_connect_dialog.server"), serverField, 15);

        // Username field, initialized to last username entered or 'anonymous' if anonymous user was previously selected
        usernameField = new JTextField(anonymousUser?ANONYMOUS_CREDENTIALS.getLogin():lastUsername);
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
        portSpinner = createPortSpinner(lastPort);
        addRow(Translator.get("server_connect_dialog.port"), portSpinner, 15);

        // Encoding combo box
        encodingSelectBox = new EncodingSelectBox(new DialogOwner(mainFrame), lastEncoding);
        encodingSelectBox.addEncodingListener(this);
        addRow(Translator.get("encoding"), encodingSelectBox, 15);

        // Connection retries when server busy
        nbRetriesSpinner = createIntSpinner(FTPFile.DEFAULT_NB_CONNECTION_RETRIES, 0, Integer.MAX_VALUE, 1);
        addRow(Translator.get("ftp_connect.nb_connection_retries"), nbRetriesSpinner, 5);

        // Delay between two retries
        retryDelaySpinner = createIntSpinner(FTPFile.DEFAULT_CONNECTION_RETRY_DELAY, 0, Integer.MAX_VALUE, 1);
        addRow(Translator.get("ftp_connect.retry_delay"), retryDelaySpinner, 15);

        // Anonymous user checkbox
        anonymousCheckBox = new JCheckBox(Translator.get("ftp_connect.anonymous_user"), anonymousUser);
        anonymousCheckBox.addActionListener(this);
        addRow("", anonymousCheckBox, 5);

        // Passive mode checkbox
        passiveCheckBox = new JCheckBox(Translator.get("ftp_connect.passive_mode"), passiveMode);
        passiveCheckBox.addActionListener(this);
        addRow("", passiveCheckBox, 0);
    }

	
    private void updateValues() {
        lastServer = serverField.getText();
        if(!anonymousUser) {
            lastUsername = usernameField.getText();
            lastPassword = new String(passwordField.getPassword());
        }

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
			
        FileURL url = FileURL.getFileURL(FileProtocols.FTP+"://"+lastServer+lastInitialDir);

        if(anonymousUser)
            url.setCredentials(new Credentials(ANONYMOUS_CREDENTIALS.getLogin(), new String(passwordField.getPassword())));
        else
            url.setCredentials(new Credentials(lastUsername, lastPassword));

        // Set port
        url.setPort(lastPort);

        // Set passiveMode property to true (default) or false
        url.setProperty(FTPFile.PASSIVE_MODE_PROPERTY_NAME, ""+passiveMode);

        // Set FTP encoding property
        url.setProperty(FTPFile.ENCODING_PROPERTY_NAME, encodingSelectBox.getSelectedEncoding());

        // Set connection retry properties
        url.setProperty(FTPFile.NB_CONNECTION_RETRIES_PROPERTY_NAME, ""+nbRetriesSpinner.getValue());
        url.setProperty(FTPFile.CONNECTION_RETRY_DELAY_PROPERTY_NAME, ""+retryDelaySpinner.getValue());

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
            anonymousUser = anonymousCheckBox.isSelected();
            if(anonymousUser) {
                usernameField.setEnabled(false);
                usernameField.setText(ANONYMOUS_CREDENTIALS.getLogin());
                passwordField.setEnabled(false);
                passwordField.setText(ANONYMOUS_CREDENTIALS.getPassword());
            }
            else {
                usernameField.setEnabled(true);
                usernameField.setText(lastUsername);
                passwordField.setEnabled(true);
                passwordField.setText(lastPassword);
            }
        }
    }


    /////////////////////////////////////
    // EncodingListener implementation //
    /////////////////////////////////////

    public void encodingChanged(Object source, String oldEncoding, String newEncoding) {
        lastEncoding = newEncoding;
    }
}
