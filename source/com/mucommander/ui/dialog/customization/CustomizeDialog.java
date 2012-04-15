/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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

package com.mucommander.ui.dialog.customization;

import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.FocusDialog;
import com.mucommander.ui.layout.XBoxPanel;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Dialog which let users customize UI element.
 * 
 * @author Arik Hadas.
 */
public abstract class CustomizeDialog extends FocusDialog implements ActionListener {
	
	public static final Dimension PREFERRED_SIZE = new Dimension(700, 500);
	
	/** Apply button. */
    private JButton     applyButton;
	/** OK button. */
    private JButton     okButton;
    /** Cancel button. */
    private JButton     cancelButton;

    // Abstract methods:
    protected abstract JPanel createCustomizationPanel();
    protected abstract void commit();
    protected abstract void componentChanged();
	
	public CustomizeDialog(Frame parent, String title) {
        super(parent, title, parent);
        initUI();
    }

    public CustomizeDialog(Dialog parent, String title) {
        super(parent, title, parent);
        initUI();
    }
  
    private void initUI() {
    	XBoxPanel buttonsPanel;
    	JPanel    tempPanel;
    	Container contentPane;
    	
    	// Get content-pane and set its layout.
        contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        
        // Add customization panel
        contentPane.add(createCustomizationPanel(), BorderLayout.CENTER);
    	
    	// Buttons panel.
        buttonsPanel = new XBoxPanel();
        buttonsPanel.add(applyButton = new JButton(Translator.get("apply")));
        buttonsPanel.addSpace(20);
        buttonsPanel.add(okButton     = new JButton(Translator.get("ok")));
        buttonsPanel.add(cancelButton = new JButton(Translator.get("cancel")));
        
        // Disable "commit buttons".
        applyButton.setEnabled(false);
        okButton.setEnabled(false);
        
        // Buttons listening.
        applyButton.addActionListener(this);
        okButton.addActionListener(this);
        cancelButton.addActionListener(this);
        
        // Aligns the button panel to the right.
        tempPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        tempPanel.add(buttonsPanel);
        contentPane.add(tempPanel, BorderLayout.SOUTH);
        
        // Selects OK when enter is pressed
        getRootPane().setDefaultButton(cancelButton);
        
        // Set preferred size
        setPreferredSize(PREFERRED_SIZE);
    }
    
    // - Listener code ----------------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Reacts to buttons being pushed.
     */
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        // Commit changes
        if (source == okButton || source == applyButton)
            commit();

        // Disable OK & Apply buttons
        if (source == applyButton)
        	setCommitButtonsEnabled(false);
        
        // Dispose dialog
        if (source == okButton || source == cancelButton)
            dispose();
    }
    
    protected void setCommitButtonsEnabled(boolean enabled) {
    	applyButton.setEnabled(enabled);
    	okButton.setEnabled(enabled);
    	
    	// if commit buttons are enabled then set the "okButton" as default button, 
    	// otherwise set the "cancelButton" as default button.
    	getRootPane().setDefaultButton(enabled ? okButton : cancelButton);
    }
}
