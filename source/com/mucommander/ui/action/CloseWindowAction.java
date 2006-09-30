package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;

/**
 * This action closes the currently active MainFrame (the one this action is attached to).
 *
 * @author Maxence Bernard
 */
public class CloseWindowAction extends MucoAction {

    public CloseWindowAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction() {
        mainFrame.dispose();
    }
}