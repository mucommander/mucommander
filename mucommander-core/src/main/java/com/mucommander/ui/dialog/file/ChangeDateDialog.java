/*
 * This file is part of muCommander, http://www.mucommander.com
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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Date;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileOperation;
import com.mucommander.commons.file.protocol.search.SearchFile;
import com.mucommander.commons.file.util.FileSet;
import com.mucommander.commons.util.ui.dialog.DialogToolkit;
import com.mucommander.commons.util.ui.layout.FluentPanel;
import com.mucommander.commons.util.ui.layout.YBoxPanel;
import com.mucommander.job.impl.ChangeFileAttributesJob;
import com.mucommander.text.CustomDateFormat;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.action.impl.ChangeDateAction;
import com.mucommander.ui.main.MainFrame;

/**
 * This dialog allows the user to change the date of the currently selected/marked file(s). By default, the date is now
 * but a specific date can be specified.
 *
 * @author Maxence Bernard
 */
public class ChangeDateDialog extends JobDialog implements ActionListener, ItemListener {

    private JRadioButton nowRadioButton;

    private JSpinner dateSpinner;

    private JCheckBox recurseDirCheckBox;
    
    private JButton okButton;
    private JButton cancelButton;


    public ChangeDateDialog(MainFrame mainFrame, FileSet files) {
        super(mainFrame, ActionProperties.getActionLabel(ChangeDateAction.Descriptor.ACTION_ID), files);

        YBoxPanel mainPanel = new YBoxPanel();

        mainPanel.add(new JLabel(ActionProperties.getActionLabel(ChangeDateAction.Descriptor.ACTION_ID)+" :"));
        mainPanel.addSpace(5);

        ButtonGroup buttonGroup = new ButtonGroup();

        AbstractFile destFile;
        boolean canChangeDate;
        if (files.size() == 1) {
            destFile = files.elementAt(0);
            canChangeDate = destFile.isFileOperationSupported(FileOperation.CHANGE_DATE);
        } else {
            destFile = files.getBaseFolder();
            switch (destFile.getURL().getScheme()) {
            case SearchFile.SCHEMA:
                canChangeDate = files.stream().allMatch(file -> file.isFileOperationSupported(FileOperation.CHANGE_DATE));
                break;
            default:
                canChangeDate = destFile.isFileOperationSupported(FileOperation.CHANGE_DATE);
            }
        }

        nowRadioButton = new JRadioButton(Translator.get("change_date_dialog.now"));
        nowRadioButton.setSelected(true);
        nowRadioButton.addItemListener(this);

        mainPanel.add(new FluentPanel(new FlowLayout(FlowLayout.LEFT)).add(nowRadioButton));

        buttonGroup.add(nowRadioButton);
        JRadioButton specificDateRadioButton = new JRadioButton(Translator.get("change_date_dialog.specific_date"));
        buttonGroup.add(specificDateRadioButton);

        this.dateSpinner = new JSpinner(new SpinnerDateModel());
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, CustomDateFormat.getDateFormatString()));
        // Use the selected file's date if there is only one file, if not use base folder's date.
        dateSpinner.setValue(new Date(destFile.getDate()));
        // Spinner is disabled until the 'Specific date' radio button is selected 
        dateSpinner.setEnabled(false);

        mainPanel.add(new FluentPanel(new FlowLayout(FlowLayout.LEFT))
                          .add(specificDateRadioButton)
                          .add(dateSpinner));

        mainPanel.addSpace(10);
        recurseDirCheckBox = new JCheckBox(Translator.get("recurse_directories"));
        mainPanel.add(recurseDirCheckBox);

        mainPanel.addSpace(15);

        // Create file details button and OK/cancel buttons and lay them out a single row
        JPanel fileDetailsPanel = createFileDetailsPanel();

        okButton = new JButton(Translator.get("change"));
        cancelButton = new JButton(Translator.get("cancel"));

        mainPanel.add(createButtonsPanel(createFileDetailsButton(fileDetailsPanel),
                DialogToolkit.createOKCancelPanel(okButton, cancelButton, getRootPane(), this)));
        mainPanel.add(fileDetailsPanel);

        getContentPane().add(mainPanel, BorderLayout.NORTH);

        if(!canChangeDate) {
            nowRadioButton.setEnabled(false);
            specificDateRadioButton.setEnabled(false);
            dateSpinner.setEnabled(false);
            recurseDirCheckBox.setEnabled(false);
            okButton.setEnabled(false);
        }

        getRootPane().setDefaultButton(canChangeDate?okButton:cancelButton);
        setInitialFocusComponent(canChangeDate?nowRadioButton:cancelButton);
        setResizable(false);
    }


    ///////////////////////////////////
    // ActionListener implementation //
    ///////////////////////////////////

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if(source==okButton) {
            dispose();

            // Starts copying files
            ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("progress_dialog.processing_files"));
            ChangeFileAttributesJob job = new ChangeFileAttributesJob(progressDialog, mainFrame, files,
                nowRadioButton.isSelected()?System.currentTimeMillis():((SpinnerDateModel)dateSpinner.getModel()).getDate().getTime(),
                recurseDirCheckBox.isSelected());
            progressDialog.start(job);
        }
        else if(source==cancelButton) {
            dispose();
        }
    }


    /////////////////////////////////
    // ItemListener implementation //
    /////////////////////////////////

    // Enable/disables the date spinner component when the radio button selection has changed  

    public void itemStateChanged(ItemEvent e) {
        if(e.getSource()==nowRadioButton) {
            dateSpinner.setEnabled(!nowRadioButton.isSelected());
        }
    }
}
