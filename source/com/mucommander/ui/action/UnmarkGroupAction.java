package com.mucommander.ui.action;

import com.mucommander.ui.FileSelectionDialog;
import com.mucommander.ui.MainFrame;

import java.util.Hashtable;

/**
 * This action brings up the 'File selection' dialog which allows to unmark a group of files that match a specified expression.
 *
 * @author Maxence Bernard
 */
public class UnmarkGroupAction extends MucoAction implements InvokesDialog {

    public UnmarkGroupAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    public void performAction() {
        new FileSelectionDialog(mainFrame, false).showDialog();
    }
}
