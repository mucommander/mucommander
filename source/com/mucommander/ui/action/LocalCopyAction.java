package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.CopyDialog;
import com.mucommander.file.FileSet;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * This action invokes the 'Copy dialog' which allows to copy the currently selected/marked files to a specified destination.
 * The only difference with {@link com.mucommander.ui.action.CopyAction} is that if a single file is selected,
 * the destination will be preset to the selected file's name so that it can easily be copied to a similar filename in
 * the current directory.
 *
 * @author Maxence Bernard
 */
public class LocalCopyAction extends MucoAction {

    public LocalCopyAction(MainFrame mainFrame) {
        super(mainFrame, "command_bar.local_copy", KeyStroke.getKeyStroke(KeyEvent.VK_F5, KeyEvent.SHIFT_MASK));
    }

    public void performAction(MainFrame mainFrame) {
        FileSet files = mainFrame.getLastActiveTable().getSelectedFiles();

        // Display copy dialog only if at least one file is selected/marked
        if(files.size()>0)
            new CopyDialog(mainFrame, files, true);
    }
}
