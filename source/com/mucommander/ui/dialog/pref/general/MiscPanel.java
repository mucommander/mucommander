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

package com.mucommander.ui.dialog.pref.general;

import com.mucommander.PlatformManager;
import com.mucommander.bonjour.BonjourDirectory;
import com.mucommander.conf.ConfigurationManager;
import com.mucommander.conf.ConfigurationVariables;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.pref.PreferencesDialog;
import com.mucommander.ui.dialog.pref.PreferencesPanel;
import com.mucommander.ui.layout.YBoxPanel;
import com.mucommander.ui.notifier.AbstractNotifier;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;


/**
 * 'Misc' preferences panel.
 *
 * @author Maxence Bernard
 */
class MiscPanel extends PreferencesPanel {

    /** Custom shell command text field */
    private JTextField customShellField;
	
    /** 'Use custom shell' radio button */
    private JRadioButton useCustomShellRadioButton;

    /** 'Check for updates on startup' checkbox */
    private JCheckBox checkForUpdatesCheckBox;

    /** 'Show confirmation dialog on quit' checkbox */
    private JCheckBox quitConfirmationCheckBox;

    /** 'Enable system notifications' checkbox */
    private JCheckBox systemNotificationsCheckBox;

    /** 'Enable Bonjour services discovery' checkbox */
    private JCheckBox bonjourDiscoveryCheckBox;


    public MiscPanel(PreferencesDialog parent) {
        super(parent, Translator.get("prefs_dialog.misc_tab"));

        setLayout(new BorderLayout());

        YBoxPanel northPanel = new YBoxPanel();

        // Shell panel
        YBoxPanel shellPanel = new YBoxPanel();
        shellPanel.setBorder(BorderFactory.createTitledBorder(Translator.get("prefs_dialog.shell")));

        JRadioButton useDefaultShellRadioButton = new JRadioButton(Translator.get("prefs_dialog.default_shell")+": "+PlatformManager.DEFAULT_SHELL_COMMAND);
        useCustomShellRadioButton = new JRadioButton(Translator.get("prefs_dialog.custom_shell")+": ");
        // Use sytem default or custom shell ?
        if(ConfigurationManager.getVariableBoolean(ConfigurationVariables.USE_CUSTOM_SHELL, ConfigurationVariables.DEFAULT_USE_CUSTOM_SHELL))
            useCustomShellRadioButton.setSelected(true);
        else
            useDefaultShellRadioButton.setSelected(true);
		
        shellPanel.add(useDefaultShellRadioButton);
        JPanel tempPanel = new JPanel(new BorderLayout());
        tempPanel.add(useCustomShellRadioButton, BorderLayout.WEST);
        customShellField = new JTextField(getPref(ConfigurationVariables.CUSTOM_SHELL));
        // Typing in the text field automatically selects the associated radio button (if not already)
        customShellField.addKeyListener(new KeyAdapter() {
                public void keyTyped(KeyEvent e) {
                    if(!useCustomShellRadioButton.isSelected())
                        useCustomShellRadioButton.setSelected(true);
                }
            });
        tempPanel.add(customShellField, BorderLayout.CENTER);
        shellPanel.add(tempPanel);

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(useDefaultShellRadioButton);
        buttonGroup.add(useCustomShellRadioButton);

        northPanel.add(shellPanel, 5);

        northPanel.addSpace(10);

        // 'Check for updates on startup' option
        checkForUpdatesCheckBox = new JCheckBox(Translator.get("prefs_dialog.check_for_updates_on_startup"));
        checkForUpdatesCheckBox.setSelected(getPref(ConfigurationVariables.CHECK_FOR_UPDATE, Boolean.toString(ConfigurationVariables.DEFAULT_CHECK_FOR_UPDATE)).equals("true"));
        northPanel.add(checkForUpdatesCheckBox);

        // 'Show confirmation dialog on quit' option
        quitConfirmationCheckBox = new JCheckBox(Translator.get("prefs_dialog.confirm_on_quit"));
        quitConfirmationCheckBox.setSelected(getPref(ConfigurationVariables.CONFIRM_ON_QUIT,
                                                    Boolean.toString(ConfigurationVariables.DEFAULT_CONFIRM_ON_QUIT)).equals("true"));
        northPanel.add(quitConfirmationCheckBox);

        // 'Enable system notifications' option, displayed only if current platform supports system notifications
        if(AbstractNotifier.isAvailable()) {
            systemNotificationsCheckBox = new JCheckBox(Translator.get("prefs_dialog.enable_system_notifications")+" ("+AbstractNotifier.getNotifier().getPrettyName()+")");
            systemNotificationsCheckBox.setSelected(getPref(ConfigurationVariables.ENABLE_SYSTEM_NOTIFICATIONS,
                                                        Boolean.toString(ConfigurationVariables.DEFAULT_ENABLE_SYSTEM_NOTIFICATIONS)).equals("true"));
            northPanel.add(systemNotificationsCheckBox);
        }

        // 'Enable Bonjour services discovery' option
        bonjourDiscoveryCheckBox = new JCheckBox(Translator.get("prefs_dialog.enable_bonjour_discovery"));
        bonjourDiscoveryCheckBox.setSelected(getPref(ConfigurationVariables.ENABLE_BONJOUR_DISCOVERY,
                                            Boolean.toString(ConfigurationVariables.DEFAULT_ENABLE_BONJOUR_DISCOVERY)).equals("true"));
        northPanel.add(bonjourDiscoveryCheckBox);

        add(northPanel, BorderLayout.NORTH);
    }


    ///////////////////////
    // PrefPanel methods //
    ///////////////////////
    protected void commit() {
        setPref(ConfigurationVariables.CHECK_FOR_UPDATE, Boolean.toString(checkForUpdatesCheckBox.isSelected()));

        setPref(ConfigurationVariables.USE_CUSTOM_SHELL, Boolean.toString(useCustomShellRadioButton.isSelected()));
        setPref(ConfigurationVariables.CUSTOM_SHELL, customShellField.getText());

        setPref(ConfigurationVariables.CONFIRM_ON_QUIT,  Boolean.toString(quitConfirmationCheckBox.isSelected()));

        boolean enabled;
        if(systemNotificationsCheckBox!=null) {
            enabled = systemNotificationsCheckBox.isSelected();
            setPref(ConfigurationVariables.ENABLE_SYSTEM_NOTIFICATIONS, Boolean.toString(enabled));
            AbstractNotifier.getNotifier().setEnabled(enabled);
        }

        enabled = bonjourDiscoveryCheckBox.isSelected();
        setPref(ConfigurationVariables.ENABLE_BONJOUR_DISCOVERY,  Boolean.toString(enabled));
        BonjourDirectory.setActive(enabled);
    }
}
