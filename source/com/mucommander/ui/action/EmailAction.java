package com.mucommander.ui.action;

import com.mucommander.file.FileSet;
import com.mucommander.ui.EmailFilesDialog;
import com.mucommander.ui.MainFrame;

/**
 * This action pops up the 'Email files' dialog that allows to email the currently marked files as attachment.
 *
 * @author Maxence Bernard
 */
public class EmailAction extends SelectedFilesAction {

    public EmailAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction() {
        FileSet files = mainFrame.getLastActiveTable().getSelectedFiles();
        if(files.size()>0)
            new EmailFilesDialog(mainFrame, files);
    }
}