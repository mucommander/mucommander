package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.WindowManager;

import java.util.Hashtable;

/**
 * This action brings the next window (next window number) to the front.
 *
 * @author Maxence Bernard
 */
public class RecallNextWindowAction extends MucoAction {

    public RecallNextWindowAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    public void performAction() {
        WindowManager.switchToNextWindow();
    }
}
