package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * This action compares the content of the 2 MainFrame's file tables and marks the files that are different.
 *
 * @author Maxence Bernard
 */
public class CompareFoldersAction extends MucoAction {

    public CompareFoldersAction(MainFrame mainFrame) {
        super(mainFrame, "mark_menu.compare_folders", KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.CTRL_MASK));
    }

    public void performAction(MainFrame mainFrame) {
        mainFrame.compareDirectories();
    }
}
