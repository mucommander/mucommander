package com.mucommander.preferences.osgi;

import com.mucommander.ui.dialog.pref.PreferencesDialog;
import com.mucommander.ui.dialog.pref.PreferencesPanel;

public interface PreferencePanelProvider {

    String getTitle();

    String getDescription();

    PreferencesPanel createPreferencePanel(PreferencesDialog parent);

    int getWeight();
}
