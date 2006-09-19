package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.table.FileTable;
import com.mucommander.file.AbstractFile;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * This action triggers in-table renaming of the currently selected file, if no file is marked.
 * If files are marked, it simply invokes 'Move dialog' just like {@link com.mucommander.ui.action}.
 *
 * @author Maxence Bernard
 */
public class RenameAction extends SelectedFileAction {

    public RenameAction(MainFrame mainFrame) {
        super(mainFrame, "command_bar.rename", KeyStroke.getKeyStroke(KeyEvent.VK_F6, KeyEvent.SHIFT_MASK), "command_bar.rename_tooltip");
    }

    public void performAction(MainFrame mainFrame) {
        FileTable activeTable = mainFrame.getLastActiveTable();
        AbstractFile file = activeTable.getSelectedFile(false);

        // Return if no file other than parent folder '..' is selected
        if(file==null)
            return;

        // Trigger in-table renaming
        activeTable.editCurrentFilename();
    }
}
