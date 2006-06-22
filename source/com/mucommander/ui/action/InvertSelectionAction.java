package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * This action .
 *
 * @author Maxence Bernard
 */
public class InvertSelectionAction extends MucoAction {

    public InvertSelectionAction(MainFrame mainFrame) {
        super(mainFrame, "mark_menu.invert_selection", KeyStroke.getKeyStroke(KeyEvent.VK_MULTIPLY, 0));
    }

    public void performAction(MainFrame mainFrame) {
        mainFrame.getLastActiveTable().invertSelection();
    }
}
