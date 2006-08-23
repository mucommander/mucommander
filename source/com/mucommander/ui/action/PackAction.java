package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.PackDialog;
import com.mucommander.file.FileSet;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * This action pops up the 'Pack files' dialog that allows to create an archive file with the currently marked files.
 *
 * @author Maxence Bernard
 */
public class PackAction extends SelectedFilesAction {

    public PackAction(MainFrame mainFrame) {
        super(mainFrame, "file_menu.pack", KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.CTRL_MASK));
    }

    public void performAction(MainFrame mainFrame) {
        FileSet files = mainFrame.getLastActiveTable().getSelectedFiles();
        if(files.size()>0)
            new PackDialog(mainFrame, files, false);
    }
}
