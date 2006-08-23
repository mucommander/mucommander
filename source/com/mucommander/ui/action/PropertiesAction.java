package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.PropertiesDialog;
import com.mucommander.file.FileSet;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * This action pops up the file Properties dialog.
 *
 * @author Maxence Bernard
 */
public class PropertiesAction extends SelectedFilesAction {

    public PropertiesAction(MainFrame mainFrame) {
        super(mainFrame, "file_menu.properties", KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.ALT_MASK));
    }

    public void performAction(MainFrame mainFrame) {
        FileSet files = mainFrame.getLastActiveTable().getSelectedFiles();
        if(files.size()>0)
            new PropertiesDialog(mainFrame, files).showDialog();
    }
}