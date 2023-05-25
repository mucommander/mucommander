/**
 * This file is part of muCommander, http://www.mucommander.com
 * <p>
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mucommander.commons.file.protocol.gcs;

import com.google.auth.Credentials;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.util.ResourceLoader;
import com.mucommander.protocol.ui.ServerPanel;
import com.mucommander.protocol.ui.ServerPanelListener;
import com.mucommander.text.Translator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This ServerPanel helps initiate Google Drive connections.
 *
 * @author Arik Hadas TODO
 */
public class GoogleCloudStoragePanel extends ServerPanel implements ActionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleCloudStoragePanel.class);
    private static final long serialVersionUID = 6345472628898453189L;
    private static final String GCS_SCHEMA = "gcs"; //FIXME
    // TODO: find a better way to load icons from plugins
    private static final String GOOGLE_ACCOUNT_ICON_PATH = "/images/file/google.png";

    private final JTextField projectIdField;

    private JTextField accountAlias;
    private JButton signingIn;
    private JLabel displayName;
    private JLabel emailAddress;
    private JLabel signingInInstructions;
    //    private LocalServerReceiver receiver;
    private LoginPhase loginPhase;
//    private GoogleCredentials credential;
    private ImageIcon googleIcon;
    private JLabel accountLabel, accountAliasLabel;

    enum LoginPhase {
        SIGN_IN, CANCEL_SIGN_IN,
    }

    GoogleCloudStoragePanel(ServerPanelListener listener, JFrame mainFrame) {
        super(listener, mainFrame);

        projectIdField = new JTextField("lastServer");
        projectIdField.selectAll();
        addTextFieldListeners(projectIdField, true);
        // FIXME description
        addRow(Translator.get("server_connect_dialog.server"), projectIdField, 15);

        URL resourceURL = ResourceLoader.getResourceAsURL(GOOGLE_ACCOUNT_ICON_PATH);
        googleIcon = new ImageIcon(resourceURL);
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
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(component);
        return panel;
    }

    ////////////////////////////////
    // ServerPanel implementation //
    ////////////////////////////////

    @Override
    public FileURL getServerURL() throws MalformedURLException {
        return FileURL.getFileURL(String.format("%s://%s", GCS_SCHEMA, projectIdField.getText()));
    }

    @Override
    public boolean usesCredentials() {
        return false;
    }

    @Override
    public void dialogValidated() {
//        String accountName = this.accountAlias.getText();
//        try {
//            String tokensDir = GoogleCloudStorageClient.getCredentialsFolder().getAbsolutePath();
//            DataStore<StoredCredential> dataStore = StoredCredential.getDefaultDataStore(new FileDataStoreFactory(new File(tokensDir)));
//            dataStore.set(accountName, new StoredCredential(credential));
        // TODO why?
//        } catch (IOException e) {
//            LOGGER.warn("failed to store credentials to Google account", e);
//        }
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        switch (loginPhase) {
            case SIGN_IN:
                setLoginPhase(LoginPhase.CANCEL_SIGN_IN, false);
                SwingUtilities.invokeLater(() -> {
                    new Thread(() -> {
//                        receiver = new LocalServerReceiver();
//                    About about;
//                        try {
//                            credential = GoogleCloudStorageClient.getCredentials();
//                            try (GoogleCloudStorageClient client = new GoogleCloudStorageClient()) {
//                                client.connect();
//                            about = client.getConnection().about().get().setFields("user").execute();
//                            }
//                        } catch (IOException e) {
//                            LOGGER.warn("failed to sign in to Google account", e);
//                            return;
//                        }
                        // FIXME
//                    Map<String, String> user = (Map<String, String>) about.get("user");
//                    displayName.setText(user.get("displayName"));
//                    String email = user.get("emailAddress");
                        displayName.setText("");
                        String email = "";

                        emailAddress.setText(email);
                        int indexOfAt = email.indexOf('@');
                        String alias = indexOfAt > 0 ? email.substring(0, indexOfAt) : email;
                        accountAlias.setText(alias);
                        setLoginPhase(LoginPhase.SIGN_IN, true);
                        accountAlias.requestFocus();
                        accountAlias.selectAll();
                    }).start();
                });
                break;
            case CANCEL_SIGN_IN:
                setLoginPhase(LoginPhase.SIGN_IN, false);
//                SwingUtilities.invokeLater(() -> {
//                    if (receiver != null) {
//                        try {
//                            receiver.stop();
//                        } catch (IOException e) {
//                            LOGGER.warn("failed to cancel signing in to Google account", e);
//                        }
//                    }
//                });
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
                signingInInstructions.setText(Translator.get("server_connect_dialog.google.wait_for_sign_in"));
                break;
            case SIGN_IN:
                if (setAccountFieldsVisible) setAccountFieldsVisible(true);
                signingIn.setText(Translator.get("server_connect_dialog.google.sign_in"));
                signingIn.setIcon(googleIcon);
                signingInInstructions.setText("");
        }
        this.loginPhase = loginPhase;
    }

    @Override
    public boolean privacyPolicyApplicable() {
        return true;
    }
}
