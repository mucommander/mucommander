package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.about.AboutDialog;

/**
 * This action displays the 'About' dialog.
 *
 * @author Maxence Bernard
 */
public class ShowAboutAction extends MucoAction {

    public ShowAboutAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction() {
        new AboutDialog(mainFrame).showDialog();
    }
}
