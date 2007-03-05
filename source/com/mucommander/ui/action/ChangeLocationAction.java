package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;

import java.util.Hashtable;

/**
 * This action transfers focus to the location field of the currently active FolderPanel to edit or type in
 * a new folder location.
 *
 * @author Maxence Bernard
 */
public class ChangeLocationAction extends MucoAction {

    public ChangeLocationAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    public void performAction() {
        mainFrame.getActiveTable().getFolderPanel().changeCurrentLocation();
    }
}
