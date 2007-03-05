package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;

import java.util.Hashtable;

/**
 * This action equalizes both FileTable's current folders: the 'inactive' FileTable's current folder becomes
 * the active FileTable's one.
 *
 * @author Maxence Bernard
 */
public class SetSameFolderAction extends MucoAction {

    public SetSameFolderAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    public void performAction() {
        mainFrame.setSameFolder();
    }
}
