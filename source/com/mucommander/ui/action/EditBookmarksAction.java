package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.bookmark.EditBookmarksDialog;

import java.util.Hashtable;


/**
 * This action brings up the 'Edit bookmarks' dialog that allows to edit bookmarks.
 *
 * @author Maxence Bernard
 */
public class EditBookmarksAction extends MucoAction implements InvokesDialog {

    public EditBookmarksAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    public void performAction() {
        new EditBookmarksDialog(mainFrame);
    }
}
