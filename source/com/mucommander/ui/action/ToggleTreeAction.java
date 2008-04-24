package com.mucommander.ui.action;

import com.mucommander.ui.main.FolderPanel;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.tree.FoldersTreePanel;

import java.util.Hashtable;

/**
 * This action toggles the visibility of a directory tree.
 * @see FoldersTreePanel   
 *
 * @author Mariusz Jakubowski
 */
public class ToggleTreeAction extends MuAction {

    public ToggleTreeAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    public void performAction() {
        FolderPanel folderPanel = mainFrame.getActiveTable().getFolderPanel();
        folderPanel.setTreeVisible(!folderPanel.isTreeVisible());
    }

}
