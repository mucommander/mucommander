package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.bookmark.AddBookmarkDialog;

import java.util.Hashtable;

/**
 * This action brings up the 'Add bookmark' dialog that allows to bookmark the current folder.
 *
 * @author Maxence Bernard
 */
public class AddBookmarkAction extends MucoAction implements InvokesDialog {

    public AddBookmarkAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    public void performAction() {
        new AddBookmarkDialog(mainFrame);
    }
}
