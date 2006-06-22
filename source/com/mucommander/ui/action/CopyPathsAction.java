package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * This action copies the path(s) of the currently selected / marked files(s) to the system clipboard.
 *
 * @author Maxence Bernard
 */
public class CopyPathsAction extends MucoAction {

    public CopyPathsAction(MainFrame mainFrame) {
        super(mainFrame, "file_menu.copy_paths", KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.SHIFT_MASK|KeyEvent.CTRL_MASK));
    }

    public void performAction(MainFrame mainFrame) {
        mainFrame.getLastActiveTable().copyFilenamesToClipboard(true);
    }
}