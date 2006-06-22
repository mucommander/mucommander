package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;

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
        super(mainFrame, "view_menu.set_same_folder", KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_MASK));
    }

    public void performAction(MainFrame mainFrame) {
        mainFrame.setSameFolder();
    }
}
