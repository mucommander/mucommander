package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.CopyDialog;
import com.mucommander.file.FileSet;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * This action invokes the 'Copy dialog' which allows to copy the currently selected/marked files to a specified destination.
 *
 * @author Maxence Bernard
 */
public class CopyAction extends MucoAction {

    public CopyAction(MainFrame mainFrame) {
        super(mainFrame, "command_bar.copy", KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
    }

    public void performAction(MainFrame mainFrame) {
        FileSet files = mainFrame.getLastActiveTable().getSelectedFiles();

        // Display copy dialog only if at least one file is selected/marked
        if(files.size()>0)
            new CopyDialog(mainFrame, files, false);
    }
}
