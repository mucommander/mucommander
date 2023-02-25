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
import java.util.stream.Stream;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
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
import com.mucommander.desktop.ActionType;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.ActionProperties;
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

    private JButton searchButton;
    private JButton cancelButton;

    private MainFrame mainFrame;

    public static final String UNLIMITED_DEPTH = Translator.get("search_dialog.unlimited_depth");
    public static final String MAX_THREADS = Translator.get("search_dialog.max_threads");

    private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(650,0);
    private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(1000,10000);
    private final static int MAX_NUM_OF_SEARCH_THREADS = 0x7fff; // taken from FormJoinPool#MAX_CAP

    public SearchDialog(MainFrame mainFrame) {
        super(mainFrame, ActionProperties.getActionLabel(ActionType.Find), mainFrame);
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

        boolean lastMatchRegex = Boolean.parseBoolean(SearchProperty.MATCH_REGEX.getValue());
        wildcards = new JLabel(!lastMatchRegex ? Translator.get("search_dialog.wildcards") : " ");
        compPanel.addRow("", wildcards, 10);

        GridBagConstraints gbc = ProportionalGridPanel.getDefaultGridBagConstraints();
        gbc.weightx = 1.0;
        JPanel groupingPanel = new ProportionalGridPanel(2, gbc);

        boolean lastMatchCase = Boolean.parseBoolean(SearchProperty.MATCH_CASESENSITIVE.getValue());
        matchCase = new JCheckBox(SearchProperty.MATCH_CASESENSITIVE.getTranslation(), lastMatchCase);
        groupingPanel.add(matchCase);

        matchRegex = new JCheckBox(SearchProperty.MATCH_REGEX.getTranslation(), lastMatchRegex);
        matchRegex.addChangeListener(e -> {
            AbstractButton b = (AbstractButton) e.getSource();
            wildcards.setText(!b.isSelected() ? Translator.get("search_dialog.wildcards") : " ");
        });
        groupingPanel.add(matchRegex);
        compPanel.addRow("", groupingPanel, 10);

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

        groupingPanel = new ProportionalGridPanel(2, gbc);

        boolean value = Boolean.parseBoolean(SearchProperty.SEARCH_IN_SUBFOLDERS.getValue());
        searchInSubfolders = new JCheckBox(SearchProperty.SEARCH_IN_SUBFOLDERS.getTranslation(), value);
        groupingPanel.add(searchInSubfolders);
        value = Boolean.parseBoolean(SearchProperty.SEARCH_FOR_SUBFOLDERS.getValue());
        searchForSubfolders = new JCheckBox(SearchProperty.SEARCH_FOR_SUBFOLDERS.getTranslation(), value);
        groupingPanel.add(searchForSubfolders);

        value = Boolean.parseBoolean(SearchProperty.SEARCH_IN_ARCHIVES.getValue());
        searchInArchives = new JCheckBox(SearchProperty.SEARCH_IN_ARCHIVES.getTranslation(), value);
        groupingPanel.add(searchInArchives);
        value = Boolean.parseBoolean(SearchProperty.SEARCH_FOR_ARCHIVES.getValue());
        searchForArchives = new JCheckBox(SearchProperty.SEARCH_FOR_ARCHIVES.getTranslation(), value);
        groupingPanel.add(searchForArchives);

        value = Boolean.parseBoolean(SearchProperty.SEARCH_IN_HIDDEN.getValue());
        searchInHidden = new JCheckBox(SearchProperty.SEARCH_IN_HIDDEN.getTranslation(), value);
        groupingPanel.add(searchInHidden);
        value = Boolean.parseBoolean(SearchProperty.SEARCH_FOR_HIDDEN.getValue());
        searchForHidden = new JCheckBox(SearchProperty.SEARCH_FOR_HIDDEN.getTranslation(), value);
        groupingPanel.add(searchForHidden);

        value = Boolean.parseBoolean(SearchProperty.SEARCH_IN_SYMLINKS.getValue());
        searchInSymlinks = new JCheckBox(SearchProperty.SEARCH_IN_SYMLINKS.getTranslation(), value);
        groupingPanel.add(searchInSymlinks);
        value = Boolean.parseBoolean(SearchProperty.SEARCH_FOR_SYMLINKS.getValue());
        searchForSymlinks = new JCheckBox(SearchProperty.SEARCH_FOR_SYMLINKS.getTranslation(), value);
        groupingPanel.add(searchForSymlinks);

        compPanel.addRow("", groupingPanel, 10);

        addSizePanel(compPanel);

        depth = new JSpinner();
        IntEditor editor = new IntEditor(depth, "#####", UNLIMITED_DEPTH);
        depth.setEditor(editor);
        depth.setModel(new SpinnerNumberModel(0, 0, null, 1));
        depth.setValue(Integer.parseInt(SearchProperty.SEARCH_DEPTH.getValue()));
        compPanel.addRow(SearchProperty.SEARCH_DEPTH.getTranslation(), depth, 5);

        threads = new JSpinner();
        editor = new IntEditor(threads, "#####", MAX_THREADS);
        threads.setEditor(editor);
        threads.setModel(new SpinnerNumberModel(0, 0, MAX_NUM_OF_SEARCH_THREADS, 1));
        threads.setValue(Integer.parseInt(SearchProperty.SEARCH_THREADS.getValue()));
        compPanel.addRow(SearchProperty.SEARCH_THREADS.getTranslation(), threads, 5);

        fileSearchPanel.add(compPanel);
        mainPanel.add(fileSearchPanel);
        mainPanel.addSpace(10);

        YBoxPanel textSearchPanel = new YBoxPanel(10);
        textSearchPanel.setBorder(BorderFactory.createTitledBorder(Translator.get("Text search (Optional)")));
        compPanel = new XAlignedComponentPanel(5);

        String lastText = SearchProperty.SEARCH_TEXT.getValue();
        searchTextField = new SelectAllOnFocusTextField(lastText);
        l = compPanel.addRow(SearchProperty.SEARCH_TEXT.getTranslation(), searchTextField, 10);
        l.setLabelFor(searchTextField);
        l.setDisplayedMnemonic('t');

        groupingPanel = new ProportionalGridPanel(2, gbc);
        value = Boolean.parseBoolean(SearchProperty.TEXT_CASESENSITIVE.getValue());
        textCase = new JCheckBox(SearchProperty.TEXT_CASESENSITIVE.getTranslation(), value);
        groupingPanel.add(textCase);

        value = Boolean.parseBoolean(SearchProperty.TEXT_MATCH_REGEX.getValue());
        textRegex = new JCheckBox(SearchProperty.TEXT_MATCH_REGEX.getTranslation(), value);
        groupingPanel.add(textRegex);
        compPanel.addRow("", groupingPanel, 5);

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
        var firstSizeClause = SearchProperty.SEARCH_SIZE.getValue();
        JPanel firstSizePanel = new JPanel(new FlowLayout());

        var firstSizeRelation = SearchUtils.getSizeRelation(firstSizeClause);
        firstSizeRel.setSelectedItem(firstSizeRelation);
        firstSizePanel.add(firstSizeRel);

        var firstSizeValue = SearchUtils.getSize(firstSizeClause);
        firstSize.setText(firstSizeValue != null ? firstSizeValue.toString() : "");
        firstSizePanel.add(firstSize);

        firstSizeUnit = new JComboBox<>(getSizeUnitDisplayStrings());
        firstSizeUnit.setSelectedIndex(SearchUtils.getSizeUnit(firstSizeClause).ordinal());
        firstSizePanel.add(firstSizeUnit);

        var secondSizeClause = SearchProperty.SEARCH_SIZE2.getValue();
        JPanel secondSizePanel = new JPanel(new FlowLayout());

        secondSizePanel.add(secondSizeRel);

        var secondSizeValue = SearchUtils.getSize(secondSizeClause);
        secondSize.setText(secondSizeValue != null ? secondSizeValue.toString() : "");
        secondSizePanel.add(secondSize);

        secondSizeUnit = new JComboBox<>(getSizeUnitDisplayStrings());
        secondSizeUnit.setSelectedIndex(SearchUtils.getSizeUnit(secondSizeClause).ordinal());
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

        JLabel sizeLabel = compPanel.addRow(SearchProperty.SEARCH_SIZE.getTranslation(), sizePanel, 20);
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
        var firstSizeRelation = (SizeRelation) firstSizeRel.getSelectedItem();
        var firstSizeUnit = SizeUnit.VALUES[this.firstSizeUnit.getSelectedIndex()];

        Long firstSize, secondSize;
        SizeRelation secondSizeRelation = null;
        SizeUnit secondSizeUnit = null;

        String size = this.firstSize.getText();
        if (StringUtils.isNullOrEmpty(size)) {
            firstSize = null;
            secondSize = null;
        } else {
            try {
                firstSize = Long.parseLong(size);
            } catch (NumberFormatException nfe) {
                InformationDialog.showErrorDialog(this, Translator.get("search_dialog.size_error"));
                this.firstSize.requestFocus();
                return false;
            }
            size = this.secondSize.getText();
            if (StringUtils.isNullOrEmpty(size))
                secondSize = null;
            else {
                try {
                    secondSize = Long.parseLong(size);
                } catch (NumberFormatException nfe) {
                    InformationDialog.showErrorDialog(this, Translator.get("search_dialog.size_error"));
                    this.secondSize.requestFocus();
                    return false;
                }
                secondSizeRelation = firstSizeRelation == SizeRelation.gt ? SizeRelation.lt : SizeRelation.gt;
                secondSizeUnit = SizeUnit.VALUES[this.secondSizeUnit.getSelectedIndex()];
            }
        }

        SearchProperty.SEARCH_IN_SUBFOLDERS.setValue(String.valueOf(searchInSubfolders.isSelected()));
        SearchProperty.SEARCH_IN_ARCHIVES.setValue(String.valueOf(searchInArchives.isSelected()));
        SearchProperty.SEARCH_IN_HIDDEN.setValue(String.valueOf(searchInHidden.isSelected()));
        SearchProperty.SEARCH_IN_SYMLINKS.setValue(String.valueOf(searchInSymlinks.isSelected()));
        SearchProperty.SEARCH_FOR_SUBFOLDERS.setValue(String.valueOf(searchForSubfolders.isSelected()));
        SearchProperty.SEARCH_FOR_ARCHIVES.setValue(String.valueOf(searchForArchives.isSelected()));
        SearchProperty.SEARCH_FOR_HIDDEN.setValue(String.valueOf(searchForHidden.isSelected()));
        SearchProperty.SEARCH_FOR_SYMLINKS.setValue(String.valueOf(searchForSymlinks.isSelected()));
        SearchProperty.MATCH_CASESENSITIVE.setValue(String.valueOf(matchCase.isSelected()));
        SearchProperty.MATCH_REGEX.setValue(String.valueOf(matchRegex.isSelected()));
        SearchProperty.SEARCH_DEPTH.setValue(String.valueOf(((Number) depth.getValue()).intValue()));
        SearchProperty.SEARCH_THREADS.setValue(String.valueOf(((Number) threads.getValue()).intValue()));
        SearchProperty.TEXT_CASESENSITIVE.setValue(String.valueOf(textCase.isSelected()));
        SearchProperty.TEXT_MATCH_REGEX.setValue(String.valueOf(textRegex.isSelected()));
        SearchProperty.SEARCH_SIZE.setValue(buildSeachSizeClause(firstSizeRelation, firstSize, firstSizeUnit));
        SearchProperty.SEARCH_SIZE2.setValue(buildSeachSizeClause(secondSizeRelation, secondSize, secondSizeUnit));
        SearchProperty.SEARCH_TEXT.setValue(searchTextField.getText());

        return true;
    }

    /**
     * Return the properties of the search in the form of a query string
     * (yes, that of URLs) that includes only the properties that are
     * assigned with non-default values.
     * @return the properties of the search as a query string
     */
    private String getSearchQuery() {
        return Stream.of(SearchProperty.values())
                .filter(property -> !property.isDefault())
                .map(SearchProperty::toString)
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
