package com.mucommander.ui.action;

import com.mucommander.file.util.FileSet;
import com.mucommander.ui.ChangeDateDialog;
import com.mucommander.ui.MainFrame;

/**
 * Brings up a dialog that allows the user to change the date of the currently selected/marked files.
 *
 * @author Maxence Bernard
 */
public class ChangeDateAction extends SelectedFilesAction implements InvokesDialog {

    public ChangeDateAction(MainFrame mainFrame) {
        super(mainFrame);
    }


    public void performAction() {
        FileSet files = mainFrame.getActiveTable().getSelectedFiles();

        // Display dialog only if at least one file is selected/marked
        if(files.size()>0)
            new ChangeDateDialog(mainFrame, files).showDialog();
    }
}
