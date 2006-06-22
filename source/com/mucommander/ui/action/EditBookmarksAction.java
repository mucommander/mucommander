package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.bookmark.EditBookmarksDialog;


/**
 * This action brings up the 'Edit bookmarks' dialog that allows to edit bookmarks.
 *
 * @author Maxence Bernard
 */
public class EditBookmarksAction extends MucoAction {

    public EditBookmarksAction(MainFrame mainFrame) {
        super(mainFrame, "bookmarks_menu.edit_bookmarks");
    }

    public void performAction(MainFrame mainFrame) {
        new EditBookmarksDialog(mainFrame);
    }
}
