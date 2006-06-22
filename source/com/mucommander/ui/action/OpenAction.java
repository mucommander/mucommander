package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * This action 'opens' the currently selected file or folder.
 *
 * @author Maxence Bernard
 */
public class OpenAction extends MucoAction {

    public OpenAction(MainFrame mainFrame) {
        super(mainFrame, "file_menu.open", KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
    }

    public void performAction(MainFrame mainFrame) {
        mainFrame.getLastActiveTable().enterAction(false);
    }
}
