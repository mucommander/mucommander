package com.mucommander.ui.action;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.filter.DirectoryFileFilter;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.editor.EditorRegistrar;

import java.util.Hashtable;

/**
 * This action opens the currently selected file in an integrated editor.
 * @author Maxence Bernard
 */
public class InternalEditAction extends SelectedFileAction {

    public InternalEditAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);

        // Only enable this action if currently selected file is not a directory
        setSelectedFileFilter(new DirectoryFileFilter());
    }

    public void performAction() {
        AbstractFile file = mainFrame.getActiveTable().getSelectedFile();
        if(file!=null && !(file.isDirectory() || file.isSymlink()))
            EditorRegistrar.createEditorFrame(mainFrame, file, getIcon().getImage());
    }
}
