package com.mucommander.ui.plaf;

import com.mucommander.preferences.osgi.PreferencePanelProvider;
import com.mucommander.ui.dialog.pref.PreferencesDialog;
import com.mucommander.ui.dialog.pref.PreferencesPanel;

public class LookAndFeelPreferencesPanelProvider implements PreferencePanelProvider {

    @Override
    public String getTitle() {
        return "Look and Feel";
    }

    @Override
    public String getDescription() {
        return "Customize application Looks and feel";
    }

    @Override
    public PreferencesPanel createPreferencePanel(PreferencesDialog parent) {
        return new LookAndFeelPreferencesPanel(parent);
    }

    @Override
    public int getWeight() {
        return 10;
    }
}
