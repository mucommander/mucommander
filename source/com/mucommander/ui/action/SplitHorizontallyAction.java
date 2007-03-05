package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;

import java.util.Hashtable;

/**
 * Splits the folder panels horizontally (left/right) within the MainFrame.
 *
 * @author Maxence Bernard
 */
public class SplitHorizontallyAction extends MucoAction {

    public SplitHorizontallyAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    public void performAction() {
        mainFrame.setSplitPaneOrientation(false);
    }
}
