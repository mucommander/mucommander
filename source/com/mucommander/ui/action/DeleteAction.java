package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.DeleteDialog;
import com.mucommander.file.FileSet;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * This action invokes a Delete confirmation dialog to delete currently the selected / marked files
 * in the currently active folder.
 *
 * @author Maxence Bernard
 */
public class DeleteAction extends SelectedFilesAction {

    public DeleteAction(MainFrame mainFrame) {
        super(mainFrame, KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0));
    }

    public void performAction(MainFrame mainFrame) {
        FileSet files = mainFrame.getLastActiveTable().getSelectedFiles();
        // Invoke confirmation dialog only if at least one file is selected/marked
        if(files.size()>0)
            new DeleteDialog(mainFrame, files);
    }
}
