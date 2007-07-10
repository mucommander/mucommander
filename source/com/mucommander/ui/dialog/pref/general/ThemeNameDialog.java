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

package com.mucommander.ui.dialog.pref.general;

import com.mucommander.text.Translator;
import com.mucommander.ui.button.ButtonChoicePanel;
import com.mucommander.ui.dialog.FocusDialog;
import com.mucommander.ui.layout.XAlignedComponentPanel;
import com.mucommander.ui.layout.YBoxPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Dialog used to ask a new theme name to the user.
 * @author Nicolas Rinaudo
 */
public class ThemeNameDialog extends FocusDialog implements ActionListener {
    // - UI fields -----------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Field in which the user will enter the new name. */
    private JTextField nameField;
    /** Ok button. */
    private JButton    okButton;
    /** Cancel button. */
    private JButton    cancelButton;



    // - Misc. fields --------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Whether the dialog was closed by the ok button or by cancelling it. */
    private boolean wasValidated;
    /** Maximum dimensions for the dialog. */
    private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(480,10000);	



    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new name dialog with the specified owner.
     * @param owner component that will own this dialog.
     * @param name  current name.
     */
    public ThemeNameDialog(Frame owner, String name) {
        super(owner, Translator.get("rename"), owner);
        init(name);
    }

    /**
     * Creates a new name dialog with the specified owner.
     * @param owner component that will own this dialog.
     * @param name  current name.
     */
    public ThemeNameDialog(Dialog owner, String name) {
        super(owner, Translator.get("rename"), owner);
        init(name);
    }



    // - UI initialisation ---------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates the panel in which we'll store the label and name field.
     * @param  name current name.
     * @return      the panel in which we'll store the label and name field.
     */
    private JPanel createNamePanel(String name) {
        XAlignedComponentPanel panel;

        panel = new XAlignedComponentPanel(5);
        nameField = new JTextField();
        nameField.setText(name);
        nameField.setSelectionStart(0);
        nameField.setSelectionEnd(name.length());
        panel.addRow(Translator.get("name"), nameField, 0);

        return panel;
    }

    /**
     * Initialises the dialog's UI.
     */
    private void init(String name) {
        YBoxPanel panel;

        setMaximumSize(MAXIMUM_DIALOG_DIMENSION);

        // Creates the name panel.
        panel = new YBoxPanel();
        panel.add(createNamePanel(name));

        // Creates the button panel.
        panel.add(new ButtonChoicePanel(new JButton[] {okButton = new JButton(Translator.get("ok")), cancelButton = new JButton(Translator.get("cancel"))},
                                        2, getRootPane()));
        okButton.addActionListener(this);
        cancelButton.addActionListener(this);
        getContentPane().add(panel, BorderLayout.NORTH);
        pack();
    }



    // - Status queries ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Returns the name entered by the user.
     * @return the name entered by the user.
     */
    public String getText() {return nameField.getText();}

    /**
     * Called when the dialog is closed through the ESC button.
     */
    public void cancel() {
        wasValidated = false;
        super.cancel();
    }

    /**
     * Shows the dialog and returns <code>true</code> if it was validated by the user.
     * @return <code>true</code> if it was validated by the user, <code>false</code> otherwise.
     */
    public boolean wasValidated() {
        showDialog();
        return wasValidated;
    }

    /**
     * Called when OK or Cancel have been pressed.
     * @param e describes the event.
     */
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == okButton)
            wasValidated = true;
        else if(e.getSource() == cancelButton)
            wasValidated = false;
        dispose();
    }
}
