package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.FileSelectionDialog;
import com.mucommander.ui.icon.IconManager;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * This action brings up the 'File selection' dialog which allows to mark a group of files that match a specified expression.
 *
 * @author Maxence Bernard
 */
public class MarkGroupAction extends MucoAction {

    public MarkGroupAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction(MainFrame mainFrame) {
        new FileSelectionDialog(mainFrame, true).showDialog();
    }
}
