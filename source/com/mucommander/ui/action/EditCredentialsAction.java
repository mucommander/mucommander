package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.auth.EditCredentialsDialog;

/**
 * This action brings up the 'Edit credentials' dialog that allows to edit persistent credentials (the ones stored
 * to disk).
 *
 * @author Maxence Bernard
 */
public class EditCredentialsAction extends MucoAction implements InvokesDialog {

    public EditCredentialsAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction() {
        new EditCredentialsDialog(mainFrame);
    }
}
