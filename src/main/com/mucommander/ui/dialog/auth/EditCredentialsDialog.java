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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.auth.CredentialsManager;
import com.mucommander.auth.CredentialsMapping;
import com.mucommander.commons.collections.AlteredVector;
import com.mucommander.commons.file.Credentials;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.action.impl.EditCredentialsAction;
import com.mucommander.ui.dialog.FocusDialog;
import com.mucommander.ui.helper.MnemonicHelper;
import com.mucommander.ui.layout.XAlignedComponentPanel;
import com.mucommander.ui.layout.XBoxPanel;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.list.DynamicList;
import com.mucommander.ui.list.SortableListPanel;
import com.mucommander.ui.main.MainFrame;


/**
 * This dialog contains a list of all persistent credentials and allows the user to edit, remove, go to and reorder them.
 *
 * <p>If the contents of this list is modified, credentials will be saved to disk when this dialog is disposed.</p>
 *
 * @author Maxence Bernard
 */
public class EditCredentialsDialog extends FocusDialog implements ActionListener, ListSelectionListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(EditCredentialsDialog.class);
	
    private MainFrame mainFrame;

    private JButton removeButton;
    private JButton goToButton;
    private JButton closeButton;

    private JTextField loginField;
    private JPasswordField passwordField;

    private AlteredVector<CredentialsMapping> credentials;
    private DynamicList<CredentialsMapping> credentialsList;

    private CredentialsMapping lastSelectedItem;

    // Dialog's size has to be at least 400x300
    private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(440,330);

    // Dialog's size has to be at most 600x400
    private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(600,400);



    public EditCredentialsDialog(MainFrame mainFrame) {
        super(mainFrame, ActionProperties.getActionLabel(EditCredentialsAction.Descriptor.ACTION_ID), mainFrame);

        this.mainFrame = mainFrame;

        Container contentPane = getContentPane();

        // Retrieve persistent credentials list
        this.credentials = CredentialsManager.getPersistentCredentialMappings();

        // Create the sortable credentials list panel
        SortableListPanel<CredentialsMapping> listPanel = new SortableListPanel<CredentialsMapping>(credentials);
        this.credentialsList = listPanel.getDynamicList();
        this.lastSelectedItem = (CredentialsMapping) credentialsList.getSelectedValue();

        contentPane.add(listPanel, BorderLayout.CENTER);

        // Text fields panel
        XAlignedComponentPanel compPanel = new XAlignedComponentPanel();

        // Add login field
        this.loginField = new JTextField();
        compPanel.addRow(Translator.get("login")+":", loginField, 5);

        // Add password field
        this.passwordField = new JPasswordField();
        compPanel.addRow(Translator.get("password")+":", passwordField, 10);

        YBoxPanel yPanel = new YBoxPanel(10);
        yPanel.add(compPanel);

        XBoxPanel buttonsPanel = new XBoxPanel();
        MnemonicHelper mnemonicHelper = new MnemonicHelper();

        // Remove button
        removeButton = new JButton(credentialsList.getRemoveAction());
        removeButton.setMnemonic(mnemonicHelper.getMnemonic(removeButton));

        buttonsPanel.add(removeButton);

        // Go to button
        goToButton = new JButton(Translator.get("go_to"));
        goToButton.setMnemonic(mnemonicHelper.getMnemonic(goToButton));
        goToButton.addActionListener(this);

        buttonsPanel.add(goToButton);

        // Button that closes the window
        closeButton = new JButton(Translator.get("close"));
        closeButton.setMnemonic(mnemonicHelper.getMnemonic(closeButton));
        closeButton.addActionListener(this);

        buttonsPanel.add(Box.createHorizontalGlue());
        buttonsPanel.add(closeButton);

        yPanel.add(buttonsPanel);

        contentPane.add(yPanel, BorderLayout.SOUTH);

        // Set initial text components and buttons' enabled state
        updateComponents();

        // Listen to selection changes to reflect the change
        credentialsList.addListSelectionListener(this);

        // table will receive initial focus
        setInitialFocusComponent(credentialsList);
		
        // Selects 'Done' button when enter is pressed
        getRootPane().setDefaultButton(closeButton);

        // Packs dialog
        setMinimumSize(MINIMUM_DIALOG_DIMENSION);
        setMaximumSize(MAXIMUM_DIALOG_DIMENSION);

        // Call dispose() on close and write credentials file
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        showDialog();
    }


    /**
     * Updates text fields and buttons' enabled state based on the current selection. Should be called
     * whenever the list selection has changed.
     */
    private void updateComponents() {
        String loginValue = null;
        String passwordValue = null;

        boolean componentsEnabled = false;

        if(!credentialsList.isSelectionEmpty() && credentials.size()>0) {
            componentsEnabled = true;

            CredentialsMapping credentialsMapping = (CredentialsMapping) credentialsList.getSelectedValue();
            Credentials credentials = credentialsMapping.getCredentials();
            loginValue = credentials.getLogin();
            passwordValue = credentials.getPassword();
        }

        loginField.setText(loginValue);
        loginField.setEnabled(componentsEnabled);

        passwordField.setText(passwordValue);
        passwordField.setEnabled(componentsEnabled);

        removeButton.setEnabled(componentsEnabled);
    }


    /**
     * Updates the value of the item that was being editing. Should be called whenever the list selection has changed.
     */
    private void modifyCredentials() {
        // Make sure that the item still exists (could have been removed) before trying to modify its value
        int itemIndex = credentials.indexOf(lastSelectedItem);
        if(lastSelectedItem!=null && itemIndex!=-1) {
            credentials.setElementAt(new CredentialsMapping(new Credentials(loginField.getText(), new String(passwordField.getPassword())), lastSelectedItem.getRealm(), true), itemIndex);
        }
        
        this.lastSelectedItem = (CredentialsMapping) credentialsList.getSelectedValue();
    }


    /**
     * Overrides dispose() to write credentials if needed (if at least one item has been changed).
     */
    @Override
    public void dispose() {
        super.dispose();

        // Write credentials file to disk, only if changes were made
        try {CredentialsManager.writeCredentials(false);}
        // We should probably pop an error dialog here...
        catch(Exception e) {}
    }


    ///////////////////////////
    // ActionListener method //
    ///////////////////////////

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        // Dispose the dialog (credentials save is performed in dispose())
        if (source== closeButton)  {
            // Commit current credentials modifications.
            // Note: if the dialog is cancelled, current modifications will be cancelled (i.e. not committed) 
            modifyCredentials();
            
            dispose();
        }
        else if(source==goToButton) {
            // Dispose dialog first
            dispose();
            // Go to credentials' realm location
            mainFrame.getActivePanel().tryChangeCurrentFolder(((CredentialsMapping)credentialsList.getSelectedValue()).getRealm());
        }
    }


    ///////////////////////////////////
    // ListSelectionListener methods //
    ///////////////////////////////////

    public void valueChanged(ListSelectionEvent e) {
        LOGGER.trace("called, e.getValueIsAdjusting="+e.getValueIsAdjusting()+" getSelectedIndex="+ credentialsList.getSelectedIndex());

        if(e.getValueIsAdjusting())
            return;

        // Commit current credentials modifications
        modifyCredentials();

        // Update components to reflect the new selection
        updateComponents();
    }

}
