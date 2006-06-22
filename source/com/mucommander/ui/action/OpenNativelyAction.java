package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * This action 'opens natively' (using native associations) the currently selected file or folder.
 *
 * @author Maxence Bernard
 */
public class OpenNativelyAction extends MucoAction {

    public OpenNativelyAction(MainFrame mainFrame) {
        super(mainFrame, "file_menu.open_natively", KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.SHIFT_MASK));
    }

    public void performAction(MainFrame mainFrame) {
        mainFrame.getLastActiveTable().enterAction(true);
    }
}
