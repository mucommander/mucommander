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
import com.mucommander.ui.layout.XAlignedComponentPanel;
import com.mucommander.ui.combobox.SaneComboBox;

import java.nio.charset.Charset;
import java.util.Iterator;
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

    /** Shell encoding combo box. */
    private JComboBox shellEncoding;

    private JComboBox createEncodingComboBox() {
        Iterator availableEncodings;
        int      index;
        String   encoding;
        String   currentEncoding;
        int      selectedIndex;

        shellEncoding = new SaneComboBox();
        shellEncoding.addItem(Translator.get("prefs_dialog.auto_detect_shell_encoding"));

        // Otherwise, look for the selected character encoding as we add it.
        availableEncodings = Charset.availableCharsets().keySet().iterator();
        index              = 1;
        selectedIndex      = 0;
        currentEncoding    = MuConfiguration.getVariable(MuConfiguration.SHELL_ENCODING);
        while(availableEncodings.hasNext()) {
            encoding = (String)availableEncodings.next();

            shellEncoding.addItem(encoding);
            if(encoding.equals(currentEncoding))
                selectedIndex = index;

            index++;
        }

        if(MuConfiguration.getVariable(MuConfiguration.AUTODETECT_SHELL_ENCODING, MuConfiguration.DEFAULT_AUTODETECT_SHELL_ENCODING))
            selectedIndex = 0;
        shellEncoding.setSelectedIndex(selectedIndex);

        return shellEncoding;
    }


    public MiscPanel(PreferencesDialog parent) {
        super(parent, Translator.get("prefs_dialog.misc_tab"));

        setLayout(new BorderLayout());

        YBoxPanel northPanel = new YBoxPanel();

        JRadioButton useDefaultShellRadioButton = new JRadioButton(Translator.get("prefs_dialog.default_shell") + ':');
        useCustomShellRadioButton = new JRadioButton(Translator.get("prefs_dialog.custom_shell") + ':');

        // Use sytem default or custom shell ?
        if(MuConfiguration.getVariable(MuConfiguration.USE_CUSTOM_SHELL, MuConfiguration.DEFAULT_USE_CUSTOM_SHELL))
            useCustomShellRadioButton.setSelected(true);
        else
            useDefaultShellRadioButton.setSelected(true);

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(useDefaultShellRadioButton);
        buttonGroup.add(useCustomShellRadioButton);

        // Shell panel
        XAlignedComponentPanel shellPanel = new XAlignedComponentPanel();
        shellPanel.setLabelLeftAligned(true);
        shellPanel.setBorder(BorderFactory.createTitledBorder(Translator.get("prefs_dialog.shell")));

        shellPanel.addRow(useDefaultShellRadioButton, new JLabel(PlatformManager.getDefaultShellCommand()), 5);
        shellPanel.addRow(useCustomShellRadioButton, customShellField = new JTextField(MuConfiguration.getVariable(MuConfiguration.CUSTOM_SHELL, "")), 10);
        customShellField.addKeyListener(new KeyAdapter() {
                public void keyTyped(KeyEvent e) {
                    if(!useCustomShellRadioButton.isSelected())
                        useCustomShellRadioButton.setSelected(true);
                }
            });
        shellPanel.addRow(Translator.get("prefs_dialog.shell_encoding"), createEncodingComboBox(), 5);

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

        // Saves the shell data.
        MuConfiguration.setVariable(MuConfiguration.USE_CUSTOM_SHELL, useCustomShellRadioButton.isSelected());
        MuConfiguration.setVariable(MuConfiguration.CUSTOM_SHELL, customShellField.getText());

        // Saves the shell encoding data.
        boolean isAutoDetect;
        isAutoDetect = shellEncoding.getSelectedIndex() == 0;
        MuConfiguration.setVariable(MuConfiguration.AUTODETECT_SHELL_ENCODING, isAutoDetect);
        if(!isAutoDetect)
            MuConfiguration.setVariable(MuConfiguration.SHELL_ENCODING, (String)shellEncoding.getSelectedItem());

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
