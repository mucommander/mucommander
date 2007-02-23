package com.mucommander.ui.action;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.RootFolders;
import com.mucommander.ui.MainFrame;

/**
 * This action changes the current folder of the currently active FolderPanel to the user home folder.
 *
 * @author Maxence Bernard
 */
public class GoToHomeAction extends MucoAction {

    public GoToHomeAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction() {
        // Changes the current folder to make it the user home folder
        AbstractFile homeFolder = RootFolders.getUserHomeFolder();
        if(homeFolder!=null)
            mainFrame.getActiveTable().getFolderPanel().tryChangeCurrentFolder(homeFolder);
    }
}
