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

import com.mucommander.Debug;
import com.mucommander.auth.CredentialsManager;
import com.mucommander.auth.MappedCredentials;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.FocusDialog;
import com.mucommander.ui.helper.MnemonicHelper;
import com.mucommander.ui.layout.XAlignedComponentPanel;
import com.mucommander.ui.layout.XBoxPanel;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.list.DynamicList;
import com.mucommander.ui.list.SortableListPanel;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.util.AlteredVector;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * This dialog contains a list of all persistent credentials and allows the user to edit, remove, go to and reorder them.
 *
 * <p>If the contents of this list is modified, credentials will be saved to disk when this dialog is disposed.</p>
 *
 * @author Maxence Bernard
 */
public class EditCredentialsDialog extends FocusDialog implements ActionListener, ListSelectionListener {

    private MainFrame mainFrame;

    private JButton removeButton;
    private JButton goToButton;
    private JButton closeButton;

    private JTextField loginField;
    private JPasswordField passwordField;

    private AlteredVector credentials;
    private DynamicList credentialsList;

    private MappedCredentials lastSelectedItem;

    // Dialog's size has to be at least 400x300
    private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(440,330);

    // Dialog's size has to be at most 600x400
    private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(600,400);



    public EditCredentialsDialog(MainFrame mainFrame) {
        super(mainFrame, Translator.get(com.mucommander.ui.action.EditCredentialsAction.class.getName()+".label"), mainFrame);

        this.mainFrame = mainFrame;

        Container contentPane = getContentPane();

        // Retrieve persistent credentials list
        this.credentials = CredentialsManager.getPersistentCredentials();

        // Create the sortable credentials list panel
        SortableListPanel listPanel = new SortableListPanel(credentials);
        this.credentialsList = listPanel.getDynamicList();
        this.lastSelectedItem = (MappedCredentials) credentialsList.getSelectedValue();

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

            MappedCredentials mappedCredentials = (MappedCredentials) credentialsList.getSelectedValue();
            loginValue = mappedCredentials.getLogin();
            passwordValue = mappedCredentials.getPassword();
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
            credentials.setElementAt(new MappedCredentials(loginField.getText(), new String(passwordField.getPassword()), lastSelectedItem.getRealm(), true), itemIndex);
        }
        
        this.lastSelectedItem = (MappedCredentials) credentialsList.getSelectedValue();
    }


    /**
     * Overrides dispose() to write credentials if needed (if at least one item has been changed).
     */
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
            mainFrame.getActiveTable().getFolderPanel().tryChangeCurrentFolder(((MappedCredentials)credentialsList.getSelectedValue()).getRealm());
        }
    }


    ///////////////////////////////////
    // ListSelectionListener methods //
    ///////////////////////////////////

    public void valueChanged(ListSelectionEvent e) {
        if(Debug.ON) Debug.trace("called, e.getValueIsAdjusting="+e.getValueIsAdjusting()+" getSelectedIndex="+ credentialsList.getSelectedIndex());

        if(e.getValueIsAdjusting())
            return;

        // Commit current credentials modifications
        modifyCredentials();

        // Update components to reflect the new selection
        updateComponents();
    }

}
