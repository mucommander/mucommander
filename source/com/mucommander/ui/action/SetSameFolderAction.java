package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.icon.IconManager;

import javax.swing.*;
import java.awt.event.KeyEvent;

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

    public void performAction(MainFrame mainFrame) {
        mainFrame.setSameFolder();
    }
}
