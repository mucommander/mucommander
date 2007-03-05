package com.mucommander.ui.action;

import com.mucommander.file.util.FileSet;
import com.mucommander.ui.CopyDialog;
import com.mucommander.ui.MainFrame;

import java.util.Hashtable;

/**
 * This action invokes the 'Copy dialog' which allows to copy the currently selected/marked files to a specified destination.
 *
 * @author Maxence Bernard
 */
public class CopyAction extends SelectedFilesAction {

    public CopyAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    public void performAction() {
        FileSet files = mainFrame.getActiveTable().getSelectedFiles();

        // Display copy dialog only if at least one file is selected/marked
        if(files.size()>0)
            new CopyDialog(mainFrame, files, false).showDialog();
    }
}
