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


package com.mucommander.ui.dialog.file;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.util.FileSet;
import com.mucommander.commons.file.util.PathUtils;
import com.mucommander.job.MkdirJob;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.action.impl.MkdirAction;
import com.mucommander.ui.action.impl.MkfileAction;
import com.mucommander.ui.chooser.SizeChooser;
import com.mucommander.ui.dialog.DialogToolkit;
import com.mucommander.ui.dialog.FocusDialog;
import com.mucommander.ui.dialog.InformationDialog;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.text.FilePathField;


/**
 * Dialog invoked when the user wants to create a new folder or an empty file in the current folder.
 *
 * @see MkdirAction
 * @see MkfileAction
 * @author Maxence Bernard
 */
public class MkdirDialog extends FocusDialog implements ActionListener, ItemListener {

    private MainFrame mainFrame;
	
    private JTextField pathField;

    private JCheckBox allocateSpaceCheckBox;
    private SizeChooser allocateSpaceChooser;

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
        super(mainFrame, ActionManager.getActionInstance(mkfileMode?MkfileAction.Descriptor.ACTION_ID:MkdirAction.Descriptor.ACTION_ID,mainFrame).getLabel(), mainFrame);
        this.mainFrame = mainFrame;
        this.mkfileMode = mkfileMode;

        Container contentPane = getContentPane();

        YBoxPanel mainPanel = new YBoxPanel();
        mainPanel.add(new JLabel(ActionProperties.getActionTooltip(mkfileMode?MkfileAction.Descriptor.ACTION_ID:MkdirAction.Descriptor.ACTION_ID)+" :"));

        // Create a path field with auto-completion capabilities
        pathField = new FilePathField();
        pathField.addActionListener(this);
        mainPanel.add(pathField);

        if(mkfileMode) {
            JPanel tempPanel = new JPanel(new BorderLayout());

            allocateSpaceCheckBox = new JCheckBox(Translator.get("mkfile_dialog.allocate_space")+":", false);
            allocateSpaceCheckBox.addItemListener(this);
            tempPanel.add(allocateSpaceCheckBox, BorderLayout.WEST);

            allocateSpaceChooser = new SizeChooser(false);
            allocateSpaceChooser.setEnabled(false);
            tempPanel.add(allocateSpaceChooser, BorderLayout.EAST);

            mainPanel.add(tempPanel);
        }
        
        mainPanel.addSpace(10);
        contentPane.add(mainPanel, BorderLayout.NORTH);
        
        okButton = new JButton(Translator.get("create"));
        JButton cancelButton = new JButton(Translator.get("cancel"));
        contentPane.add(DialogToolkit.createOKCancelPanel(okButton, cancelButton, getRootPane(), this), BorderLayout.SOUTH);

        // Path field will receive initial focus
        setInitialFocusComponent(pathField);

        setMinimumSize(MINIMUM_DIALOG_DIMENSION);
        setMaximumSize(MAXIMUM_DIALOG_DIMENSION);
    }



    /**
     * Starts an {@link com.mucommander.job.MkdirJob}. This method is trigged by the 'OK' button or the return key.
     */
    public void startJob() {
        String enteredPath = pathField.getText();

        // Resolves destination folder
        PathUtils.ResolvedDestination resolvedDest = PathUtils.resolveDestination(enteredPath, mainFrame.getActivePanel().getCurrentFolder());
        // The path entered doesn't correspond to any existing folder
        if (resolvedDest==null) {
            InformationDialog.showErrorDialog(mainFrame, Translator.get("invalid_path", enteredPath));
            return;
        }

        // Checks if the directory already exists and reports the error if that's the case
        int destinationType = resolvedDest.getDestinationType();
        if(destinationType==PathUtils.ResolvedDestination.EXISTING_FOLDER) {
            InformationDialog.showErrorDialog(mainFrame, Translator.get("directory_already_exists", enteredPath));
            return;
        }

        // Don't check for existing regular files, MkdirJob will take of it and popup a FileCollisionDialog 
        AbstractFile destFile = resolvedDest.getDestinationFile();

        FileSet fileSet = new FileSet(destFile.getParent());
        // Job's FileSet needs to contain at least one file
        fileSet.add(destFile);

        ProgressDialog progressDialog = new ProgressDialog(mainFrame, getTitle());

        MkdirJob job;
        if(mkfileMode)
            job = new MkdirJob(progressDialog, mainFrame, fileSet, allocateSpaceCheckBox.isSelected()?allocateSpaceChooser.getValue():-1);
        else
            job = new MkdirJob(progressDialog, mainFrame, fileSet);

        progressDialog.start(job);
    }


    ///////////////////////////////////
    // ActionListener implementation //
    ///////////////////////////////////
	
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        dispose();
		
        // OK Button
        if(source == okButton || source == pathField) {
            startJob();
        }
    }


    /////////////////////////////////
    // ItemListener implementation //
    /////////////////////////////////

    public void itemStateChanged(ItemEvent e) {
        allocateSpaceChooser.setEnabled(allocateSpaceCheckBox.isSelected());
    }
}
