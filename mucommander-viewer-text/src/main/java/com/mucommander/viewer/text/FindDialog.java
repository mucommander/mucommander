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
import com.mucommander.search.SearchProperty;
import com.mucommander.text.Translator;
import com.mucommander.ui.text.SelectAllOnFocusTextField;


import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

/**
 * This dialog allows the user to enter a string to be searched for in the text editor.
 *
 * @author Maxence Bernard
 */
public class FindDialog extends FocusDialog implements ActionListener {

    /** The text field where a search string can be entered */
    private final JTextField findField;

    /** The text field where a replace string can be entered */
    private final JTextField replaceField;

    /** Whether replace operation should be done */
    private final JCheckBox doReplace;

    /** Find case-sensitivity setting */
    private final JCheckBox textCase;

    /** Find regex match */
    private final JCheckBox textRegex;

    /** Find whole words */
    private final JCheckBox wholeWords;

    /** The 'OK' button */
    private final JButton okButton;

    /**
     * Forward or backward find direction.
     */
    private boolean forwardFindDirection = true;

    /** true if the dialog was validated by the user */
    private boolean wasValidated;

    /**
     * Creates a new FindDialog and shows it on the screen.
     *
     * @param editorFrame the parent editor frame
     * @param showReplace whether to show replace-related fields
     */
    public FindDialog(JFrame editorFrame, boolean showReplace) {
        super(editorFrame, Translator.get("text_viewer.find"), editorFrame);

        Container contentPane = getContentPane();

        YBoxPanel textFindPanel = new YBoxPanel(10);
        XAlignedComponentPanel compPanel = new XAlignedComponentPanel(0);

        findField = new SelectAllOnFocusTextField(SearchProperty.SEARCH_TEXT.getValue());
        JLabel findLabel = compPanel.addRow(Translator.get("text_viewer.find") + ":", findField, 10);
        findLabel.setLabelFor(findField);

        XAlignedComponentPanel replacePanel = new XAlignedComponentPanel(0);
        replaceField = new SelectAllOnFocusTextField("");
        replaceField.setEnabled(false);
        doReplace = new JCheckBox(Translator.get("text_viewer.find_replace.replace") + ":", false);

        GridBagConstraints gbc = ProportionalGridPanel.getDefaultGridBagConstraints();
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        gbc.fill = GridBagConstraints.VERTICAL;
        ProportionalGridPanel groupingPanel = new ProportionalGridPanel(2, gbc);

        YBoxPanel optionsPanel = new YBoxPanel(5);
        optionsPanel.setBorder(BorderFactory.createTitledBorder(Translator.get("text_viewer.find_replace.options")));

        textCase = new JCheckBox(Translator.get("text_viewer.find_replace.case_sensitive"),
                SearchProperty.TEXT_CASESENSITIVE.getBoolValue());
        optionsPanel.add(textCase);

        textRegex = new JCheckBox(Translator.get("text_viewer.find_replace.regex_match"),
                SearchProperty.TEXT_MATCH_REGEX.getBoolValue());
        optionsPanel.add(textRegex);

        wholeWords = new JCheckBox(Translator.get("text_viewer.find_replace.whole_words"),
                SearchProperty.TEXT_WHOLE_WORDS.getBoolValue());
        optionsPanel.add(wholeWords);

        groupingPanel.add(optionsPanel);

        YBoxPanel directionPanel = new YBoxPanel(5);
        directionPanel.setBorder(BorderFactory.createTitledBorder(Translator.get("text_viewer.find_replace.direction")));

        ButtonGroup directionGroup = new ButtonGroup();

        JRadioButton fwdFind = new JRadioButton(Translator.get("text_viewer.find_replace.forward"), SearchProperty.TEXT_SEARCH_FORWARD.getBoolValue());
        fwdFind.addActionListener(e -> forwardFindDirection = true );
        directionPanel.add(fwdFind);
        directionGroup.add(fwdFind);

        JRadioButton bkwdFind = new JRadioButton(Translator.get("text_viewer.find_replace.backward"), !SearchProperty.TEXT_SEARCH_FORWARD.getBoolValue());
        bkwdFind.addActionListener(e -> forwardFindDirection = false );
        directionPanel.add(bkwdFind);
        directionGroup.add(bkwdFind);

        groupingPanel.add(directionPanel);

        if (showReplace) {
            replacePanel.addRow(doReplace, replaceField, 10);
            compPanel.addRow(replacePanel, 5);
            doReplace.addItemListener(e -> {
                boolean selected = e.getStateChange() == ItemEvent.SELECTED;
                replaceField.setEnabled(selected);
                // due to nature of RSyntaxTextArea these don't matter for replaceAll operation.
                directionPanel.setEnabled(!selected);
                fwdFind.setEnabled(!selected);
                bkwdFind.setEnabled(!selected);
            });
        } // else - simply don't show them
        compPanel.addRow(groupingPanel, 5);

        textFindPanel.add(compPanel);
        contentPane.add(textFindPanel, BorderLayout.NORTH);

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
     * Returns the reaplce string if 'replace' check box was selected, null otherwise.
     *
     * @return the reaplce string if 'replace' check box was selected, null otherwise.
     */
    public String getReplaceString() {
        return doReplace.isSelected() ? replaceField.getText() : null;
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

    /**
     * Returns whether search should use whole words.
     * @return whether search should use whole words.
     */
    public boolean isWholeWords() {
        return wholeWords.isSelected();
    }

    /**
     * Returns whether search should use forward direction.
     * @return whether search should use forward direction.
     */
    public boolean isForwardDirection() {
        return forwardFindDirection;
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        wasValidated = source == okButton || source == findField;
        dispose();
    }
}
