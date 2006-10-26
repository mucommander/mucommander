package com.mucommander.ui.action;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.filter.DirectoryFileFilter;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.viewer.ViewerRegistrar;

/**
 * This action opens the currently selected file in an integrated viewer.
 *
 * @author Maxence Bernard
 */
public class ViewAction extends SelectedFileAction {

    public ViewAction(MainFrame mainFrame) {
        super(mainFrame);

        // Only enable this action if currently selected file is not a directory
        setSelectedFileFilter(new DirectoryFileFilter());
    }

    public void performAction() {
        AbstractFile file = mainFrame.getActiveTable().getSelectedFile();
        if(file!=null && !(file.isDirectory() || file.isSymlink()))
            ViewerRegistrar.createViewerFrame(mainFrame, file);
    }
}
