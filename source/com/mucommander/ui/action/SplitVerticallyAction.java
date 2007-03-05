package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;

import java.util.Hashtable;

/**
 * Splits the folder panels vertically (top/bottom) within the MainFrame.
 * This is the default split orientation.
 *
 * @author Maxence Bernard
 */
public class SplitVerticallyAction extends MucoAction {

    public SplitVerticallyAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    public void performAction() {
        mainFrame.setSplitPaneOrientation(true);
    }
}
