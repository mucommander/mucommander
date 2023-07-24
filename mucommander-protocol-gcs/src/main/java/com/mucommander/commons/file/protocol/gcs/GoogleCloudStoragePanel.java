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

import static com.mucommander.commons.file.protocol.gcs.Activator.GCS_SCHEMA;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.Optional;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.StorageOptions;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.commons.util.StringUtils;
import com.mucommander.protocol.ui.ServerPanel;
import com.mucommander.protocol.ui.ServerPanelListener;
import com.mucommander.text.Translator;

/**
 * This ServerPanel helps initiate Google Cloud Storage connections.
 *
 * @author miroslav.spak
 */
public class GoogleCloudStoragePanel extends ServerPanel {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleCloudStoragePanel.class);
    private static final int Y_SPACE_AFTER_TEXT_FIELD = 15;

    private final TextField projectIdField;
    private final TextField credentialsJsonPathField;
    private final TextField locationField;
    private final TextField impersonatedPrincipalField;

    private final JCheckBox defaultProjectIdCheckBox;
    private final JCheckBox defaultCredentialsCheckBox;
    private final JCheckBox defaultLocationCheckBox;
    private final JCheckBox impersonationCheckBox;

    // Store to static, so it is saved for the next instance
    private static final boolean gsUtilsDefaults = hasGsUtilsDefaults();
    private static String lastProjectId = gsUtilsDefaults ? StorageOptions.getDefaultProjectId() : "";
    private static String lastCredentialsJsonPath = "";
    private static String lastLocation = "";
    private static String lastImpersonatedPrincipal = "";
    private static boolean lastDefaultProjectId = gsUtilsDefaults;
    private static boolean lastDefaultCredentials = gsUtilsDefaults;
    private static boolean lastDefaultLocation = gsUtilsDefaults;
    private static boolean lastImpersonation = false;

    GoogleCloudStoragePanel(ServerPanelListener listener, JFrame mainFrame) {
        super(listener, mainFrame);

        var gsUtilsDefaults = hasGsUtilsDefaults();

        // Add all text fields
        projectIdField = addTextField(
                Translator.get("server_connect_dialog.gcs.project_id"),
                lastProjectId,
                !lastDefaultProjectId,
                true);
        credentialsJsonPathField = addFilePathChooser(
                Translator.get("server_connect_dialog.gcs.credentials_json"),
                lastCredentialsJsonPath,
                !lastDefaultCredentials);
        locationField = addTextField(
                Translator.get("server_connect_dialog.gcs.bucket_location"),
                lastLocation,
                !lastDefaultLocation,
                false);
        impersonatedPrincipalField = addTextField(
                Translator.get("server_connect_dialog.gcs.impersonated_principal"),
                lastImpersonatedPrincipal,
                lastImpersonation,
                false);

        // Add all check boxes
        defaultProjectIdCheckBox = addCheckBoxToTextField(
                Translator.get("server_connect_dialog.gcs.default.project_id"),
                lastDefaultProjectId,
                gsUtilsDefaults,
                projectIdField,
                StorageOptions.getDefaultProjectId());
        defaultCredentialsCheckBox = addCheckBoxToTextField(
                Translator.get("server_connect_dialog.gcs.default.credentials"),
                lastDefaultCredentials,
                gsUtilsDefaults,
                credentialsJsonPathField,
                "");
        defaultLocationCheckBox = addCheckBoxToTextField(
                Translator.get("server_connect_dialog.gcs.default.bucket_location"),
                lastDefaultLocation,
                gsUtilsDefaults,
                locationField,
                "");
        impersonationCheckBox = addCheckBoxToTextField(
                Translator.get("server_connect_dialog.gcs.impersonation"),
                lastImpersonation,
                true,
                impersonatedPrincipalField,
                "");

        addWarnLabel(
                Translator.get("server_connect_dialog.gcs.missing_default_project_id"),
                !lastDefaultProjectId && lastProjectId.isBlank(),
                defaultProjectIdCheckBox,
                projectIdField);
        addWarnLabel(
                Translator.get("server_connect_dialog.gcs.missing_default_credentials"),
                !lastDefaultCredentials && lastCredentialsJsonPath.isBlank(),
                defaultCredentialsCheckBox,
                credentialsJsonPathField);
        addWarnLabel(
                Translator.get("server_connect_dialog.gcs.missing_defaults"),
                !gsUtilsDefaults,
                null,
                null);
    }

    /**
     * Adds simple standard TextField to the Server connection panel.
     *
     * @param label
     *            the label od the new textField
     * @param initValue
     *            initial value of this textField
     * @param enabled
     *            if the textField should be enabled or not
     * @param updateUrl
     *            if the connection panel url should be updated for this textField
     * @return a new simple textField already added to the Server connection panel
     */
    private TextField addTextField(String label, String initValue, boolean enabled, boolean updateUrl) {
        var jTextField = new JTextField(initValue);
        jTextField.setEnabled(enabled);
        jTextField.selectAll();
        if (updateUrl) {
            // Add listener for the file url if needed
            addTextFieldListeners(jTextField, true);
        }
        addRow(label, jTextField, Y_SPACE_AFTER_TEXT_FIELD);

        return new TextField(jTextField);
    }

    /**
     * Adds TextField with the file chooser to the Server connection panel.
     *
     * @param label
     *            the label of the new textField
     * @param initValue
     *            initial value of this textField
     * @param enabled
     *            if the textField and the file chooser button should be enabled or not
     * @return a composite textField with path chooser added to the Server connection panel
     */
    private TextField addFilePathChooser(String label, String initValue, boolean enabled) {
        var fileChooserPanel = new JPanel(new BorderLayout());

        // Prepare text field
        var jTextField = new JTextField(initValue);
        jTextField.setEnabled(enabled);
        jTextField.selectAll();
        fileChooserPanel.add(jTextField, BorderLayout.CENTER);

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
                jTextField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });
        fileChooserPanel.add(chooseFileButton, BorderLayout.EAST);

        addRow(label, fileChooserPanel, Y_SPACE_AFTER_TEXT_FIELD);

        return new TextField(jTextField) {
            @Override
            public boolean switchEnabled() {
                var enabled = super.switchEnabled();
                // Update also the button state to match the text field
                chooseFileButton.setEnabled(enabled);
                return enabled;
            }
        };
    }

    /**
     * Adds checkBox to the Server connection panel with the ability to switch text field "enabled" state and reset it
     * to the default value.
     *
     * @param label
     *            the label of the new checkBox
     * @param initValue
     *            initial state of this checkBox
     * @param enabled
     *            if the checkBox should be enabled or not
     * @param textField
     *            controlled textField using this checkBox
     * @param textFieldDefaultValue
     *            the default value of the textField when it is disabled
     * @return a new checkBox (controlling textField) already added to the Server connection panel
     */
    private JCheckBox addCheckBoxToTextField(
            String label,
            boolean initValue,
            boolean enabled,
            TextField textField,
            String textFieldDefaultValue) {
        var checkBox = new JCheckBox(label, initValue);
        checkBox.setEnabled(enabled);
        checkBox.addActionListener(event -> {
            var textFieldEnabled = textField.switchEnabled();
            if (!textFieldEnabled) {
                // Revert the field to the default value if disabled
                textField.setText(textFieldDefaultValue);
            }
        });
        addRow("", checkBox, 5);
        return checkBox;
    }

    /**
     * Adds warning label to the Server connection panel. The label can be associated with the defaults checkbox and
     * textField to be shown only when there is no valid configuration for the two. Associated checkBox and textField
     * can be both null or not.
     *
     * @param label
     *            the text of this warning label
     * @param visible
     *            initial visibility of the warning
     * @param associatedCheckBox
     *            default value checkBox that when selected hides this warning label
     * @param associatedTextField
     *            textField that shows label when empty or hides it otherwise
     */
    private void addWarnLabel(
            String label,
            boolean visible,
            JCheckBox associatedCheckBox,
            TextField associatedTextField) {
        JLabel warnLabel = new JLabel(label);
        warnLabel.setVisible(visible);
        warnLabel.setForeground(Color.red);
        addRow(warnLabel, 10);

        if (associatedTextField != null && associatedCheckBox != null) {
            // Add action listeners - show warning on empty associated text field without defaults on
            SimpleDocumentActionListener action = () -> warnLabel
                    .setVisible(!associatedCheckBox.isSelected() && associatedTextField.getText().isBlank());
            associatedTextField.addActionListener(action);
            associatedCheckBox.addActionListener(action);
        }
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

        if (StringUtils.isNullOrEmpty(lastProjectId)) {
            throw new MalformedURLException("Project id cannot be empty");
        }

        return FileURL.getFileURL(String.format("%s://%s", GCS_SCHEMA, lastProjectId));
    }

    @Override
    public boolean usesCredentials() {
        return false;
    }

    @Override
    public void dialogValidated() {
        updateValues();

        // We will store credentials only if there is a project id, without it, it will fail anyway
        if (!StringUtils.isNullOrEmpty(lastProjectId)) {

            // Prepare connection properties
            var connectionProperties = new GoogleCloudStorageConnectionProperties(
                    lastProjectId,
                    lastCredentialsJsonPath,
                    lastImpersonatedPrincipal,
                    lastLocation,
                    lastDefaultProjectId,
                    lastDefaultCredentials,
                    lastImpersonation,
                    lastDefaultLocation);

            try {
                // Find path for this properties
                var outputPath = GoogleCloudStorageConnectionHandler.getCredentialFileUrl(lastProjectId);
                // There are no secrets, just write it as plain json
                Files.writeString(outputPath,
                        connectionProperties.toJson(),
                        StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.CREATE);
            } catch (Exception e) {
                LOGGER.error("Failed to persist credentials for google cloud storage project " + lastProjectId, e);
            }
        }
    }

    /**
     * Checks if the google-cloud library can find default credentials and default project id. Typically signifying that
     * the "gsUtils" are installed. It doesn't matter if the defaults were provided in a different way, we are using
     * only those two.
     */
    private static boolean hasGsUtilsDefaults() {
        try {
            // Test we can use default credentials and project id
            GoogleCredentials.getApplicationDefault();
            Objects.requireNonNull(StorageOptions.getDefaultProjectId());
            return true;
        } catch (Exception ex) {
            // Defaults does not exist
            return false;
        }
    }

    /**
     * Wrapper class for any composite UI elements containing text field.
     */
    private static class TextField {
        private final JTextField textField;

        public TextField(JTextField textField) {
            this.textField = textField;
        }

        /**
         * Adds event action to apply on any text field change.
         *
         * @param documentListener
         *            listener with action to be executed
         */
        public void addActionListener(DocumentListener documentListener) {
            textField.getDocument().addDocumentListener(documentListener);
        }

        /**
         * Switch enabled state of the composite text field.
         *
         * @return the final isEnabled state of the field
         */
        boolean switchEnabled() {
            textField.setEnabled(!textField.isEnabled());
            return textField.isEnabled();
        }

        void setText(String text) {
            textField.setText(text);
        }

        String getText() {
            return Optional.ofNullable(textField).map(JTextComponent::getText).orElse("");
        }
    }

    /**
     * Simple no-args action listener interface usable for the {@link DocumentListener} and {@link ActionListener}
     */
    private interface SimpleDocumentActionListener extends DocumentListener, ActionListener {

        void executeAction();

        default void actionPerformed(ActionEvent e) {
            executeAction();
        }

        default void insertUpdate(DocumentEvent e) {
            executeAction();
        }

        default void removeUpdate(DocumentEvent e) {
            executeAction();
        }

        default void changedUpdate(DocumentEvent e) {
            executeAction();
        }
    }
}
