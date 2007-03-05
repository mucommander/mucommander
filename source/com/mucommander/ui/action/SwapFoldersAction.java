package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;

import java.util.Hashtable;

/**
 * This action swaps both FileTable's current folders: the left table's current folder becomes the right table's one
 * and vice versa.
 *
 * @author Maxence Bernard
 */
public class SwapFoldersAction extends MucoAction {

    public SwapFoldersAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    public void performAction() {
        mainFrame.swapFolders();
    }
}
