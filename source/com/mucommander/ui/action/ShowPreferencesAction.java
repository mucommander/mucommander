package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.pref.general.GeneralPreferencesDialog;

import java.util.Hashtable;

/**
 * This action shows up the preferences dialog.
 *
 * @author Maxence Bernard
 */
public class ShowPreferencesAction extends MucoAction implements InvokesDialog {

    public ShowPreferencesAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    public void performAction() {
        new GeneralPreferencesDialog(mainFrame).showDialog();
    }
}
