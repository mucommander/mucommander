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

package com.mucommander.ui.dialog.file;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.file.util.FileSet;
import com.mucommander.file.util.FileToolkit;
import com.mucommander.io.security.MuProvider;
import com.mucommander.job.CalculateChecksumJob;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.CalculateChecksumAction;
import com.mucommander.ui.dialog.DialogToolkit;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.util.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * This dialog prepares a {@link com.mucommander.job.CalculateChecksumJob} and lets the user choose a checksum
 * algorithm, and a destination for the checksum file.
 *
 * @author Maxence Bernard
 */
public class CalculateChecksumDialog extends JobDialog implements ActionListener, ItemListener {

    private JComboBox algorithmComboBox;
    private JRadioButton specificLocationRadioButton;
    private JTextField specificLocationTextField;
    private JButton okButton;

    /** An instance of all MessageDigest implementations */
    private MessageDigest[] messageDigests;

    /** Default checksum algorithm (most commonly used) */
    private final static String DEFAULT_ALGORITHM = "MD5";

    /** Last algorithm used, saved after validation of this dialog */
    private static String lastUsedAlgorithm = DEFAULT_ALGORITHM;

    /** Dialog size constraints */
    private final static Dimension MINIMUM_DIALOG_DIMENSION = new Dimension(320,0);


    static {
        // Register additional MessageDigest implementations provided by the muCommander API
        MuProvider.registerProvider();
    }


    public CalculateChecksumDialog(MainFrame mainFrame, FileSet files) {
        super(mainFrame, Translator.get(CalculateChecksumAction.class.getName()+".label"), files);

        YBoxPanel mainPanel = new YBoxPanel();

        // Retrieve all MessageDigest instances and sort them by alphabetical order of their algorithm

        // Create a TreeSet with a custom Comparator
        SortedSet algorithmSortedSet = new TreeSet(new Comparator() {
                    public int compare(Object o1, Object o2) {
                        return ((MessageDigest)o1).getAlgorithm().compareTo(((MessageDigest)o2).getAlgorithm());
                    }
                });

        // Add all MessageDigest to the TreeSet
        Iterator algoIter = Security.getAlgorithms("MessageDigest").iterator();
        while(algoIter.hasNext()) {
            try {
                algorithmSortedSet.add(MessageDigest.getInstance((String)algoIter.next()));
            }
            catch(NoSuchAlgorithmException e) {
                // Should never happen and if it ever does, the digest will simply be discarded
            }
        }

        // Convert the sorted set into an array
        messageDigests = new MessageDigest[algorithmSortedSet.size()];
        algorithmSortedSet.toArray(messageDigests);

        // Add the sorted list of algorithms to a combo box to let the user choose one
        algorithmComboBox = new JComboBox();
        for(int i=0; i<messageDigests.length; i++)
            algorithmComboBox.addItem(messageDigests[i].getAlgorithm());

        // Select the last used algorithm (if any), or the default algorithm
        algorithmComboBox.setSelectedItem(lastUsedAlgorithm);
        algorithmComboBox.addItemListener(this);

        FlowLayout flowLayout = new FlowLayout(FlowLayout.LEADING, 0, 0);
        JPanel tempPanel = new JPanel(flowLayout);
        tempPanel.add(new JLabel(Translator.get("calculate_checksum_dialog.checksum_algorithm")+" : "));
        tempPanel.add(algorithmComboBox);

        mainPanel.add(tempPanel);
        mainPanel.addSpace(10);

        // Create the components that allow to choose where the checksum file should be created

        mainPanel.add(new JLabel(Translator.get("destination")+" :"));
        mainPanel.addSpace(5);

        JRadioButton tempLocationRadioButton = new JRadioButton(Translator.get("calculate_checksum_dialog.temporary_file"), true);
        mainPanel.add(tempLocationRadioButton);

        specificLocationRadioButton = new JRadioButton("", false);
        tempPanel = new JPanel(new BorderLayout());
        tempPanel.add(specificLocationRadioButton, BorderLayout.WEST);
        specificLocationRadioButton.addItemListener(this);
        
        specificLocationTextField = new JTextField(getChecksumFilename(lastUsedAlgorithm));
        specificLocationTextField.setEnabled(false);
        tempPanel.add(specificLocationTextField, BorderLayout.CENTER);

        JPanel tempPanel2 = new JPanel(new BorderLayout());
        tempPanel2.add(tempPanel, BorderLayout.NORTH);
        mainPanel.add(tempPanel2);

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(tempLocationRadioButton);
        buttonGroup.add(specificLocationRadioButton);

        // Create file details button and OK/cancel buttons and lay them out a single row

        JPanel fileDetailsPanel = createFileDetailsPanel();

        okButton = new JButton(Translator.get("ok"));
        JButton cancelButton = new JButton(Translator.get("cancel"));

        mainPanel.add(createButtonsPanel(createFileDetailsButton(fileDetailsPanel),
                DialogToolkit.createOKCancelPanel(okButton, cancelButton, getRootPane(), this)));

        mainPanel.add(fileDetailsPanel);
        
        getContentPane().add(mainPanel);

        // Give initial keyboard focus to the 'Delete' button
        setInitialFocusComponent(algorithmComboBox);

        // Call dispose() when dialog is closed
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Size dialog and show it to the screen
        setMinimumSize(MINIMUM_DIALOG_DIMENSION);
        setResizable(true);
        showDialog();
    }

    /**
     * Returns the MessageDigest instance corresponding to the currently selected algorithm.
     *
     * @return the MessageDigest instance corresponding to the currently selected algorithm.
     */
    private MessageDigest getSelectedMessageDigest() {
        return messageDigests[algorithmComboBox.getSelectedIndex()];
    }

    /**
     * Returns a de-facto standard filename for the specified checksum algorithm, e.g. <code>MD5SUMS</code> for
     * <code>md5</code>.
     *
     * @param algorithm a checksum algorithm
     * @return a standard filename for the specified checksum algorithm
     */
    private String getChecksumFilename(String algorithm) {
        // Adler32 -> ADLER32SUMS
        // CRC32   -> <filename>.sfv    (needs special treatment)
        // MD2     -> MD2SUMS
        // MD4     -> MD4SUMS
        // MD5     -> MD5SUMS
        // SHA     -> SHA1SUMS          (needs special treatment)
        // SHA-256 -> SHA256SUMS
        // SHA-384 -> SHA384SUMS
        // SHA-512 -> SHA512SUMS

        algorithm = algorithm.toUpperCase();

        if(algorithm.equals("SHA"))
            return "SHA1SUMS";

        if(algorithm.equals("CRC32"))
            return (files.size()==1?files.fileAt(0):files.getBaseFolder()).getName()+".sfv";

        return StringUtils.replaceCompat(algorithm, "-", "")+"SUMS";
    }


    ///////////////////////////////////
    // ActionListener implementation //
    ///////////////////////////////////

    public void actionPerformed(ActionEvent e) {
        // Start by disposing this dialog
        dispose();

        if(e.getSource()==okButton) {
            try {
                MessageDigest digest = getSelectedMessageDigest();
                String algorithm = digest.getAlgorithm();
                AbstractFile checksumFile;

                // Resolve the destination checksum file

                if(specificLocationRadioButton.isSelected()) {
                    // User-defined checksum file
                    String enteredPath = specificLocationTextField.getText();

                    Object ret[] = FileToolkit.resolvePath(enteredPath, mainFrame.getActiveTable().getCurrentFolder());
                    // The path entered doesn't correspond to any existing folder
                    if (ret==null) {
                        showErrorDialog(Translator.get("invalid_path", enteredPath));
                        return;
                    }

                    AbstractFile destFolder = (AbstractFile)ret[0];
                    String filename = (String)ret[1];

                    if(filename==null)
                        filename = getChecksumFilename(algorithm);

                    checksumFile = destFolder.getDirectChild(filename);
                }
                else {
                    // Temporary file
                    checksumFile = FileFactory.getTemporaryFile(getChecksumFilename(algorithm), true);
                }

                // Save the algorithm that was used for the next time this dialog is invoked
                lastUsedAlgorithm = algorithm; 

                // Start processing files
                ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("properties_dialog.calculating"));
                CalculateChecksumJob job = new CalculateChecksumJob(progressDialog, mainFrame, files, checksumFile, digest);
                progressDialog.start(job);
            }
            catch(IOException ex) {
                // Note: FileFactory.getTemporaryFile() should never throw an IOException

                showErrorDialog(Translator.get("invalid_path", specificLocationTextField.getText()));
                return;
            }
        }
    }


    /////////////////////////////////
    // ItemListener implementation //
    /////////////////////////////////

    public void itemStateChanged(ItemEvent e) {
        Object source = e.getSource();

        if(source==specificLocationRadioButton) {
            // Enables/disables the text field when the corresponding radio button's selected state has changed.
            specificLocationTextField.setEnabled(specificLocationRadioButton.isSelected());
            specificLocationTextField.requestFocus();
        }
        else if(source==algorithmComboBox) {
            specificLocationTextField.setText(getChecksumFilename(getSelectedMessageDigest().getAlgorithm()));
        }
    }
}
