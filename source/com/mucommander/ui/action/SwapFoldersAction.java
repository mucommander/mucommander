package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * This action swaps both FileTable's current folders: the left table's current folder becomes the right table's one
 * and vice versa.
 *
 * @author Maxence Bernard
 */
public class SwapFoldersAction extends MucoAction {

    public SwapFoldersAction(MainFrame mainFrame) {
        super(mainFrame, "view_menu.swap_folders", KeyStroke.getKeyStroke(KeyEvent.VK_U, KeyEvent.CTRL_MASK));
    }

    public void performAction(MainFrame mainFrame) {
        mainFrame.swapFolders();
    }
}
