package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;

/**
 * Splits the folder panels vertically (top/bottom) within the MainFrame.
 * This is the default split orientation.
 *
 * @author Maxence Bernard
 */
public class SplitVerticallyAction extends MucoAction {

    public SplitVerticallyAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction() {
        mainFrame.setSplitOrientation(true);
    }
}
