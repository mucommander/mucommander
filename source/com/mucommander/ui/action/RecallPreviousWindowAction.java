package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.WindowManager;

/**
 * This action brings the previous window (previous window number) to the front.
 * 
 * @author Maxence Bernard
 */
public class RecallPreviousWindowAction extends MucoAction {

    public RecallPreviousWindowAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction() {
        WindowManager.switchToPreviousWindow();
    }
}
