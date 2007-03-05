package com.mucommander.ui.action;

import com.mucommander.file.util.FileSet;
import com.mucommander.ui.ChangePermissionsDialog;
import com.mucommander.ui.MainFrame;

import java.util.Hashtable;

/**
 * Brings up a dialog that allows the user to change the file permissions the currently selected/marked files.
 * 
 * @author Maxence Bernard
 */
public class ChangePermissionsAction extends SelectedFilesAction implements InvokesDialog {

    public ChangePermissionsAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }


    public void performAction() {
        FileSet files = mainFrame.getActiveTable().getSelectedFiles();

        // Display dialog only if at least one file is selected/marked
        if(files.size()>0)
            new ChangePermissionsDialog(mainFrame, files).showDialog();        
    }
}
