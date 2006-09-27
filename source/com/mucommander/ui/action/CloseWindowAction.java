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
        super(mainFrame);
    }

    public void performAction(MainFrame mainFrame) {
        mainFrame.dispose();
    }
}