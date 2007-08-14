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
import com.mucommander.conf.impl.MuConfiguration;
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
        if(MuConfiguration.getVariable(MuConfiguration.USE_CUSTOM_SHELL, MuConfiguration.DEFAULT_USE_CUSTOM_SHELL))
            useCustomShellRadioButton.setSelected(true);
        else
            useDefaultShellRadioButton.setSelected(true);
		
        shellPanel.add(useDefaultShellRadioButton);
        JPanel tempPanel = new JPanel(new BorderLayout());
        tempPanel.add(useCustomShellRadioButton, BorderLayout.WEST);
        customShellField = new JTextField(MuConfiguration.getVariable(MuConfiguration.CUSTOM_SHELL, ""));
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
        checkForUpdatesCheckBox.setSelected(MuConfiguration.getVariable(MuConfiguration.CHECK_FOR_UPDATE, MuConfiguration.DEFAULT_CHECK_FOR_UPDATE));
        northPanel.add(checkForUpdatesCheckBox);

        // 'Show confirmation dialog on quit' option
        quitConfirmationCheckBox = new JCheckBox(Translator.get("prefs_dialog.confirm_on_quit"));
        quitConfirmationCheckBox.setSelected(MuConfiguration.getVariable(MuConfiguration.CONFIRM_ON_QUIT, MuConfiguration.DEFAULT_CONFIRM_ON_QUIT));
        northPanel.add(quitConfirmationCheckBox);

        // 'Enable system notifications' option, displayed only if current platform supports system notifications
        if(AbstractNotifier.isAvailable()) {
            systemNotificationsCheckBox = new JCheckBox(Translator.get("prefs_dialog.enable_system_notifications")+" ("+AbstractNotifier.getNotifier().getPrettyName()+")");
            systemNotificationsCheckBox.setSelected(MuConfiguration.getVariable(MuConfiguration.ENABLE_SYSTEM_NOTIFICATIONS,
                                                                                     MuConfiguration.DEFAULT_ENABLE_SYSTEM_NOTIFICATIONS));
            northPanel.add(systemNotificationsCheckBox);
        }

        // 'Enable Bonjour services discovery' option
        bonjourDiscoveryCheckBox = new JCheckBox(Translator.get("prefs_dialog.enable_bonjour_discovery"));
        bonjourDiscoveryCheckBox.setSelected(MuConfiguration.getVariable(MuConfiguration.ENABLE_BONJOUR_DISCOVERY,
                                                                              MuConfiguration.DEFAULT_ENABLE_BONJOUR_DISCOVERY));
        northPanel.add(bonjourDiscoveryCheckBox);

        add(northPanel, BorderLayout.NORTH);
    }


    ///////////////////////
    // PrefPanel methods //
    ///////////////////////
    protected void commit() {
        MuConfiguration.setVariable(MuConfiguration.CHECK_FOR_UPDATE, checkForUpdatesCheckBox.isSelected());

        MuConfiguration.setVariable(MuConfiguration.USE_CUSTOM_SHELL, useCustomShellRadioButton.isSelected());
        MuConfiguration.setVariable(MuConfiguration.CUSTOM_SHELL, customShellField.getText());

        MuConfiguration.setVariable(MuConfiguration.CONFIRM_ON_QUIT, quitConfirmationCheckBox.isSelected());

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
