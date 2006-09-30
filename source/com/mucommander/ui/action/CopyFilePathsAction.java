package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;

/**
 * This action copies the path(s) of the currently selected / marked files(s) to the system clipboard.
 *
 * @author Maxence Bernard
 */
public class CopyFilePathsAction extends SelectedFilesAction {

    public CopyFilePathsAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction() {
        CopyFileNamesAction.copyFilenamesToClipboard(mainFrame, true);
    }
}