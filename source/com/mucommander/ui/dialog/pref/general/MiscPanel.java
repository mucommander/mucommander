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

package com.mucommander.ui.dialog.pref.general;

import com.mucommander.bonjour.BonjourDirectory;
import com.mucommander.conf.MuConfiguration;
import com.mucommander.desktop.DesktopManager;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.DialogOwner;
import com.mucommander.ui.dialog.pref.PreferencesDialog;
import com.mucommander.ui.dialog.pref.PreferencesPanel;
import com.mucommander.ui.dialog.pref.component.*;
import com.mucommander.ui.layout.XAlignedComponentPanel;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.notifier.AbstractNotifier;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * 'Misc' preferences panel.
 *
 * @author Maxence Bernard
 */
class MiscPanel extends PreferencesPanel implements ItemListener {

    /** Custom shell command text field */
    private PrefTextField customShellField;
	
    /** 'Use custom shell' radio button */
    private PrefRadioButton useCustomShellRadioButton;

    /** 'Check for updates on startup' checkbox */
    private PrefCheckBox checkForUpdatesCheckBox;

    /** 'Show confirmation dialog on quit' checkbox */
    private PrefCheckBox quitConfirmationCheckBox;
    
    /** 'Show splash screen' checkbox */
    private PrefCheckBox showSplashScreenCheckBox;

    /** 'Enable system notifications' checkbox */
    private PrefCheckBox systemNotificationsCheckBox;

    /** 'Enable Bonjour services discovery' checkbox */
    private PrefCheckBox bonjourDiscoveryCheckBox;

    /** Shell encoding auto-detect checkbox */
    private PrefCheckBox shellEncodingautoDetectCheckbox;

    /** Shell encoding select box. */
    private PrefEncodingSelectBox shellEncodingSelectBox;


    private JPanel createShellEncodingPanel(PreferencesDialog parent) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING));

        shellEncodingautoDetectCheckbox = new PrefCheckBox(Translator.get("prefs_dialog.auto_detect_shell_encoding")) {
            public boolean hasChanged() {
                return isSelected() != MuConfiguration.getVariable(MuConfiguration.AUTODETECT_SHELL_ENCODING, MuConfiguration.DEFAULT_AUTODETECT_SHELL_ENCODING);
            }
        };
        boolean autoDetect = MuConfiguration.getVariable(MuConfiguration.AUTODETECT_SHELL_ENCODING, MuConfiguration.DEFAULT_AUTODETECT_SHELL_ENCODING);
        shellEncodingautoDetectCheckbox.setSelected(autoDetect);
        shellEncodingautoDetectCheckbox.addItemListener(this);

        panel.add(shellEncodingautoDetectCheckbox);

        shellEncodingSelectBox = new PrefEncodingSelectBox(new DialogOwner(parent), MuConfiguration.getVariable(MuConfiguration.SHELL_ENCODING)) {
            public boolean hasChanged() {
                return !MuConfiguration.getVariable(MuConfiguration.SHELL_ENCODING).equals(getSelectedEncoding());
            }
        };
        shellEncodingSelectBox.setEnabled(!autoDetect); 

        panel.add(shellEncodingSelectBox);

        return panel;
    }


    public MiscPanel(PreferencesDialog parent) {
        super(parent, Translator.get("prefs_dialog.misc_tab"));

        setLayout(new BorderLayout());

        YBoxPanel northPanel = new YBoxPanel();

        JRadioButton useDefaultShellRadioButton = new JRadioButton(Translator.get("prefs_dialog.default_shell") + ':');
        useCustomShellRadioButton = new PrefRadioButton(Translator.get("prefs_dialog.custom_shell") + ':') {
			public boolean hasChanged() {
				return isSelected() != MuConfiguration.getVariable(MuConfiguration.USE_CUSTOM_SHELL, MuConfiguration.DEFAULT_USE_CUSTOM_SHELL);
			}
        };

        // Use sytem default or custom shell ?
        if(MuConfiguration.getVariable(MuConfiguration.USE_CUSTOM_SHELL, MuConfiguration.DEFAULT_USE_CUSTOM_SHELL))
            useCustomShellRadioButton.setSelected(true);
        else
            useDefaultShellRadioButton.setSelected(true);

        useCustomShellRadioButton.addItemListener(this);

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(useDefaultShellRadioButton);
        buttonGroup.add(useCustomShellRadioButton);

        // Shell panel
        XAlignedComponentPanel shellPanel = new XAlignedComponentPanel();
        shellPanel.setLabelLeftAligned(true);
        shellPanel.setBorder(BorderFactory.createTitledBorder(Translator.get("prefs_dialog.shell")));

        // Create a path field with auto-completion capabilities
        customShellField = new PrefFilePathField(MuConfiguration.getVariable(MuConfiguration.CUSTOM_SHELL, "")) {
			public boolean hasChanged() {
				return isEnabled() && !getText().equals(MuConfiguration.getVariable(MuConfiguration.CUSTOM_SHELL));
			}
        };
        customShellField.setEnabled(useCustomShellRadioButton.isSelected());

        shellPanel.addRow(useDefaultShellRadioButton, new JLabel(DesktopManager.getDefaultShell()), 5);
        shellPanel.addRow(useCustomShellRadioButton, customShellField, 10);
        shellPanel.addRow(Translator.get("prefs_dialog.shell_encoding"), createShellEncodingPanel(parent), 5);

        northPanel.add(shellPanel, 5);

        northPanel.addSpace(10);

        // 'Show splash screen' option
        showSplashScreenCheckBox = new PrefCheckBox(Translator.get("prefs_dialog.show_splash_screen")) {
			public boolean hasChanged() {
				return isSelected() != MuConfiguration.getVariable(MuConfiguration.SHOW_SPLASH_SCREEN, MuConfiguration.DEFAULT_SHOW_SPLASH_SCREEN);
			}
        };
        showSplashScreenCheckBox.setSelected(MuConfiguration.getVariable(MuConfiguration.SHOW_SPLASH_SCREEN, MuConfiguration.DEFAULT_SHOW_SPLASH_SCREEN));
        northPanel.add(showSplashScreenCheckBox);

        // 'Check for updates on startup' option
        checkForUpdatesCheckBox = new PrefCheckBox(Translator.get("prefs_dialog.check_for_updates_on_startup")) {
			public boolean hasChanged() {
				return isSelected() != MuConfiguration.getVariable(MuConfiguration.CHECK_FOR_UPDATE, MuConfiguration.DEFAULT_CHECK_FOR_UPDATE);
			}
        };
        checkForUpdatesCheckBox.setSelected(MuConfiguration.getVariable(MuConfiguration.CHECK_FOR_UPDATE, MuConfiguration.DEFAULT_CHECK_FOR_UPDATE));
        northPanel.add(checkForUpdatesCheckBox);

        // 'Show confirmation dialog on quit' option
        quitConfirmationCheckBox = new PrefCheckBox(Translator.get("prefs_dialog.confirm_on_quit")) {
			public boolean hasChanged() {
				return isSelected() != MuConfiguration.getVariable(MuConfiguration.CONFIRM_ON_QUIT, MuConfiguration.DEFAULT_CONFIRM_ON_QUIT);
			}
        };
        quitConfirmationCheckBox.setSelected(MuConfiguration.getVariable(MuConfiguration.CONFIRM_ON_QUIT, MuConfiguration.DEFAULT_CONFIRM_ON_QUIT));
        northPanel.add(quitConfirmationCheckBox);

        // 'Enable system notifications' option, displayed only if current platform supports system notifications
        if(AbstractNotifier.isAvailable()) {
            systemNotificationsCheckBox = new PrefCheckBox(Translator.get("prefs_dialog.enable_system_notifications")+" ("+AbstractNotifier.getNotifier().getPrettyName()+")") {
				public boolean hasChanged() {
					return isSelected() != MuConfiguration.getVariable(MuConfiguration.ENABLE_SYSTEM_NOTIFICATIONS,
                            								MuConfiguration.DEFAULT_ENABLE_SYSTEM_NOTIFICATIONS);
				}
            };
            systemNotificationsCheckBox.setSelected(MuConfiguration.getVariable(MuConfiguration.ENABLE_SYSTEM_NOTIFICATIONS,
                                                                                     MuConfiguration.DEFAULT_ENABLE_SYSTEM_NOTIFICATIONS));
            northPanel.add(systemNotificationsCheckBox);
        }

        // 'Enable Bonjour services discovery' option
        bonjourDiscoveryCheckBox = new PrefCheckBox(Translator.get("prefs_dialog.enable_bonjour_discovery")) {
			public boolean hasChanged() {
				return isSelected() != MuConfiguration.getVariable(MuConfiguration.ENABLE_BONJOUR_DISCOVERY,
                        									MuConfiguration.DEFAULT_ENABLE_BONJOUR_DISCOVERY);
			}
        };
        bonjourDiscoveryCheckBox.setSelected(MuConfiguration.getVariable(MuConfiguration.ENABLE_BONJOUR_DISCOVERY,
                                                                              MuConfiguration.DEFAULT_ENABLE_BONJOUR_DISCOVERY));
        northPanel.add(bonjourDiscoveryCheckBox);

        add(northPanel, BorderLayout.NORTH);
        
        customShellField.addDialogListener(parent);
    	useCustomShellRadioButton.addDialogListener(parent);
    	checkForUpdatesCheckBox.addDialogListener(parent);
    	quitConfirmationCheckBox.addDialogListener(parent);
        showSplashScreenCheckBox.addDialogListener(parent);
        bonjourDiscoveryCheckBox.addDialogListener(parent);
        shellEncodingautoDetectCheckbox.addDialogListener(parent);
        shellEncodingSelectBox.addDialogListener(parent);
        if(systemNotificationsCheckBox!=null)
            systemNotificationsCheckBox.addDialogListener(parent);
    }


    /////////////////////////////////
    // ItemListener implementation //
    /////////////////////////////////

    public void itemStateChanged(ItemEvent e) {
        Object source = e.getSource();
        if(source==useCustomShellRadioButton) {
            customShellField.setEnabled(useCustomShellRadioButton.isSelected());
        }
        else if(source==shellEncodingautoDetectCheckbox) {
            shellEncodingSelectBox.setEnabled(!shellEncodingautoDetectCheckbox.isSelected());
        }
    }


    //////////////////////////////
    // PrefPanel implementation //
    //////////////////////////////

    @Override
    protected void commit() {
        MuConfiguration.setVariable(MuConfiguration.CHECK_FOR_UPDATE, checkForUpdatesCheckBox.isSelected());

        // Saves the shell data.
        MuConfiguration.setVariable(MuConfiguration.USE_CUSTOM_SHELL, useCustomShellRadioButton.isSelected());
        MuConfiguration.setVariable(MuConfiguration.CUSTOM_SHELL, customShellField.getText());

        // Saves the shell encoding data.
        boolean isAutoDetect = shellEncodingautoDetectCheckbox.isSelected();
        MuConfiguration.setVariable(MuConfiguration.AUTODETECT_SHELL_ENCODING, isAutoDetect);
        if(!isAutoDetect)
            MuConfiguration.setVariable(MuConfiguration.SHELL_ENCODING, shellEncodingSelectBox.getSelectedEncoding());

        MuConfiguration.setVariable(MuConfiguration.CONFIRM_ON_QUIT, quitConfirmationCheckBox.isSelected());
        MuConfiguration.setVariable(MuConfiguration.SHOW_SPLASH_SCREEN, showSplashScreenCheckBox.isSelected());

        boolean enabled;
        if(systemNotificationsCheckBox!=null) {
            enabled = systemNotificationsCheckBox.isSelected();
            MuConfiguration.setVariable(MuConfiguration.ENABLE_SYSTEM_NOTIFICATIONS, enabled);
            AbstractNotifier.getNotifier().setEnabled(enabled);
        }

        enabled = bonjourDiscoveryCheckBox.isSelected();
        MuConfiguration.setVariable(MuConfiguration.ENABLE_BONJOUR_DISCOVERY, enabled);
        BonjourDirectory.setActive(enabled);
    }
}
