package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.RunDialog;
import com.mucommander.ui.icon.IconManager;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * This action pops up the 'Run command' dialog that is used to execute a shell command.
 *
 * @author Maxence Bernard
 */
public class RunCommandAction extends MucoAction {

    public RunCommandAction(MainFrame mainFrame) {
        super(mainFrame, KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_MASK));
    }

    public void performAction(MainFrame mainFrame) {
        new RunDialog(mainFrame);        
    }
}