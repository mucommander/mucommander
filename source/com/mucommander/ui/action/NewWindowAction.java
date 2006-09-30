package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.WindowManager;

/**
 * This action creates a new muCommander window.
 *
 * @author Maxence Bernard
 */
public class NewWindowAction extends MucoAction {

    public NewWindowAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction() {
        WindowManager.createNewMainFrame();
    }
}
