/**
 * This file is part of muCommander, http://www.mucommander.com
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mucommander.commons.file.protocol.onedrive;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.graph.models.User;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.util.ResourceLoader;
import com.mucommander.protocol.ui.ServerPanel;
import com.mucommander.protocol.ui.ServerPanelListener;
import com.mucommander.text.Translator;

/**
 * This ServerPanel helps initiate OneDrive connections.
 *
 * @author Arik Hadas
 */
public class OneDrivePanel extends ServerPanel implements ActionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(OneDrivePanel.class);
    private static final long serialVersionUID = -3850165192515539062L;

    // TODO: find a better way to load icons from plugins
    private static final String MICROSOFT_ACCOUNT_ICON_PATH = "/images/file/microsoft.png";

    private JTextField accountAlias;
    private JButton signingIn;
    private JLabel displayName;
    private JLabel emailAddress;
    private JLabel signingInInstructions;
    private LoginPhase loginPhase;
    private ImageIcon microsoftIcon;
    private JLabel accountLabel, accountAliasLabel;
    private OneDriveClient client;

    private String token;

    enum LoginPhase {
        SIGN_IN,
        CANCEL_SIGN_IN,
    }

    OneDrivePanel(ServerPanelListener listener, JFrame mainFrame) {
        super(listener, mainFrame);

        URL resourceURL = ResourceLoader.getResourceAsURL(MICROSOFT_ACCOUNT_ICON_PATH);
        microsoftIcon = new ImageIcon(resourceURL);
        signingIn = new JButton();
        signingIn.addActionListener(this);
        addRow(wrapWithJPanel(signingIn), 5);

        signingInInstructions = new JLabel();
        addRow(wrapWithJPanel(signingInInstructions), 15);

        emailAddress = new JLabel(" ");
        displayName = new JLabel(" ");
        accountLabel = new JLabel(Translator.get(("server_connect_dialog.account")));
        addRow(accountLabel, displayName, 5);
        addRow("", emailAddress, 15);

        accountAlias = new JTextField();
        addTextFieldListeners(accountAlias, true);
        accountAliasLabel = new JLabel(Translator.get("server_connect_dialog.account_alias"));
        addRow(accountAliasLabel, accountAlias, 5);

        // hide the following widgets until the user signs in
        setAccountFieldsVisible(false);

        setLoginPhase(LoginPhase.SIGN_IN, false);
    }

    private static JPanel wrapWithJPanel(JComponent component) {
        JPanel panel = new JPanel();
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(component);
        return panel;
    }

    ////////////////////////////////
    // ServerPanel implementation //
    ////////////////////////////////

    @Override
    public FileURL getServerURL() throws MalformedURLException {
        return FileURL.getFileURL(String.format("%s://%s", Activator.SCHEMA, accountAlias.getText()));
    }

    @Override
    public boolean usesCredentials() {
        return false;
    }

    @Override
    public void dialogValidated() {
        client.saveToken(accountAlias.getText(), token);
    }

    private User login() {
        try {
            client = new OneDriveClient(null);
            token = client.connect();
            return client.getClient().me().buildRequest().get();
        } catch (IOException e) {
            LOGGER.warn("failed to sign in to OneDrive account", e);
            return null;
        }
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        switch (loginPhase) {
        case SIGN_IN:
            setLoginPhase(LoginPhase.CANCEL_SIGN_IN, false);
            SwingUtilities.invokeLater(() -> {
                new Thread(() -> {
                    User user = login();
                    if (user != null) {
                        displayName.setText(user.displayName);
                        emailAddress.setText(user.userPrincipalName);
                        accountAlias.setText(user.displayName.replaceAll(" ", "-"));
                        setLoginPhase(LoginPhase.SIGN_IN, true);
                        accountAlias.requestFocus();
                        accountAlias.selectAll();
                    }
                }).start();
            });
            break;
        case CANCEL_SIGN_IN:
            setLoginPhase(LoginPhase.SIGN_IN, false);
        }
    }

    private void setAccountFieldsVisible(boolean enabled) {
        accountLabel.setVisible(enabled);
        accountAliasLabel.setVisible(enabled);
        emailAddress.setVisible(enabled);
        displayName.setVisible(enabled);
        accountAlias.setVisible(enabled);
    }

    private void setLoginPhase(LoginPhase loginPhase, boolean setAccountFieldsVisible) {
        switch (loginPhase) {
        case CANCEL_SIGN_IN:
            signingIn.setText(Translator.get("cancel"));
            signingIn.setIcon(null);
            signingInInstructions.setText(Translator.get("server_connect_dialog.wait_for_sign_in"));
            break;
        case SIGN_IN:
            if (setAccountFieldsVisible)
                setAccountFieldsVisible(true);
            signingIn.setText(Translator.get("server_connect_dialog.microsoft.sign_in"));
            signingIn.setIcon(microsoftIcon);
            signingInInstructions.setText("");
        }
        this.loginPhase = loginPhase;
    }
}
