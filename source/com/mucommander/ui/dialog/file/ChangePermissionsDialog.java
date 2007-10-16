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
import com.mucommander.file.FilePermissions;
import com.mucommander.file.util.FileSet;
import com.mucommander.job.ChangeFileAttributesJob;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.DialogToolkit;
import com.mucommander.ui.dialog.FocusDialog;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.text.SizeConstrainedDocument;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * This dialog allows the user to change the permissions of the currently selected/marked file(s). The permissions can be
 * selected either by clicking individual read/write/executable checkboxes for each of the user/group/other accesses,
 * or by entering an octal permission value. 
 *
 * @author Maxence Bernard
 */
public class ChangePermissionsDialog extends FocusDialog implements FilePermissions, ActionListener, ItemListener, DocumentListener {

    private MainFrame mainFrame;

    private FileSet files;

    private JCheckBox permCheckBoxes[][];

    private JTextField octalPermTextField;

    private JCheckBox recurseDirCheckBox;

    /** If true, ItemEvent events should be ignored */
    private boolean ignoreItemEvent;
    /** If true, DocumentEvent events should be ignored */
    private boolean ignoreDocumentEvent;

    private JButton okButton;
    private JButton cancelButton;


    public ChangePermissionsDialog(MainFrame mainFrame, FileSet files) {
        super(mainFrame, Translator.get(com.mucommander.ui.action.ChangePermissionsAction.class.getName()+".label"), mainFrame);

        this.mainFrame = mainFrame;
        this.files = files;

        YBoxPanel yBoxPanel = new YBoxPanel();

        yBoxPanel.add(new JLabel(Translator.get(com.mucommander.ui.action.ChangePermissionsAction.class.getName()+".tooltip")+" :"));
        yBoxPanel.addSpace(10);

        JPanel gridPanel = new JPanel(new GridLayout(4, 4));
        permCheckBoxes = new JCheckBox[5][5];
        JCheckBox permCheckBox;

        AbstractFile destFile = files.size()==1?files.fileAt(0):files.getBaseFolder();
        int permSetMask = destFile.getPermissionSetMask();
        boolean canSetPermission = permSetMask!=0;
        int defaultPerms = destFile.getPermissions();

        gridPanel.add(new JLabel());
        gridPanel.add(new JLabel(Translator.get("permissions.read")));
        gridPanel.add(new JLabel(Translator.get("permissions.write")));
        gridPanel.add(new JLabel(Translator.get("permissions.executable")));

        for(int a= USER_ACCESS; a>=OTHER_ACCESS; a--) {
            gridPanel.add(new JLabel(Translator.get(a== USER_ACCESS ?"permissions.user":a==GROUP_ACCESS?"permissions.group":"permissions.other")));

            for(int p=READ_PERMISSION; p>=EXECUTE_PERMISSION; p=p>>1) {
                permCheckBox = new JCheckBox();
                permCheckBox.setSelected((defaultPerms & (p<<a*3))!=0);

                // Enable the checkbox only if the permission can be set in the destination
                if((permSetMask & (p<<a*3))==0)
                    permCheckBox.setEnabled(false);
                else
                    permCheckBox.addItemListener(this);

                gridPanel.add(permCheckBox);
                permCheckBoxes[a][p] = permCheckBox;
            }
        }

        yBoxPanel.add(gridPanel);

        octalPermTextField = new JTextField(3);
        // Constrains text field to 3 digits, from 0 to 7 (octal base)
        Document doc = new SizeConstrainedDocument(3) {
            public void insertString(int offset, String str, AttributeSet attributeSet) throws BadLocationException {
                int strLen = str.length();
                char c;
                for(int i=0; i<strLen; i++) {
                    c = str.charAt(i);
                    if(c<'0' || c>'7')
                        return;
                }

                super.insertString(offset, str, attributeSet);
            }
        };
        octalPermTextField.setDocument(doc);
        // Initializes the field's value
        updateOctalPermTextField();

        if(canSetPermission) {
            setInitialFocusComponent(octalPermTextField);
            doc.addDocumentListener(this);
        }
        // Disable text field if no permission bit can be set
        else {
            octalPermTextField.setEnabled(false);
        }

        yBoxPanel.addSpace(10);
        JPanel tempPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tempPanel.add(new JLabel(Translator.get("permissions.octal_notation")));
        tempPanel.add(octalPermTextField);
        yBoxPanel.add(tempPanel);

        yBoxPanel.addSpace(15);

        recurseDirCheckBox = new JCheckBox(Translator.get("recurse_directories"));
        // Disable check box if no permission bit can be set
        recurseDirCheckBox.setEnabled(canSetPermission && (files.size()>1 || ((AbstractFile)files.elementAt(0)).isDirectory()));
        yBoxPanel.add(recurseDirCheckBox);

        Container contentPane = getContentPane();
        contentPane.add(yBoxPanel, BorderLayout.NORTH);

        okButton = new JButton(Translator.get("ok"));
        cancelButton = new JButton(Translator.get("cancel"));

        contentPane.add(DialogToolkit.createOKCancelPanel(okButton, cancelButton, this), BorderLayout.SOUTH);

        if(!canSetPermission) {
            // Disable OK button if no permission bit can be set
            okButton.setEnabled(false);
        }

        getRootPane().setDefaultButton(canSetPermission?okButton:cancelButton);
        setResizable(false);
    }


    /**
     * Creates and returns a permissions int using the values of the permission checkboxes.
     */
    private int getPermInt() {
        JCheckBox permCheckBox;
        int perms = 0;

        for(int a= USER_ACCESS; a>=OTHER_ACCESS; a--) {
            for(int p=READ_PERMISSION; p>=EXECUTE_PERMISSION; p=p>>1) {
                permCheckBox = permCheckBoxes[a][p];

                if(permCheckBox.isSelected())
                    perms |= (p<<a*3);
            }
        }

        return perms;
    }


    /**
     * Updates the octal permissions text field's value to reflect the permission checkboxes' values.
     */
    private void updateOctalPermTextField() {
        String octalStr = Integer.toOctalString(getPermInt());
        int len = octalStr.length();
        for(int i=len; i<3; i++)
            octalStr = "0"+octalStr;

        octalPermTextField.setText(octalStr);
    }


    /**
     * Updates the permission checkboxes' values to reflect the octal permissions text field.
     */
    private void updatePermCheckBoxes() {
        JCheckBox permCheckBox;
        String octalStr = octalPermTextField.getText();

        int perms = octalStr.equals("")?0:Integer.parseInt(octalStr, 8);

        for(int a= USER_ACCESS; a>=OTHER_ACCESS; a--) {
            for(int p=READ_PERMISSION; p>=EXECUTE_PERMISSION; p=p>>1) {
                permCheckBox = permCheckBoxes[a][p];

//                if(permCheckBox.isEnabled())
                permCheckBox.setSelected((perms & (p<<a*3))!=0);
            }
        }

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
            ChangeFileAttributesJob job = new ChangeFileAttributesJob(progressDialog, mainFrame, files, getPermInt(), recurseDirCheckBox.isSelected());
            progressDialog.start(job);
        }
        else if(source==cancelButton) {
            dispose();
        }
    }


    /////////////////////////////////
    // ItemListener implementation //
    /////////////////////////////////

    // Update the octal permission text field whenever one of the permission checkboxes' value has changed

    public void itemStateChanged(ItemEvent e) {
        if(ignoreItemEvent)
            return;

        ignoreDocumentEvent = true;
        updateOctalPermTextField();
        ignoreDocumentEvent = false;
    }


    //////////////////////////////
    // DocumentListener methods //
    //////////////////////////////

    // Update the permission checkboxes' values whenever the octal permission text field has changed

    public void changedUpdate(DocumentEvent e) {
        if(ignoreDocumentEvent)
            return;

        ignoreItemEvent = true;
        updatePermCheckBoxes();
        ignoreItemEvent = false;
    }

    public void insertUpdate(DocumentEvent e) {
        if(ignoreDocumentEvent)
            return;

        ignoreItemEvent = true;
        updatePermCheckBoxes();
        ignoreItemEvent = false;
    }

    public void removeUpdate(DocumentEvent e) {
        if(ignoreDocumentEvent)
            return;

        ignoreItemEvent = true;
        updatePermCheckBoxes();
        ignoreItemEvent = false;
    }
}
