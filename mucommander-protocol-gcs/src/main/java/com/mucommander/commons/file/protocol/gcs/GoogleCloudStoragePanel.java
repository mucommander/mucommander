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

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.StorageOptions;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.commons.util.StringUtils;
import com.mucommander.protocol.ui.ServerPanel;
import com.mucommander.protocol.ui.ServerPanelListener;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;

/**
 * This ServerPanel helps initiate Google Drive connections.
 *
 * @author Arik Hadas TODO
 */
public class GoogleCloudStoragePanel extends ServerPanel {

    private static final int Y_SPACE_AFTER_TEXT_FIELD = 15;
    static final String GCS_SCHEMA = "gcs";
    static final String GCS_DEFAULT_PROJECT_ID = "gcs_default_project_id";
    static final String GCS_CREDENTIALS_JSON = "gcs_credentials_json";
    static final String GCS_DEFAULT_CREDENTIALS = "gcs_default_credentials";
    static final String GCS_BUCKET_LOCATION = "gcs_bucket_location";
    static final String GCS_DEFAULT_BUCKET_LOCATION = "gcs_default_bucket_location";
    static final String GCS_IMPERSONATED_PRINCIPAL = "gcs_impersonated_principal";
    static final String GCS_IMPERSONATION = "gcs_impersonation";

    private final TextField projectIdField;
    private final TextField credentialsJsonPathField;
    private final TextField locationField;
    private final TextField impersonatedPrincipalField;

    private final JCheckBox defaultProjectIdCheckBox;
    private final JCheckBox defaultCredentialsCheckBox;
    private final JCheckBox defaultLocationCheckBox;
    private final JCheckBox impersonationCheckBox;

    // Store to static, so it is saved for the next instance
    private static String lastProjectId = null;
    private static String lastCredentialsJsonPath = "";
    private static String lastLocation = "";
    private static String lastImpersonatedPrincipal = "";
    private static boolean lastDefaultProjectId = false;
    private static boolean lastDefaultCredentials = false;
    private static boolean lastDefaultLocation = false;
    private static boolean lastImpersonation = false;

    GoogleCloudStoragePanel(ServerPanelListener listener, JFrame mainFrame) {
        super(listener, mainFrame);

        // Prepare default values
        var gsutilsDefaults = true;
        try {
            // If there is no previous project id config, try defaults
            // FIXME problem with second initialization - gs utils
            if (StringUtils.isNullOrEmpty(lastProjectId)) {
                // Test we can use default credentials and project id
                GoogleCredentials.getApplicationDefault();

                // By default, use all defaults
                lastProjectId = StorageOptions.getDefaultProjectId(); // To show the default project id here
                lastDefaultProjectId = true;
                lastDefaultCredentials = true;
                lastDefaultLocation = true;
            }
        } catch (IOException ex) {
            // Defaults does not exist
            lastProjectId = "";
            gsutilsDefaults = false;
        }

        // TODO use translator for descriptions

        // Add all text fields
        projectIdField = addTextField("Project id", lastProjectId, !lastDefaultProjectId, true);
        credentialsJsonPathField = addFilePathChooser("Credentials json", lastCredentialsJsonPath, !lastDefaultCredentials);
        locationField = addTextField("Bucket location", lastLocation, !lastDefaultLocation, false);
        impersonatedPrincipalField = addTextField("Impersonated principal", lastImpersonatedPrincipal, lastImpersonation, false);

        // Add all check boxes
        // FIXME default is wrong
        defaultProjectIdCheckBox = addCheckBoxToTextField("Default project id", lastDefaultProjectId, gsutilsDefaults, projectIdField, StorageOptions.getDefaultProjectId());
        defaultCredentialsCheckBox = addCheckBoxToTextField("Default credentials", lastDefaultCredentials, gsutilsDefaults, credentialsJsonPathField, "");
        defaultLocationCheckBox = addCheckBoxToTextField("Default bucket location", lastDefaultLocation, gsutilsDefaults, locationField, "");
        impersonationCheckBox = addCheckBoxToTextField("Impersonation", lastImpersonation, true, impersonatedPrincipalField, "");

        if (!gsutilsDefaults) {
            // Missing GS utils warning
            JLabel warnLabel = new JLabel("To use defaults install gsutils!");
            warnLabel.setForeground(Color.red);
            addRow(warnLabel, 10);
        }
    }

    /**
     * todo
     *
     * @param label
     * @param initValue
     * @param enabled
     * @param updateUrl
     * @return
     */
    private TextField addTextField(String label, String initValue, boolean enabled, boolean updateUrl) {
        var textField = new JTextField(initValue);
        textField.setEnabled(enabled);
        textField.selectAll();
        if (updateUrl) {
            // Add listener for the file url if needed
            addTextFieldListeners(textField, true);
        }
        addRow(label, textField, Y_SPACE_AFTER_TEXT_FIELD);

        return new TextField(textField) {
            @Override
            public boolean switchEnabled() {
                textField.setEnabled(!textField.isEnabled());
                return textField.isEnabled();
            }
        };
    }

    /**
     * TODO
     *
     * @param label
     * @param initValue
     * @param enabled
     * @return
     */
    private TextField addFilePathChooser(String label, String initValue, boolean enabled) {
        var fileChooserPanel = new JPanel(new BorderLayout());

        // Prepare text field
        var textField = new JTextField(initValue);
        textField.setEnabled(enabled);
        textField.selectAll();
        fileChooserPanel.add(textField, BorderLayout.CENTER);

        // Prepare button
        var chooseFileButton = new JButton("...");
        chooseFileButton.setEnabled(enabled);
        // Mac OS X: small component size
        if (OsFamily.MAC_OS.isCurrent())
            chooseFileButton.putClientProperty("JComponent.sizeVariant", "small");

        // Prepare file chooser
        var fileChooser = new JFileChooser(System.getProperty("user.home"));
        chooseFileButton.addActionListener(event -> {
            int returnVal = fileChooser.showOpenDialog(mainFrame);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                textField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });
        fileChooserPanel.add(chooseFileButton, BorderLayout.EAST);

        addRow(label, fileChooserPanel, Y_SPACE_AFTER_TEXT_FIELD);

        return new TextField(textField) {
            @Override
            public boolean switchEnabled() {
                textField.setEnabled(!textField.isEnabled());
                chooseFileButton.setEnabled(textField.isEnabled());
                return textField.isEnabled();
            }
        };
    }

    /**
     * TODO
     *
     * @param label
     * @param initValue
     * @param enabled
     * @param textField
     * @param textFieldDefaultValue
     * @return
     */
    private JCheckBox addCheckBoxToTextField(
            String label, boolean initValue, boolean enabled, TextField textField, String textFieldDefaultValue) {
        var checkBox = new JCheckBox(label, initValue);
        checkBox.setEnabled(enabled);
        checkBox.addActionListener(event -> {
            var textFieldEnabled = textField.switchEnabled();
            if (!textFieldEnabled) {
                // Revert to the default filed value if disabled
                textField.setText(textFieldDefaultValue);
            }
        });
        addRow("", checkBox, 5);
        return checkBox;
    }

    private void updateValues() {
        lastProjectId = projectIdField.getText();
        lastCredentialsJsonPath = credentialsJsonPathField.getText();
        lastImpersonatedPrincipal = impersonatedPrincipalField.getText();
        lastLocation = locationField.getText();
        lastDefaultProjectId = defaultProjectIdCheckBox.isSelected();
        lastDefaultCredentials = defaultCredentialsCheckBox.isSelected();
        lastDefaultLocation = defaultLocationCheckBox.isSelected();
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
        url.setProperty(GCS_DEFAULT_BUCKET_LOCATION, Boolean.toString(lastDefaultLocation));
        url.setProperty(GCS_IMPERSONATION, Boolean.toString(lastImpersonation));
        return url;
    }

    @Override
    public boolean usesCredentials() {
        return false;
    }

    @Override
    public void dialogValidated() {
        updateValues();
    }

    private static abstract class TextField {
        private final JTextField textField;

        public TextField(JTextField textField) {
            this.textField = textField;
        }

        abstract boolean switchEnabled();

        void setText(String text) {
            textField.setText(text);
        }

        String getText() {
            return textField.getText();
        }
    }
}
