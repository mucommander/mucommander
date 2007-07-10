/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
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
import com.mucommander.file.util.FileSet;
import com.mucommander.file.util.FileToolkit;
import com.mucommander.job.MkdirJob;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.action.MkdirAction;
import com.mucommander.ui.action.MkfileAction;
import com.mucommander.ui.dialog.DialogToolkit;
import com.mucommander.ui.dialog.FocusDialog;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.main.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * Dialog invoked when the user wants to create a new folder or an empty file in the current folder.
 *
 * @see MkdirAction
 * @see MkfileAction
 * @author Maxence Bernard
 */
public class MkdirDialog extends FocusDialog implements ActionListener {

    private MainFrame mainFrame;
	
    private JTextField mkdirPathField;
	
    private JButton okButton;

    private boolean mkfileMode;

    // Dialog size constraints
    private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(320,0);	
    // Dialog width should not exceed 360, height is not an issue (always the same)
    private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(400,10000);


    /**
     * Creates a new Mkdir/Mkfile dialog.
     *
     * @param mkfileMode if true, the dialog will operate in 'mkfile' mode, if false in 'mkdir' mode
     */
    public MkdirDialog(MainFrame mainFrame, boolean mkfileMode) {
        super(mainFrame, ActionManager.getActionInstance(mkfileMode?MkfileAction.class:MkdirAction.class,mainFrame).getLabel(), mainFrame);
        this.mainFrame = mainFrame;
        this.mkfileMode = mkfileMode;

        Container contentPane = getContentPane();

        YBoxPanel mainPanel = new YBoxPanel();
        mainPanel.add(new JLabel(Translator.get(mkfileMode?com.mucommander.ui.action.MkfileAction.class.getName()+".tooltip":com.mucommander.ui.action.MkdirAction.class.getName()+".tooltip")+" :"));
        mkdirPathField = new JTextField();
        mkdirPathField.addActionListener(this);
        mainPanel.add(mkdirPathField);
		
        mainPanel.addSpace(10);
        contentPane.add(mainPanel, BorderLayout.NORTH);
        
        okButton = new JButton(Translator.get("create"));
        JButton cancelButton = new JButton(Translator.get("cancel"));
        contentPane.add(DialogToolkit.createOKCancelPanel(okButton, cancelButton, this), BorderLayout.SOUTH);

        // Path field will receive initial focus
        setInitialFocusComponent(mkdirPathField);		

        // Selects OK when enter is pressed
        getRootPane().setDefaultButton(okButton);
        
        setMinimumSize(MINIMUM_DIALOG_DIMENSION);
        setMaximumSize(MAXIMUM_DIALOG_DIMENSION);
        showDialog();
    }



    /**
     * Creates a new directory. This method is trigged by the 'OK' button or return key.
     */
    public void doMkdir() {
        String enteredPath = mkdirPathField.getText();

        // Resolves destination folder
        Object ret[] = FileToolkit.resolvePath(enteredPath, mainFrame.getActiveTable().getCurrentFolder());
        // The path entered doesn't correspond to any existing folder
        if (ret==null) {
            showErrorDialog(Translator.get("invalid_path", enteredPath));
            return;
        }

        // Checks if the directory already exists and reports the error if that's the case
        if(ret[1]==null) {
            showErrorDialog(Translator.get("directory_already_exists", enteredPath));
            return;
        }

        AbstractFile folder = (AbstractFile)ret[0];
        String newName = (String)ret[1];

        FileSet fileSet = new FileSet(folder);
        // Job's FileSet needs to contain at least one file
        fileSet.add(folder);
        new MkdirJob(mainFrame, fileSet, newName, mkfileMode).start();
    }

	
    private void showErrorDialog(String msg) {
        JOptionPane.showMessageDialog(mainFrame, msg, Translator.get("error"), JOptionPane.ERROR_MESSAGE);
    }


    ////////////////////////////
    // ActionListener methods //
    ////////////////////////////
	
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        dispose();
		
        // OK Button
        if(source == okButton || source == mkdirPathField) {
            doMkdir();
        }
    }
	
	
}
