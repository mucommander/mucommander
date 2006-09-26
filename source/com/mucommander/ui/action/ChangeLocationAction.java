package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * This action transfers focus to the location field of the currently active FolderPanel to type in a new folder location.
 *
 * @author Maxence Bernard
 */
public class ChangeLocationAction extends MucoAction {

    public ChangeLocationAction(MainFrame mainFrame) {
        super(mainFrame, KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_MASK));
    }

    public void performAction(MainFrame mainFrame) {
        mainFrame.getLastActiveTable().getFolderPanel().changeCurrentLocation();
    }
}
