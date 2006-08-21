package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.MoveDialog;
import com.mucommander.ui.table.FileTable;
import com.mucommander.file.FileSet;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * This action triggers in-table renaming of the currently selected file, if no file is marked.
 * If files are marked, it simply invokes 'Move dialog' just like {@link com.mucommander.ui.action}.
 *
 * @author Maxence Bernard
 */
public class RenameAction extends MucoAction {

    public RenameAction(MainFrame mainFrame) {
        super(mainFrame, "command_bar.rename", KeyStroke.getKeyStroke(KeyEvent.VK_F6, KeyEvent.SHIFT_MASK), "command_bar.rename_tooltip");
    }

    public void performAction(MainFrame mainFrame) {
        FileTable activeTable = mainFrame.getLastActiveTable();
        FileSet files = activeTable.getSelectedFiles();

        // Display move dialog only if at least one file is selected/marked
        if(files.size()>0) {
            // Trigger in-table renaming if only one file is selected (not marked)
            if(files.size()==1 && files.elementAt(0).equals(activeTable.getSelectedFile())) {
                activeTable.editCurrentFilename();
            }
            // Show up move dialog
            else
                new MoveDialog(mainFrame, files, true);
        }
    }
}
