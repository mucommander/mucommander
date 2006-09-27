package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.WindowManager;
import com.mucommander.ui.icon.IconManager;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * This action creates a new muCommander window.
 *
 * @author Maxence Bernard
 */
public class NewWindowAction extends MucoAction {

    public NewWindowAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction(MainFrame mainFrame) {
        WindowManager.createNewMainFrame();
    }
}
