package com.mucommander.ui.action;

import com.mucommander.file.FileSet;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.dnd.ClipboardSupport;

/**
 * This action copies the selected / marked files to the system clipboard, allowing to paste
 * them to muCommander or another application.
 *
 * @author Maxence Bernard
 */
public class CopyFilesToClipboardAction extends SelectedFilesAction {

    public CopyFilesToClipboardAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction() {
        FileSet selectedFiles = mainFrame.getActiveTable().getSelectedFiles();

        if(selectedFiles.size()>0)
            ClipboardSupport.setClipboardFiles(selectedFiles);
    }
}