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

package com.mucommander.ui.dialog.pref.general;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.plaf.basic.BasicTextFieldUI;
import javax.swing.text.JTextComponent;

import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.conf.MuPreferences;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.pref.PreferencesDialog;
import com.mucommander.ui.dialog.pref.PreferencesPanel;
import com.mucommander.ui.dialog.pref.component.PrefCheckBox;
import com.mucommander.ui.dialog.pref.component.PrefFilePathField;
import com.mucommander.ui.dialog.pref.component.PrefRadioButton;
import com.mucommander.ui.layout.SpringUtilities;
import com.mucommander.ui.layout.XBoxPanel;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.main.WindowManager;


/**
 * 'Folders' preferences panel.
 *
 * @author Maxence Bernard, Mariuz Jakubowski
 */
class FoldersPanel extends PreferencesPanel implements ItemListener, KeyListener, ActionListener {

    // Startup folders
    private PrefRadioButton lastFoldersRadioButton;
    private PrefRadioButton customFoldersRadioButton;
    
    private PrefFilePathFieldWithDefaultValue leftCustomFolderTextField;
    private JButton leftCustomFolderButton;
	
    private PrefFilePathFieldWithDefaultValue rightCustomFolderTextField;
	private JButton rightCustomFolderButton;

    // Show hidden files?
    private PrefCheckBox showHiddenFilesCheckBox;

    // Show Mac OS X .DS_Store?
    private PrefCheckBox showDSStoreFilesCheckBox;

    // Show Mac OS X system folders ?
    private PrefCheckBox showSystemFoldersCheckBox;

    // Display compact file size ?
    private PrefCheckBox compactSizeCheckBox;
	
    // Follow symlinks when changing directory ?
    private PrefCheckBox followSymlinksCheckBox;
    
    // Always show single tab's header ?
    private PrefCheckBox showTabHeaderCheckBox;

    public FoldersPanel(PreferencesDialog parent) {
        super(parent, Translator.get("prefs_dialog.folders_tab"));

        setLayout(new BorderLayout());


        // Startup folders panel
        YBoxPanel startupFolderPanel = new YBoxPanel();
        startupFolderPanel.setBorder(BorderFactory.createTitledBorder(Translator.get("prefs_dialog.startup_folders")));
		
        // Last folders or custom folders selections
        lastFoldersRadioButton = new PrefRadioButton(Translator.get("prefs_dialog.last_folder")) {
			public boolean hasChanged() {
				return !(isSelected() ? 
						MuPreferences.STARTUP_FOLDERS_LAST	: MuPreferences.STARTUP_FOLDERS_CUSTOM).equals(
								MuConfigurations.getPreferences().getVariable(MuPreference.STARTUP_FOLDERS));
			}
		};
		customFoldersRadioButton = new PrefRadioButton(Translator.get("prefs_dialog.custom_folder")) {
			public boolean hasChanged() {
				return !(isSelected() ? 
						MuPreferences.STARTUP_FOLDERS_CUSTOM : MuPreferences.STARTUP_FOLDERS_LAST).equals(
								MuConfigurations.getPreferences().getVariable(MuPreference.STARTUP_FOLDERS));
			}
        };
        startupFolderPanel.add(lastFoldersRadioButton);
        startupFolderPanel.addSpace(5);
        startupFolderPanel.add(customFoldersRadioButton);

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(lastFoldersRadioButton);
        buttonGroup.add(customFoldersRadioButton);

        customFoldersRadioButton.addItemListener(this);
        
        // Custom folders specification
        JLabel leftFolderLabel = new JLabel(Translator.get("prefs_dialog.left_folder"));
        leftFolderLabel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel rightFolderLabel = new JLabel(Translator.get("prefs_dialog.right_folder"));
        rightFolderLabel.setAlignmentX(LEFT_ALIGNMENT);

        // Panel that contains the text field and button for specifying custom left folder
        XBoxPanel leftCustomFolderSpecifyingPanel = new XBoxPanel(5);
        leftCustomFolderSpecifyingPanel.setAlignmentX(LEFT_ALIGNMENT);
        
        // Create a path field with auto-completion capabilities
        leftCustomFolderTextField = new PrefFilePathFieldWithDefaultValue(true);
        leftCustomFolderTextField.addKeyListener(this);
        leftCustomFolderSpecifyingPanel.add(leftCustomFolderTextField);

        leftCustomFolderButton = new JButton("...");
        leftCustomFolderButton.addActionListener(this);
        leftCustomFolderSpecifyingPanel.add(leftCustomFolderButton);

        // Panel that contains the text field and button for specifying custom right folder
        XBoxPanel rightCustomFolderSpecifyingPanel = new XBoxPanel(5);
        rightCustomFolderSpecifyingPanel.setAlignmentX(LEFT_ALIGNMENT);

        // Create a path field with auto-completion capabilities
        rightCustomFolderTextField = new PrefFilePathFieldWithDefaultValue(false);
        rightCustomFolderTextField.addKeyListener(this);
        rightCustomFolderSpecifyingPanel.add(rightCustomFolderTextField);

        rightCustomFolderButton = new JButton("...");
        rightCustomFolderButton.addActionListener(this);
        rightCustomFolderSpecifyingPanel.add(rightCustomFolderButton);
        
        JPanel container = new JPanel(new SpringLayout());
        container.add(leftFolderLabel);
        container.add(leftCustomFolderSpecifyingPanel);
        container.add(rightFolderLabel);
        container.add(rightCustomFolderSpecifyingPanel);
        
        //Lay out the panel.
        SpringUtilities.makeCompactGrid(container,
                                        2, 2, //rows, cols
                                        20, 6,        //initX, initY
                                        6, 6);       //xPad, yPad
        
        startupFolderPanel.add(container);
        
        if(MuConfigurations.getPreferences().getVariable(MuPreference.STARTUP_FOLDERS, "").equals(MuPreferences.STARTUP_FOLDERS_LAST)) {
            lastFoldersRadioButton.setSelected(true);
            setCustomFolderComponentsEnabled(false);
        }
        else
            customFoldersRadioButton.setSelected(true);
        
        // --------------------------------------------------------------------------------------------------------------

        YBoxPanel northPanel = new YBoxPanel();
        northPanel.add(startupFolderPanel);
        northPanel.addSpace(5);
		
        showHiddenFilesCheckBox = new PrefCheckBox(Translator.get("prefs_dialog.show_hidden_files")){
			public boolean hasChanged() {
				return isSelected() != MuConfigurations.getPreferences().getVariable(MuPreference.SHOW_HIDDEN_FILES, MuPreferences.DEFAULT_SHOW_HIDDEN_FILES);
			}        	
        };
        showHiddenFilesCheckBox.setSelected(MuConfigurations.getPreferences().getVariable(MuPreference.SHOW_HIDDEN_FILES, MuPreferences.DEFAULT_SHOW_HIDDEN_FILES));
        northPanel.add(showHiddenFilesCheckBox);

        // Mac OS X-only options
        if(OsFamily.MAC_OS_X.isCurrent()) {
            // Monitor showHiddenFilesCheckBox state to disable 'show .DS_Store files' option
            // when 'Show hidden files' is disabled, as .DS_Store files are hidden files
            showHiddenFilesCheckBox.addItemListener(this);

            showDSStoreFilesCheckBox = new PrefCheckBox(Translator.get("prefs_dialog.show_ds_store_files")){
				public boolean hasChanged() {
					return isSelected() != MuConfigurations.getPreferences().getVariable(MuPreference.SHOW_DS_STORE_FILES, MuPreferences.DEFAULT_SHOW_DS_STORE_FILES);
				}
            };
            showDSStoreFilesCheckBox.setSelected(MuConfigurations.getPreferences().getVariable(MuPreference.SHOW_DS_STORE_FILES,
                                                                                  MuPreferences.DEFAULT_SHOW_DS_STORE_FILES));
            showDSStoreFilesCheckBox.setEnabled(showHiddenFilesCheckBox.isSelected());
            // Shift the check box to the right to indicate that it is a sub-option
            northPanel.add(showDSStoreFilesCheckBox, 20);

            showSystemFoldersCheckBox = new PrefCheckBox(Translator.get("prefs_dialog.show_system_folders")) {
				public boolean hasChanged() {
					return isSelected() != MuConfigurations.getPreferences().getVariable(MuPreference.SHOW_SYSTEM_FOLDERS, MuPreferences.DEFAULT_SHOW_SYSTEM_FOLDERS);
				}
            };
            showSystemFoldersCheckBox.setSelected(MuConfigurations.getPreferences().getVariable(MuPreference.SHOW_SYSTEM_FOLDERS,
                                                                                   MuPreferences.DEFAULT_SHOW_SYSTEM_FOLDERS));
            northPanel.add(showSystemFoldersCheckBox);
        }

        compactSizeCheckBox = new PrefCheckBox(Translator.get("prefs_dialog.compact_file_size")) {
			public boolean hasChanged() {
				return isSelected() != MuConfigurations.getPreferences().getVariable(MuPreference.DISPLAY_COMPACT_FILE_SIZE, MuPreferences.DEFAULT_DISPLAY_COMPACT_FILE_SIZE);
			}
        };
        compactSizeCheckBox.setSelected(MuConfigurations.getPreferences().getVariable(MuPreference.DISPLAY_COMPACT_FILE_SIZE,
                                                                         MuPreferences.DEFAULT_DISPLAY_COMPACT_FILE_SIZE));
        northPanel.add(compactSizeCheckBox);

        followSymlinksCheckBox = new PrefCheckBox(Translator.get("prefs_dialog.follow_symlinks_when_cd")) {
			public boolean hasChanged() {
				return isSelected() != MuConfigurations.getPreferences().getVariable(MuPreference.CD_FOLLOWS_SYMLINKS, MuPreferences.DEFAULT_CD_FOLLOWS_SYMLINKS); 
			}
        };
        followSymlinksCheckBox.setSelected(MuConfigurations.getPreferences().getVariable(MuPreference.CD_FOLLOWS_SYMLINKS,
                                                                            MuPreferences.DEFAULT_CD_FOLLOWS_SYMLINKS));
        northPanel.add(followSymlinksCheckBox);

        showTabHeaderCheckBox = new PrefCheckBox(Translator.get("prefs_dialog.show_tab_header")) {
			public boolean hasChanged() {
				return isSelected() != MuConfigurations.getPreferences().getVariable(MuPreference.SHOW_TAB_HEADER, MuPreferences.DEFAULT_SHOW_TAB_HEADER); 
			}
        };
        showTabHeaderCheckBox.setSelected(MuConfigurations.getPreferences().getVariable(MuPreference.SHOW_TAB_HEADER,
                                                                            MuPreferences.DEFAULT_SHOW_TAB_HEADER));
        northPanel.add(showTabHeaderCheckBox);
        
        add(northPanel, BorderLayout.NORTH);
        
        lastFoldersRadioButton.addDialogListener(parent);
        customFoldersRadioButton.addDialogListener(parent);
        rightCustomFolderTextField.addDialogListener(parent);
        leftCustomFolderTextField.addDialogListener(parent);
        showHiddenFilesCheckBox.addDialogListener(parent);
        compactSizeCheckBox.addDialogListener(parent);
        followSymlinksCheckBox.addDialogListener(parent);
        showTabHeaderCheckBox.addDialogListener(parent);
        if(OsFamily.MAC_OS_X.isCurrent()) {
        	showDSStoreFilesCheckBox.addDialogListener(parent);
        	showSystemFoldersCheckBox.addDialogListener(parent);
        }
    }

    private void setCustomFolderComponentsEnabled(boolean enabled) {
        leftCustomFolderTextField.setEnabled(enabled);
        leftCustomFolderButton.setEnabled(enabled);
        rightCustomFolderTextField.setEnabled(enabled);
        rightCustomFolderButton.setEnabled(enabled);
    }


    /////////////////////////////////////
    // PreferencesPanel implementation //
    /////////////////////////////////////

    @Override
    protected void commit() {
    	MuConfigurations.getPreferences().setVariable(MuPreference.STARTUP_FOLDERS, lastFoldersRadioButton.isSelected() ? MuPreferences.STARTUP_FOLDERS_LAST : MuPreferences.STARTUP_FOLDERS_CUSTOM);
    	
    	MuConfigurations.getPreferences().setVariable(MuPreference.LEFT_CUSTOM_FOLDER, leftCustomFolderTextField.getFilePath());
		
    	MuConfigurations.getPreferences().setVariable(MuPreference.RIGHT_CUSTOM_FOLDER, rightCustomFolderTextField.getFilePath());

    	MuConfigurations.getPreferences().setVariable(MuPreference.DISPLAY_COMPACT_FILE_SIZE, compactSizeCheckBox.isSelected());

    	MuConfigurations.getPreferences().setVariable(MuPreference.CD_FOLLOWS_SYMLINKS, followSymlinksCheckBox.isSelected());
    	
    	MuConfigurations.getPreferences().setVariable(MuPreference.SHOW_TAB_HEADER, showTabHeaderCheckBox.isSelected());

        // If one of the show/hide file filters have changed, refresh current folders of current MainFrame
        boolean refreshFolders = MuConfigurations.getPreferences().setVariable(MuPreference.SHOW_HIDDEN_FILES, showHiddenFilesCheckBox.isSelected());
        
        if(OsFamily.MAC_OS_X.isCurrent()) {
            refreshFolders |= MuConfigurations.getPreferences().setVariable(MuPreference.SHOW_DS_STORE_FILES, showDSStoreFilesCheckBox.isSelected());
            refreshFolders |= MuConfigurations.getPreferences().setVariable(MuPreference.SHOW_SYSTEM_FOLDERS, showSystemFoldersCheckBox.isSelected());
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
        else if(source==customFoldersRadioButton) {
            setCustomFolderComponentsEnabled(customFoldersRadioButton.isSelected());
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
		
        if(source==leftCustomFolderTextField || source==rightCustomFolderTextField) {
            if(!customFoldersRadioButton.isSelected())
                customFoldersRadioButton.setSelected(true);
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
                if(!customFoldersRadioButton.isSelected())
                    customFoldersRadioButton.setSelected(true);
            }
            else if (source==rightCustomFolderButton) {
                rightCustomFolderTextField.setText(file.getPath());
                if(!customFoldersRadioButton.isSelected())
                    customFoldersRadioButton.setSelected(true);
            }
        }
	}
    
    public class PrefFilePathFieldWithDefaultValue extends PrefFilePathField {
    	
    	private boolean isLeft;
    	private final String HOME_FOLDER_PATH = System.getProperty("user.home");
    	
    	public PrefFilePathFieldWithDefaultValue(boolean isLeft) {
    		super(isLeft ? MuConfigurations.getPreferences().getVariable(MuPreference.LEFT_CUSTOM_FOLDER, "") : MuConfigurations.getPreferences().getVariable(MuPreference.RIGHT_CUSTOM_FOLDER, ""));
    		this.isLeft = isLeft;
    		
//    		setUI(new HintTextFieldUI(HOME_FOLDER_PATH, true));
    	}
    	
		public boolean hasChanged() {
			return isLeft ? 
					!getText().equals(MuConfigurations.getPreferences().getVariable(MuPreference.LEFT_CUSTOM_FOLDER)) :
					!getText().equals(MuConfigurations.getPreferences().getVariable(MuPreference.RIGHT_CUSTOM_FOLDER));
		}
		
		public String getFilePath() {
			String text = super.getText();
			
			return text.trim().isEmpty() ? HOME_FOLDER_PATH : text;
		}

    	private class HintTextFieldUI extends BasicTextFieldUI implements FocusListener {

    	    private String hint;
    	    private boolean hideOnFocus;
    	    private Color color;

    	    public Color getColor() {
    	        return color;
    	    }

    	    public void setColor(Color color) {
    	        this.color = color;
    	        repaint();
    	    }

    	    private void repaint() {
    	        if(getComponent() != null) {
    	            getComponent().repaint();           
    	        }
    	    }

    	    public boolean isHideOnFocus() {
    	        return hideOnFocus;
    	    }

    	    public void setHideOnFocus(boolean hideOnFocus) {
    	        this.hideOnFocus = hideOnFocus;
    	        repaint();
    	    }

    	    public String getHint() {
    	        return hint;
    	    }

    	    public void setHint(String hint) {
    	        this.hint = hint;
    	        repaint();
    	    }
    	    public HintTextFieldUI(String hint) {
    	        this(hint,false);
    	    }

    	    public HintTextFieldUI(String hint, boolean hideOnFocus) {
    	        this(hint,hideOnFocus, Color.gray);
    	    }

    	    public HintTextFieldUI(String hint, boolean hideOnFocus, Color color) {
    	        this.hint = hint;
    	        this.hideOnFocus = hideOnFocus;
    	        this.color = color;
    	    }

    	    @Override
    	    protected void paintSafely(Graphics g) {
    	        super.paintSafely(g);
    	        JTextComponent comp = getComponent();
    	        if(hint!=null && comp.getText().length() == 0 && (!(hideOnFocus && comp.hasFocus()))){
    	            if(color != null) {
    	                g.setColor(color);
    	            } else {
    	                g.setColor(comp.getForeground().brighter().brighter().brighter());              
    	            }
    	            int padding = (comp.getHeight() - comp.getFont().getSize())/2;
    	            g.drawString(hint, 3, comp.getHeight()-padding-1);          
    	        }
    	    }

    	    public void focusGained(FocusEvent e) {
    	        if(hideOnFocus) repaint();

    	    }

    	    public void focusLost(FocusEvent e) {
    	        if(hideOnFocus) repaint();
    	    }
    	    
    	    @Override
    	    protected void installListeners() {
    	        super.installListeners();
    	        getComponent().addFocusListener(this);
    	    }
    	    @Override
    	    protected void uninstallListeners() {
    	        super.uninstallListeners();
    	        getComponent().removeFocusListener(this);
    	    }
    	}
    }
}
