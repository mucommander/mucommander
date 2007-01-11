package com.mucommander.ui.action;

import com.mucommander.file.util.FileSet;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.PackDialog;

/**
 * This action pops up the 'Pack files' dialog that allows to create an archive file with the currently marked files.
 *
 * @author Maxence Bernard
 */
public class PackAction extends SelectedFilesAction implements InvokesDialog {

    public PackAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction() {
        FileSet files = mainFrame.getActiveTable().getSelectedFiles();
        if(files.size()>0)
            new PackDialog(mainFrame, files, false);
    }
}
