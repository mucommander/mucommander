package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.pref.PreferencesDialog;

/**
 * This action shows up the preferences dialog.
 *
 * @author Maxence Bernard
 */
public class PreferencesAction extends MucoAction {

    public PreferencesAction(MainFrame mainFrame) {
        super(mainFrame, "file_menu.preferences");
    }

    public void performAction(MainFrame mainFrame) {
        new PreferencesDialog(mainFrame).showDialog();
    }
}