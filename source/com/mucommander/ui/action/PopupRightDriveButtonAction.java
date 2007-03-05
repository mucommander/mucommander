package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;

import java.util.Hashtable;

/**
 * Pops up the DrivePopupButton (the drop down button that allows to quickly select a volume or bookmark)
 * of the left FolderPanel.
 *
 * @author Maxence Bernard
 */
public class PopupRightDriveButtonAction extends MucoAction {

    public PopupRightDriveButtonAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    public void performAction() {
        mainFrame.getFolderPanel2().getDriveButton().popupMenu();
    }
}
