package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.WindowManager;

/**
 * This action brings the next window (next window number) to the front.
 *
 * @author Maxence Bernard
 */
public class RecallNextWindowAction extends MucoAction {

    public RecallNextWindowAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction(MainFrame mainFrame) {
        WindowManager.switchToNextWindow();
    }
}
