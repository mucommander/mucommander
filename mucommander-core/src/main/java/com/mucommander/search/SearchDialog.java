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

package com.mucommander.search;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.util.PathUtils;
import com.mucommander.commons.util.Pair;
import com.mucommander.commons.util.ui.dialog.DialogToolkit;
import com.mucommander.commons.util.ui.dialog.FocusDialog;
import com.mucommander.commons.util.ui.layout.XAlignedComponentPanel;
import com.mucommander.commons.util.ui.layout.YBoxPanel;
import com.mucommander.commons.util.ui.spinner.IntEditor;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.action.impl.FindAction;
import com.mucommander.ui.main.MainFrame;

/**
 * Dialog used to set parameters for file searching.
 *
 * @author Arik Hadas
 */
public class SearchDialog extends FocusDialog implements ActionListener, DocumentListener {
    private JTextField searchFilesField;
    private JTextField searchInField;
    private JCheckBox searchSubfolders;
    private JCheckBox searchArchives;
    private JCheckBox searchHidden;
    private JCheckBox matchCase;
    private JCheckBox matchRegex;
    private JSpinner depth;
    private JLabel wildcards;
    private JTextField searchTextField;
    private JCheckBox textCase;
    private JCheckBox textRegex;

    // Store last values, initialized to the default values
    private static boolean lastSearchSubfolders = true;
    private static boolean lastSearchArchives = false;
    private static boolean lastSearchHidden = false;
    private static boolean lastMatchCase = true;
    private static boolean lastMatchRegex = false;
    private static int lastDepth = 0;
    private static String lastText = "";
    private static boolean lastTextCase = true;
    private static boolean lastTextRegex = false;

    private JButton searchButton;
    private JButton cancelButton;

    private MainFrame mainFrame;

    public static final String UNLIMITED_DEPTH = Translator.get("search_dialog.unlimited_depth");

    private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(550,0);

    private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(800,10000);

    public SearchDialog(MainFrame mainFrame) {
        super(mainFrame, ActionProperties.getActionLabel(FindAction.Descriptor.ACTION_ID), mainFrame);
        this.mainFrame = mainFrame;

        AbstractFile currentFolder = mainFrame.getActivePanel().getCurrentFolder();
        FileURL searchURL = SearchUtils.toSearchURL(currentFolder);

        Container contentPane = getContentPane();
        YBoxPanel mainPanel = new YBoxPanel(5);

        YBoxPanel fileSearchPanel = new YBoxPanel(10);
        fileSearchPanel.setBorder(BorderFactory.createTitledBorder(Translator.get("File search")));

        XAlignedComponentPanel compPanel = new XAlignedComponentPanel(5);

        String searchFor = PathUtils.removeLeadingSeparator(searchURL.getPath());
        searchFilesField = new JTextField(searchFor);
        searchFilesField.getDocument().addDocumentListener(this);
        compPanel.addRow(Translator.get("search_dialog.search_files"), searchFilesField, 5);

        wildcards = new JLabel(!lastMatchRegex ? Translator.get("search_dialog.wildcards") : " ");
        compPanel.addRow("", wildcards, 10);

        matchCase = new JCheckBox(Translator.get("search_dialog.case_sensitive"), lastMatchCase);
        compPanel.addRow("", matchCase, 10);

        matchRegex = new JCheckBox(Translator.get("search_dialog.matches_regexp"), lastMatchRegex);
        matchRegex.addChangeListener(e -> {
            AbstractButton b = (AbstractButton) e.getSource();
            wildcards.setText(!b.isSelected() ? Translator.get("search_dialog.wildcards") : " ");
        });
        compPanel.addRow("", matchRegex, 10);

        searchInField = new JTextField(searchURL.getHost());
        Border border = searchInField.getBorder();
        searchInField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent e) {
                cleanError();
                SearchDialog.this.removeUpdate(e);
            }
            @Override
            public void insertUpdate(DocumentEvent e) {
                cleanError();
                SearchDialog.this.insertUpdate(e);
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                cleanError();
                SearchDialog.this.changedUpdate(e);
            }
            private void cleanError() {
                searchInField.setBorder(border);
                searchInField.setToolTipText(null);
            }
        });
        compPanel.addRow(Translator.get("search_dialog.search_path"), searchInField, 10);

        searchSubfolders = new JCheckBox(Translator.get("search_dialog.search_subfolders"), lastSearchSubfolders);
        compPanel.addRow("", searchSubfolders, 10);

        searchArchives = new JCheckBox(Translator.get("search_dialog.search_archives"), lastSearchArchives);
        compPanel.addRow("", searchArchives, 10);

        searchHidden = new JCheckBox(Translator.get("search_dialog.search_hidden_files"), lastSearchHidden);
        compPanel.addRow("", searchHidden, 10);

        depth = new JSpinner();
        IntEditor editor = new IntEditor(depth, "#####", UNLIMITED_DEPTH);
        depth.setEditor(editor);
        depth.setModel(new SpinnerNumberModel(0, 0, null, 1));
        depth.setValue(lastDepth);
        compPanel.addRow(Translator.get("search_dialog.search_depth"), depth, 5);

        fileSearchPanel.add(compPanel);
        mainPanel.add(fileSearchPanel);
        mainPanel.addSpace(10);

        YBoxPanel textSearchPanel = new YBoxPanel(10);
        textSearchPanel.setBorder(BorderFactory.createTitledBorder(Translator.get("Text search (Optional)")));
        compPanel = new XAlignedComponentPanel(5);

        searchTextField = new JTextField(lastText);
        compPanel.addRow(Translator.get("search_dialog.search_text"), searchTextField, 10);

        textCase = new JCheckBox(Translator.get("search_dialog.text_case_sensitive"), lastTextCase);
        compPanel.addRow("", textCase, 10);

        textRegex = new JCheckBox(Translator.get("search_dialog.text_matches_regexp"), lastTextRegex);
        compPanel.addRow("", textRegex, 5);

        searchButton = new JButton(Translator.get("Find.label"));
        searchButton.setEnabled(false);
        cancelButton = new JButton(Translator.get("cancel"));

        textSearchPanel.add(compPanel);
        mainPanel.add(textSearchPanel);
        mainPanel.addSpace(10);

        contentPane.add(mainPanel, BorderLayout.CENTER);

        contentPane.add(DialogToolkit.createOKCancelPanel(searchButton, cancelButton, getRootPane(), this), BorderLayout.SOUTH);

        setInitialFocusComponent(searchFilesField);

        // Packs dialog
        setMinimumSize(MINIMUM_DIALOG_DIMENSION);
        setMaximumSize(MAXIMUM_DIALOG_DIMENSION);
    
        checkInputs();
        showDialog();
    }

    /**
     * Checks if search string or search location is empty (or white space), and enable/disable 'Add'
     * button accordingly, in order to prevent user from triggering a search with empty parameters.
     */
    private void checkInputs() {
        if (searchFilesField.getText().trim().isEmpty() || searchInField.getText().trim().isEmpty()) {
            if (searchButton.isEnabled())
                searchButton.setEnabled(false);
        } else {
            if (!searchButton.isEnabled())
                searchButton.setEnabled(true);
        }
    }

    ///////////////////////////
    // ActionListener method //
    ///////////////////////////

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == cancelButton) {
            dispose();
            return;
        }
        // otherwise, searchButton was pressed
        updateValues();
        String searchIn = searchInField.getText();
        AbstractFile file = FileFactory.getFile(searchIn);
        if (file == null || !file.exists()) {
            searchInField.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
            searchInField.setToolTipText(Translator.get("folder_does_not_exist"));
            return;
        }
        FileURL fileURL = SearchUtils.toSearchURL(file);
        fileURL.setPath(searchFilesField.getText());
        String searchQuery = getSearchQuery();
        if (!searchQuery.isEmpty())
            fileURL.setQuery(searchQuery);
        dispose();
        mainFrame.getActivePanel().tryChangeCurrentFolder(fileURL);
    }

    private void updateValues() {
        lastSearchSubfolders = searchSubfolders.isSelected();
        lastSearchArchives = searchArchives.isSelected();
        lastSearchHidden = searchHidden.isSelected();
        lastMatchCase = matchCase.isSelected();
        lastMatchRegex = matchRegex.isSelected();
        lastDepth = ((Number) this.depth.getValue()).intValue();
        lastText = searchTextField.getText();
        lastTextCase = textCase.isSelected();
        lastTextRegex = textRegex.isSelected();
    }

    /**
     * Return the properties of the search in the form of a query string
     * (yes, that of URLs) that includes only the properties that are
     * assigned with non-default values.
     * @return the properties of the search as a query string
     */
    private String getSearchQuery() {
        List<Pair<String, String>> properties = new ArrayList<>();
        if (!lastSearchSubfolders)
            properties.add(new Pair<>(SearchBuilder.SEARCH_SUBFOLDERS, Boolean.FALSE.toString()));
        if (lastSearchArchives)
            properties.add(new Pair<>(SearchBuilder.SEARCH_ARCHIVES, Boolean.TRUE.toString()));
        if (lastSearchHidden)
            properties.add(new Pair<>(SearchBuilder.SEARCH_HIDDEN, Boolean.TRUE.toString()));
        if (!lastMatchCase)
            properties.add(new Pair<>(SearchBuilder.MATCH_CASEINSENSITIVE, Boolean.TRUE.toString()));
        if (lastMatchRegex)
            properties.add(new Pair<>(SearchBuilder.MATCH_REGEX, Boolean.TRUE.toString()));
        if (lastDepth > 0)
            properties.add(new Pair<>(SearchBuilder.SEARCH_DEPTH, String.valueOf(lastDepth)));
        if (!lastText.isEmpty()) {
            properties.add(new Pair<>(SearchBuilder.SEARCH_TEXT, lastText));
            if (!lastTextCase)
                properties.add(new Pair<>(SearchBuilder.TEXT_CASEINSENSITIVE, Boolean.TRUE.toString()));
            if (lastTextRegex)
                properties.add(new Pair<>(SearchBuilder.TEXT_MATCH_REGEX, Boolean.TRUE.toString()));
        }
        return properties.stream()
                .map(pair -> pair.first + "=" + pair.second)
                .collect(Collectors.joining("&"));
    }

    //////////////////////////////
    // DocumentListener methods //
    //////////////////////////////

    @Override
    public void insertUpdate(DocumentEvent e) {
        checkInputs();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        checkInputs();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        checkInputs();
    }

}
