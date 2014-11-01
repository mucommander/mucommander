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
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.mucommander.bonjour.BonjourDirectory;
import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.conf.MuPreferences;
import com.mucommander.desktop.DesktopManager;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.DialogOwner;
import com.mucommander.ui.dialog.pref.PreferencesDialog;
import com.mucommander.ui.dialog.pref.PreferencesPanel;
import com.mucommander.ui.dialog.pref.component.PrefCheckBox;
import com.mucommander.ui.dialog.pref.component.PrefEncodingSelectBox;
import com.mucommander.ui.dialog.pref.component.PrefFilePathField;
import com.mucommander.ui.dialog.pref.component.PrefRadioButton;
import com.mucommander.ui.dialog.pref.component.PrefTextField;
import com.mucommander.ui.layout.XAlignedComponentPanel;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.notifier.AbstractNotifier;

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
                return isSelected() != MuConfigurations.getPreferences().getVariable(MuPreference.AUTODETECT_SHELL_ENCODING, MuPreferences.DEFAULT_AUTODETECT_SHELL_ENCODING);
            }
        };
        boolean autoDetect = MuConfigurations.getPreferences().getVariable(MuPreference.AUTODETECT_SHELL_ENCODING, MuPreferences.DEFAULT_AUTODETECT_SHELL_ENCODING);
        shellEncodingautoDetectCheckbox.setSelected(autoDetect);
        shellEncodingautoDetectCheckbox.addItemListener(this);

        panel.add(shellEncodingautoDetectCheckbox);

        shellEncodingSelectBox = new PrefEncodingSelectBox(new DialogOwner(parent), MuConfigurations.getPreferences().getVariable(MuPreference.SHELL_ENCODING)) {
            public boolean hasChanged() {
                return !MuConfigurations.getPreferences().getVariable(MuPreference.SHELL_ENCODING).equals(getSelectedEncoding());
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
				return isSelected() != MuConfigurations.getPreferences().getVariable(MuPreference.USE_CUSTOM_SHELL, MuPreferences.DEFAULT_USE_CUSTOM_SHELL);
			}
        };

        // Use sytem default or custom shell ?
        if(MuConfigurations.getPreferences().getVariable(MuPreference.USE_CUSTOM_SHELL, MuPreferences.DEFAULT_USE_CUSTOM_SHELL))
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
        customShellField = new PrefFilePathField(MuConfigurations.getPreferences().getVariable(MuPreference.CUSTOM_SHELL, "")) {
			public boolean hasChanged() {
				return isEnabled() && !getText().equals(MuConfigurations.getPreferences().getVariable(MuPreference.CUSTOM_SHELL));
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
				return isSelected() != MuConfigurations.getPreferences().getVariable(MuPreference.SHOW_SPLASH_SCREEN, MuPreferences.DEFAULT_SHOW_SPLASH_SCREEN);
			}
        };
        showSplashScreenCheckBox.setSelected(MuConfigurations.getPreferences().getVariable(MuPreference.SHOW_SPLASH_SCREEN, MuPreferences.DEFAULT_SHOW_SPLASH_SCREEN));
        northPanel.add(showSplashScreenCheckBox);

        // 'Check for updates on startup' option
        checkForUpdatesCheckBox = new PrefCheckBox(Translator.get("prefs_dialog.check_for_updates_on_startup")) {
			public boolean hasChanged() {
				return isSelected() != MuConfigurations.getPreferences().getVariable(MuPreference.CHECK_FOR_UPDATE, MuPreferences.DEFAULT_CHECK_FOR_UPDATE);
			}
        };
        checkForUpdatesCheckBox.setSelected(MuConfigurations.getPreferences().getVariable(MuPreference.CHECK_FOR_UPDATE, MuPreferences.DEFAULT_CHECK_FOR_UPDATE));
        northPanel.add(checkForUpdatesCheckBox);

        // 'Show confirmation dialog on quit' option
        quitConfirmationCheckBox = new PrefCheckBox(Translator.get("prefs_dialog.confirm_on_quit")) {
			public boolean hasChanged() {
				return isSelected() != MuConfigurations.getPreferences().getVariable(MuPreference.CONFIRM_ON_QUIT, MuPreferences.DEFAULT_CONFIRM_ON_QUIT);
			}
        };
        quitConfirmationCheckBox.setSelected(MuConfigurations.getPreferences().getVariable(MuPreference.CONFIRM_ON_QUIT, MuPreferences.DEFAULT_CONFIRM_ON_QUIT));
        northPanel.add(quitConfirmationCheckBox);

        // 'Enable system notifications' option, displayed only if current platform supports system notifications
        if(AbstractNotifier.isAvailable()) {
            systemNotificationsCheckBox = new PrefCheckBox(Translator.get("prefs_dialog.enable_system_notifications")+" ("+AbstractNotifier.getNotifier().getPrettyName()+")") {
				public boolean hasChanged() {
					return isSelected() != MuConfigurations.getPreferences().getVariable(MuPreference.ENABLE_SYSTEM_NOTIFICATIONS,
                            								MuPreferences.DEFAULT_ENABLE_SYSTEM_NOTIFICATIONS);
				}
            };
            systemNotificationsCheckBox.setSelected(MuConfigurations.getPreferences().getVariable(MuPreference.ENABLE_SYSTEM_NOTIFICATIONS,
                                                                                     MuPreferences.DEFAULT_ENABLE_SYSTEM_NOTIFICATIONS));
            northPanel.add(systemNotificationsCheckBox);
        }

        // 'Enable Bonjour services discovery' option
        bonjourDiscoveryCheckBox = new PrefCheckBox(Translator.get("prefs_dialog.enable_bonjour_discovery")) {
			public boolean hasChanged() {
				return isSelected() != MuConfigurations.getPreferences().getVariable(MuPreference.ENABLE_BONJOUR_DISCOVERY,
                        									MuPreferences.DEFAULT_ENABLE_BONJOUR_DISCOVERY);
			}
        };
        bonjourDiscoveryCheckBox.setSelected(MuConfigurations.getPreferences().getVariable(MuPreference.ENABLE_BONJOUR_DISCOVERY,
                                                                              MuPreferences.DEFAULT_ENABLE_BONJOUR_DISCOVERY));
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
    	MuConfigurations.getPreferences().setVariable(MuPreference.CHECK_FOR_UPDATE, checkForUpdatesCheckBox.isSelected());

        // Saves the shell data.
    	MuConfigurations.getPreferences().setVariable(MuPreference.USE_CUSTOM_SHELL, useCustomShellRadioButton.isSelected());
        MuConfigurations.getPreferences().setVariable(MuPreference.CUSTOM_SHELL, customShellField.getText());

        // Saves the shell encoding data.
        boolean isAutoDetect = shellEncodingautoDetectCheckbox.isSelected();
        MuConfigurations.getPreferences().setVariable(MuPreference.AUTODETECT_SHELL_ENCODING, isAutoDetect);
        if(!isAutoDetect)
        	MuConfigurations.getPreferences().setVariable(MuPreference.SHELL_ENCODING, shellEncodingSelectBox.getSelectedEncoding());

        MuConfigurations.getPreferences().setVariable(MuPreference.CONFIRM_ON_QUIT, quitConfirmationCheckBox.isSelected());
        MuConfigurations.getPreferences().setVariable(MuPreference.SHOW_SPLASH_SCREEN, showSplashScreenCheckBox.isSelected());

        boolean enabled;
        if(systemNotificationsCheckBox!=null) {
            enabled = systemNotificationsCheckBox.isSelected();
            MuConfigurations.getPreferences().setVariable(MuPreference.ENABLE_SYSTEM_NOTIFICATIONS, enabled);
            AbstractNotifier.getNotifier().setEnabled(enabled);
        }

        enabled = bonjourDiscoveryCheckBox.isSelected();
        MuConfigurations.getPreferences().setVariable(MuPreference.ENABLE_BONJOUR_DISCOVERY, enabled);
        BonjourDirectory.setActive(enabled);
    }
}
