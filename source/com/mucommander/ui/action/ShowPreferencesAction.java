package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.pref.PreferencesDialog;

/**
 * This action shows up the preferences dialog.
 *
 * @author Maxence Bernard
 */
public class ShowPreferencesAction extends MucoAction implements InvokesDialog {

    public ShowPreferencesAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction() {
        new PreferencesDialog(mainFrame).showDialog();
    }
}