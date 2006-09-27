package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * This action copies the path(s) of the currently selected / marked files(s) to the system clipboard.
 *
 * @author Maxence Bernard
 */
public class CopyFilePathsAction extends SelectedFilesAction {

    public CopyFilePathsAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction(MainFrame mainFrame) {
        CopyFileNamesAction.copyFilenamesToClipboard(mainFrame, true);
    }
}