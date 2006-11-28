package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;

/**
 * Splits the folder panels horizontally (left/right) within the MainFrame.
 *
 * @author Maxence Bernard
 */
public class SplitHorizontallyAction extends MucoAction {

    public SplitHorizontallyAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction() {
        mainFrame.setSplitOrientation(false);
    }
}
