package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.editor.EditorRegistrar;
import com.mucommander.file.AbstractFile;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * This action opens the currently selected file in an integrated editor.
 *
 * @author Maxence Bernard
 */
public class EditAction extends MucoAction {

    public EditAction(MainFrame mainFrame) {
        super(mainFrame, "command_bar.edit", KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));
    }

    public void performAction(MainFrame mainFrame) {
        AbstractFile file = mainFrame.getLastActiveTable().getSelectedFile();
        if(file!=null && !(file.isDirectory() || file.isSymlink()))
            EditorRegistrar.createEditorFrame(mainFrame, file);
    }
}
