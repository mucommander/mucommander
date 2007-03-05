package com.mucommander.ui.action;

import com.mucommander.ui.FolderPanel;
import com.mucommander.ui.MainFrame;

import java.util.Hashtable;

/**
 * This action changes the current folder of the currently active FolderPanel to the current folder's root.
 * This action only gets enabled when the current folder has a parent.
 *
 * @author Maxence Bernard
 */
public class GoToRootAction extends GoToParentAction {

    public GoToRootAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    public void performAction() {
        // Changes the current folder to make it the current folder's root folder.
        // Does nothing if the current folder already is the root.
        FolderPanel folderPanel = mainFrame.getActiveTable().getFolderPanel();
        folderPanel.tryChangeCurrentFolder(folderPanel.getCurrentFolder().getRoot());
    }
}