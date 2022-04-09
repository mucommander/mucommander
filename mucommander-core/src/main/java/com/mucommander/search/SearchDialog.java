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

import static com.mucommander.search.SearchUtils.buildSeachSizeClause;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.util.PathUtils;
import com.mucommander.commons.util.Pair;
import com.mucommander.commons.util.StringUtils;
import com.mucommander.commons.util.ui.dialog.DialogToolkit;
import com.mucommander.commons.util.ui.dialog.FocusDialog;
import com.mucommander.commons.util.ui.layout.ProportionalGridPanel;
import com.mucommander.commons.util.ui.layout.XAlignedComponentPanel;
import com.mucommander.commons.util.ui.layout.YBoxPanel;
import com.mucommander.commons.util.ui.spinner.IntEditor;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.action.impl.FindAction;
import com.mucommander.ui.dialog.InformationDialog;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.text.SelectAllOnFocusTextField;

/**
 * Dialog used to set parameters for file searching.
 *
 * @author Arik Hadas, Gerolf Scherr
 */
public class SearchDialog extends FocusDialog implements ActionListener, DocumentListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchDialog.class);

    private JTextField searchFilesField;
    private JTextField searchInField;
    private JCheckBox searchInSubfolders;
    private JCheckBox searchInArchives;
    private JCheckBox searchInHidden;
    private JCheckBox searchInSymlinks;
    private JCheckBox searchForSubfolders;
    private JCheckBox searchForArchives;
    private JCheckBox searchForHidden;
    private JCheckBox searchForSymlinks;
    private JCheckBox matchCase;
    private JCheckBox matchRegex;
    private JSpinner depth;
    private JSpinner threads;
    private JLabel wildcards;
    private JTextField searchTextField;
    private JCheckBox textCase;
    private JCheckBox textRegex;

    private JComboBox<SizeRelation> firstSizeRel = new JComboBox<>(SizeRelation.values());
    private JComboBox<String> firstSizeUnit;
    private JTextField firstSize = new SelectAllOnFocusTextField(8);
    private JLabel secondSizeRel = new JLabel();
    private JComboBox<String> secondSizeUnit;
    private JTextField secondSize = new SelectAllOnFocusTextField(8);

    // Store last values, initialized to the default values
    private static boolean lastSearchInSubfolders = true;
    private static boolean lastSearchInArchives;
    private static boolean lastSearchInHidden;
    private static boolean lastSearchInSymlinks;
    private static boolean lastSearchForSubfolders = true;
    private static boolean lastSearchForArchives = true;
    private static boolean lastSearchForHidden = true;
    private static boolean lastSearchForSymlinks = true;
    private static boolean lastMatchCase;
    private static boolean lastMatchRegex;
    private static int lastDepth = 0;
    private static int lastThreads = SearchBuilder.DEFAULT_THREADS;
    private static String lastText = "";
    private static boolean lastTextCase;
    private static boolean lastTextRegex;
    private static Long lastFirstSize;
    private static SizeRelation lastFirstSizeRel = SizeRelation.eq;
    private static SizeUnit lastFirstSizeUnit = SizeUnit.kB;
    private static Long lastSecondSize;
    private static SizeRelation lastSecondSizeRel = SizeRelation.gt;
    private static SizeUnit lastSecondSizeUnit = SizeUnit.kB;

    private JButton searchButton;
    private JButton cancelButton;

    private MainFrame mainFrame;

    public static final String UNLIMITED_DEPTH = Translator.get("search_dialog.unlimited_depth");
    public static final String MAX_THREADS = Translator.get("search_dialog.max_threads");

    private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(650,0);
    private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(1000,10000);
    private final static int MAX_NUM_OF_SEARCH_THREADS = 0x7fff; // taken from FormJoinPool#MAX_CAP

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
        if (searchFor.isEmpty())
            searchFor = "*";
        searchFilesField = new SelectAllOnFocusTextField(searchFor);
        searchFilesField.getDocument().addDocumentListener(this);
        JLabel l = compPanel.addRow(Translator.get("search_dialog.search_files"), searchFilesField, 5);
        l.setLabelFor(searchFilesField);
        l.setDisplayedMnemonic('n');

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

        l = compPanel.addRow(Translator.get("search_dialog.search_path"), searchInField, 10);
        l.setLabelFor(searchInField);
        l.setDisplayedMnemonic('p');

        GridBagConstraints gbc = ProportionalGridPanel.getDefaultGridBagConstraints();
        gbc.weightx = 1.0;
        JPanel groupingPanel = new ProportionalGridPanel(2, gbc);

        searchInSubfolders = new JCheckBox(Translator.get("search_dialog.search_in_subfolders"), lastSearchInSubfolders);
        groupingPanel.add(searchInSubfolders);
        searchForSubfolders = new JCheckBox(Translator.get("search_dialog.search_for_folders"), lastSearchForSubfolders);
        groupingPanel.add(searchForSubfolders);

        searchInArchives = new JCheckBox(Translator.get("search_dialog.search_in_archives"), lastSearchInArchives);
        groupingPanel.add(searchInArchives);
        searchForArchives = new JCheckBox(Translator.get("search_dialog.search_for_archives"), lastSearchForArchives);
        groupingPanel.add(searchForArchives);

        searchInHidden = new JCheckBox(Translator.get("search_dialog.search_in_hidden_files"), lastSearchInHidden);
        groupingPanel.add(searchInHidden);
        searchForHidden = new JCheckBox(Translator.get("search_dialog.search_for_hidden_files"), lastSearchForHidden);
        groupingPanel.add(searchForHidden);

        searchInSymlinks = new JCheckBox(Translator.get("search_dialog.search_in_symlinks"), lastSearchInSymlinks);
        groupingPanel.add(searchInSymlinks);
        searchForSymlinks = new JCheckBox(Translator.get("search_dialog.search_for_symlinks"), lastSearchForSymlinks);
        groupingPanel.add(searchForSymlinks);

        compPanel.addRow("", groupingPanel, 10);

        addSizePanel(compPanel);

        depth = new JSpinner();
        IntEditor editor = new IntEditor(depth, "#####", UNLIMITED_DEPTH);
        depth.setEditor(editor);
        depth.setModel(new SpinnerNumberModel(0, 0, null, 1));
        depth.setValue(lastDepth);
        compPanel.addRow(Translator.get("search_dialog.search_depth"), depth, 5);

        threads = new JSpinner();
        editor = new IntEditor(threads, "#####", MAX_THREADS);
        threads.setEditor(editor);
        threads.setModel(new SpinnerNumberModel(0, 0, MAX_NUM_OF_SEARCH_THREADS, 1));
        threads.setValue(lastThreads);
        compPanel.addRow(Translator.get("search_dialog.search_threads"), threads, 5);

        fileSearchPanel.add(compPanel);
        mainPanel.add(fileSearchPanel);
        mainPanel.addSpace(10);

        YBoxPanel textSearchPanel = new YBoxPanel(10);
        textSearchPanel.setBorder(BorderFactory.createTitledBorder(Translator.get("Text search (Optional)")));
        compPanel = new XAlignedComponentPanel(5);

        searchTextField = new SelectAllOnFocusTextField(lastText);
        l = compPanel.addRow(Translator.get("search_dialog.search_text"), searchTextField, 10);
        l.setLabelFor(searchTextField);
        l.setDisplayedMnemonic('t');

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

    void addSizePanel(XAlignedComponentPanel compPanel) {
        JPanel firstSizePanel = new JPanel(new FlowLayout());
        firstSizeRel.setSelectedItem(lastFirstSizeRel);
        firstSizePanel.add(firstSizeRel);

        firstSize.setText(lastFirstSize == null ? "" : lastFirstSize.toString());
        firstSizePanel.add(firstSize);

        firstSizeUnit = new JComboBox<>(getSizeUnitDisplayStrings());
        firstSizeUnit.setSelectedIndex(lastFirstSizeUnit.ordinal());
        firstSizePanel.add(firstSizeUnit);

        JPanel secondSizePanel = new JPanel(new FlowLayout());
        secondSizePanel.add(secondSizeRel);

        secondSize.setText(lastSecondSize == null ? "" : lastSecondSize.toString());
        secondSizePanel.add(secondSize);

        secondSizeUnit = new JComboBox<>(getSizeUnitDisplayStrings());
        secondSizeUnit.setSelectedIndex(lastSecondSizeUnit.ordinal());
        secondSizePanel.add(secondSizeUnit);

        Runnable updater = () -> {
            if (firstSizeRel.getSelectedItem() == SizeRelation.eq || firstSize.getText().trim().isEmpty()) {
                secondSizePanel.setVisible(false);
                secondSize.setText("");
            }
            else {
                secondSizePanel.setVisible(true);
                SizeRelation item = firstSizeRel.getSelectedItem() == SizeRelation.lt ? SizeRelation.gt : SizeRelation.lt;
                secondSizeRel.setText(item.toString());
            }
        };

        firstSizeRel.addItemListener(e -> updater.run());
        firstSize.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent e) {
                updater.run();
            }
            @Override
            public void insertUpdate(DocumentEvent e) {
                updater.run();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });

        updater.run();

        JPanel combinedSizesPanel = new JPanel();
        combinedSizesPanel.add(firstSizePanel);
        combinedSizesPanel.add(secondSizePanel);
        JPanel sizePanel = new JPanel(new BorderLayout());
        sizePanel.add(combinedSizesPanel, BorderLayout.WEST);

        JLabel sizeLabel = compPanel.addRow(Translator.get("search_dialog.size", "Size"), sizePanel, 20);
        sizeLabel.setDisplayedMnemonic('s');
        sizeLabel.setLabelFor(firstSizeRel);
    }


    private String[] getSizeUnitDisplayStrings() {
        return Arrays.stream(SizeUnit.VALUES)
                .map(s -> String.format("search_dialog.size_unit.%s", s.name()))
                .map(Translator::get)
                .toArray(String[]::new);
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
        if (!validateAndUpdateValues())
            return;

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

    /**
     *
     * @return true on success, false on input validation error
     */
    private boolean validateAndUpdateValues() {
        lastSearchInSubfolders = searchInSubfolders.isSelected();
        lastSearchInArchives = searchInArchives.isSelected();
        lastSearchInHidden = searchInHidden.isSelected();
        lastSearchInSymlinks = searchInSymlinks.isSelected();
        lastSearchForSubfolders = searchForSubfolders.isSelected();
        lastSearchForArchives = searchForArchives.isSelected();
        lastSearchForHidden = searchForHidden.isSelected();
        lastSearchForSymlinks = searchForSymlinks.isSelected();
        lastMatchCase = matchCase.isSelected();
        lastMatchRegex = matchRegex.isSelected();
        lastDepth = ((Number) depth.getValue()).intValue();
        lastThreads = ((Number) threads.getValue()).intValue();
        lastText = searchTextField.getText();
        lastTextCase = textCase.isSelected();
        lastTextRegex = textRegex.isSelected();

        lastFirstSizeRel = (SizeRelation) firstSizeRel.getSelectedItem();
        lastFirstSizeUnit = SizeUnit.VALUES[firstSizeUnit.getSelectedIndex()];

        String size = firstSize.getText();
        if (StringUtils.isNullOrEmpty(size)) {
            lastFirstSize = null;
            lastSecondSize = null;
        } else {
            try {
                lastFirstSize = Long.parseLong(size);
            } catch (NumberFormatException nfe) {
                InformationDialog.showErrorDialog(this, Translator.get("search_dialog.size_error"));
                firstSize.requestFocus();
                return false;
            }
            size = secondSize.getText();
            if (StringUtils.isNullOrEmpty(size))
                lastSecondSize = null;
            else {
                try {
                    lastSecondSize = Long.parseLong(size);
                } catch (NumberFormatException nfe) {
                    InformationDialog.showErrorDialog(this, Translator.get("search_dialog.size_error"));
                    secondSize.requestFocus();
                    return false;
                }
                lastSecondSizeRel = lastFirstSizeRel == SizeRelation.gt ? SizeRelation.lt : SizeRelation.gt;
                lastSecondSizeUnit = SizeUnit.VALUES[secondSizeUnit.getSelectedIndex()];
            }
        }

        return true;
    }

    /**
     * Return the properties of the search in the form of a query string
     * (yes, that of URLs) that includes only the properties that are
     * assigned with non-default values.
     * @return the properties of the search as a query string
     */
    private String getSearchQuery() {
        List<Pair<String, String>> properties = new ArrayList<>();
        if (!lastSearchInSubfolders)
            properties.add(new Pair<>(SearchBuilder.SEARCH_IN_SUBFOLDERS, Boolean.FALSE.toString()));
        if (lastSearchInArchives)
            properties.add(new Pair<>(SearchBuilder.SEARCH_IN_ARCHIVES, Boolean.TRUE.toString()));
        if (lastSearchInHidden)
            properties.add(new Pair<>(SearchBuilder.SEARCH_IN_HIDDEN, Boolean.TRUE.toString()));
        if (lastSearchInSymlinks)
            properties.add(new Pair<>(SearchBuilder.SEARCH_IN_SYMLINKS, Boolean.TRUE.toString()));
        if (!lastSearchForSubfolders)
            properties.add(new Pair<>(SearchBuilder.SEARCH_FOR_SUBFOLDERS, Boolean.FALSE.toString()));
        if (!lastSearchForArchives)
            properties.add(new Pair<>(SearchBuilder.SEARCH_FOR_ARCHIVES, Boolean.FALSE.toString()));
        if (!lastSearchForHidden)
            properties.add(new Pair<>(SearchBuilder.SEARCH_FOR_HIDDEN, Boolean.FALSE.toString()));
        if (!lastSearchForSymlinks)
            properties.add(new Pair<>(SearchBuilder.SEARCH_FOR_SYMLINKS, Boolean.FALSE.toString()));
        if (lastMatchCase)
            properties.add(new Pair<>(SearchBuilder.MATCH_CASESENSITIVE, Boolean.TRUE.toString()));
        if (lastMatchRegex)
            properties.add(new Pair<>(SearchBuilder.MATCH_REGEX, Boolean.TRUE.toString()));
        if (lastDepth > 0)
            properties.add(new Pair<>(SearchBuilder.SEARCH_DEPTH, String.valueOf(lastDepth)));
        if (lastThreads != SearchBuilder.DEFAULT_THREADS)
            properties.add(new Pair<>(SearchBuilder.SEARCH_THREADS, String.valueOf(lastThreads)));
        if (!lastText.isEmpty()) {
            properties.add(new Pair<>(SearchBuilder.SEARCH_TEXT, lastText));
            if (lastTextCase)
                properties.add(new Pair<>(SearchBuilder.TEXT_CASESENSITIVE, Boolean.TRUE.toString()));
            if (lastTextRegex)
                properties.add(new Pair<>(SearchBuilder.TEXT_MATCH_REGEX, Boolean.TRUE.toString()));
        }
        if (lastFirstSize != null) {
            properties.add(new Pair<>(SearchBuilder.SEARCH_SIZE, buildSeachSizeClause(lastFirstSizeRel, lastFirstSize, lastFirstSizeUnit)));
            if (lastSecondSize != null)
                properties.add(new Pair<>(SearchBuilder.SEARCH_SIZE, buildSeachSizeClause(lastSecondSizeRel, lastSecondSize, lastSecondSizeUnit)));
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
