package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.UnpackDialog;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.file.FileSet;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * This action pops up the 'Unpack files' dialog that allows to unpack the currently marked files.
 *
 * @author Maxence Bernard
 */
public class UnpackAction extends SelectedFilesAction {

    public UnpackAction(MainFrame mainFrame) {
        super(mainFrame, "file_menu.unpack", KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_MASK));
    }

    public void performAction(MainFrame mainFrame) {
        FileSet files = mainFrame.getLastActiveTable().getSelectedFiles();
        if(files.size()>0)
            new UnpackDialog(mainFrame, files, false);
    }
}
