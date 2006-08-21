package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * This action closes the currently active MainFrame (the one this action is attached to).
 *
 * @author Maxence Bernard
 */
public class CloseWindowAction extends MucoAction {

    public CloseWindowAction(MainFrame mainFrame) {
        super(mainFrame, "file_menu.close_window", KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0), "file_menu.close_window_tooltip");
    }

    public void performAction(MainFrame mainFrame) {
        mainFrame.dispose();
    }
}