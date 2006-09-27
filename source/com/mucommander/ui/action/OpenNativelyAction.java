package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.PlatformManager;
import com.mucommander.file.AbstractFile;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * This action 'opens natively' (using native associations) the currently selected file or folder.
 *
 * @author Maxence Bernard
 */
public class OpenNativelyAction extends MucoAction {

    public OpenNativelyAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction(MainFrame mainFrame) {
        AbstractFile selectedFile = mainFrame.getLastActiveTable().getSelectedFile(true);

        if(selectedFile==null)
            return;

        // Tries to execute file
        PlatformManager.open(selectedFile);
    }
}
