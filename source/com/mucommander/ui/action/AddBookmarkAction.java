package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.icon.IconManager;
import com.mucommander.ui.bookmark.AddBookmarkDialog;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * This action brings up the 'Add bookmark' dialog that allows to bookmark the current folder.
 *
 * @author Maxence Bernard
 */
public class AddBookmarkAction extends MucoAction {

    public AddBookmarkAction(MainFrame mainFrame) {
        super(mainFrame, "bookmarks_menu.add_bookmark", KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_MASK));
    }

    public void performAction(MainFrame mainFrame) {
        new AddBookmarkDialog(mainFrame);
    }
}
