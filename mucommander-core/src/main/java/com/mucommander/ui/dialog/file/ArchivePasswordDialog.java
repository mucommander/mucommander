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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.mucommander.commons.util.ui.dialog.DialogToolkit;
import com.mucommander.commons.util.ui.dialog.FocusDialog;
import com.mucommander.commons.util.ui.layout.YBoxPanel;
import com.mucommander.commons.util.ui.text.MultiLineLabel;
import com.mucommander.job.ui.DialogResult;
import com.mucommander.text.Translator;
import com.mucommander.ui.main.MainFrame;

public class ArchivePasswordDialog extends FocusDialog implements ActionListener, DialogResult {

    private JTextField passwordField;

    private JButton okButton;

    private String password;

    // Dialog size constraints
    private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(500,0); 
    // Dialog width should not exceed 360, height is not an issue (always the same)
    private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(800,10000);


    /**
     * Creates a new rename file dialog.
     *
     * @param mainFrame the parent MainFrame 
     * @param file the file to rename.
     */
    public ArchivePasswordDialog(MainFrame mainFrame) {
        super(mainFrame, Translator.get("maybe_password_protected"), mainFrame);

        Container contentPane = getContentPane();

        YBoxPanel mainPanel = new YBoxPanel();
        mainPanel.add(new JLabel(Translator.get("maybe_password_protected_failure") + ":"));
        passwordField = new JPasswordField();
        passwordField.addActionListener(this);

        // Sets the initial selection.
        mainPanel.add(passwordField);
   
        mainPanel.addSpace(10);
        contentPane.add(mainPanel, BorderLayout.NORTH);
        
        okButton = new JButton(Translator.get("ok"));
        okButton.setEnabled(false);
        passwordField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent e) {
                okButton.setEnabled(!passwordField.getText().trim().isEmpty());
            }
            
            @Override
            public void insertUpdate(DocumentEvent e) {
                okButton.setEnabled(!passwordField.getText().trim().isEmpty());
            }
            
            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
        JButton cancelButton = new JButton(Translator.get("cancel"));
        contentPane.add(DialogToolkit.createOKCancelPanel(okButton, cancelButton, getRootPane(), this), BorderLayout.SOUTH);

        // Path field will receive initial focus
        setInitialFocusComponent(passwordField);

        setMinimumSize(MINIMUM_DIALOG_DIMENSION);
        setMaximumSize(MAXIMUM_DIALOG_DIMENSION);
    }


    ///////////////////////////////////
    // ActionListener implementation //
    ///////////////////////////////////
    
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        // OK Button
        if (source == okButton || source == passwordField) {
            if (!okButton.isEnabled())
                return;
            password = passwordField.getText();
        } else {
            password = null;
        }
        dispose();
    }

    public Object getUserInput() {
        showDialog();
        return password;
    }

}
