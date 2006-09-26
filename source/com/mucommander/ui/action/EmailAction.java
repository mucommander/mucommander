package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.EmailFilesDialog;
import com.mucommander.file.FileSet;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * This action pops up the 'Email files' dialog that allows to email the currently marked files as attachment.
 *
 * @author Maxence Bernard
 */
public class EmailAction extends SelectedFilesAction {

    public EmailAction(MainFrame mainFrame) {
        super(mainFrame, KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK));
    }

    public void performAction(MainFrame mainFrame) {
        FileSet files = mainFrame.getLastActiveTable().getSelectedFiles();
        if(files.size()>0)
            new EmailFilesDialog(mainFrame, files);
    }
}