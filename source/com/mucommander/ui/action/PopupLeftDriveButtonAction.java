package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;

/**
 * Pops up the DriveButton (the drop down button that allows to quickly select a volume or bookmark)
 * of the left FolderPanel.
 *
 * @author Maxence Bernard
 */
public class PopupLeftDriveButtonAction extends MucoAction {

    public PopupLeftDriveButtonAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction() {
        mainFrame.getFolderPanel1().getDriveButton().popupMenu();
    }
}
