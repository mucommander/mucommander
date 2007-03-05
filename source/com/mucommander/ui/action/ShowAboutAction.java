package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.about.AboutDialog;

import java.util.Hashtable;

/**
 * This action displays the 'About' dialog.
 *
 * @author Maxence Bernard
 */
public class ShowAboutAction extends MucoAction {

    public ShowAboutAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    public void performAction() {
        new AboutDialog(mainFrame).showDialog();
    }
}
