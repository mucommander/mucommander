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

package com.mucommander.ui.dialog.pref.general;

import com.mucommander.commons.util.ui.button.ButtonChoicePanel;
import com.mucommander.commons.util.ui.dialog.FocusDialog;
import com.mucommander.commons.util.ui.layout.XAlignedComponentPanel;
import com.mucommander.commons.util.ui.layout.YBoxPanel;
import com.mucommander.text.Translator;


import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

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
    private JButton okButton;
    /** Cancel button. */
    private JButton cancelButton;



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

        List<JButton> buttons = Arrays.asList(
                okButton = new JButton(Translator.get("ok")),
                cancelButton = new JButton(Translator.get("cancel"))
        );
        // Creates the button panel.
        panel.add(new ButtonChoicePanel(buttons,
                                        2, getRootPane()));
        // TODO rework it to use Action based enums....
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
    @Override
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
