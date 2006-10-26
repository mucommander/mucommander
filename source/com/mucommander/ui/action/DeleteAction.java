package com.mucommander.ui.action;

import com.mucommander.file.FileSet;
import com.mucommander.ui.DeleteDialog;
import com.mucommander.ui.MainFrame;

/**
 * This action invokes a Delete confirmation dialog to delete currently the selected / marked files
 * in the currently active folder.
 *
 * @author Maxence Bernard
 */
public class DeleteAction extends SelectedFilesAction {

    public DeleteAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction() {
        FileSet files = mainFrame.getActiveTable().getSelectedFiles();
        // Invoke confirmation dialog only if at least one file is selected/marked
        if(files.size()>0)
            new DeleteDialog(mainFrame, files);
    }
}
