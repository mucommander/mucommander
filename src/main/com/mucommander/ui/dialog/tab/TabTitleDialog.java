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

package com.mucommander.ui.dialog.tab;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.action.impl.SetTabTitleAction;
import com.mucommander.ui.dialog.DialogToolkit;
import com.mucommander.ui.dialog.FocusDialog;
import com.mucommander.ui.layout.XBoxPanel;
import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.text.SizeConstrainedDocument;

/**
 * This dialog allow the user to enter a title for the currently selected tab.
 * Empty title means that the tab title will be based on the current location
 * presented in the tab.
 * 
 * @author Arik Hadas
 */
public class TabTitleDialog extends FocusDialog implements ActionListener {
	
	/** Ensure the dialog width is at least 300 */
	private static final Dimension MINIMUM_SIZE = new Dimension(250, 0);
	
	/** OK button. */
    private JButton okButton;
    
    /** Cancel button. */
    private JButton cancelButton;

    /** The text field in which the title is entered */
    private JTextField titleTextField;

    /** The FolderPanel to which this tab belongs */
    private FolderPanel folderPanel;

	public TabTitleDialog(MainFrame mainFrame, FolderPanel folderPanel) {
        super(mainFrame, ActionProperties.getActionLabel(SetTabTitleAction.Descriptor.ACTION_ID), folderPanel);

        this.folderPanel = folderPanel;

        initUI();
    }
  
    private void initUI() {
    	titleTextField = new JTextField();
        titleTextField.setDocument(new SizeConstrainedDocument(31));
        titleTextField.setText(folderPanel.getTabs().getCurrentTab().getTitle());
        titleTextField.selectAll();

        okButton = new JButton(Translator.get("ok"));
        cancelButton = new JButton(Translator.get("cancel"));

    	// Get content-pane and set its layout.
    	Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        // Add customization panel
        contentPane.add(createInnerPanel(), BorderLayout.CENTER);

        // Aligns the button panel to the right.
        contentPane.add(DialogToolkit.createOKCancelPanel(okButton, cancelButton, rootPane, this), BorderLayout.SOUTH);

        // Set preferred size
        setMinimumSize(MINIMUM_SIZE);
    }

    private JPanel createInnerPanel() {
    	XBoxPanel panel = new XBoxPanel();
    	panel.add(new JLabel(Translator.get("title") + ":"));
    	panel.addSpace(10);
    	panel.add(titleTextField);//, BorderLayout.CENTER);
    	panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    	return panel;
    }

    public void changeTabTitle() {
    	String title = titleTextField.getText().trim();
    	folderPanel.getTabs().setTitle(title.length() == 0 ? null : title);
    }
    
    // - Listener code ----------------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Reacts to buttons being pushed.
     */
    public void actionPerformed(ActionEvent e) {
        dispose();
        
        if (e.getSource() == okButton)
            changeTabTitle();
    }
}
