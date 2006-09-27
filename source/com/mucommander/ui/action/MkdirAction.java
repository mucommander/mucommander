package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.MkdirDialog;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * This action brings up the 'Make directory' dialog which allows to create a new directory in the currently active folder.
 *
 * @author Maxence Bernard
 */
public class MkdirAction extends MucoAction {

    public MkdirAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction(MainFrame mainFrame) {
        new MkdirDialog(mainFrame);
    }
}
