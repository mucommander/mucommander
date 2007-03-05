package com.mucommander.ui.action;

import com.mucommander.file.util.FileSet;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.dnd.ClipboardSupport;

import java.util.Hashtable;

/**
 * This action copies the selected / marked files to the system clipboard, allowing to paste
 * them to muCommander or another application.
 *
 * @author Maxence Bernard
 */
public class CopyFilesToClipboardAction extends SelectedFilesAction {

    public CopyFilesToClipboardAction(MainFrame mainFrame, Hashtable properties) {
        super(mainFrame, properties);
    }

    public void performAction() {
        FileSet selectedFiles = mainFrame.getActiveTable().getSelectedFiles();

        if(selectedFiles.size()>0)
            ClipboardSupport.setClipboardFiles(selectedFiles);
    }
}