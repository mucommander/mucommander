package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.QuitDialog;
import com.mucommander.ui.WindowManager;

/**
 * This action pops up the Quit confirmation dialog (if it hasn't been disabled) and if quit has been confirmed,
 * quits the application.
 *
 * @author Maxence Bernard
 */
public class QuitAction extends MucoAction {

    public QuitAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction() {
        if(QuitDialog.confirmQuit())
            WindowManager.quit();
    }
}