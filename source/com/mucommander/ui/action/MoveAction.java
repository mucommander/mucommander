package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.MoveDialog;
import com.mucommander.file.FileSet;

import javax.swing.*;
import java.awt.event.KeyEvent;

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

    public void performAction(MainFrame mainFrame) {
        FileSet files = mainFrame.getLastActiveTable().getSelectedFiles();

        // Display move dialog only if at least one file is selected/marked
        if(files.size()>0)
            new MoveDialog(mainFrame, files);
    }
}
