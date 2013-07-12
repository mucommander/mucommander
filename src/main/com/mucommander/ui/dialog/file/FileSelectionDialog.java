/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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
import java.util.regex.PatternSyntaxException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.filter.AndFileFilter;
import com.mucommander.commons.file.filter.AttributeFileFilter;
import com.mucommander.commons.file.filter.AttributeFileFilter.FileAttribute;
import com.mucommander.commons.file.filter.ContainsFilenameFilter;
import com.mucommander.commons.file.filter.EndsWithFilenameFilter;
import com.mucommander.commons.file.filter.EqualsFilenameFilter;
import com.mucommander.commons.file.filter.FileFilter;
import com.mucommander.commons.file.filter.PassThroughFileFilter;
import com.mucommander.commons.file.filter.RegexpFilenameFilter;
import com.mucommander.commons.file.filter.StartsWithFilenameFilter;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.DialogToolkit;
import com.mucommander.ui.dialog.FocusDialog;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.table.FileTable;

/**
 * This dialog allows the user to add (mark) or remove (unmark) files from the current selection,
 * based on a match criterium and string.
 *
 * @author Maxence Bernard
 */
public class FileSelectionDialog extends FocusDialog implements ActionListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(FileSelectionDialog.class);
	
    /* Filename comparison criteria */		
    private final static int CONTAINS    = 0;
    private final static int STARTS_WITH = 1;
    private final static int ENDS_WIDTH  = 2;
    private final static int IS          = 3;
    private final static int REGEXP      = 4;

    /** Add to or remove from selection ? */	 
    private boolean addToSelection;

    private JComboBox comparisonComboBox;
    private JTextField selectionField;

    private JCheckBox caseSensitiveCheckBox;
    private JCheckBox includeFoldersCheckBox;

    private JButton okButton;

    private MainFrame mainFrame;
	
    /** 
     * Is selection case sensitive? (initially false)
     * <br>Note: this field is static so the value is kept after the dialog is OKed.
     */ 
    private static boolean caseSensitive = false;

    /** 
     * Does the selection include folders? (initially false)
     * <br>Note: this field is static so the value is kept after the dialog is OKed.
     */ 
    private static boolean includeFolders = false;
	
    /** 
     * Filename comparison: contains, starts with, ends with, is ?
     * <br>Note: this field is static so the value is kept after the dialog is OKed.
     */ 
    private static int comparison = CONTAINS;

    /** 
     * Keyword which has last been typed to mark or unmark files.
     * <br>Note: this field is static so the value is kept after the dialog is OKed.
     */ 
    private static String keywordString = "*";
	

    private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(320,0);	
    private final static Dimension MAXIMUM_DIALOG_DIMENSION = new Dimension(400,10000);	


    /**
     * Creates a new 'mark' or 'unmark' dialog.
     *
     * @param addToSelection if <true>, files matching
     */
    public FileSelectionDialog(MainFrame mainFrame, boolean addToSelection) {

        super(mainFrame, Translator.get(addToSelection?"file_selection_dialog.mark":"file_selection_dialog.unmark"), mainFrame);
	
        this.mainFrame = mainFrame;
        this.addToSelection = addToSelection;

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        YBoxPanel northPanel = new YBoxPanel(5);
        JLabel label = new JLabel(Translator.get(addToSelection?"file_selection_dialog.mark_description":"file_selection_dialog.unmark_description")+" :");
        northPanel.add(label);

        JPanel tempPanel = new JPanel();
        tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));
        comparisonComboBox = new JComboBox();
        comparisonComboBox.addItem(Translator.get("file_selection_dialog.contains"));
        comparisonComboBox.addItem(Translator.get("file_selection_dialog.starts_with"));
        comparisonComboBox.addItem(Translator.get("file_selection_dialog.ends_with"));
        comparisonComboBox.addItem(Translator.get("file_selection_dialog.is"));
        comparisonComboBox.addItem(Translator.get("file_selection_dialog.matches_regexp"));
        comparisonComboBox.setSelectedIndex(comparison);
        tempPanel.add(comparisonComboBox);
				
        // selectionField is initialized with last textfield's value (if any)
        selectionField = new JTextField(keywordString);
        selectionField.addActionListener(this);
        selectionField.setSelectionStart(0);
        selectionField.setSelectionEnd(keywordString.length());
        tempPanel.add(selectionField);
        northPanel.add(tempPanel);

        // Add some vertical space
        northPanel.addSpace(10);
		
        caseSensitiveCheckBox = new JCheckBox(Translator.get("file_selection_dialog.case_sensitive"), caseSensitive);
        northPanel.add(caseSensitiveCheckBox);

        includeFoldersCheckBox = new JCheckBox(Translator.get("file_selection_dialog.include_folders"), includeFolders);
        northPanel.add(includeFoldersCheckBox);
		
        northPanel.addSpace(10);
        northPanel.add(Box.createVerticalGlue());

        contentPane.add(northPanel, BorderLayout.NORTH);

        okButton = new JButton(Translator.get(addToSelection?"file_selection_dialog.mark":"file_selection_dialog.unmark"));
        contentPane.add(DialogToolkit.createOKCancelPanel(okButton, new JButton(Translator.get("cancel")), getRootPane(), this), BorderLayout.SOUTH);

        // Selection field receives initial keyboard focus
        setInitialFocusComponent(selectionField);

        setMinimumSize(MINIMUM_DIALOG_DIMENSION);
        setMaximumSize(MAXIMUM_DIALOG_DIMENSION);
    }


    ////////////////////////////
    // ActionListener methods //
    ////////////////////////////
	
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        FileTable activeTable = mainFrame.getActiveTable();

        // Action coming from the selection dialog
        if ((source==okButton || source==selectionField)) {
            // Save values for next time this dialog is invoked
            caseSensitive = caseSensitiveCheckBox.isSelected();
            includeFolders = includeFoldersCheckBox.isSelected();
            comparison = comparisonComboBox.getSelectedIndex();


            String testString;
            keywordString = selectionField.getText();
            if(comparison!=REGEXP) {
                // Remove '*' characters
                testString = keywordString.replace("*", "");
            }
            else {
                testString = keywordString;
            }

            // Instantiate the main file filter
            FileFilter filter;
            switch (comparison) {
                case CONTAINS:
                    filter = new ContainsFilenameFilter(testString, caseSensitive);
                    break;
                case STARTS_WITH:
                    filter = new StartsWithFilenameFilter(testString, caseSensitive);
                    break;
                case ENDS_WIDTH:
                    filter = new EndsWithFilenameFilter(testString, caseSensitive);
                    break;
                case IS:
                    filter = new EqualsFilenameFilter(testString, caseSensitive);
                    break;
                case REGEXP:
                default:
                    try {
                        filter = new RegexpFilenameFilter(testString, caseSensitive);
                    }
                    catch(PatternSyntaxException ex) {
                        // Todo: let the user know the regexp is invalid
                        LOGGER.debug("Invalid regexp", ex);

                        // This filter does match any file
                        filter = new PassThroughFileFilter(false);
                    }
                    break;
            }

            // If folders are excluded, add a regular file filter and chain it with an AndFileFilter
            if(!includeFolders) {
                filter = new AndFileFilter(
                    new AttributeFileFilter(FileAttribute.FILE),
                    filter
                );
            }

            // Mark/unmark the files using the filter
            activeTable.getFileTableModel().setFilesMarked(filter, addToSelection);

            // Notify registered listeners that currently marked files have changed on this FileTable
            activeTable.fireMarkedFilesChangedEvent();

            activeTable.repaint();
        }
		
        dispose();
    }

}
