package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * This action copies the filename(s) of the currently selected / marked files(s) to the system clipboard.
 *
 * @author Maxence Bernard
 */
public class CopyFilenamesAction extends MucoAction {

    public CopyFilenamesAction(MainFrame mainFrame) {
        super(mainFrame, "file_menu.copy_filenames", KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK));
    }

    public void performAction(MainFrame mainFrame) {
        mainFrame.getLastActiveTable().copyFilenamesToClipboard(false);
    }
}