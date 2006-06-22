package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * This action unmarks all files in the current file table.
 *
 * @author Maxence Bernard
 */
public class UnmarkAllAction extends MucoAction {

    public UnmarkAllAction(MainFrame mainFrame) {
        super(mainFrame, "mark_menu.unmark_all", KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_MASK));
    }

    public void performAction(MainFrame mainFrame) {
        mainFrame.getLastActiveTable().unmarkAll();
    }
}
