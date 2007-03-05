package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;

import java.util.Hashtable;

/**
 * This action closes the currently active MainFrame (the one this action is attached to).
 *
 * @author Maxence Bernard
 */
public class CloseWindowAction extends MucoAction {

    public CloseWindowAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    public void performAction() {
        mainFrame.dispose();
    }
}