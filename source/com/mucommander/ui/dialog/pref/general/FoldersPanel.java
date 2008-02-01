/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
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

import com.mucommander.conf.impl.MuConfiguration;
import com.mucommander.runtime.OsFamilies;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.pref.PreferencesDialog;
import com.mucommander.ui.dialog.pref.PreferencesPanel;
import com.mucommander.ui.layout.XBoxPanel;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.main.WindowManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;


/**
 * 'Folders' preferences panel.
 *
 * @author Maxence Bernard, Mariuz Jakubowski
 */
class FoldersPanel extends PreferencesPanel implements ItemListener, KeyListener, ActionListener {

    // Startup folders
    private JRadioButton leftLastFolderRadioButton;
    private JRadioButton leftCustomFolderRadioButton;
    private JTextField leftCustomFolderTextField;
    private JButton leftCustomFolderButton;
	
    private JRadioButton rightLastFolderRadioButton;
    private JRadioButton rightCustomFolderRadioButton;
    private JTextField rightCustomFolderTextField;
	private JButton rightCustomFolderButton;

    // Show hidden files?
    private JCheckBox showHiddenFilesCheckBox;

    // Show Mac OS X .DS_Store?
    private JCheckBox showDSStoreFilesCheckBox;

    // Show Mac OS X system folders ?
    private JCheckBox showSystemFoldersCheckBox;

    // Display compact file size ?
    private JCheckBox compactSizeCheckBox;
	
    // Follow symlinks when changing directory ?
    private JCheckBox followSymlinksCheckBox;

    public FoldersPanel(PreferencesDialog parent) {
        super(parent, Translator.get("prefs_dialog.folders_tab"));

        setLayout(new BorderLayout());

        XBoxPanel tempPanel;

        // Startup folders panel
        YBoxPanel startupFolderPanel = new YBoxPanel();
        startupFolderPanel.setBorder(BorderFactory.createTitledBorder(Translator.get("prefs_dialog.startup_folders")));
				
        // Left folder
        JLabel leftFolderLabel = new JLabel(Translator.get("prefs_dialog.left_folder"));
        leftFolderLabel.setAlignmentX(LEFT_ALIGNMENT);
        startupFolderPanel.add(leftFolderLabel);
        startupFolderPanel.addSpace(5);
		
        tempPanel = new XBoxPanel(5);
        tempPanel.setAlignmentX(LEFT_ALIGNMENT);
        leftLastFolderRadioButton = new JRadioButton(Translator.get("prefs_dialog.last_folder"));
        tempPanel.add(leftLastFolderRadioButton);
        startupFolderPanel.add(tempPanel);
		
        leftCustomFolderRadioButton = new JRadioButton(Translator.get("prefs_dialog.custom_folder"));
        tempPanel = new XBoxPanel(5);
        tempPanel.setAlignmentX(LEFT_ALIGNMENT);
        tempPanel.add(leftCustomFolderRadioButton);
        tempPanel.addSpace(5);
        leftCustomFolderTextField = new JTextField(MuConfiguration.getVariable(MuConfiguration.LEFT_CUSTOM_FOLDER, ""));
        leftCustomFolderTextField.addKeyListener(this);
        tempPanel.add(leftCustomFolderTextField);

        leftCustomFolderButton = new JButton("...");
        leftCustomFolderButton.addActionListener(this);
        tempPanel.add(leftCustomFolderButton);
        startupFolderPanel.add(tempPanel);

        if(MuConfiguration.getVariable(MuConfiguration.LEFT_STARTUP_FOLDER, "").equals(MuConfiguration.STARTUP_FOLDER_LAST)) {
            leftLastFolderRadioButton.setSelected(true);
            setCustomFolderComponentsEnabled(true, false);
        }
        else
            leftCustomFolderRadioButton.setSelected(true);

        leftCustomFolderRadioButton.addItemListener(this);

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(leftLastFolderRadioButton);
        buttonGroup.add(leftCustomFolderRadioButton);

        startupFolderPanel.addSpace(15);

        // Right folder
        JLabel rightFolderLabel = new JLabel(Translator.get("prefs_dialog.right_folder"));
        rightFolderLabel.setAlignmentX(LEFT_ALIGNMENT);
        startupFolderPanel.add(rightFolderLabel);
        startupFolderPanel.addSpace(5);

        tempPanel = new XBoxPanel(5);
        tempPanel.setAlignmentX(LEFT_ALIGNMENT);
        rightLastFolderRadioButton = new JRadioButton(Translator.get("prefs_dialog.last_folder"));
        tempPanel.add(rightLastFolderRadioButton);
        startupFolderPanel.add(tempPanel);
		
        rightCustomFolderRadioButton = new JRadioButton(Translator.get("prefs_dialog.custom_folder"));
        tempPanel = new XBoxPanel(5);
        tempPanel.setAlignmentX(LEFT_ALIGNMENT);
        tempPanel.add(rightCustomFolderRadioButton);
        tempPanel.addSpace(5);
        rightCustomFolderTextField = new JTextField(MuConfiguration.getVariable(MuConfiguration.RIGHT_CUSTOM_FOLDER, ""));
        rightCustomFolderTextField.addKeyListener(this);
        tempPanel.add(rightCustomFolderTextField);

        rightCustomFolderButton = new JButton("...");
        rightCustomFolderButton.addActionListener(this);
        tempPanel.add(rightCustomFolderButton);
        startupFolderPanel.add(tempPanel);

        if(MuConfiguration.getVariable(MuConfiguration.RIGHT_STARTUP_FOLDER, "").equals(MuConfiguration.STARTUP_FOLDER_LAST)) {
            rightLastFolderRadioButton.setSelected(true);
            setCustomFolderComponentsEnabled(false, false);
        }
        else
            rightCustomFolderRadioButton.setSelected(true);

        rightCustomFolderRadioButton.addItemListener(this);

        buttonGroup = new ButtonGroup();
        buttonGroup.add(rightLastFolderRadioButton);
        buttonGroup.add(rightCustomFolderRadioButton);

        YBoxPanel northPanel = new YBoxPanel();
        northPanel.add(startupFolderPanel);
        northPanel.addSpace(5);
		
        showHiddenFilesCheckBox = new JCheckBox(Translator.get("prefs_dialog.show_hidden_files"));
        showHiddenFilesCheckBox.setSelected(MuConfiguration.getVariable(MuConfiguration.SHOW_HIDDEN_FILES, MuConfiguration.DEFAULT_SHOW_HIDDEN_FILES));
        northPanel.add(showHiddenFilesCheckBox);

        // Mac OS X-only options
        if(OsFamilies.MAC_OS_X.isCurrent()) {
            // Monitor showHiddenFilesCheckBox state to disable 'show .DS_Store files' option
            // when 'Show hidden files' is disabled, as .DS_Store files are hidden files
            showHiddenFilesCheckBox.addItemListener(this);

            showDSStoreFilesCheckBox = new JCheckBox(Translator.get("prefs_dialog.show_ds_store_files"));
            showDSStoreFilesCheckBox.setSelected(MuConfiguration.getVariable(MuConfiguration.SHOW_DS_STORE_FILES,
                                                                                  MuConfiguration.DEFAULT_SHOW_DS_STORE_FILES));
            showDSStoreFilesCheckBox.setEnabled(showHiddenFilesCheckBox.isSelected());
            // Shift the check box to the right to indicate that it is a sub-option
            northPanel.add(showDSStoreFilesCheckBox, 20);

            showSystemFoldersCheckBox = new JCheckBox(Translator.get("prefs_dialog.show_system_folders"));
            showSystemFoldersCheckBox.setSelected(MuConfiguration.getVariable(MuConfiguration.SHOW_SYSTEM_FOLDERS,
                                                                                   MuConfiguration.DEFAULT_SHOW_SYSTEM_FOLDERS));
            northPanel.add(showSystemFoldersCheckBox);
        }

        compactSizeCheckBox = new JCheckBox(Translator.get("prefs_dialog.compact_file_size"));
        compactSizeCheckBox.setSelected(MuConfiguration.getVariable(MuConfiguration.DISPLAY_COMPACT_FILE_SIZE,
                                                                         MuConfiguration.DEFAULT_DISPLAY_COMPACT_FILE_SIZE));
        northPanel.add(compactSizeCheckBox);

        followSymlinksCheckBox = new JCheckBox(Translator.get("prefs_dialog.follow_symlinks_when_cd"));
        followSymlinksCheckBox.setSelected(MuConfiguration.getVariable(MuConfiguration.CD_FOLLOWS_SYMLINKS,
                                                                            MuConfiguration.DEFAULT_CD_FOLLOWS_SYMLINKS));
        northPanel.add(followSymlinksCheckBox);

        add(northPanel, BorderLayout.NORTH);
    }

    /**
     * Enables/disables the custom folder components.
     *
     * @param isLeft true if the components pertain to the left folder
     * @param enabled true to enable the components
     */
    private void setCustomFolderComponentsEnabled(boolean isLeft, boolean enabled) {
        (isLeft?leftCustomFolderTextField:rightCustomFolderTextField).setEnabled(enabled);
        (isLeft?leftCustomFolderButton:rightCustomFolderButton).setEnabled(enabled);
    }


    /////////////////////////////////////
    // PreferencesPanel implementation //
    /////////////////////////////////////

    protected void commit() {
        MuConfiguration.setVariable(MuConfiguration.LEFT_STARTUP_FOLDER, leftLastFolderRadioButton.isSelected() ? MuConfiguration.STARTUP_FOLDER_LAST :
                MuConfiguration.STARTUP_FOLDER_CUSTOM);
        MuConfiguration.setVariable(MuConfiguration.LEFT_CUSTOM_FOLDER, leftCustomFolderTextField.getText());
		
        MuConfiguration.setVariable(MuConfiguration.RIGHT_STARTUP_FOLDER, rightLastFolderRadioButton.isSelected() ? MuConfiguration.STARTUP_FOLDER_LAST :
                MuConfiguration.STARTUP_FOLDER_CUSTOM);
        MuConfiguration.setVariable(MuConfiguration.RIGHT_CUSTOM_FOLDER, rightCustomFolderTextField.getText());

        MuConfiguration.setVariable(MuConfiguration.DISPLAY_COMPACT_FILE_SIZE, compactSizeCheckBox.isSelected());

        MuConfiguration.setVariable(MuConfiguration.CD_FOLLOWS_SYMLINKS, followSymlinksCheckBox.isSelected());

        // If one of the show/hide file filters have changed, refresh current folders of current MainFrame
        boolean refreshFolders = MuConfiguration.setVariable(MuConfiguration.SHOW_HIDDEN_FILES, showHiddenFilesCheckBox.isSelected());
        
        if(OsFamilies.MAC_OS_X.isCurrent()) {
            refreshFolders |= MuConfiguration.setVariable(MuConfiguration.SHOW_DS_STORE_FILES, showDSStoreFilesCheckBox.isSelected());
            refreshFolders |= MuConfiguration.setVariable(MuConfiguration.SHOW_SYSTEM_FOLDERS, showSystemFoldersCheckBox.isSelected());
        }

        if(refreshFolders)
            WindowManager.tryRefreshCurrentFolders();
    }


    /////////////////////////////////
    // ItemListener implementation //
    /////////////////////////////////

    public void itemStateChanged(ItemEvent event) {
        Object source = event.getSource();

        // Disable 'show .DS_Store files' option when 'Show hidden files' is disabled, as .DS_Store files are hidden files
        if(source==showHiddenFilesCheckBox) {
            showDSStoreFilesCheckBox.setEnabled(showHiddenFilesCheckBox.isSelected());
        }
        else if(source==leftCustomFolderRadioButton) {
            setCustomFolderComponentsEnabled(true, leftCustomFolderRadioButton.isSelected());
        }
        else if(source==rightCustomFolderRadioButton) {
            setCustomFolderComponentsEnabled(false, rightCustomFolderRadioButton.isSelected());
        }
    }


    ////////////////////////////////
    // KeyListener implementation //
    ////////////////////////////////

    /**
     * Catches key events to automagically select custom folder radio button if it was not already selected.
     */
    public void keyTyped(KeyEvent e) {
        Object source = e.getSource();
		
        if(source==leftCustomFolderTextField) {
            if(!leftCustomFolderRadioButton.isSelected())
                leftCustomFolderRadioButton.setSelected(true);
        }
        else if(source==rightCustomFolderTextField) {
            if(!rightCustomFolderRadioButton.isSelected())
                rightCustomFolderRadioButton.setSelected(true);
        }			
    }
	
    public void keyPressed(KeyEvent e) {
    }
	
    public void keyReleased(KeyEvent e) {
    }


    ///////////////////////////////////
    // ActionListener implementation //
    ///////////////////////////////////

    /**
     * Opens dialog for selecting starting folder.
     */
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

    	JFileChooser chooser = new JFileChooser();
    	chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle(Translator.get("choose_folder"));
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        if(chooser.showDialog(parent, Translator.get("choose")) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (source==leftCustomFolderButton) {
                leftCustomFolderTextField.setText(file.getPath());
                if(!leftCustomFolderRadioButton.isSelected())
                    leftCustomFolderRadioButton.setSelected(true);
            }
            else if (source==rightCustomFolderButton) {
                rightCustomFolderTextField.setText(file.getPath());
                if(!rightCustomFolderRadioButton.isSelected())
                	rightCustomFolderRadioButton.setSelected(true);
            }
        }
	}
}
