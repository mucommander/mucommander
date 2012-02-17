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


package com.mucommander.ui.dialog.auth;

import com.mucommander.auth.CredentialsManager;
import com.mucommander.auth.CredentialsMapping;
import com.mucommander.commons.file.Credentials;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.util.StringUtils;
import com.mucommander.text.Translator;
import com.mucommander.ui.combobox.EditableComboBox;
import com.mucommander.ui.combobox.EditableComboBoxListener;
import com.mucommander.ui.combobox.SaneComboBox;
import com.mucommander.ui.dialog.DialogToolkit;
import com.mucommander.ui.dialog.FocusDialog;
import com.mucommander.ui.helper.FocusRequester;
import com.mucommander.ui.layout.InformationPane;
import com.mucommander.ui.layout.XAlignedComponentPanel;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.main.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * This dialog is used to ask the user for credentials (login/password) to access a particular location and offer him
 * to store them to disk.
 *
 * <p>It uses CredentialsManager to retrieve and display a list of credentials matching the location so
 * they can quickly be recalled.
 *
 * @see CredentialsManager
 * @author Maxence Bernard
 */
public class AuthDialog extends FocusDialog implements ActionListener, EditableComboBoxListener {

    private JButton okButton;
    private JButton cancelButton;

    private JRadioButton guestRadioButton;
    private JRadioButton userRadioButton;

    private JTextField loginField;
    private EditableComboBox loginComboBox;

    private JPasswordField passwordField;

    private JCheckBox saveCredentialsCheckBox;

    private CredentialsMapping selectedCredentialsMapping;
    private boolean guestCredentialsSelected;

    private FileURL fileURL;

    private CredentialsMapping[] credentialsMappings;

    // Dialog size constraints
    private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(320,0);
    private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(480,10000);


    public AuthDialog(MainFrame mainFrame, FileURL fileURL, boolean authFailed, String errorMessage) {
        super(mainFrame, Translator.get("auth_dialog.title"), mainFrame);
	
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        YBoxPanel yPanel = new YBoxPanel();

        if(authFailed) {
            yPanel.add(new InformationPane(Translator.get("auth_dialog.authentication_failed"), errorMessage, errorMessage==null?Font.PLAIN:Font.BOLD, InformationPane.ERROR_ICON));
            yPanel.addSpace(5);
            yPanel.add(new JSeparator());
        }

        yPanel.addSpace(5);
        
        this.fileURL = fileURL;

        // Retrieve guest credentials (if any)
        Credentials guestCredentials = fileURL.getGuestCredentials();
        // Fetch credentials from the specified FileURL (if any) and use them only if they're different from the guest ones
        Credentials urlCredentials = fileURL.getCredentials();
        if(urlCredentials!=null && guestCredentials!=null && urlCredentials.equals(guestCredentials))
            urlCredentials = null;
        // Retrieve a list of credentials matching the URL from CredentialsManager
        credentialsMappings = CredentialsManager.getMatchingCredentials(fileURL);

        XAlignedComponentPanel compPanel = new XAlignedComponentPanel(10);

        // Connect as Guest/User radio buttons, displayed only if the URL has guest credentials
        if(guestCredentials!=null) {
            guestRadioButton = new JRadioButton(StringUtils.capitalize(guestCredentials.getLogin()));
            guestRadioButton.addActionListener(this);
            compPanel.addRow(Translator.get("auth_dialog.connect_as"), guestRadioButton, 0);

            userRadioButton = new JRadioButton(Translator.get("user"));
            userRadioButton.addActionListener(this);
            compPanel.addRow("", userRadioButton, 15);

            ButtonGroup buttonGroup = new ButtonGroup();
            buttonGroup.add(guestRadioButton);
            buttonGroup.add(userRadioButton);
        }
        // If not, display an introduction label ("please enter a login and password")
        else {
            yPanel.add(new JLabel(Translator.get("auth_dialog.desc")));
            yPanel.addSpace(15);
        }

        // Server URL for which the user has to authenticate
        compPanel.addRow(Translator.get("auth_dialog.server"), new JLabel(fileURL.toString(false)), 10);

        // Login field: create either a text field or an editable combo box, depending on whether
        // CredentialsManager returned matches (-> combo box) or not (-> text field).
        int nbCredentials = credentialsMappings.length;
        JComponent loginComponent;
        if(nbCredentials>0) {
            // Editable combo box
            loginComboBox = new EditableComboBox();
            this.loginField = loginComboBox.getTextField();

            // Add credentials to the combo box's choices
            for(int i=0; i<nbCredentials; i++)
                loginComboBox.addItem(credentialsMappings[i].getCredentials().getLogin());

            loginComboBox.addEditableComboBoxListener(this);

            loginComponent = loginComboBox;
        }
		else {
            // Simple text field
            loginField = new JTextField();
            loginComponent = loginField;
        }

        compPanel.addRow(Translator.get("login"), loginComponent, 5);

        // Create password field
        this.passwordField = new JPasswordField();
        passwordField.addActionListener(this);
        compPanel.addRow(Translator.get("password"), passwordField, 10);

        // Contains the credentials to set in the login and password text fields
        Credentials selectedCredentials = null;
        // Whether the 'save credentials' checkbox should be enabled
        boolean saveCredentialsCheckBoxSelected = false;

        // If the provided URL contains credentials, use them
        if(urlCredentials!=null) {
            selectedCredentials = urlCredentials;
        }
        // Else if CredentialsManager had matching credentials, use the best ones  
        else if(nbCredentials>0) {
            CredentialsMapping bestCredentialsMapping = credentialsMappings[0];

            selectedCredentials = bestCredentialsMapping.getCredentials();
            saveCredentialsCheckBoxSelected = bestCredentialsMapping.isPersistent();
        }

        yPanel.add(compPanel);

        this.saveCredentialsCheckBox = new JCheckBox(Translator.get("auth_dialog.store_credentials"), saveCredentialsCheckBoxSelected);
        yPanel.add(saveCredentialsCheckBox);

        yPanel.addSpace(5);
        contentPane.add(yPanel, BorderLayout.CENTER);

        // If we have some existing credentials for this location...
        if(selectedCredentials!=null) {
            // Prefill the login and password fields with the selected credentials
            loginField.setText(selectedCredentials.getLogin());
            passwordField.setText(selectedCredentials.getPassword());

            // Select the text fields' so their content can be erased just by typing the replacement string
            loginField.selectAll();
            passwordField.selectAll();

            // Select the 'Connect as User' radio button if there is one
            if(userRadioButton!=null)
                userRadioButton.setSelected(true);
        }
        else {
            // Prefill the login field with the current user's name (ticket #185)
            loginField.setText(System.getProperty("user.name"));

            // Select the 'Connect as Guest' radio button if there is one
            if(guestRadioButton!=null) {
                guestRadioButton.setSelected(true);

                loginField.setEnabled(false);
                passwordField.setEnabled(false);
                saveCredentialsCheckBox.setEnabled(false);
            }
        }

        // Add OK/Cancel buttons
        this.okButton = new JButton(Translator.get("ok"));
        this.cancelButton = new JButton(Translator.get("cancel"));
        contentPane.add(DialogToolkit.createOKCancelPanel(okButton, cancelButton, getRootPane(), this), BorderLayout.SOUTH);

        // Set the component that will receive the initial focus
        setInitialFocusComponent(guestRadioButton==null?loginField:guestRadioButton.isSelected()?guestRadioButton:loginField);

        // Set minimum dimension
        setMinimumSize(MINIMUM_DIALOG_DIMENSION);

        // Set minimum dimension
        setMaximumSize(MAXIMUM_DIALOG_DIMENSION);
    }


    /**
     * Returns the <Code>CredentialsMapping</code> corresponding to the credentials selected by the user, either
     * entered in the login and password fields, or the guest credentials.
     *
     * @return the credentials entered by the user, <code>null</code> if the dialog was cancelled
     */
    public CredentialsMapping getCredentialsMapping() {
        return selectedCredentialsMapping;
    }

    /**
     * Returns <code>true</code> if the user chose the guest credentials (radio button) in the dialog.
     * If <code>true</code>, {@link #getCredentialsMapping()} will return the guest credentials.
     *
     * @return <code>true</code> if the user chose the guest credentials (radio button) in the dialog
     */
    public boolean guestCredentialsSelected() {
        return guestCredentialsSelected;
    }

    /**
     * Called when the dialog has been validated by the user, when the OK button has been pressed or when enter has
     * been pressed in a text field.
     */
    private void setCredentialMapping() {
        if(guestRadioButton!=null && guestRadioButton.isSelected()) {
            guestCredentialsSelected = true;
            selectedCredentialsMapping = new CredentialsMapping(fileURL.getGuestCredentials(), fileURL, false);
        }
        else {
            Credentials enteredCredentials = new Credentials(loginField.getText(), new String(passwordField.getPassword()));
            guestCredentialsSelected = false;

            boolean isPersistent = saveCredentialsCheckBox.isSelected();
            selectedCredentialsMapping = new CredentialsMapping(enteredCredentials, fileURL, isPersistent);

            // Look for an existing matching CredentialsMapping instance to re-use the realm which may contain
            // connection properties.
            int nbCredentials = credentialsMappings.length;
            CredentialsMapping cm;
            for(int i=0; i<nbCredentials; i++) {
                cm = credentialsMappings[i];
                if(cm.getCredentials().equals(enteredCredentials, true)) {  // Comparison must be password-sensitive
                    // Create a new CredentialsMapping instance in case the 'isPersistent' flag has changed.
                    // (original credentials may have originally been added as 'volatile' and then made persistent by
                    // ticking the checkbox, or vice-versa)
                    selectedCredentialsMapping = new CredentialsMapping(cm.getCredentials(), cm.getRealm(), isPersistent);
                    break;
                }
            }
        }
    }


    ////////////////////////////
    // ActionListener methods //
    ////////////////////////////

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if(source==okButton || source==loginField || source==passwordField) {
            setCredentialMapping();
            dispose();
        }
        else if(source==cancelButton) {
            dispose();
        }
        else if(source==guestRadioButton) {
            loginField.setEnabled(false);
            passwordField.setEnabled(false);
            saveCredentialsCheckBox.setEnabled(false);
        }
        else if(source==userRadioButton) {
            loginField.setEnabled(true);
            passwordField.setEnabled(true);
            saveCredentialsCheckBox.setEnabled(true);

            loginField.selectAll();
            FocusRequester.requestFocus(loginField);
        }
    }


    /////////////////////////////////////////////
    // EditableComboBoxListener implementation //
    /////////////////////////////////////////////

    public void comboBoxSelectionChanged(SaneComboBox source) {
        CredentialsMapping selectedCredentialsMapping = credentialsMappings[loginComboBox.getSelectedIndex()];
        Credentials selectedCredentials = selectedCredentialsMapping.getCredentials();
        loginField.setText(selectedCredentials.getLogin());
        passwordField.setText(selectedCredentials.getPassword());

        // Enable/disable 'save credentials' checkbox depending on whether the selected credentials are persistent or not
        if(saveCredentialsCheckBox!=null)
            saveCredentialsCheckBox.setSelected(selectedCredentialsMapping.isPersistent());
    }

    public void textFieldValidated(EditableComboBox source) {
        setCredentialMapping();
        dispose();
    }

    public void textFieldCancelled(EditableComboBox source) {
    }
}
