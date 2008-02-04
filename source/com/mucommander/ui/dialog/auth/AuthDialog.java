/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
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


import com.mucommander.auth.Credentials;
import com.mucommander.auth.CredentialsManager;
import com.mucommander.auth.CredentialsMapping;
import com.mucommander.file.FileURL;
import com.mucommander.text.Translator;
import com.mucommander.ui.combobox.EditableComboBox;
import com.mucommander.ui.combobox.EditableComboBoxListener;
import com.mucommander.ui.combobox.SaneComboBox;
import com.mucommander.ui.dialog.DialogToolkit;
import com.mucommander.ui.dialog.FocusDialog;
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
	
    private JTextField loginField;
    private EditableComboBox loginComboBox;
    private JPasswordField passwordField;

    private JCheckBox saveCredentialsCheckBox;

    private CredentialsMapping enteredCredentialsMapping;

    private FileURL fileURL;

    private CredentialsMapping[] credentialsMappings;

    // Dialog size constraints
    private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(320,0);	
    private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(480,10000);


    public AuthDialog(MainFrame mainFrame, FileURL fileURL, String errorMessage) {
        super(mainFrame, Translator.get("auth_dialog.title"), mainFrame);
	
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        YBoxPanel yPanel = new YBoxPanel(5);

        if(errorMessage!=null) {
            yPanel.add(new JLabel(Translator.get("auth_dialog.error_was", errorMessage)));
            yPanel.addSpace(15);
        }

        yPanel.add(new JLabel(Translator.get("auth_dialog.desc")+" :"));
        yPanel.addSpace(10);
		
        this.fileURL = fileURL;

        XAlignedComponentPanel compPanel = new XAlignedComponentPanel(10);

        // Server URL for which the user has to authenticate
        JTextField serverField = new JTextField(fileURL.toString(false));
        serverField.setEditable(false);
        compPanel.addRow(Translator.get("auth_dialog.server")+":", serverField, 15);

        // Retrieve a list of credentials matching the URL
        this.credentialsMappings = CredentialsManager.getMatchingCredentials(fileURL);

        // Create login field / combo box: if no matching credentials were found, a text field is created
        // to let the user enter a login, otherwise an editable combo box is created to also allow him to select
        // an existing login/password pair (credentials)
        JComponent loginComp;
        int nbCredentials = credentialsMappings.length;
        if(nbCredentials>0) {
            // Editable combo box
            loginComboBox = new EditableComboBox();
            this.loginField = loginComboBox.getTextField();

            // Add credentials to the combo box's choices
            for(int i=0; i<nbCredentials; i++)
                loginComboBox.addItem(credentialsMappings[i].getCredentials().getLogin());

            loginComboBox.addEditableComboBoxListener(this);

            loginComp = loginComboBox;
        }
		else {
            // Simple text field
            this.loginField = new JTextField();
            loginComp = loginField;
        }

        compPanel.addRow(Translator.get("login")+":", loginComp, 10);

        // Create password field
        this.passwordField = new JPasswordField();
        passwordField.addActionListener(this);
        compPanel.addRow(Translator.get("password")+":", passwordField, 10);

        // If the the provided URL contains credentials, use them to intialize the login and password fields so
        // the user can easily correct a typo
        Credentials urlCredentials = fileURL.getCredentials();
        boolean saveCredentialsCheckBoxSelected = false;
        if(urlCredentials!=null) {
            loginField.setText(urlCredentials.getLogin());
            passwordField.setText(urlCredentials.getPassword());
        }
        // If not, initialize the login and password fields with the best credentials matching the url (if any)
        else if(nbCredentials>0) {
            CredentialsMapping bestCredentialsMapping = credentialsMappings[0];
            Credentials bestCredentials = bestCredentialsMapping.getCredentials();

            loginField.setText(bestCredentials.getLogin());
            passwordField.setText(bestCredentials.getPassword());

            saveCredentialsCheckBoxSelected = bestCredentialsMapping.isPersistent();
        }

        // Select any text in the text fields so that the login and password can be erased just by typing
        loginField.selectAll();
        passwordField.selectAll();

        yPanel.add(compPanel);

        // Save login/password checkbox : enabled when persistent credentials are selected
        this.saveCredentialsCheckBox = new JCheckBox(Translator.get("auth_dialog.store_credentials"), saveCredentialsCheckBoxSelected);

        yPanel.add(saveCredentialsCheckBox);

        yPanel.addSpace(5);
        contentPane.add(yPanel, BorderLayout.NORTH);
		
        // Add OK/Cancel buttons
        this.okButton = new JButton(Translator.get("ok"));
        this.cancelButton = new JButton(Translator.get("cancel"));
        contentPane.add(DialogToolkit.createOKCancelPanel(okButton, cancelButton, getRootPane(), this), BorderLayout.SOUTH);

        // Login field will receive initial focus
        setInitialFocusComponent(loginField);
		
        // Set minimum dimension
        setMinimumSize(MINIMUM_DIALOG_DIMENSION);

        // Set minimum dimension
        setMaximumSize(MAXIMUM_DIALOG_DIMENSION);
    }


    /**
     * Returns the credentials entered by the user, <code>null</code> if the dialog was cancelled.
     *
     * @return the credentials entered by the user, <code>null</code> if the dialog was cancelled
     */
    public CredentialsMapping getCredentialsMapping() {
        return enteredCredentialsMapping;
    }

    /**
     * Called when the dialog has been validated by the user, when the OK button has been pressed or when enter has
     * been pressed in a text field.
     */
    private void setCredentials() {
        Credentials enteredCredentials = new Credentials(loginField.getText(), new String(passwordField.getPassword()));
        this.enteredCredentialsMapping = new CredentialsMapping(enteredCredentials, fileURL, saveCredentialsCheckBox.isSelected());

        // Reuse any existing instance which may contain connection properties
        for(int i=0; i< credentialsMappings.length; i++) {
            if(credentialsMappings[i].getCredentials().equals(enteredCredentials, true)) {  // Comparison must be password-sensitive
                this.enteredCredentialsMapping = credentialsMappings[i];
                break;
            }
        }
    }


    ////////////////////////////
    // ActionListener methods //
    ////////////////////////////

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if(source==okButton || source==loginField || source==passwordField) {
            setCredentials();
            dispose();
        }
        else if(source==cancelButton) {
            dispose();
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
        setCredentials();
        dispose();
    }

    public void textFieldCancelled(EditableComboBox source) {
    }
}
