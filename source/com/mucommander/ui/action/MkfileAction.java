package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.MkdirDialog;

import java.util.Hashtable;

/**
 * This action brings up the 'Make file' dialog which allows to create a new file in the currently active folder.
 *
 * @author Maxence Bernard
 */
public class MkfileAction extends MucoAction {

    public MkfileAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    public void performAction() {
        new MkdirDialog(mainFrame, true);
    }
}
