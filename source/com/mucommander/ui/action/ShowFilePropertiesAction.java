package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.PropertiesDialog;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.file.FileSet;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * This action pops up the file Properties dialog.
 *
 * @author Maxence Bernard
 */
public class ShowFilePropertiesAction extends SelectedFilesAction {

    public ShowFilePropertiesAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction(MainFrame mainFrame) {
        FileSet files = mainFrame.getLastActiveTable().getSelectedFiles();
        if(files.size()>0)
            new PropertiesDialog(mainFrame, files).showDialog();
    }
}