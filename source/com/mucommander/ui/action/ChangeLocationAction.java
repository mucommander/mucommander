package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;

/**
 * This action transfers focus to the location field of the currently active FolderPanel to type in a new folder location.
 *
 * @author Maxence Bernard
 */
public class ChangeLocationAction extends MucoAction {

    public ChangeLocationAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction() {
        mainFrame.getActiveTable().getFolderPanel().changeCurrentLocation();
    }
}
