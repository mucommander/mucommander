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

package com.mucommander.viewer.text;

import com.mucommander.commons.util.ui.dialog.DialogToolkit;
import com.mucommander.commons.util.ui.dialog.FocusDialog;
import com.mucommander.commons.util.ui.layout.ProportionalGridPanel;
import com.mucommander.commons.util.ui.layout.XAlignedComponentPanel;
import com.mucommander.commons.util.ui.layout.YBoxPanel;
import com.mucommander.job.impl.SearchJob;
import com.mucommander.text.Translator;
import com.mucommander.ui.text.SelectAllOnFocusTextField;


import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * This dialog allows the user to enter a string to be searched for in the text editor.
 *
 * @author Maxence Bernard
 */
public class FindDialog extends FocusDialog implements ActionListener {

    /** The text field where a search string can be entered */
    private final JTextField findField;

    /** Find case-sensitivity setting */
    private final JCheckBox textCase;

    /** Find regex match */
    private final JCheckBox textRegex;

    /** The 'OK' button */
    private final JButton okButton;

    /** true if the dialog was validated by the user */
    private boolean wasValidated;

    /**
     * Creates a new FindDialog and shows it on the screen.
     *
     * @param editorFrame the parent editor frame
     */
    public FindDialog(JFrame editorFrame) {
        super(editorFrame, Translator.get("text_viewer.find"), editorFrame);

        Container contentPane = getContentPane();

        YBoxPanel textSearchPanel = new YBoxPanel(10);
        // TODO FIXME "Text search (Optional)" - not in dictionary?
        textSearchPanel.setBorder(BorderFactory.createTitledBorder(Translator.get("text_viewer.find_text")));
        XAlignedComponentPanel compPanel = new XAlignedComponentPanel(5);

        findField = new SelectAllOnFocusTextField(SearchJob.lastSearchString);
        JLabel findLabel = compPanel.addRow(Translator.get("text_viewer.find") + ":", findField, 10);
        findLabel.setLabelFor(findField);
        findLabel.setDisplayedMnemonic('t');

        GridBagConstraints gbc = ProportionalGridPanel.getDefaultGridBagConstraints();
        gbc.weightx = 1.0;
        ProportionalGridPanel groupingPanel = new ProportionalGridPanel(2, gbc);
        textCase = new JCheckBox(Translator.get("text_viewer.find.case_sensitive") + ":",
                SearchJob.lastSearchCaseSensitive);
        groupingPanel.add(textCase);

        textRegex = new JCheckBox(Translator.get("text_viewer.find.regexp_match") + ":",
                SearchJob.lastSearchMatchRegex);
        groupingPanel.add(textRegex);
        compPanel.addRow("", groupingPanel, 5);

        textSearchPanel.add(compPanel);
        contentPane.add(textSearchPanel, BorderLayout.NORTH);

        okButton = new JButton(Translator.get("ok"));
        JButton cancelButton = new JButton(Translator.get("cancel"));
        contentPane.add(DialogToolkit.createOKCancelPanel(okButton, cancelButton, getRootPane(), this), BorderLayout.SOUTH);

        // The text field will receive initial focus
        setInitialFocusComponent(findField);

        showDialog();
    }

    /**
     * Returns <code>true</code> if the dialog was validated by the user, i.e. the user pressed the 'OK' button
     * or the 'Enter' key in the text field.
     *
     * @return <code>true</code> if the dialog was validated by the user
     */
    public boolean wasValidated() {
        return wasValidated;
    }

    /**
     * Returns the search string entered by the user in the text field.
     *
     * @return the search string entered by the user in the text field
     */
    public String getSearchString() {
        return findField.getText();
    }

    /**
     * Returns whether search should be case-sensitive.
     * @return whether search should be case-sensitive.
     */
    public boolean getCaseSensitivity() {
        return textCase.isSelected();
    }

    /**
     * Returns whether search should use regex.
     * @return whether search should use regex.
     */
    public boolean getRegexMatch() {
        return textRegex.isSelected();
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        wasValidated = source == okButton || source == findField;
        dispose();
    }
}
