package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;

/**
 * Positions the split pane divider in the middle so that both folder panels have the same space.
 *
 * @author Maxence Bernard
 */
public class SplitEquallyAction extends MucoAction {

    public SplitEquallyAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction() {
        mainFrame.getSplitPane().setSplitRatio(0.5f);
    }
}
