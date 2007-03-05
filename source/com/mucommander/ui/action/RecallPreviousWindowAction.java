package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.WindowManager;

import java.util.Hashtable;

/**
 * This action brings the previous window (previous window number) to the front.
 * 
 * @author Maxence Bernard
 */
public class RecallPreviousWindowAction extends MucoAction {

    public RecallPreviousWindowAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    public void performAction() {
        WindowManager.switchToPreviousWindow();
    }
}
