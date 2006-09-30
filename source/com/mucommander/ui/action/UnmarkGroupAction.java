package com.mucommander.ui.action;

import com.mucommander.ui.FileSelectionDialog;
import com.mucommander.ui.MainFrame;

/**
 * This action brings up the 'File selection' dialog which allows to unmark a group of files that match a specified expression.
 *
 * @author Maxence Bernard
 */
public class UnmarkGroupAction extends MucoAction {

    public UnmarkGroupAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction() {
        new FileSelectionDialog(mainFrame, false).showDialog();
    }
}
