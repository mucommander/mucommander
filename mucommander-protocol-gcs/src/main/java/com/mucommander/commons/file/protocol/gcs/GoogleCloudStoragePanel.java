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

import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.protocol.ui.ServerPanel;
import com.mucommander.protocol.ui.ServerPanelListener;

import javax.swing.*;
import java.awt.*;
import java.net.MalformedURLException;

/**
 * This ServerPanel helps initiate Google Drive connections.
 *
 * @author Arik Hadas TODO
 */
public class GoogleCloudStoragePanel extends ServerPanel {

    static final String GCS_SCHEMA = "gcs";
    static final String GCS_DEFAULT_PROJECT_ID = "gcs_default_project_id";
    static final String GCS_CREDENTIALS_JSON = "gcs_credentials_json";
    static final String GCS_DEFAULT_CREDENTIALS = "gcs_default_credentials";
    static final String GCS_BUCKET_LOCATION = "gcs_bucket_location";
    static final String GCS_IMPERSONATED_PRINCIPAL = "gcs_impersonated_principal";
    static final String GCS_IMPERSONATION = "gcs_impersonation";

    private final JTextField projectIdField;
    private final JTextField credentialsJsonPathField;
    private final JTextField locationField;
    private final JTextField impersonatedPrincipalField;

    private final JCheckBox defaultProjectIdCheckBox;
    private final JCheckBox defaultCredentialsCheckBox;
    private final JCheckBox impersonationCheckBox;

    private static String lastProjectId = "";
    private static String lastCredentialsJsonPath = "";
    private static String lastLocation = "";
    private static String lastImpersonatedPrincipal = "";

    private static boolean lastDefaultProjectId = false;
    private static boolean lastDefaultCredentials = false;
    private static boolean lastImpersonation = false;

    GoogleCloudStoragePanel(ServerPanelListener listener, JFrame mainFrame) {
        super(listener, mainFrame);
//        - gsUtilswarning;

        // TODO use translator for descriptions

        // Project id field
        projectIdField = new JTextField(lastProjectId);
        projectIdField.selectAll();
        addTextFieldListeners(projectIdField, true);
        addRow("Project id", projectIdField, 15);

        // Credentials Json path field
        var credentialsJsonChooser = new JPanel(new BorderLayout());

        credentialsJsonPathField = new JTextField(lastCredentialsJsonPath);
        credentialsJsonPathField.selectAll();
        addTextFieldListeners(credentialsJsonPathField, false);
        credentialsJsonChooser.add(credentialsJsonPathField, BorderLayout.CENTER);

        var chooseFileButton = new JButton("...");
        // Mac OS X: small component size
        if (OsFamily.MAC_OS.isCurrent())
            chooseFileButton.putClientProperty("JComponent.sizeVariant", "small");

        var fileChooser = new JFileChooser(System.getProperty("user.home"));
        chooseFileButton.addActionListener(event -> {
            int returnVal = fileChooser.showOpenDialog(mainFrame);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                credentialsJsonPathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });
        credentialsJsonChooser.add(chooseFileButton, BorderLayout.EAST);

        addRow("Credentials json", credentialsJsonChooser, 15);

        // Impersonation field
        impersonatedPrincipalField = new JTextField(lastImpersonatedPrincipal);
        impersonatedPrincipalField.selectAll();
        addTextFieldListeners(impersonatedPrincipalField, false);
        addRow("Impersonated principal", impersonatedPrincipalField, 15);

        // Location field
        locationField = new JTextField(lastLocation);
        locationField.selectAll();
        // FIXME is necessary?
        addTextFieldListeners(locationField, false);
        // FIXME description
        addRow("Location", locationField, 15);

        defaultProjectIdCheckBox = new JCheckBox("Default project id", lastDefaultProjectId);
        addRow("", defaultProjectIdCheckBox, 5);

        defaultCredentialsCheckBox = new JCheckBox("Default credentials", lastDefaultCredentials);
        addRow("", defaultCredentialsCheckBox, 5);

        impersonationCheckBox = new JCheckBox("Impersonation", lastImpersonation);
        addRow("", impersonationCheckBox, 5);
    }

    ////////////////////////////////
    // ServerPanel implementation //
    ////////////////////////////////

    private void updateValues() {
        lastProjectId = projectIdField.getText();
        lastCredentialsJsonPath = credentialsJsonPathField.getText();
        lastImpersonatedPrincipal = impersonatedPrincipalField.getText();
        lastLocation = locationField.getText();
        lastDefaultProjectId = defaultProjectIdCheckBox.isSelected();
        lastDefaultCredentials = defaultCredentialsCheckBox.isSelected();
        lastImpersonation = impersonationCheckBox.isSelected();
    }

    @Override
    public FileURL getServerURL() throws MalformedURLException {
        updateValues();

        var url = FileURL.getFileURL(String.format("%s://%s", GCS_SCHEMA, lastProjectId));

        url.setProperty(GCS_CREDENTIALS_JSON, lastCredentialsJsonPath);
        url.setProperty(GCS_BUCKET_LOCATION, lastLocation);
        url.setProperty(GCS_IMPERSONATED_PRINCIPAL, lastImpersonatedPrincipal);
        url.setProperty(GCS_DEFAULT_PROJECT_ID, Boolean.toString(lastDefaultProjectId));
        url.setProperty(GCS_DEFAULT_CREDENTIALS, Boolean.toString(lastDefaultCredentials));
        url.setProperty(GCS_IMPERSONATION, Boolean.toString(lastImpersonation));
        return url;
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
}
