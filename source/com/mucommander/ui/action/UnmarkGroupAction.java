package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.FileSelectionDialog;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * This action brings up the 'File selection' dialog which allows to unmark a group of files that match a specified expression.
 *
 * @author Maxence Bernard
 */
public class UnmarkGroupAction extends MucoAction {

    public UnmarkGroupAction(MainFrame mainFrame) {
        super(mainFrame, "mark_menu.unmark", KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0));
    }

    public void performAction(MainFrame mainFrame) {
        new FileSelectionDialog(mainFrame, false).showDialog();
    }
}
