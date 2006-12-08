package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.help.ShortcutsDialog;

/**
 * This action displays the 'Keyboard shortcuts' dialog that lists all available keyboard shortcuts sorted by topic.
 *
 * @author Maxence Bernard
 */
public class ShowKeyboardShortcutsAction extends MucoAction {

    public ShowKeyboardShortcutsAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction() {
        new ShortcutsDialog(mainFrame).showDialog();
    }
}
