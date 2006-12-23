package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;

/**
 * Pops up the DrivePopupButton (the drop down button that allows to quickly select a volume or bookmark)
 * of the left FolderPanel.
 *
 * @author Maxence Bernard
 */
public class PopupRightDriveButtonAction extends MucoAction {

    public PopupRightDriveButtonAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction() {
        mainFrame.getFolderPanel2().getDriveButton().popupMenu();
    }
}
