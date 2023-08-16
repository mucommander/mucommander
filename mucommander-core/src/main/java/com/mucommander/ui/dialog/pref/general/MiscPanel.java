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

package com.mucommander.ui.dialog.pref.general;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JRadioButton;

import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.commons.util.ui.layout.XAlignedComponentPanel;
import com.mucommander.commons.util.ui.layout.YBoxPanel;
import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.conf.MuPreferences;
import com.mucommander.core.desktop.DesktopManager;
import com.mucommander.text.Translator;
import com.mucommander.ui.dialog.pref.PreferencesDialog;
import com.mucommander.ui.dialog.pref.PreferencesPanel;
import com.mucommander.ui.dialog.pref.component.PrefCheckBox;
import com.mucommander.ui.dialog.pref.component.PrefFilePathField;
import com.mucommander.ui.dialog.pref.component.PrefRadioButton;
import com.mucommander.ui.dialog.pref.component.PrefTextField;
import com.mucommander.ui.notifier.NotifierProvider;

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

    /** 'Show keyboard shortcuts hints' checkbox */
    private PrefCheckBox showKeyboardHintsCheckBox;

    /** 'Enable system notifications' checkbox */
    private PrefCheckBox systemNotificationsCheckBox;

    /** 'Enable Bonjour services discovery' checkbox */
    private PrefCheckBox bonjourDiscoveryCheckBox;

    /** 'Open the file with the viewer in case of opening error' checkbox */
    private PrefCheckBox viewOnErrorDiscoveryCheckBox;

    /** 'Set default file drag and drop action to COPY' checkbox */
    private PrefCheckBox setDropActionToCopyCheckBox;

    /** 'Use Option as Meta key in Terminal' checkbox */
    private PrefCheckBox useOptionAsMetaKey;

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

        // Use system default or custom shell ?
        if (MuConfigurations.getPreferences().getVariable(MuPreference.USE_CUSTOM_SHELL, MuPreferences.DEFAULT_USE_CUSTOM_SHELL)) {
            useCustomShellRadioButton.setSelected(true);
        } else {
            useDefaultShellRadioButton.setSelected(true);
        }

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

        if (OsFamily.MAC_OS.isCurrent()) {
            useOptionAsMetaKey = new PrefCheckBox(Translator.get("prefs_dialog.use_option_as_meta_key"), () -> MuConfigurations.getPreferences().getVariable(
                    MuPreference.USE_OPTION_AS_META_KEY,
                    MuPreferences.DEFAULT_USE_OPTION_AS_META_KEY));
            useOptionAsMetaKey.addDialogListener(parent);
            shellPanel.add(useOptionAsMetaKey);
        }

        northPanel.add(shellPanel, 5);

        northPanel.addSpace(10);

        // 'Show splash screen' option
        showSplashScreenCheckBox = new PrefCheckBox(Translator.get("prefs_dialog.show_splash_screen"), () -> MuConfigurations.getPreferences().getVariable(
                MuPreference.SHOW_SPLASH_SCREEN,
                MuPreferences.DEFAULT_SHOW_SPLASH_SCREEN));
        showSplashScreenCheckBox.addDialogListener(parent);
        northPanel.add(showSplashScreenCheckBox);

        showKeyboardHintsCheckBox = new PrefCheckBox(Translator.get("prefs_dialog.show_keyboard_hints"), () -> MuConfigurations.getPreferences().getVariable(
                MuPreference.SHOW_KEYBOARD_HINTS,
                MuPreferences.DEFAULT_SHOW_KEYBOARD_HINTS));
        showKeyboardHintsCheckBox.addDialogListener(parent);
        northPanel.add(showKeyboardHintsCheckBox);

        // 'Check for updates on startup' option
        checkForUpdatesCheckBox = new PrefCheckBox(Translator.get("prefs_dialog.check_for_updates_on_startup"), () -> MuConfigurations.getPreferences().getVariable(
                MuPreference.CHECK_FOR_UPDATE,
                MuPreferences.DEFAULT_CHECK_FOR_UPDATE));
        checkForUpdatesCheckBox.addDialogListener(parent);
        northPanel.add(checkForUpdatesCheckBox);

        // 'Show confirmation dialog on quit' option
        quitConfirmationCheckBox = new PrefCheckBox(Translator.get("prefs_dialog.confirm_on_quit"), () -> MuConfigurations.getPreferences().getVariable(
                MuPreference.CONFIRM_ON_QUIT,
                MuPreferences.DEFAULT_CONFIRM_ON_QUIT));
        quitConfirmationCheckBox.addDialogListener(parent);
        northPanel.add(quitConfirmationCheckBox);

        // 'Enable system notifications' option, displayed only if current platform supports system notifications
        if (NotifierProvider.isAvailable()) {
            systemNotificationsCheckBox = new PrefCheckBox(Translator.get("prefs_dialog.enable_system_notifications")+" ("+NotifierProvider.getNotifier().getPrettyName()+")",
                    () -> MuConfigurations.getPreferences().getVariable(
                            MuPreference.ENABLE_SYSTEM_NOTIFICATIONS,
                            MuPreferences.DEFAULT_ENABLE_SYSTEM_NOTIFICATIONS));
            systemNotificationsCheckBox.addDialogListener(parent);
            northPanel.add(systemNotificationsCheckBox);
        }
        // 'Open the file with the viewer in case of opening error' option
        viewOnErrorDiscoveryCheckBox = new PrefCheckBox(Translator.get("prefs_dialog.open_with_viewer_on_error"), () -> MuConfigurations.getPreferences().getVariable(
                MuPreference.VIEW_ON_ERROR,
                MuPreferences.DEFAULT_VIEW_ON_ERROR));
        viewOnErrorDiscoveryCheckBox.addDialogListener(parent);
        northPanel.add(viewOnErrorDiscoveryCheckBox);

        // 'Enable Bonjour services discovery' option
        bonjourDiscoveryCheckBox = new PrefCheckBox(Translator.get("prefs_dialog.enable_bonjour_discovery"), () -> MuConfigurations.getPreferences().getVariable(
                MuPreference.ENABLE_BONJOUR_DISCOVERY,
                MuPreferences.DEFAULT_ENABLE_BONJOUR_DISCOVERY));
        bonjourDiscoveryCheckBox.addDialogListener(parent);
        northPanel.add(bonjourDiscoveryCheckBox);

        setDropActionToCopyCheckBox = new PrefCheckBox(Translator.get("prefs_dialog.set_drop_action_to_copy"), () -> MuConfigurations.getPreferences().getVariable(
                MuPreference.SET_DROP_ACTION_TO_COPY,
                MuPreferences.DEFAULT_SET_DROP_ACTION_TO_COPY));
        setDropActionToCopyCheckBox.addDialogListener(parent);
        northPanel.add(setDropActionToCopyCheckBox);

        add(northPanel, BorderLayout.NORTH);

        customShellField.addDialogListener(parent);
        useCustomShellRadioButton.addDialogListener(parent);
    }


    /////////////////////////////////
    // ItemListener implementation //
    /////////////////////////////////

    public void itemStateChanged(ItemEvent e) {
        Object source = e.getSource();
        if (source == useCustomShellRadioButton) {
            customShellField.setEnabled(useCustomShellRadioButton.isSelected());
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

        MuConfigurations.getPreferences().setVariable(MuPreference.CONFIRM_ON_QUIT, quitConfirmationCheckBox.isSelected());
        MuConfigurations.getPreferences().setVariable(MuPreference.SHOW_SPLASH_SCREEN, showSplashScreenCheckBox.isSelected());
        MuConfigurations.getPreferences().setVariable(MuPreference.SHOW_KEYBOARD_HINTS, showKeyboardHintsCheckBox.isSelected());

        boolean enabled;
        if (systemNotificationsCheckBox != null) {
            enabled = systemNotificationsCheckBox.isSelected();
            MuConfigurations.getPreferences().setVariable(MuPreference.ENABLE_SYSTEM_NOTIFICATIONS, enabled);
            NotifierProvider.getNotifier().setEnabled(enabled);
        }

        MuConfigurations.getPreferences().setVariable(MuPreference.VIEW_ON_ERROR, viewOnErrorDiscoveryCheckBox.isSelected());

        MuConfigurations.getPreferences().setVariable(MuPreference.ENABLE_BONJOUR_DISCOVERY, bonjourDiscoveryCheckBox.isSelected());

        MuConfigurations.getPreferences().setVariable(MuPreference.SET_DROP_ACTION_TO_COPY, setDropActionToCopyCheckBox.isSelected());
        if (OsFamily.MAC_OS.isCurrent()) {
            MuConfigurations.getPreferences().setVariable(MuPreference.USE_OPTION_AS_META_KEY, useOptionAsMetaKey.isSelected());
        }
    }
}
