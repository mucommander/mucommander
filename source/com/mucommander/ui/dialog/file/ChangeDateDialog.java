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

import com.mucommander.file.util.FileSet;
import com.mucommander.job.ChangeFileAttributesJob;
import com.mucommander.text.CustomDateFormat;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.DialogToolkit;
import com.mucommander.ui.dialog.FocusDialog;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.main.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Date;

/**
 * This dialog allows the user to change the date of the currently selected/marked file(s). By default, the date is now
 * but a specific date can be specified.
 *
 * @author Maxence Bernard
 */
public class ChangeDateDialog extends FocusDialog implements ActionListener, ItemListener {

    private MainFrame mainFrame;

    private FileSet files;

    private JRadioButton nowRadioButton;

    private JSpinner dateSpinner;

    private JCheckBox recurseDirCheckBox;
    
    private JButton okButton;
    private JButton cancelButton;


    public ChangeDateDialog(MainFrame mainFrame, FileSet files) {
        super(mainFrame, Translator.get(com.mucommander.ui.action.ChangeDateAction.class.getName()+".label"), mainFrame);

        this.mainFrame = mainFrame;
        this.files = files;

        YBoxPanel yBoxPanel = new YBoxPanel();

        yBoxPanel.add(new JLabel(Translator.get(com.mucommander.ui.action.ChangeDateAction.class.getName()+".tooltip")+" :"));
        yBoxPanel.addSpace(10);

        ButtonGroup buttonGroup = new ButtonGroup();

        JPanel tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        nowRadioButton = new JRadioButton(Translator.get("change_date_dialog.now"));
        nowRadioButton.setSelected(true);
        nowRadioButton.addItemListener(this);
        tempPanel.add(nowRadioButton);

        yBoxPanel.add(tempPanel);

        buttonGroup.add(nowRadioButton);
        JRadioButton radioButton = new JRadioButton(Translator.get("change_date_dialog.specific_date"));
        buttonGroup.add(radioButton);

        tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tempPanel.add(radioButton);

        this.dateSpinner = new JSpinner(new SpinnerDateModel());
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, CustomDateFormat.getDateFormatString()));
        // Use the selected file's date if there is only one file, if not use base folder's date.
        dateSpinner.setValue(new Date((files.size()==1?files.fileAt(0):files.getBaseFolder()).getDate()));
        // Spinner is disabled until the 'Specific date' radio button is selected 
        dateSpinner.setEnabled(false);
        tempPanel.add(dateSpinner);

        yBoxPanel.add(tempPanel);

        yBoxPanel.addSpace(10);
        recurseDirCheckBox = new JCheckBox(Translator.get("recurse_directories"));
        yBoxPanel.add(recurseDirCheckBox);

        yBoxPanel.addSpace(15);

        Container contentPane = getContentPane();
        contentPane.add(yBoxPanel, BorderLayout.NORTH);

        okButton = new JButton(Translator.get("ok"));
        cancelButton = new JButton(Translator.get("cancel"));
        contentPane.add(DialogToolkit.createOKCancelPanel(okButton, cancelButton, this), BorderLayout.SOUTH);

        getRootPane().setDefaultButton(okButton);
        setInitialFocusComponent(dateSpinner);
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