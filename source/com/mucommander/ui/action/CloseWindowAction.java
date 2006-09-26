package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.WindowManager;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * This action closes the currently active MainFrame (the one this action is attached to).
 *
 * @author Maxence Bernard
 */
public class CloseWindowAction extends MucoAction {

    public CloseWindowAction(MainFrame mainFrame) {
        super(mainFrame, KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0));
    }

    public void performAction(MainFrame mainFrame) {
        mainFrame.dispose();
    }
}