package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.WindowManager;

import java.util.Hashtable;

/**
 * This action creates a new muCommander window.
 *
 * @author Maxence Bernard
 */
public class NewWindowAction extends MucoAction {

    public NewWindowAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    public void performAction() {
        WindowManager.createNewMainFrame();
    }
}
