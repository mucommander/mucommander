package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.table.FileTable;
import com.mucommander.file.AbstractFile;
import com.mucommander.PlatformManager;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * This action 'opens' the currently selected file or folder.
 *
 * @author Maxence Bernard
 */
public class OpenAction extends MucoAction {

    public OpenAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction(MainFrame mainFrame) {
        FileTable fileTable = mainFrame.getLastActiveTable();
        AbstractFile selectedFile = fileTable.getSelectedFile(true);

        if(selectedFile==null)
            return;

        if(selectedFile.isBrowsable()) {
            fileTable.getFolderPanel().trySetCurrentFolder(selectedFile);
        }
        else {
            // Tries to execute file
            PlatformManager.open(selectedFile);
        }
    }
}
