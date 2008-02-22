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


package com.mucommander.ui.dialog.file;

import com.mucommander.file.AbstractFile;
import com.mucommander.job.CopyJob;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.DialogToolkit;
import com.mucommander.ui.dialog.FocusDialog;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.main.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * Dialog invoked when the user wants to change a file name after a collision has been detected
 * while copying or moving files.
 *
 * @see CopyJob
 * @author Mariusz Jakubowski
 */
public class RenameDialog extends FocusDialog implements ActionListener {
	
    private JTextField edtNewName;

    private JButton okButton;

	private String newName;

    // Dialog size constraints
    private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(320,0);	
    // Dialog width should not exceed 360, height is not an issue (always the same)
    private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(400,10000);


    /**
     * Creates a new rename file dialog.
     *
     */
    public RenameDialog(MainFrame mainFrame, AbstractFile file) {
        super(mainFrame, Translator.get("rename"), mainFrame);

        Container contentPane = getContentPane();

        YBoxPanel mainPanel = new YBoxPanel();
        mainPanel.add(new JLabel(Translator.get("rename_dialog.new_name") + ":"));
        edtNewName = new JTextField();
        edtNewName.addActionListener(this);

        // Sets the initial selection.
        edtNewName.setText(file.getName());
        edtNewName.setSelectionStart(0);
        edtNewName.setSelectionEnd(edtNewName.getText().length());
        mainPanel.add(edtNewName);
   
        mainPanel.addSpace(10);
        contentPane.add(mainPanel, BorderLayout.NORTH);
        
        okButton = new JButton(Translator.get("rename"));
        JButton cancelButton = new JButton(Translator.get("cancel"));
        contentPane.add(DialogToolkit.createOKCancelPanel(okButton, cancelButton, getRootPane(), this), BorderLayout.SOUTH);

        // Path field will receive initial focus
        setInitialFocusComponent(edtNewName);

        setMinimumSize(MINIMUM_DIALOG_DIMENSION);
        setMaximumSize(MAXIMUM_DIALOG_DIMENSION);
        showDialog();
    }

    public String getNewName() {
    	return newName;
    }

    ///////////////////////////////////
    // ActionListener implementation //
    ///////////////////////////////////
	
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
		
        // OK Button
        if(source == okButton || source == edtNewName) {
        	newName = edtNewName.getText();
        } else {
            newName = null;
        }
        dispose();
    }
    
}
