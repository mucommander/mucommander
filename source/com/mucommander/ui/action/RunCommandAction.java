package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.RunDialog;

import java.util.Hashtable;

/**
 * This action pops up the 'Run command' dialog that is used to execute a shell command.
 *
 * @author Maxence Bernard
 */
public class RunCommandAction extends MucoAction implements InvokesDialog {

    public RunCommandAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    public void performAction() {
        new RunDialog(mainFrame).showDialog();
    }
}
