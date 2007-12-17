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

import com.mucommander.conf.impl.MuConfiguration;
import com.mucommander.file.AbstractArchiveFile;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.AbstractTrash;
import com.mucommander.file.FileFactory;
import com.mucommander.file.util.FileSet;
import com.mucommander.job.DeleteJob;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.DialogToolkit;
import com.mucommander.ui.layout.InformationPane;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.main.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;


/**
 * Confirmation dialog invoked when the user wants to delete currently selected files. It allows to choose between two
 * different ways of deleting files: move them to the trash or permanently erase them. The former choice is only given
 * if a trash is available on the current platform and capable of moving the selected files.
 * The choice (use trash or not) is saved in the preferences and reused next time this dialog is invoked.   
 *
 * @see com.mucommander.ui.action.DeleteAction
 * @author Maxence Bernard
 */
public class DeleteDialog extends JobDialog implements ItemListener, ActionListener {

    /** Should files be moved to the trash or permanently erased */
    private boolean moveToTrash;

    /** Allows to control whether files should be moved to trash when deleted or permanently erased */
    private JCheckBox moveToTrashCheckBox;

    /** Informs the user about the consequences of deleting files, based on the current 'Move to trash' choice */
    private InformationPane informationPane;

    /** The button that confirms deletion */
    private JButton deleteButton;

    /** Dialog size constraints */
    private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(320,0);


    public DeleteDialog(MainFrame mainFrame, FileSet files) {
        super(mainFrame, Translator.get("delete"), files);

        YBoxPanel mainPanel = new YBoxPanel();

        // Allow 'Move to trash' option only if:
        // - the current platform has a trash
        // - the base folder is not an archive
        // - the base folder of the to-be-deleted files is not a trash folder or one of its children
        // - the base folder can be moved to the trash (the eligibility conditions should be the same as the files to-be-deleted)
        AbstractTrash trash = FileFactory.getTrash();
        AbstractFile baseFolder = files.getBaseFolder();
        if(trash!=null && !(baseFolder instanceof AbstractArchiveFile) && !trash.isTrashFile(baseFolder) && trash.canMoveToTrash(baseFolder)) {
            moveToTrash = MuConfiguration.getVariable(
                        MuConfiguration.DELETE_TO_TRASH,
                        MuConfiguration.DEFAULT_DELETE_TO_TRASH);

            moveToTrashCheckBox = new JCheckBox(Translator.get("delete_dialog.move_to_trash.option"), moveToTrash);
            moveToTrashCheckBox.addItemListener(this);
        }

        informationPane = new InformationPane();
        updateInformationPane();

        mainPanel.add(informationPane);
        mainPanel.addSpace(10);

        JPanel fileDetailsPanel = createFileDetailsPanel();

        // Create file details button and OK/cancel buttons and lay them out a single row
        deleteButton = new JButton(Translator.get("delete"));
        JButton cancelButton = new JButton(Translator.get("cancel"));

        mainPanel.add(createButtonsPanel(createFileDetailsButton(fileDetailsPanel),
                DialogToolkit.createOKCancelPanel(deleteButton, cancelButton, getRootPane(), this)));

        mainPanel.add(fileDetailsPanel);

        if(moveToTrashCheckBox!=null)
            mainPanel.add(moveToTrashCheckBox);

        getContentPane().add(mainPanel);

        // Give initial keyboard focus to the 'Delete' button
        setInitialFocusComponent(deleteButton);

        // Call dispose() when dialog is closed
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Size dialog and show it to the screen
        setMinimumSize(MINIMUM_DIALOG_DIMENSION);
        setResizable(false);
        showDialog();
    }


    /**
     * Updates the information pane to reflect the current 'Move to trash' choice.
     */
    private void updateInformationPane() {
        informationPane.getMainLabel().setText(Translator.get(moveToTrash?"delete_dialog.move_to_trash.confirmation":"delete_dialog.permanently_delete.confirmation"));
        informationPane.getCaptionLabel().setText(Translator.get(moveToTrash?"delete_dialog.move_to_trash.confirmation_details":"delete_dialog.permanently_delete.confirmation_details"));
        informationPane.setIcon(moveToTrash?null: InformationPane.getPredefinedIcon(InformationPane.WARNING_ICON));
    }


    /////////////////////////////////
    // ItemListener implementation //
    /////////////////////////////////

    public void itemStateChanged(ItemEvent e) {
        moveToTrash = moveToTrashCheckBox.isSelected();
        updateInformationPane();
        pack();
    }


    ///////////////////////////////////
    // ActionListener implementation //
    ///////////////////////////////////

    public void actionPerformed(ActionEvent e) {
        // Start by disposing this dialog
        dispose();

        if(e.getSource()==deleteButton) {
            if(moveToTrashCheckBox!=null) {
                // Save the 'Move to trash' option choice in the preferences, will be used next time this dialog is invoked.
                MuConfiguration.setVariable(MuConfiguration.DELETE_TO_TRASH, moveToTrash);
            }

            // Starts deleting files
            ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("delete_dialog.deleting"));
            DeleteJob deleteJob = new DeleteJob(progressDialog, mainFrame, files, moveToTrash);
            progressDialog.start(deleteJob);
        }
    }
}
