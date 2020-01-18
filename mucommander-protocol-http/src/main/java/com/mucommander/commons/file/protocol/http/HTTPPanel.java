/*
 * This file is part of muCommander, http://www.mucommander.com
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


package com.mucommander.commons.file.protocol.http;

import java.net.MalformedURLException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.mucommander.commons.file.Credentials;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.protocol.FileProtocols;
import com.mucommander.protocol.ui.ServerPanel;
import com.mucommander.protocol.ui.ServerPanelListener;
import com.mucommander.text.Translator;


/**
 * This ServerPanel helps initiate HTTP connections. 
 *
 * @author Maxence Bernard
 */
public class HTTPPanel extends ServerPanel {

    private JTextField urlField;
    private JTextField usernameField;
    private JPasswordField passwordField;

    private static String lastURL = "http://";
    private static String lastUsername = "";
    // Not static so that it is not saved (for security reasons)
    private String lastPassword = "";

	
    HTTPPanel(ServerPanelListener listener, JFrame mainFrame) {
        super(listener, mainFrame);

        // Webserver (URL) field
        urlField = new JTextField(lastURL);
        urlField.selectAll();
        addTextFieldListeners(urlField, true);
        addRow(Translator.get("server_connect_dialog.http_url"), urlField, 20);

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
    }


    ////////////////////////////////
    // ServerPanel implementation //
    ////////////////////////////////
	
    @Override
    public FileURL getServerURL() throws MalformedURLException {
        updateValues();
        
        if(!(lastURL.toLowerCase().startsWith(FileProtocols.HTTP+"://") || lastURL.toLowerCase().startsWith(FileProtocols.HTTPS+"://")))
            lastURL = FileProtocols.HTTP+"://"+lastURL;

        FileURL fileURL = FileURL.getFileURL(lastURL);

        fileURL.setCredentials(new Credentials(lastUsername, lastPassword));

        return fileURL;
    }
	
    @Override
    public boolean usesCredentials() {
        return true;
    }

    @Override
    public void dialogValidated() {
        updateValues();
    }

}
