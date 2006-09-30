package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;

/**
 * This action equalizes both FileTable's current folders: the 'inactive' FileTable's current folder becomes
 * the active FileTable's one.
 *
 * @author Maxence Bernard
 */
public class SetSameFolderAction extends MucoAction {

    public SetSameFolderAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction() {
        mainFrame.setSameFolder();
    }
}
