package com.mucommander.ui.action;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.filter.DirectoryFileFilter;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.editor.EditorRegistrar;

/**
 * This action opens the currently selected file in an integrated editor.
 *
 * @author Maxence Bernard
 */
public class EditAction extends SelectedFileAction {

    public EditAction(MainFrame mainFrame) {
        super(mainFrame);

        // Only enable this action if currently selected file is not a directory
        setFileFilter(new DirectoryFileFilter());
    }

    public void performAction() {
        AbstractFile file = mainFrame.getLastActiveTable().getSelectedFile();
        if(file!=null && !(file.isDirectory() || file.isSymlink()))
            EditorRegistrar.createEditorFrame(mainFrame, file);
    }
}
