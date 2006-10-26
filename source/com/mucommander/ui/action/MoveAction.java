package com.mucommander.ui.action;

import com.mucommander.file.FileSet;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.MoveDialog;

/**
 * This action invokes the 'Move dialog' which allows to move the currently selected/marked files
 * in the current folder to a specified destination.
 *
 * @author Maxence Bernard
 */
public class MoveAction extends SelectedFilesAction {

    public MoveAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction() {
        FileSet files = mainFrame.getActiveTable().getSelectedFiles();

        // Display move dialog only if at least one file is selected/marked
        if(files.size()>0)
            new MoveDialog(mainFrame, files);
    }
}
