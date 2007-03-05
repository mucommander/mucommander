package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.help.ShortcutsDialog;

import java.util.Hashtable;

/**
 * This action displays the 'Keyboard shortcuts' dialog that lists all available keyboard shortcuts sorted by topic.
 *
 * @author Maxence Bernard
 */
public class ShowKeyboardShortcutsAction extends MucoAction {

    public ShowKeyboardShortcutsAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    public void performAction() {
        new ShortcutsDialog(mainFrame).showDialog();
    }
}
