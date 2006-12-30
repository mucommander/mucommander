package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.RunDialog;

/**
 * This action pops up the 'Run command' dialog that is used to execute a shell command.
 *
 * @author Maxence Bernard
 */
public class RunCommandAction extends MucoAction implements InvokesDialog {

    public RunCommandAction(MainFrame mainFrame) {super(mainFrame);}

    public void performAction() {new RunDialog(mainFrame).showDialog();}
}
