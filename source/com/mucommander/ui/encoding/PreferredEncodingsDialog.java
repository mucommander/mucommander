/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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

package com.mucommander.ui.encoding;

import com.mucommander.runtime.OsFamilies;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.FocusDialog;
import com.mucommander.ui.layout.YBoxPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.charset.Charset;
import java.util.Vector;

/**
 * This dialog allows the list of preferred character encodings to be modified by the end user. Each of the supported
 * encodings are represented as a checkbox and can individually be selected/unselected. A 'revert to defaults' button
 * allows the {@link EncodingPreferences#getDefaultPreferredEncodings() default preferred encodings} to be used.
 *
 * @see EncodingPreferences 
 * @author Maxence Bernard
 */
public class PreferredEncodingsDialog extends FocusDialog {

    /** Contains all the checkbox added to this dialog */
    private Vector<JCheckBox> checkboxes;

    /** Minimum dimensions of this dialog */
    private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(300,0);

    /** Maximum dimensions of this dialog */
    private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(550,400);

    /**
     * Creates a new PreferredEncodingsDialog, without showing it on screen.
     *
     * @param owner the frame that invoked this dialog
     */
    public PreferredEncodingsDialog(Frame owner) {
        super(owner, Translator.get("preferred_encodings"), owner);
        init();
    }

    /**
     * Creates a new PreferredEncodingsDialog, without showing it on screen.
     *
     * @param owner the dialog that invoked this dialog
     */
    public PreferredEncodingsDialog(Dialog owner) {
        super(owner, Translator.get("preferred_encodings"), owner);
        init();
    }


    protected void init() {
        // Mac OS X: small window borders
        if(OsFamilies.MAC_OS_X.isCurrent())
            getRootPane().putClientProperty("Window.style", "small");

        Container contentPane = getContentPane();

        // Label

        JLabel label = new JLabel(Translator.get("preferred_encodings")+":");

        // Mac OS X: small component size
        if(OsFamilies.MAC_OS_X.isCurrent())
            label.putClientProperty("JComponent.sizeVariant", "small");

        contentPane.add(label, BorderLayout.NORTH);

        // Checkboxes

        YBoxPanel yPanel = new YBoxPanel();

        checkboxes = new Vector<JCheckBox>();
        JCheckBox checkbox;
        for(String enc : Charset.availableCharsets().keySet()) {
            checkbox = new JCheckBox(enc);
            // Mac OS X: component size
            if(OsFamilies.MAC_OS_X.isCurrent())
                checkbox.putClientProperty("JComponent.sizeVariant", "small");

            checkboxes.add(checkbox);
            yPanel.add(checkbox);
        }

        selectCheckboxes(EncodingPreferences.getPreferredEncodings());

        JScrollPane scrollPane = new JScrollPane(yPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        contentPane.add(scrollPane, BorderLayout.CENTER);

        // 'Revert to defaults' button

        JButton defaultsButton = new JButton(Translator.get("reset"));
        // Mac OS X: component size
        if(OsFamilies.MAC_OS_X.isCurrent())
            defaultsButton.putClientProperty("JComponent.sizeVariant", "small");

        defaultsButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                selectCheckboxes(EncodingPreferences.getDefaultPreferredEncodings());
            }
        });

        JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        flowPanel.add(defaultsButton);
        contentPane.add(flowPanel, BorderLayout.SOUTH);

        setMinimumSize(MINIMUM_DIALOG_DIMENSION);
        setMaximumSize(MAXIMUM_DIALOG_DIMENSION);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                JCheckBox checkbox;
                int nbCheckboxes = checkboxes.size();
                Vector<String> preferredEncodings = new Vector<String>();

                for(int i=0; i<nbCheckboxes; i++) {
                    checkbox = checkboxes.elementAt(i);
                    if(checkbox.isSelected())
                        preferredEncodings.add(checkbox.getText());
                }

                EncodingPreferences.setPreferredEncodings(preferredEncodings);
            }
        });
    }

    /**
     * Selects all the checkboxes which correspond to an encoding that is present in the given vector.
     *
     * @param selectedEncodings list of encodings to select
     */
    protected void selectCheckboxes(Vector<String> selectedEncodings) {
        JCheckBox checkbox;
        int nbCheckboxes = checkboxes.size();
        for(int i=0; i<nbCheckboxes; i++) {
            checkbox = checkboxes.elementAt(i);
            checkbox.setSelected(selectedEncodings.contains(checkbox.getText()));
        }
    }
}
